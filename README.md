# sbt-classy

Generates classy lenses using scala.meta.

## How to install

The plugin is not published yet.

## How to use

Enable the plugin. In build.sbt:

```
enablePlugins(ClassyPlugin)
```

Annotate your case classes with a comment containing `generate-classy-lenses`,
e.g.:

```scala
/*
 * generate-classy-lenses
 */
case class DbConfig(host: String, port: Int)
```

or

```scala
// generate-classy-lenses
case class DbConfig(host: String, port: Int)
```

The plugin will generate the following source file:

```scala
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
}
```

## Notes

### Monocle

The generated lenses are [Monocle](http://julien-truffaut.github.io/Monocle/)
lenses. However, the plugin does *not* add any Monocle dependencies to your
project. You need to do this yourself, which means you are free to choose things
like the Monocle version and whether you want to use macros or not.

The generated code only depends on `monocle.Lens`.

### Classy what?

"Classy lenses" basically means generating a type class and an instance of that
type class, along with some lenses.

Classy lenses often come in handy when you are writing code in so-called "MTL
style". Here is a great talk that explains in more detail (in Haskell): [Next
Level MTL - George Wilson](https://www.youtube.com/watch?v=GZPup5Iuaqw).

For related work in Scala, see [meow-mtl](https://github.com/oleg-py/meow-mtl).

## TODO

* Generate `HasFoo` type class instances for case classes with parameters of type
`Foo`
* Generate classy prisms
* Write some proper tests
