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
      val outputDirectory = (sourceManaged in Compile).value / "classy-lenses"
      generate((unmanagedSources in Compile).value, outputDirectory, baseDirectory.value, streams.value.log)
    }.taskValue
  )

  def generate(sources: Seq[File], outputDirectory: File, baseDirectory: File, log: Logger): Seq[File] = {
    sources.flatMap { sourceFile =>
      val path = sourceFile.toPath
      val bytes = Files.readAllBytes(path)
      val text = new String(bytes, "UTF-8")
      val input = Input.VirtualFile(path.toString, text)

      input.parse[Source] match {
        case Parsed.Success(source) =>
          val generatedFiles = Generator.generateOptics(source)
          generatedFiles.map { gen =>
            log.info(s"sbt-classy: Writing generated file ${gen.path}")
            val outputFile = new File(outputDirectory, gen.path)
            Files.createDirectories(outputFile.toPath.getParent)
            IO.write(outputFile, gen.source.toString)
            outputFile
          }
        case Parsed.Error(pos, msg, details) =>
          log.warn(s"sbt-classy: skipping file because failed to parse as Scala (${path.relativize(baseDirectory.toPath)})")
          Nil
      }
    }
  }

}
