import sbt.Keys.libraryDependencies

name := "research-project"
organization in ThisBuild := "org.bosch"
scalaVersion in ThisBuild := "2.12.13"
scapegoatVersion in ThisBuild := "1.4.7"

lazy val global = project
  .in(file("."))
  .aggregate(common, spark2, spark3)

lazy val common = project
  .settings(resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/")
  .settings(
    libraryDependencies += "org.scodec" %% "scodec-bits" % "1.1.6",
    libraryDependencies += "org.scodec" %% "scodec-core" % "1.10.3",
    libraryDependencies += "org.scodec" %% "scodec-stream" % "2.0.0",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test",
    // https://mvnrepository.com/artifact/co.fs2/fs2-io
    libraryDependencies += "co.fs2" %% "fs2-io" % "2.5.3"
    // https://mvnrepository.com/artifact/co.fs2/fs2-io
      //libraryDependencies += "co.fs2" %% "fs2-io" % "2.5-15-e328d68"


  )

lazy val spark2 = project
  .settings(libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.7")
  .dependsOn(common)

lazy val spark3 = project
  .settings(libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.1")
  .dependsOn(common)