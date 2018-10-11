package classy

import org.scalatest._
import scala.meta._
import scala.meta.contrib._

class GeneratorSpec extends FlatSpec {

  private val input = """package foo
                        |
                        |case class Config(
                        |  dbConfig: DbConfig,
                        |  apiConfig: ApiConfig
                        |)
                        |
                        |/*
                        | * generate-classy-lenses
                        | */
                        |case class DbConfig(host: String, port: Int)
                        |
                        |// generate-classy-lenses
                        |case class ApiConfig(url: String, apikey: String)
                        |""".stripMargin.parse[Source].get

  private val warn: String => Unit = _ => ()

  it should "generate classy lenses for case classes" in {
    val generatedFiles = Generator.generateOptics(warn)(input).sortBy(_.path)
    generatedFiles.foreach(println)

    assert(generatedFiles(0).path == "foo/HasApiConfig.scala")
    assert(generatedFiles(0).source.isEqual(
      source"""
              package foo

              import monocle.Lens

              trait HasApiConfig[T] {
                def apiConfig: Lens[T, ApiConfig]
                def apiConfigUrl: Lens[T, String] =
                  apiConfig composeLens Lens[ApiConfig, String](_.url)(x => a => a.copy(url = x))
                def apiConfigApikey: Lens[T, String] =
                  apiConfig composeLens Lens[ApiConfig, String](_.apikey)(x => a => a.copy(apikey = x))
              }

              object HasApiConfig {
                def apply[T](implicit instance: HasApiConfig[T]): HasApiConfig[T] = instance

                implicit val id: HasApiConfig[ApiConfig] = new HasApiConfig[ApiConfig]() {
                  def apiConfig: Lens[ApiConfig, ApiConfig] = Lens.id[ApiConfig]
                }

                //implicit val configHasApiConfig: HasApiConfig[Config] = new HasApiConfig[Config]() {
                //  def apiConfig: Lens[Config, ApiConfig] = Lens[Config, ApiConfig](_.apiConfig)(x => a => a.copy(apiConfig = x))
                //}

              }
      """))

    assert(generatedFiles(1).path == "foo/HasDbConfig.scala")
    assert(generatedFiles(1).source.isEqual(
      source"""
              package foo

              import monocle.Lens

              trait HasDbConfig[T] {
                def dbConfig: Lens[T, DbConfig]
                def dbConfigHost: Lens[T, String] =
                  dbConfig composeLens Lens[DbConfig, String](_.host)(x => a => a.copy(host = x))
                def dbConfigPort: Lens[T, Int] =
                  dbConfig composeLens Lens[DbConfig, Int](_.port)(x => a => a.copy(port = x))
              }

              object HasDbConfig {
                def apply[T](implicit instance: HasDbConfig[T]): HasDbConfig[T] = instance

                implicit val id: HasDbConfig[DbConfig] = new HasDbConfig[DbConfig]() {
                  def dbConfig: Lens[DbConfig, DbConfig] = Lens.id[DbConfig]
                }

                //implicit val configHasDbConfig: HasDbConfig[Config] = new HasDbConfig[Config]() {
                //  def dbConfig: Lens[Config, DbConfig] = Lens[Config, DbConfig](_.dbConfig)(x => a => a.copy(dbConfig = x))
                //}

              }
      """))
  }

}
