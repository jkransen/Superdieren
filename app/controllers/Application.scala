package controllers

import play.api.GlobalSettings
import play.api.mvc.Action
import play.api.mvc.Controller

import models.Gebruiker

object Application extends Controller {

  def index = Action {
    request =>
      val gebruiker = request.session.get("connected").map { eigenFacebookId =>
        Gebruiker(eigenFacebookId.toLong)
      }
      Ok(views.html.index(gebruiker))
  }

  def login = Action {
    Redirect(controllers.routes.Facebook.login)
  }

  def logout = Action {
    Redirect(controllers.routes.Application.index).withNewSession
  }
}

