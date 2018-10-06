package classy

import org.scalatest._
import scala.meta._

class GeneratorSpec extends FlatSpec {

  val input = """package foo
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

  it should "generate classy lenses for case classes" in {
    println(Generator.generateOptics(input))
  }

}
