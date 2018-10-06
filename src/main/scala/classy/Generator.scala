package classy

import scala.meta._
import scala.meta.contrib.AssociatedComments

object Generator {

  def generateOptics(tree: Source): Option[Source] = {
    val comments = AssociatedComments(tree)
    tree.traverse {
      case c @ Defn.Class(mods, name, tparams, ctor, templ) if mods.exists(isCase) && hasDirective(comments.leading(c)) =>
        println(s"Found case class $name with generate-classy-lenses directive")

        q"""
           package classy
         """
    }
    None
  }

  private def isCase(mod: Mod) = mod match {
    case mod"case" => true
    case _ => false
  }

  private def hasDirective(leadingComments: Set[Token.Comment]): Boolean =
    leadingComments.exists(_.value.contains("generate-classy-lenses"))

}
