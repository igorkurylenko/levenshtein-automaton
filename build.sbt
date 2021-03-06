name := "levenshtein-automaton"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.10" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.specs2" %% "specs2" % "2.3.11" % "test")

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

enablePlugins(JavaAppPackaging)
