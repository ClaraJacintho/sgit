name := "SGIT"

version := "1.0"

scalaVersion := "2.13.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"
parallelExecution in test := false
