package spec

import java.io.File

import cortex.io.Cortex
import cortex.util.test
import cortex.view.Images
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

/**
  */
class ImagesSpec extends FlatSpec with Matchers {

   object images extends Images {
     override def viewDir = new File(
       "/Users/jason/git/cortex/src/test/images/"
     )
   }

   @test object app extends Cortex {
     override def port = 9995

     override def controllers = Seq()
     override def views = Seq()
   }

   app.hashCode()

   "A dynamic image file" should "equal the matching file" in {
     Source.fromFile(
       "/Users/jason/git/cortex/src/test/images/conf_room.png"
     )(scala.io.Codec.ISO8859).map(
         _.toByte
       ).toArray should equal (images.conf_room())
   }
 }
