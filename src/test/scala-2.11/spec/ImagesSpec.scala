package spec

import java.io.{FileInputStream, File}
import cortex.view.Images

import scala.io.{BufferedSource, Source}

/**
  */
class ImagesSpec extends BaseSpec {

   object images extends Images {
     lazy val fav = {
       val source = scala.io.Source.fromFile(
         "src/test/images/conf_room.png"
       )(encoding)
       val byteArray = source.map(_.toByte).toArray
       source.close()
       byteArray
     }

     override def favicon = Option(fav)

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
