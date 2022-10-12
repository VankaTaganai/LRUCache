import sbt.Keys.libraryDependencies

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "LRUCache",
    libraryDependencies ++= Seq(
      Dependencies.cats,
    ).flatten
  )
  .settings(
    libraryDependencies ++= Dependencies.test,
  )

