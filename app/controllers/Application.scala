package controllers

import play.api.GlobalSettings
import play.api.mvc.Action
import play.api.mvc.Controller

import models.Gebruiker

object Application extends Controller {

  def index = Action {
    request =>
      val session = request.session
      session.get("connected").map { facebookId =>
        val user = Gebruiker(facebookId.toLong);
        Ok(views.html.index(user))
      }.getOrElse {
        Redirect(controllers.routes.Facebook.login)
      }
  }

  def login = Action {
    Ok(views.html.login())
  }

  def logout = Action {
    Redirect(controllers.routes.Application.index).withNewSession
  }
}

