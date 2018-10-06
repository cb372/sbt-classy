package foo

case class Config(
  dbConfig: DbConfig,
  apiConfig: ApiConfig
)

// generate-classy-lenses
case class DbConfig(host: String, port: Int)

// generate-classy-lenses
case class ApiConfig(url: String, apikey: String)
