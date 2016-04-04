name := "Bk_Scala"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++=  Seq(
  "mysql"                %     "mysql-connector-java"     %      "5.1.36",
  "com.typesafe.akka" %% "akka-actor"                           % "2.4.2",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % "2.0.3",
  "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.3",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "2.0.3",
  "net.liftweb" % "lift-json_2.11" % "2.6.3",
  "com.typesafe.slick"  %%     "slick-hikaricp"           %      "3.1.1",
  "ch.qos.logback"       %     "logback-classic"          %      "1.1.3",
  "com.typesafe.slick"   %%    "slick"            	      %      "3.1.1",
  "org.scalatest"        %%    "scalatest"    	      %      "2.2.5"     %    "test",
  "com.h2database"       % 	   "h2"                       %      "1.4.187",
  "com.typesafe.akka" %% "akka-http-testkit-experimental" % "2.0.3" %    "test",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
  "org.reactivemongo" %% "reactivemongo" % "0.11.10",
  "ch.qos.logback" % "logback-classic" % "1.1.2"
)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"

mainClass in assembly := some("com.knoldus.service.HttpService")
assemblyJarName := "runnableNode.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf"            => MergeStrategy.concat
  case "reference.conf"              => MergeStrategy.concat
  case PathList("reference.conf") => MergeStrategy.concat
  case x =>
    val baseStrategy = (assemblyMergeStrategy in assembly).value
    baseStrategy(x)
}

fork in run := true