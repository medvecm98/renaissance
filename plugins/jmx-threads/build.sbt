lazy val renaissanceCore = RootProject(uri("../../renaissance-core"))

lazy val pluginJMXThreads = (project in file("."))
  .settings(
    name := "plugin-jmxthreads",
    version := "0.0.1",
    crossPaths := false,
    autoScalaLibrary := false,
    organization := "org.renaissance",
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case PathList("org", "renaissance", "plugins", _*) => MergeStrategy.first
      case PathList("org", "renaissance", _*) => MergeStrategy.discard
      case _ => MergeStrategy.singleOrError
    },
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    packageOptions += sbt.Package.ManifestAttributes(
      ("Renaissance-Plugin", "org.renaissance.plugins.jmxthreads.Main")
    ),
  )
  .dependsOn(renaissanceCore % "provided")
