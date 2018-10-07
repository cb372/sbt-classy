package foo

import monocle.Lens
import monocle.macros.GenLens

/*
 * generate-classy-lenses
 */
case class DbConfig(host: String, port: Int)

// generate-classy-lenses
case class ApiConfig(url: String, apikey: String)

case class Config(
                   dbConfig: DbConfig,
                   apiConfig: ApiConfig
                 )

object Config {
  // TODO with some more scalameta magic we'll be able to auto-generate these as well
  implicit val hasDbConfig: HasDbConfig[Config] = new HasDbConfig[Config] {
    def dbConfig: Lens[Config, DbConfig] = GenLens[Config](_.dbConfig)
  }
  implicit val hasApiConfig: HasApiConfig[Config] = new HasApiConfig[Config] {
    def apiConfig: Lens[Config, ApiConfig] = GenLens[Config](_.apiConfig)
  }
}

