import sbt._

object Dependencies {
  val cats: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core"   % "2.8.0",
    "org.typelevel" %% "cats-effect" % "3.3.14",
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest"                     % "3.2.14" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0"  % Test
  )
}
