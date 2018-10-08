name := "resultset_as_stream_poc"

version := "0.1"

scalaVersion := "2.12.7"

val AkkaStream             = "com.typesafe.akka"             %% "akka-stream"     % "2.5.11"
val Postgres               = "org.postgresql"                % "postgresql"       % "42.2.4"
val AkkaActor              = "com.typesafe.akka"             %% "akka-actor"      % "2.5.11"
val AkkaHttp               = "com.typesafe.akka"             %% "akka-http"                         % "10.1.5"
val AkkaHttpCors           = "ch.megard"                     %% "akka-http-cors"                    % "0.3.0"


libraryDependencies ++= Seq(AkkaStream, Postgres, AkkaActor, AkkaHttp, AkkaHttpCors)
