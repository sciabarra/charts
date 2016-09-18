package mosaico.generator

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

/**
  * Created by msciab on 13/09/16.
  */
object MosaicoGeneratorPlugin
  extends AutoPlugin {

  override def requires = JvmPlugin

  object Keys {
    lazy val genDeps = taskKey[Unit]("genDeps")
  }
  val autoImport = Keys
  import Keys._

  /**
    * Generate Subtasks
    */
  val genDepsTask = genDeps := {
    def norm(s: String) = s.replace("-", "_")
    val folders = (file(".") ** "test-test-docker.sbt").get.map(_.getParentFile)
    val projects = folders.map(_.getName).map(norm)
    val projectDefs = folders.
      map(folder =>
        s"""lazy val ${norm(folder.getName)} = (project in file("${folder}"))
            |.enablePlugins(MosaicoDockerPlugin,MosaicoAmmonitePlugin)
            |""".stripMargin.replaceAll("\n", ""))

    IO.write(baseDirectory.value / "deps.sbt",
      s"""|${projectDefs.mkString("\n\n")}
          |
          |addCommandAlias("dockers", "; ${projects.mkString("/docker ; ")}/docker")
    """.stripMargin)

    println("*** Docker subprojects found:")
    projects.foreach(println)
    println("*** Generated deps.sbt, please reload")
  }

  override val projectSettings = Seq(genDepsTask)
}