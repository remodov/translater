package ru.translater.format;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    public static void main(String file)throws IOException{
        BufferedImage img = null;
        File f = null;

        //read image
        try{
            f = new File(file);
            img = ImageIO.read(f);

            int width = img.getWidth() ;
            int height = img.getHeight();

            BufferedImage mimg = new BufferedImage(width*2, height, img.getType());

            for(int y = 0; y < height; y++){
                for(int lx = 0, rx = width*2 - 1; lx < width; lx++, rx--){
                    //lx starts from the left side of the image
                    //rx starts from the right side of the image
                    //get source pixel value
                    int p = img.getRGB(lx, y);
                    //set mirror image pixel value - both left and right
                    mimg.setRGB(lx, y, p);
                    mimg.setRGB(rx, y, p);
                }
            }

            img = Scalr.crop(mimg, mimg.getWidth()/2, 0, mimg.getWidth()/2  , mimg.getHeight());

            img = Scalr.resize(img,
                    Scalr.Method.ULTRA_QUALITY,
                    Scalr.Mode.FIT_EXACT,
                    img.getWidth() /2,
                    img.getHeight() /2 );

        }catch(IOException e){
            System.out.println(e);
        }

        //get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

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
        try{
            f.delete();
            f = new File(file);
            ImageIO.write(img, "jpg", f);
        }catch(IOException e){
            System.out.println(e);
        }
    }
}
