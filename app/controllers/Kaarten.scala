package controllers

import play.api.data.Form
import play.api.data.Forms._

import play.api.GlobalSettings
import play.api.mvc.{ Action, Controller, Result }

import models.{ Gebruiker, Kaart }

object Kaarten extends Controller {

  val form = Form(
    tuple(
      "reeks" -> text,
      "actie" -> text))

  val aantalVerzamelingenform = Form(
    "aantalVerzamelingen" -> number)

  def GebruikerAction(doeMetGebruiker: Gebruiker => Result) = Action {
    request =>
      val session = request.session
      session.get("connected").map { facebookId =>
        val gebruiker = Gebruiker(facebookId.toLong);
        doeMetGebruiker(gebruiker)
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }

  def overzichtBezit = GebruikerAction {
    gebruiker =>
      val bezittingen = Kaart.laadBezittingen(gebruiker)
      Ok(views.html.kaarten(gebruiker, bezittingen, form))
  }

  def overzichtTekort = GebruikerAction {
    gebruiker =>
      val tekort = Kaart.laadTekort(gebruiker)
      Ok(views.html.tekort(gebruiker, tekort, aantalVerzamelingenform.fill(gebruiker.aantalVerzamelingen)))
  }
  
  def selectie = Action {
    implicit request =>
      session.get("connected").map { facebookId =>
        val gebruiker = Gebruiker(facebookId.toLong);
        form.bindFromRequest.fold(
          failure => BadRequest,
          filledForm => {
            val (reeks, actie) = filledForm
            if (!reeks.isEmpty()) {
              val kaartNummers = reeks.split(",").map(numString => numString.toInt)
              val selectie = kaartNummers.groupBy(kaartNummer => kaartNummer)
                .map {
                  case (nummer, nummers) => (Kaart(nummer), nummers.length)
                }
              actie match {
                case "Toevoegen" => Kaart.voegToe(gebruiker, selectie)
                case "Verwijderen" => Kaart.trekAf(gebruiker, selectie)
              }
            }
            Redirect(controllers.routes.Kaarten.overzichtBezit)
          })
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }
  
  def nieuwAantalVerzamelingen = Action {
    implicit request =>
      session.get("connected").map { facebookId =>
        val gebruiker = Gebruiker(facebookId.toLong);
        aantalVerzamelingenform.bindFromRequest.fold(
          failure => BadRequest,
          filledForm => {
            val nieuwAantal = filledForm
            val gewijzigdeGebruiker = Gebruiker(gebruiker.facebookId, nieuwAantal)
            Gebruiker.update(gewijzigdeGebruiker)
            Redirect(controllers.routes.Kaarten.overzichtTekort)
          })
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }
}