package controllers

import play.api.data.Form
import play.api.data.Forms._

import play.api.GlobalSettings
import play.api.mvc.{ Action, Controller, Result }

import models.{ Gebruiker, Kaart }

object Gebruikers extends Controller {

  def selecteer(anderFacebookId: String) = Action {
    request =>
      val session = request.session
      session.get("connected").map { eigenFacebookId =>
        val gebruiker = Gebruiker(eigenFacebookId.toLong)
        val vriend = Gebruiker(anderFacebookId.toLong)

        val bezittingenVriend = Kaart.laadBezittingen(vriend)

        Ok(views.html.kaartenAnder(gebruiker, vriend, bezittingenVriend))
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }

  def teKrijgen(anderFacebookId: String) = Action {
    request =>
      val session = request.session
      session.get("connected").map { eigenFacebookId =>
        val gebruiker = Gebruiker(eigenFacebookId.toLong)
        val vriend = Gebruiker(anderFacebookId.toLong)

        val eigenTekort = Kaart.laadTekort(gebruiker)
        val overschotVriend = Kaart.laadOverschot(vriend)
        val teKrijgen = Kaart.verenig(eigenTekort, overschotVriend)

        Ok(views.html.teKrijgenAnder(gebruiker, vriend, teKrijgen))
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }

  def teGeven(anderFacebookId: String) = Action {
    request =>
      val session = request.session
      session.get("connected").map { eigenFacebookId =>
        val gebruiker = Gebruiker(eigenFacebookId.toLong)
        val vriend = Gebruiker(anderFacebookId.toLong)

        val eigenOverschot = Kaart.laadOverschot(gebruiker)
        val tekortVriend = Kaart.laadTekort(vriend)
        val teGeven = Kaart.verenig(eigenOverschot, tekortVriend)

        Ok(views.html.teGevenAnder(gebruiker, vriend, teGeven))
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }
}