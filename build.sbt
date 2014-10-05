name := """isite-fetcher"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype-Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.3.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
  "com.google.inject" % "guice" % "3.0",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "net.debasishg" %% "redisclient" % "2.13",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.amazonaws" % "aws-java-sdk" % "1.8.10.2",
  "org.specs2" % "specs2_2.11" % "2.3.11",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "com.timgroup" % "java-statsd-client" % "3.0.2",
  "com.netaporter" %% "scala-uri" % "0.4.3"
)


