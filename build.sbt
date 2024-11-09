ThisBuild / name := "zio-slack"
ThisBuild / organization := "com.github.dapperware"

val mainScala = "2.12.20"
val allScala  = Seq("2.13.14", mainScala, "3.3.4")

inThisBuild(
  List(
    homepage := Some(url("https://github.com/dapperware/zio-slack")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
//    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
//    pgpPublicRing := file("/tmp/public.asc"),
//    pgpSecretRing := file("/tmp/secret.asc"),
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
    )
  )
)

ThisBuild / scalaVersion := mainScala
//ThisBuild / gitVersioningSnapshotLowerBound := "0.8.0"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

val circeV = "0.14.3"
val zioV   = "2.0.13"
val sttpV  = "3.8.15"

ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val root = (projectMatrix in file("."))
  .aggregate(core, client, examples)
  .settings(publish / skip := true)
  .settings(historyPath := None)

lazy val core = projectMatrix
  .in(file("core"))
  .settings(name := "zio-slack-core")
  .jvmPlatform(
    scalaVersions = allScala,
    settings = commonSettings ++ List(
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      libraryDependencies ++= Seq(
        "dev.zio"                       %% "zio"                           % zioV,
        "dev.zio"                       %% "zio-test"                      % zioV  % Test,
        "dev.zio"                       %% "zio-test-sbt"                  % zioV  % Test,
        "com.softwaremill.sttp.client3" %% "core"                          % sttpV,
        "com.softwaremill.sttp.client3" %% "zio"                           % sttpV,
        "com.softwaremill.sttp.client3" %% "circe"                         % sttpV,
        "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV % "test"
      )
    )
  )

lazy val client = projectMatrix
  .in(file("client"))
  .settings(name := "zio-slack-api-web")
  .dependsOn(core % "compile->compile;test->test")
  .jvmPlatform(
    scalaVersions = allScala,
    settings = commonSettings ++ List(
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      libraryDependencies ++= Seq(
        "io.circe"                      %% "circe-generic"                 % circeV,
        "dev.zio"                       %% "zio-test"                      % zioV  % "test",
        "dev.zio"                       %% "zio-test-sbt"                  % zioV  % "test",
        "com.softwaremill.sttp.client3" %% "core"                          % sttpV,
        "com.softwaremill.sttp.client3" %% "zio"                           % sttpV,
        "com.softwaremill.sttp.client3" %% "circe"                         % sttpV,
        "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV % "test"
      )
    )
  )

lazy val examples = projectMatrix
  .in(file("examples"))
  .jvmPlatform(scalaVersions = Seq(mainScala))
  .dependsOn(client, core)
  .settings(commonSettings)
  .settings(
    publish / skip := true,
    libraryDependencies ++= Seq(
      "dev.zio"                       %% "zio-config"                    % "3.0.7",
      "dev.zio"                       %% "zio-config-typesafe"           % "3.0.7",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % sttpV
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
    case Some((3, _))  => Nil
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
    case _             => Seq("-Xmax-inlines", "50")
  })
)
