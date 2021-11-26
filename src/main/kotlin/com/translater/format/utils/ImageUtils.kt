package com.translater.format.utils

import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class ImageUtils {
    companion object {
        fun main(file: String) {
            var img: BufferedImage? = null
            var f: File? = null

            //read image

            //read image
            try {
                f = File(file)
                img = ImageIO.read(f)
                val width = img.width
                val height = img.height
                val mimg = BufferedImage(width * 2, height, img.type)
                for (y in 0 until height) {
                    var lx = 0
                    var rx = width * 2 - 1
                    while (lx < width) {

                        //lx starts from the left side of the image
                        //rx starts from the right side of the image
                        //get source pixel value
                        val p = img.getRGB(lx, y)
                        //set mirror image pixel value - both left and right
                        mimg.setRGB(lx, y, p)
                        mimg.setRGB(rx, y, p)
                        lx++
                        rx--
                    }
                }
                img = Scalr.crop(mimg, mimg.width / 2, 0, mimg.width / 2, mimg.height)
                img = Scalr.resize(
                    img,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    img.width / 2,
                    img.height / 2
                )
            } catch (e: IOException) {
                println(e)
            }

            //get image width and height

            //get image width and height
            val width = img!!.width
            val height = img.height

            //        //convert to grayscale
            //        for(int y = 0; y < height; y++){
            //            for(int x = 0; x < width; x++){
            //                int p = img.getRGB(x,y);
            //
            //                int a = (p>>24)&0xff;
            //                int r = (p>>16)&0xff;
            //                int g = (p>>8)&0xff;
            //                int b = p&0xff;
            //
            //                //calculate average
            //                int avg = (r+g+b)/3;
            //
            //                //replace RGB value with avg
            //                p = (a<<24) | (avg<<16) | (avg<<8) | avg;
            //
            //                img.setRGB(x, y, p);
            //            }
            //        }

            //write image

            //        //convert to grayscale
            //        for(int y = 0; y < height; y++){
            //            for(int x = 0; x < width; x++){
            //                int p = img.getRGB(x,y);
            //
            //                int a = (p>>24)&0xff;
            //                int r = (p>>16)&0xff;
            //                int g = (p>>8)&0xff;
            //                int b = p&0xff;
            //
            //                //calculate average
            //                int avg = (r+g+b)/3;
            //
            //                //replace RGB value with avg
            //                p = (a<<24) | (avg<<16) | (avg<<8) | avg;
            //
            //                img.setRGB(x, y, p);
            //            }
            //        }

            //write image
            try {
                f!!.delete()
                f = File(file)
                ImageIO.write(img, "jpg", f)
            } catch (e: IOException) {
                println(e)
            }
        }
    }
}