val akkaVer = "2.3.11"
val akkaDataRepVer = "0.11"
val logbackVer = "1.1.3"
val scalaVer = "2.11.6"
val scalaParsersVer= "1.0.4"
val scalaTestVer = "2.2.4"

lazy val compileOptions = Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

lazy val commonDependencies = Seq(
  "com.typesafe.akka"        %% "akka-actor"                    % akkaVer,
  "com.typesafe.akka"        %% "akka-cluster"                  % akkaVer,
  "com.typesafe.akka"        %% "akka-contrib"                  % akkaVer,
  "com.github.patriknw"      %% "akka-data-replication"         % akkaDataRepVer,
  "com.typesafe.akka"        %% "akka-persistence-experimental" % akkaVer,
  "com.typesafe.akka"        %% "akka-slf4j"                    % akkaVer,
  "ch.qos.logback"           %  "logback-classic"               % logbackVer,
  "org.scala-lang.modules"   %% "scala-parser-combinators"      % scalaParsersVer,
  "com.typesafe.akka"        %% "akka-testkit"                  % akkaVer            % "test",
  "org.scalatest"            %% "scalatest"                     % scalaTestVer       % "test"
)

lazy val fttas = project in file(".")
name := "akka-collect"
organization := "com.typesafe.training"
version := "1.0.0"
scalaVersion := scalaVer
scalacOptions ++= compileOptions
unmanagedSourceDirectories in Compile := List((scalaSource in Compile).value)
unmanagedSourceDirectories in Test := List((scalaSource in Test).value)
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
EclipseKeys.eclipseOutput := Some(".target")
EclipseKeys.withSource := true
parallelExecution in Test := false
logBuffered in Test := false
parallelExecution in ThisBuild := false
libraryDependencies ++= commonDependencies
