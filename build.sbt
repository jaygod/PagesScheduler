name := """facebook-pages-scheduler"""

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-Xmax-classfile-name", "200",
  "-Yno-adapted-args", "-Yrangepos", "-Ywarn-dead-code", "-Ywarn-inaccessible",
  "-Ywarn-infer-any", "-Ywarn-nullary-override", "-Ywarn-numeric-widen",
  "-Ywarn-unused", "-Ywarn-unused-import", "-Xfatal-warnings"
)


libraryDependencies ++= {
  val akkaVsn = "2.4.17"
  val akkaHttpVsn = "10.0.5"
  Seq(
    "com.typesafe"                  % "config"                            % "1.3.0",
    "com.typesafe.akka"            %% "akka-http-core"                    % akkaHttpVsn,
    "com.typesafe.akka"            %% "akka-http"                         % akkaHttpVsn,
    "com.typesafe.akka"            %% "akka-actor"                        % akkaVsn,
    "com.typesafe.akka"            %% "akka-typed-experimental"           % akkaVsn,
    "com.typesafe.akka"            %% "akka-slf4j"                        % akkaVsn,
    "com.typesafe.akka"            %% "akka-http-spray-json"              % akkaHttpVsn,
    "com.typesafe.scala-logging"   %% "scala-logging"                     % "3.5.0",
    "com.restfb"                    % "restfb"                            % "1.45.0",
    "com.google.apis"               % "google-api-services-calendar"      % "v3-rev180-1.22.0",
    "com.google.api-client"         % "google-api-client"                 % "1.22.0",
    "com.google.oauth-client"       % "google-oauth-client-jetty"         % "1.22.0",
    "org.slf4j"                     % "slf4j-api"                         % "1.7.22",
    "org.slf4j"                     % "slf4j-simple"                      % "1.7.22",
    "org.postgresql"                % "postgresql"                        % "9.3-1102-jdbc41",
    "org.apache.commons"            % "commons-dbcp2"                     % "2.0.1"
  )
}
