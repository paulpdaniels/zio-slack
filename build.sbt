ThisBuild / name := "zio-slack"
ThisBuild / organization := "com.github.dapperware"

val mainScala = "2.12.14"
val allScala  = Seq("2.13.6", mainScala, "3.0.2")

inThisBuild(
  List(
    homepage := Some(url("https://github.com/dapperware/zio-slack")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/dapperware/zio-slack/"),
        "scm:git:git@github.com:dapperware/zio-slack.git"
      )
    ),
    developers := List(
      Developer(
        "paulpdaniels",
        "Paul Daniels",
        "",
        url("https://github.com/paulpdaniels")
      )
    ),
    crossScalaVersions := allScala
  )
)

ThisBuild / scalaVersion := mainScala
ThisBuild / gitVersioningSnapshotLowerBound := "0.8.0"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val circeV = "0.14.1"
val zioV   = "1.0.12"
val sttpV  = "3.3.18"

ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val root = (project in file("."))
  .aggregate(core, client, realtime, examples)
  .settings(publish / skip := true)
  .settings(historyPath := None)

lazy val core = project
  .in(file("core"))
  .settings(name := "zio-slack-core")
  .settings(commonSettings)
  .settings(
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio"                           % zioV,
      "dev.zio"                       %% "zio-test"                      % zioV % Test,
      "dev.zio"                       %% "zio-test-sbt"                  % zioV % Test,
      "com.softwaremill.sttp.client3" %% "core"                          % sttpV,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV
    )
  )

lazy val client = project
  .in(file("client"))
  .dependsOn(core)
  .settings(name := "zio-slack-api-web")
  .settings(commonSettings)
  .settings(
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    libraryDependencies ++= Seq(
      "io.circe"                      %% "circe-generic"                 % circeV,
      "dev.zio"                       %% "zio-test"                      % zioV % "it,test",
      "dev.zio"                       %% "zio-test-sbt"                  % zioV % "it,test",
      "com.softwaremill.sttp.client3" %% "core"                          % sttpV,
      "com.softwaremill.sttp.client3" %% "circe"                         % sttpV,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV
    )
    // addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    // addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

lazy val realtime = project
  .in(file("realtime"))
  .dependsOn(core, client)
  .settings(name := "zio-slack-api-realtime")
  .settings(commonSettings)
  .settings(
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    libraryDependencies ++= Seq(
      "io.circe"                      %% "circe-generic"                 % circeV,
      "dev.zio"                       %% "zio-streams"                   % zioV,
      "dev.zio"                       %% "zio-test"                      % zioV % "it,test",
      "dev.zio"                       %% "zio-test-sbt"                  % zioV % "it,test",
      "com.softwaremill.sttp.client3" %% "core"                          % sttpV,
      "com.softwaremill.sttp.client3" %% "circe"                         % sttpV,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV
    )
  )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

lazy val examples = project
  .in(file("examples"))
  .dependsOn(client, realtime)
  .settings(commonSettings)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-config"          % "1.0.10",
      "dev.zio" %% "zio-config-typesafe" % "1.0.10"
    )
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
//  "-Ymacro-annotations"
)

val commonSettings = Def.settings(
  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 13)) => Nil
    case Some((3, 0))  => Nil
    case _             => compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full) :: Nil
  }),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-unchecked"
  ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) =>
      Seq(
        "-Xsource:2.13",
        "-Yno-adapted-args",
        "-Ypartial-unification",
        "-Ywarn-extra-implicit",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-opt-inline-from:<source>",
        "-opt-warnings",
        "-opt:l:inline",
        "-explaintypes",
        "-Yrangepos",
        "-Xlint:_,-type-parameter-shadow",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused:patvars,-implicits",
        "-Ywarn-value-discard"
      )
    case Some((2, 13)) =>
      Seq(
        "-Ymacro-annotations",
        "-explaintypes",
        "-Yrangepos",
        "-Xlint:_,-type-parameter-shadow",
        "-Ywarn-numeric-widen",
        "-Ywarn-unused:patvars,-implicits",
        "-Ywarn-value-discard"
      )
    case _             => Seq("-Ykind-projector:underscores") ++ Seq("-Xmax-inlines", "50")
  })
)
