import sbt._
import Keys._
import play.Project._

// import play.api.db.DB

object ApplicationBuild extends Build {

  val appName = "Superdieren"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    jdbc,
    anorm,
    // Add your project dependencies here,
    "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
    // "org.apache.jena" % "jena-arq" % "2.9.3",
    "com.restfb" % "restfb" % "1.6.11",
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test")
//    "org.scalatest" %% "scalatest" % "1.8" % "test")

  val main = play.Project(appName, appVersion, appDependencies).settings( // Add your own project settings here      
  )
}
