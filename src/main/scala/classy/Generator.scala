package classy

import scala.meta._
import scala.meta.contrib.AssociatedComments

object Generator {

  def generateOptics(tree: Source): List[Source] = {
    val comments = AssociatedComments(tree)

    val pkg = tree.collect { case p @ Pkg(ref, stats) => ref }
      .headOption
      .getOrElse(Term.Name("classy"))

    tree.collect {
      case c @ Defn.Class(mods, name, tparams, ctor, templ) if mods.exists(isCase) && hasDirective(comments.leading(c)) =>
        println(s"Found case class $name with generate-classy-lenses directive")

        val typeclassName = Type.Name(s"Has${name.value}")
        val mainLensMethodName = Term.Name(name.value.head.toLower + name.value.tail) // lowercase the first char of the case class name
        val mainLensReturnType = t"Lens[T, $name]"

        // TODO handle private ctors
        val fieldLenses = ctor.paramss.flatten.map { param =>
          val fieldName = Term.Name(param.name.value)
          val fieldType = param.decltpe.get
          val methodName = Term.Name(s"$mainLensMethodName${fieldName.value.head.toUpper + fieldName.value.tail}")
          val returnType = t"Lens[T, $fieldType]"
          val fieldLens = q"Lens[$name, $fieldType](_.$fieldName)(x => a => a.copy($fieldName = x))"
          q"def $methodName: $returnType = $mainLensMethodName composeLens $fieldLens"
        }

        val typeclassInstanceType = t"$typeclassName[$name]"
        val typeclassInstanceInit = init"$typeclassInstanceType()"

        source"""
           import monocle.Lens

           package $pkg {

            trait $typeclassName[T] {
              def $mainLensMethodName: $mainLensReturnType
              ..$fieldLenses
            }

            object ${Term.Name(typeclassName.value)} {
              implicit val id: $typeclassInstanceType = new $typeclassInstanceInit {
                def $mainLensMethodName: Lens[$name, $name] = Lens.id[$name, $name]
              }
            }

           }
         """
    }
  }

  private def isCase(mod: Mod) = mod match {
    case mod"case" => true
    case _ => false
  }

  private def hasDirective(leadingComments: Set[Token.Comment]): Boolean =
    leadingComments.exists(_.value.contains("generate-classy-lenses"))

}
