package models

import play.api.db._
import play.api.Play.current
import anorm._

case class Kaart(nummer: Int) {

  val serie: Int = (nummer - 1) / Kaart.perSerie + 1
  val index: Int = (nummer - 1) % Kaart.perSerie + 1
  private val config = play.api.Play.configuration
  private val kaartUrl = config.getString("kaarturl").get
  val imageUrl = kaartUrl.replace("$SET", serie.toString).replace("$INDEX", "%02d".format(index))

  def <(andere: Kaart): Boolean = {
    nummer < andere.nummer
  }

  def >(andere: Kaart): Boolean = {
    nummer > andere.nummer
  }
}

object Kaart {

  val aantalSeries = 17
  val perSerie = 12
  val totaalAantal = aantalSeries * perSerie

  def apply(serie: Int, index: Int): Kaart = {
    Kaart((serie - 1) * perSerie + index)
  }

  def alle: Seq[Kaart] = {
    for (i <- 1 to aantalSeries * perSerie) yield Kaart(i)
  }

  def bewaarBezittingen(gebruiker: Gebruiker, bezittingen: List[(Kaart, Int)]) {
    DB.withConnection { implicit c =>
      SQL("delete from Bezitting where gebruikerId = {facebookId}")
        .on("facebookId" -> gebruiker.facebookId).executeUpdate
      val insert = SQL("insert into Bezitting(gebruikerId, kaartId, aantal) values ({facebookId}, {kaartNummer}, {aantal})")
      for ((kaart, aantal) <- bezittingen) {
        insert.on("facebookId" -> gebruiker.facebookId, "kaartNummer" -> kaart.nummer, "aantal" -> aantal).executeUpdate
      }
    }
  }

  def laadBezittingen(gebruiker: Gebruiker): List[(Kaart, Int)] = {
    DB.withConnection { implicit c =>
      val query = SQL("select kaartid, aantal from Bezitting where gebruikerId = {facebookId} order by kaartId")
        .on("facebookId" -> gebruiker.facebookId)
      query().map((row => (Kaart(row[Int]("kaartid")), row[Int]("aantal")))).toList
    }
  }

  def laadTekort(gebruiker: Gebruiker): List[(Kaart, Int)] = {
    def loadTekort0(kaartenNodig: List[(Kaart, Int)], kaartenBezit: List[(Kaart, Int)], tussenResultaat: List[(Kaart, Int)]): List[(Kaart, Int)] = {
      if (kaartenNodig.isEmpty)
        tussenResultaat
      else if (kaartenBezit.isEmpty)
        tussenResultaat ++ kaartenNodig
      else {
        (kaartenNodig.head, kaartenBezit.head) match {
          case ((nodigKaart, nodigAantal), (bezitKaart, _)) if (nodigKaart < bezitKaart) =>
            loadTekort0(kaartenNodig.tail, kaartenBezit, tussenResultaat ++ List((nodigKaart, nodigAantal)))
          case ((nodigKaart, nodigAantal), (_, bezitAantal)) if (bezitAantal >= nodigAantal) =>
            loadTekort0(kaartenNodig.tail, kaartenBezit.tail, tussenResultaat)
          case ((nodigKaart, nodigAantal), (_, bezitAantal)) =>
            loadTekort0(kaartenNodig.tail, kaartenBezit.tail, tussenResultaat ++ List((nodigKaart, nodigAantal - bezitAantal)))
        }
      }
    }

    val aantalVerzamelingen = gebruiker.aantalVerzamelingen
    val bezittingen = laadBezittingen(gebruiker)
    val kaartenNodig = for (kaart <- Kaart.alle) yield (kaart, aantalVerzamelingen)
    loadTekort0(kaartenNodig.toList, bezittingen, List())
  }

  def sorteer(unsorted: Map[Kaart, Int]): List[(Kaart, Int)] = {
    unsorted.toList.sortWith {
      case ((kaart1, _), (kaart2, _)) => kaart1 < kaart2
    }
  }

  def voegToe(gebruiker: Gebruiker, selectie: Map[Kaart, Int]) {
    def voegToe0(bestaandBezit: List[(Kaart, Int)], uitbreiding: List[(Kaart, Int)], tussenResultaat: List[(Kaart, Int)]): List[(Kaart, Int)] = {
      if (uitbreiding.isEmpty) {
        tussenResultaat ++ bestaandBezit
      } else if (bestaandBezit.isEmpty) {
        tussenResultaat ++ uitbreiding
      } else {
        (bestaandBezit.head, uitbreiding.head) match {
          case ((bestaandKaart, bestaandAantal), (nieuwKaart, _)) if (bestaandKaart < nieuwKaart) =>
            voegToe0(bestaandBezit.tail, uitbreiding, tussenResultaat ++ List((bestaandKaart, bestaandAantal)))
          case ((bestaandKaart, _), (nieuwKaart, nieuwAantal)) if (bestaandKaart > nieuwKaart) =>
            voegToe0(bestaandBezit, uitbreiding.tail, tussenResultaat ++ List((nieuwKaart, nieuwAantal)))
          case ((bestaandKaart, bestaandAantal), (_, nieuwAantal)) =>
            voegToe0(bestaandBezit.tail, uitbreiding.tail, tussenResultaat ++ List((bestaandKaart, bestaandAantal + nieuwAantal)))
        }
      }
    }

    val bestaandBezit: List[(Kaart, Int)] = Kaart.laadBezittingen(gebruiker)
    val uitbreiding = sorteer(selectie)

    val nieuwBezit = voegToe0(bestaandBezit, uitbreiding, List())
    Kaart.bewaarBezittingen(gebruiker, nieuwBezit)
  }

  def trekAf(gebruiker: Gebruiker, selectie: Map[Kaart, Int]) {
    def trekAf0(bestaandBezit: List[(Kaart, Int)], inperking: List[(Kaart, Int)], tussenResultaat: List[(Kaart, Int)]): List[(Kaart, Int)] = {
      if (inperking.isEmpty) {
        tussenResultaat ++ bestaandBezit
      } else if (bestaandBezit.isEmpty) {
        // eventuele overgebleven inperkingen worden genegeerd, omdat er geen bestaande kaarten meer zijn
        tussenResultaat
      } else {
        (bestaandBezit.head, inperking.head) match {
          case ((bestaandKaart, bestaandAantal), (nieuwKaart, _)) if (bestaandKaart < nieuwKaart) =>
            trekAf0(bestaandBezit.tail, inperking, tussenResultaat ++ List((bestaandKaart, bestaandAantal)))
          case ((bestaandKaart, _), (nieuwKaart, _)) if (bestaandKaart > nieuwKaart) =>
            // inperking van kaart die we niet hebben, negeren
            trekAf0(bestaandBezit, inperking.tail, tussenResultaat)
          case ((bestaandKaart, bestaandAantal), (_, nieuwAantal)) if (nieuwAantal >= bestaandAantal) =>
            // we houden niets van deze kaart over, helemaal verwijderen
            trekAf0(bestaandBezit.tail, inperking.tail, tussenResultaat)
          case ((bestaandKaart, bestaandAantal), (_, nieuwAantal)) =>
            trekAf0(bestaandBezit.tail, inperking.tail, tussenResultaat ++ List((bestaandKaart, bestaandAantal - nieuwAantal)))
        }
      }
    }

    val bestaandBezit: List[(Kaart, Int)] = Kaart.laadBezittingen(gebruiker)
    val inperking = sorteer(selectie)

    val nieuwBezit = trekAf0(bestaandBezit, inperking, List())
    Kaart.bewaarBezittingen(gebruiker, nieuwBezit)
  }
}