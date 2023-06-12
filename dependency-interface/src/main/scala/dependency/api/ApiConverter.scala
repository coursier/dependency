package dependency.api

import scala.collection.JavaConverters._

object ApiConverter {

  // originally adapted from https://github.com/VirtusLab/scala-cli/blob/9283e874966a86861d099273e121fed4b0954b7d/modules/options/src/main/scala/scala/build/internals/Util.scala#L33-L40
  // and https://github.com/VirtusLab/scala-cli/blob/9283e874966a86861d099273e121fed4b0954b7d/modules/options/src/main/scala/scala/build/internals/Util.scala#L57-L77

  def module(mod: _root_.dependency.Module): coursierapi.Module =
    coursierapi.Module.of(mod.organization, mod.name, mod.attributes.asJava)

  def dependency(dep: _root_.dependency.Dependency): coursierapi.Dependency = {
    val mod  = module(dep.module)
    var dep0 = coursierapi.Dependency.of(mod, dep.version)
    if (dep.exclude.nonEmpty)
      dep0 = dep0.withExclusion {
        dep.exclude
          .toSet[_root_.dependency.Module]
          .map { mod =>
            new java.util.AbstractMap.SimpleEntry(mod.organization, mod.name) : java.util.Map.Entry[String, String]
          }
          .asJava
      }
    val cl = dep.userParams.get("classifier").flatten
      .orElse(Option(dep0.getPublication).map(_.getClassifier))
      .getOrElse("")
    val tpe = dep.userParams.get("type").flatten
      .orElse(Option(dep0.getPublication).map(_.getType))
      .getOrElse("")
    val ext = dep.userParams.get("ext").flatten
      .orElse(Option(dep0.getPublication).map(_.getExtension))
      .getOrElse("")
    val pubName = Option(dep0.getPublication).map(_.getName)
      .getOrElse("")
    dep0 = dep0.withPublication(
      if (pubName.isEmpty && tpe.isEmpty && ext.isEmpty && cl.isEmpty)
        null
      else
        new coursierapi.Publication(pubName, tpe, ext, cl)
    )
    for (_ <- dep.userParams.get("intransitive"))
      dep0 = dep0.withTransitive(false)
    dep0
  }

}
