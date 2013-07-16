libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.13" % "test"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _ % "provided")

scalaVersion := "2.10.2"
