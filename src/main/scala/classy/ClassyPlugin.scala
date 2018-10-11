package classy

import sbt._
import sbt.Keys._
import sbt.util.Tracked
import sjsonnew.BasicJsonProtocol._
import java.io.File
import java.nio.file._
import scala.meta._

object ClassyPlugin extends AutoPlugin {

  object autoImport {

  }

  override lazy val projectSettings = Seq(
    sourceGenerators in Compile += sourceGenTask.taskValue
  )

  def sourceGenTask = Def.task {
    val strs = streams.value
    val cacheDir = strs.cacheDirectory
    val log = strs.log
    val outputDirectory = (sourceManaged in Compile).value / "classy-lenses"

    /*
     * Caching to avoid unnecessary work:
     *
     * if the maximum last-modified timestamp of the unmanaged source files has changed:
     *   run the source generator and cache its output (i.e. the list of generated files)
     * else:
     *   do nothing except return the cached output of the previous run
     */
    def gen(unmanagedSrcs: List[File]): List[File] = {
      def execute = generate(unmanagedSrcs, outputDirectory, baseDirectory.value, log)

      val maxLastModified: () => Long =
        () => unmanagedSrcs.foldRight[Long](0) { case (file, maxSoFar) => file.lastModified() max maxSoFar }

      val execIfLastModifiedChanged: (() => Long) => List[File] =
        Tracked.outputChanged(cacheDir / "classy-unmanaged-sources-last-modified") { (lastModifiedChanged: Boolean, lastModified: Long) =>
          val execOrReturnCachedOutput: Unit => List[File] =
            Tracked.lastOutput(cacheDir / "classy-generated-sources") { (_: Unit, prevOutput: Option[List[File]]) =>
              if (lastModifiedChanged) {
                execute
              } else {
                prevOutput.getOrElse(execute)
              }
            }

          execOrReturnCachedOutput(())
        }

      execIfLastModifiedChanged(maxLastModified)
    }

    gen((unmanagedSources in Compile).value.toList)
  }

  def generate(sources: List[File], outputDirectory: File, baseDirectory: File, log: Logger): List[File] = {
    sources.flatMap { sourceFile =>
      val path = sourceFile.toPath
      val bytes = Files.readAllBytes(path)
      val text = new String(bytes, "UTF-8")
      val input = Input.VirtualFile(path.toString, text)

      input.parse[Source] match {
        case Parsed.Success(source) =>
          val generatedFiles = Generator.generateOptics(msg => log.warn(msg))(source)
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
