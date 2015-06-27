package cortex.view

import java.io.File

import scala.annotation.tailrec
import scala.io.Source
import scala.language.dynamics

/* Created by Jason Flax 6/27/2015 */
trait View extends Dynamic {
  /**
   * Abstract method so that inheritor must
   * choose a directory that their views are in (can be nested).
   * @return top views directory
   */
  protected def viewDir: File

  /**
   * Declare partial function type to be added
   * to our map (allows for all generic methods).
   */
  type GenFn = PartialFunction[Seq[Any],Any]

  import collection.mutable

  /** Dynamic method storage */
  private val fields =
    mutable.Map.empty[String, GenFn]
      .withDefault{ key => throw new NoSuchFieldError(key) }

  def selectDynamic(key: String) = fields(key)
  def updateDynamic(key: String)(value: GenFn) = fields(key) = value
  def applyDynamic(key: String)(args: Any*) = fields(key)(args)
  def applyDynamicNamed(name: String)(args: (String, Any)*) = fields(name)(args)

  /**
   * Traverse all files in view directory using tail recursion.
   * @param file top directory
   * @return list of all files
   */
  private def listFiles(file: File): List[File] = {
    @tailrec
    def listFiles(files: List[File], result: List[File]): List[File] =
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

  // create a dynamic method for each file so that the
  // consumer can have easy access, e.g. views.homepage()
  // TODO: cache file data as strings
  listFiles(viewDir).foreach { file =>
    this.updateDynamic(file.getName.replaceFirst("[.][^.]+$", "")) { case _ =>
      Source.fromFile(file).getLines() mkString "\n"
    }
  }
}