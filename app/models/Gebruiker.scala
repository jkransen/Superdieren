package models

import play.api.db._
import play.api.Play.current
import anorm._

case class Gebruiker(facebookId: Long, aantalVerzamelingen: Int)

object Gebruiker {

  // returns existing Gebruiker by facebookId, or null if not exists
  def apply(facebookId: Long): Gebruiker = {
    findByFacebookId(facebookId).getOrElse(create(facebookId))
  }

  def findByFacebookId(facebookId: Long): Option[Gebruiker] = {
    DB.withConnection { implicit c =>
      val sql = SQL("select * from Gebruiker where facebookId = {facebookId}").on("facebookId" -> facebookId)
      sql().map(row => Gebruiker(row[Long]("facebookId"), row[Int]("aantalVerzamelingen"))).headOption
    }
  }

  def create(facebookId: Long): Gebruiker = {
    val gebruiker = Gebruiker(facebookId, 1)
    DB.withConnection { implicit c =>
      SQL("insert into Gebruiker(facebookId, aantalVerzamelingen) values ({facebookId}, {aantalVerzamelingen})")
        .on("facebookId" -> gebruiker.facebookId, "aantalVerzamelingen" -> gebruiker.aantalVerzamelingen).executeUpdate
    }
    gebruiker
  }

  def update(gebruiker: Gebruiker) {
    DB.withConnection { implicit c =>
      SQL("update Gebruiker set aantalVerzamelingen = {aantalVerzamelingen} where facebookId = {facebookId}")
        .on("facebookId" -> gebruiker.facebookId, "aantalVerzamelingen" -> gebruiker.aantalVerzamelingen).executeUpdate
    }
  }

  def findVrienden(gebruiker: Gebruiker): Seq[Gebruiker] = {
    DB.withConnection { implicit c =>
      val sql = SQL("select * from Gebruiker g, Vriend v where (v.eerste = g.facebookId and v.tweede = {facebookId}) or (v.tweede = g.facebookId and v.eerste = {facebookId})").on("facebookId" -> gebruiker.facebookId)
      val gebruikers = sql().map(row => Gebruiker(row[Long]("facebookId"), row[Int]("aantalVerzamelingen"))).toList
      gebruikers
    }
  }
}