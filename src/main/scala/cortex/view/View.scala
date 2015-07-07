package cortex.view

import java.io.File

import scala.annotation.tailrec
import scala.language.dynamics

/* Created by Jason Flax 6/27/2015 */
trait View extends Dynamic {
  /**
   * Abstract method so that inheritor must
   * choose a directory that their views are in (can be nested).
   * @return top views directory
   */
  protected def viewDir: File

  protected[view] def encoding = scala.io.Codec.UTF8

  /**
   * Declare partial function type to be added
   * to our map (allows for all generic methods).
   */
  type GenFn = PartialFunction[Seq[Any], Array[Byte]]

  /** Dynamic method storage */
  // create a dynamic method for each file so that the
  // consumer can have easy access, e.g. views.homepage()
  // TODO: cache file data as strings
  protected[view] lazy val fields: Map[String, (String, GenFn)] =
    listFiles(viewDir).map { file =>
      val ext = """.\w+$""".r
      this.updateDynamic(
        file.getName.replaceFirst("[.][^.]+$", "")
      )(
        ext.findFirstIn(file.getName).getOrElse("") ->
          { case _ =>
            val source = scala.io.Source.fromFile(file)(encoding)
            val byteArray = source.map(_.toByte).toArray
            source.close()
            byteArray
          }
      )
    }.toMap.withDefault { key => throw new NoSuchFieldError(key) }

  def selectDynamic(key: String) = fields(key)._2
  def updateDynamic(key: String)(value: (String, GenFn)) = key -> value
  def applyDynamic(key: String)(args: (Any, Any)*): Array[Byte] = fields(key)._2(args)
  def applyDynamicNamed(name: String)(args: (String, Any)*) = fields(name)._2(args)

  /**
   * Traverse all files in view directory using tail recursion.
   * @param file top directory
   * @return list of all files
   */
  private def listFiles(file: File): List[File] = {
    @tailrec def listFiles(files: List[File], result: List[File]): List[File] =
      files match {
        case Nil => result
        case head :: tail if head.isDirectory =>
          listFiles(Option(head.listFiles).map(
            _.toList ::: tail
          ).getOrElse(tail), result)
        case head :: tail if head.isFile =>
          listFiles(tail, head :: result)
      }
    listFiles(List(file), Nil)
  }
}
