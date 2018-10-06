package classy

import sbt._
import sbt.Keys._
import java.io.File
import java.nio.file._
import scala.meta._

object ClassyPlugin extends AutoPlugin {

  object autoImport {

  }

  // TODO cache using Tracking.inputChanged, outputChanged

  override lazy val projectSettings = Seq(
    sourceGenerators in Compile += Def.task {
      val srcDirectory = sourceDirectory.value
      val outputDirectory = (sourceManaged in Compile).value / "classy-lenses"
      generate(srcDirectory, outputDirectory)
    }.taskValue
  )

  def generate(srcDirectory: File, outputDirectory: File): Seq[File] = {
    // TODO traverse all files
    val file = srcDirectory / "main/scala/foo/Config.scala"
    val path = file.toPath
    val bytes = Files.readAllBytes(path)
    val text = new String(bytes, "UTF-8")
    val input = Input.VirtualFile(path.toString, text)

    // TODO error handling
    val tree = input.parse[Source].get

    println(tree)
    Nil
  }

}
