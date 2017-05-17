import sbt.Keys._

name := "vault"

val log4j            = "2.8.2"
lazy val uscalaVersion = "0.5.1"
lazy val specs2Version = "3.8.8"
lazy val circeVersion = "0.7.0"
lazy val dispatchVersion = "0.12.0"
lazy val startVaultTask = TaskKey[Unit](
  "startVaultTask",
  "Start dev vault server for integration test"
)
// start vault settings
startVaultTask := {
  import sys.process._
  "./scripts/start_vault" !
}
lazy val checkStyleBeforeCompile = TaskKey[Unit](
  "checkStyleBeforeCompile",
  "Check style before compile"
)

val pomInfo = (
  <url>https://github.com/janstenpickle/scala-vault</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>
        https://github.com/janstenpickle/scala-vault/blob/master/LICENSE
      </url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:janstenpickle/scala-vault.git</url>
    <connection>
      scm:git:git@github.com:janstenpickle/scala-vault.git
    </connection>
  </scm>
  <developers>
    <developer>
      <id>janstepickle</id>
      <name>Chris Jansen</name>
    </developer>
  </developers>
)

lazy val commonSettings = Seq(
  version := "0.4.0",
  scalaVersion := "2.12.2",
  organization := "janstenpickle.vault",
  pomExtra := pomInfo,
  autoAPIMappings := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  bintrayReleaseOnPublish := false,
  licenses += (
    "MIT",
    url("https://github.com/janstenpickle/scala-vault/blob/master/LICENSE")
  ),
  resolvers ++= Seq(Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo,
    Resolver.bintrayRepo("albertpastrana", "maven")),
  libraryDependencies ++= Seq(
    "net.databinder.dispatch" %% "dispatch-core" % dispatchVersion,
    "org.uscala" %% "uscala-result" % uscalaVersion,
    "org.uscala" %% "uscala-result-async" % uscalaVersion,
    "org.uscala" %% "uscala-result-specs2" % uscalaVersion % "it,test",
    "org.specs2" %% "specs2-core" % specs2Version % "it,test",
    "org.specs2" %% "specs2-scalacheck" % specs2Version % "it,test",
    "org.specs2" %% "specs2-junit" % specs2Version % "it,test",
    "org.apache.logging.log4j" %  "log4j-api"                   % log4j,
    "org.apache.logging.log4j" %  "log4j-core"                  % log4j,
    "org.apache.logging.log4j" %  "log4j-slf4j-impl"            % log4j
),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion),
  scalacOptions in Test ++= Seq(
    "-Yrangepos",
    "-Xlint",
    "-deprecation",
    "-Xfatal-warnings"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"),
  javacOptions in Compile ++= Seq(
    "-source", "1.8",
    "-target", "1.8",
    "-Xlint:all"
  ),
  testOptions in IntegrationTest ++= Seq(
    Tests.Argument("junitxml"),
    Tests.Argument("console")
  ),
  unmanagedSourceDirectories in IntegrationTest += baseDirectory.value /
  "test" / "scala",
  // check style settings
  checkStyleBeforeCompile :=
  org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
  (compile in Compile) := (
    (compile in Compile) dependsOn
    checkStyleBeforeCompile
  ).value
) ++ Defaults.itSettings

lazy val core = (project in file("core")).
  settings(name := "vault-core").
  settings(commonSettings: _*).
  configs(IntegrationTest)
lazy val manage = (project in file("manage")).
  settings(name := "vault-manage").
  settings(commonSettings: _*).
  configs(IntegrationTest).
  dependsOn(core % "compile->compile;it->it,it->test")
lazy val auth = (project in file("auth")).
  settings(name := "vault-auth").
  settings(commonSettings: _*).
  configs(IntegrationTest).
  dependsOn(core % "compile->compile;it->it", manage % "it->compile")
