package spec

import java.io.{FileInputStream, File}
import cortex.view.Images

import scala.io.{BufferedSource, Source}

/**
  */
class ImagesSpec extends BaseSpec {

   object images extends Images {
     override def favicon = new BufferedSource(
       new FileInputStream("src/test/images/conf_room.png"),
       1024
     )
     override def viewDir = new File(
       "src/test/images/"
     )
   }

   "A dynamic image file" should "equal the matching file" in {
     Source.fromFile(
       "src/test/images/conf_room.png"
     )(scala.io.Codec.ISO8859).map(
         _.toByte
       ).toArray should equal (images.conf_room())
   }
 }
