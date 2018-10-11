package classy

import scala.meta._
import scala.meta.contrib.AssociatedComments

object Generator {

  case class GeneratedFile(path: String, source: Source)

  def generateOptics(warn: String => Unit)(inputSource: Source): List[GeneratedFile] = {
    val comments = AssociatedComments(inputSource)

    val pkg = inputSource.collect { case p @ Pkg(ref, stats) => ref }
      .headOption
      .getOrElse(Term.Name("classy"))

    inputSource.collect {
      case c @ Defn.Class(mods, name, tparams, ctor, templ) if mods.exists(isCase) && hasDirective(comments.leading(c)) && tparams.nonEmpty =>
        warn(s"Cannot generate classy lenses for case class ${c.name.value} because it is generic and that makes my head hurt")
        None
      case c @ Defn.Class(mods, name, tparams, ctor, templ) if mods.exists(isCase) && hasDirective(comments.leading(c)) =>
        val typeclassName = Type.Name(s"Has${name.value}")
        val mainLensMethodName = Term.Name(name.value.head.toLower + name.value.tail) // lowercase the first char of the case class name
        val mainLensReturnType = t"Lens[T, $name]"

        val fieldLenses = ctor.paramss.flatten.collect {
          case param if !param.mods.exists(isPrivate) =>
            val fieldName = Term.Name(param.name.value)
            val fieldType = param.decltpe.get
            val methodName = Term.Name(s"$mainLensMethodName${fieldName.value.head.toUpper + fieldName.value.tail}")
            val returnType = t"Lens[T, $fieldType]"
            val fieldLens = q"Lens[$name, $fieldType](_.$fieldName)(x => a => a.copy($fieldName = x))"
            q"def $methodName: $returnType = $mainLensMethodName composeLens $fieldLens"
        }

        val typeclassInstanceType = t"$typeclassName[$name]"
        val typeclassInstanceInit = init"$typeclassInstanceType()"

        // TODO also add instances for any other case classes in this file that have this type as a field

        val generatedSource = source"""
          package $pkg {

            import monocle.Lens

            trait $typeclassName[T] {
              def $mainLensMethodName: $mainLensReturnType
              ..$fieldLenses
            }

            object ${Term.Name(typeclassName.value)} {
              def apply[T](implicit instance: $typeclassName[T]): $typeclassName[T] = instance

              implicit val id: $typeclassInstanceType = new $typeclassInstanceInit {
                def $mainLensMethodName: Lens[$name, $name] = Lens.id[$name]
              }
            }

          }
         """

        val path = pkg.toString.replaceAllLiterally(".", "/") + "/" + s"${typeclassName.value}.scala"

        Some(GeneratedFile(path, generatedSource))
    }.flatten
  }

  private def isCase(mod: Mod): Boolean = mod match {
    case mod"case" => true
    case _ => false
  }

  private def isPrivate(mod: Mod): Boolean = mod match {
    case mod"private" => true
    case _ => false
  }

  private def hasDirective(leadingComments: Set[Token.Comment]): Boolean =
    leadingComments.exists(_.value.contains("generate-classy-lenses"))

}
