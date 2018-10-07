package foo

object Main extends App {

  def needsDbConfig[A: HasDbConfig](a: A): Unit = {
    val port = HasDbConfig[A].dbConfigPort.get(a)
    println(s"The DB port is $port")
  }

  val config = Config(
    DbConfig("host", 1234),
    ApiConfig("url", "api key")
  )

  needsDbConfig(config)

  needsDbConfig(config.dbConfig)

}