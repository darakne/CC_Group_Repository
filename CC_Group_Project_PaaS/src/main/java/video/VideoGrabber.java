package video;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

public class VideoGrabber {
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;


	/**
	 * if img is read by ImageIO
	 * sets type of BufferedImage or else we have problems later
	 * from: https://stackoverflow.com/questions/22391353/get-color-of-each-pixel-of-an-image-using-bufferedimages
	 * @param image
	 * @return
	 */
	public BufferedImage convertToARGB(BufferedImage image){
	    BufferedImage newImage = new BufferedImage(
	    image.getWidth(), image.getHeight(),
	    BUFFERED_IMAGE_TYPE);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	
	/**
	 * 
	 * @param image BufferedImage after it was converted by method "convertToARGB(img)"
	 * @return Color[][]
	 */
	public HashSet<Color> getColorFromBufferedImage(BufferedImage image) {
		HashSet<Color> list = new HashSet<>();
		// Color[][] colors = new Color[image.getWidth()][image.getHeight()];
	        for (int x = 0; x < image.getWidth(); x++) {
	            for (int y = 0; y < image.getHeight(); y++) {
	            	list.add(new Color(image.getRGB(x, y)));
	            }
	        }
	      //  System.out.println("getColorFromBufferedImage(): " + new ArrayList<Color>(list));
		  //now we have colors!
	        return list;
	}
	//to be thread-ed
	//no error handling with path yet
	public Color[] getSortedColors(String filepath) {
		HashSet<Color> list = new HashSet<>();
		
    	FFmpegFrameGrabber g = new FFmpegFrameGrabber(filepath);
		try {
			g.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//from here: https://github.com/bytedeco/javacv/blob/master/platform/src/test/java/org/bytedeco/javacv/FrameConverterTest.java

		int nullCounter = 0;
    	int len = g.getLengthInFrames();
    	list = new HashSet<>(len);
    	System.out.println("Frames to do: " + len);

        Java2DFrameConverter converter = new Java2DFrameConverter();        
        for(int i=1; i<len; ++i) {
        	try {
				g.setFrameNumber(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
        	Frame frame = null;
			try {
				frame = g.grab();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
            BufferedImage image = converter.convert(frame);           
            if(null != image) {
            	image  = convertToARGB(image);
            	list.addAll(getColorFromBufferedImage(image));
            	/*try {
            	ImageIO.write(image,"png", new File("D:\temp2\Img" + i + ".png"));
            	 } catch (Exception e) {
     	            // TODO Auto-generated catch block
     	            e.printStackTrace();
     	        }*/
            }else 
            	++nullCounter;
	       
        }
        try {
			g.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Null-Frames: " + nullCounter + "//" + len);
		System.out.println("Colors collected: " + list.size());
		
		//from here: https://stackoverflow.com/questions/25680108/how-to-sort-color-codes-from-light-to-dark-in-java
        ArrayList<Color> list1 = new ArrayList<Color>(list);
		list1.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
            	 return Float.compare(((float) c1.getRed() * 0.299f + (float) c1.getGreen() * 0.587f
                         + (float) c1.getBlue() * 0.114f) / 256f, ((float) c2.getRed() * 0.299f + (float) c2.getGreen()
                         * 0.587f + (float) c2.getBlue() * 0.114f) / 256f);
             }
        });
        
        return list.toArray(new Color[0]);
	}
	
	public BufferedImage createGradientImage(Color[] colors) {
		final BufferedImage resultImg = new BufferedImage(
                10, 100, BUFFERED_IMAGE_TYPE);
				
         float[] dist = new float[colors.length];
         for(int i=0, j=colors.length;i<j;++i) {
        	 float x = (float)i/(float)j;
        	 if(x < (float)1)  dist[i] = x;
        	 else x=1;       	
        	// System.out.println("dist[" + i +" / " + j + "]: " + dist[i]);       	 
         }
         Graphics2D g1 = resultImg.createGraphics();
         Point2D start = new Point2D.Float(0, 100);
         Point2D end = new Point2D.Float(200,100);
         //not enough heap space for this
         
         //https://stackoverflow.com/questions/27532/generating-gradients-programmatically
        for(int i=0, j=colors.length-1;i<j;++i) {
        	Color color1 = colors[i];
        	Color color2 = colors[i+1];
        	float ratio = dist[i];
 
        	if(null!=color1 && null!= color2) {
        		
        	
            int red = (int) (color2.getRed() * ratio + color1.getRed() * (1 - ratio));
            int green = (int) (color2.getGreen() * ratio + color1.getGreen() * (1 - ratio));
            int blue = (int) (color2.getBlue() * ratio + color1.getBlue() * (1 - ratio));
            Color stepColor = new Color(red, green, blue);
            
            g1.setPaint(stepColor);
            g1.fillRect(0, 0, 200, 200);
        	}
        }
     
         
        // LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors); 
     
         //g1.fillRect(0, 0, 200, 200);
         g1.dispose();
         return resultImg;
	}
	
	/**
	 * adds 2 BufferedImages together
	 * @param img1 A BufferedImage
	 * @param img2 A BufferedImage
	 * @return
	 */
	//copied from https://stackoverflow.com/questions/20826216/copy-two-bufferedimages-into-one-image-side-by-side
	public BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        int newWidth = img1.getWidth()+img2.getWidth();
        int newHeight = Math.max(img1.getHeight(),img2.getHeight());
        
        BufferedImage newImage = new BufferedImage(newWidth,newHeight, BUFFERED_IMAGE_TYPE);
        Graphics2D g2 = newImage.createGraphics();
       
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth(), 0);
        g2.dispose();
        return newImage;
    }
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		VideoGrabber vg = new VideoGrabber();
		

	//	BufferedImage image = ImageIO.read(new File("D:\\temp\\randomvideo.mp4"));
		
		Color[] colors = vg.getSortedColors("D:\\temp\\randomvideo.mp4");
		int colorPart = 100000; //(int) ((float)colors.length*0.1);
		boolean continueIt = true;
		int imgNr=0;
		for(int i=0-colorPart+1, j=0, colorLen=colors.length, testJ=0;continueIt;) {			
			if(testJ < colorLen) {
				i = i+colorPart-1;
				j = j+colorPart;
			}
			else {
				j = colorLen;
				i = colorLen-i;
				continueIt=false;
			}
			//System.out.println("copy arr: " + i + " " + (i+colorPart) + " / " + colorLen );
			Color[] colorsPartArr = Arrays.copyOfRange(colors, i, i +colorPart);
			BufferedImage image1 = vg.createGradientImage(colorsPartArr);
			
			ImageIO.write(image1,"png", new File("D:\\temp2\\Img" +imgNr + ".png"));
			
			System.out.println("\n---------------");
			System.out.println(imgNr + ".part (" + i + "/" + colorLen + ")");
			System.out.println("\n---------------");
					
			testJ = j+colorPart;
			++imgNr;
		}
		--imgNr;
		//now get images and add them together
		BufferedImage resultImg = ImageIO.read(new File("D:\\temp2\\Img" +0 + ".png"));
		joiningfor:for(int i=1; i<imgNr;++i) {
			BufferedImage image1 = ImageIO.read(new File("D:\\temp2\\Img" +i + ".png"));
			resultImg = vg.joinBufferedImage(resultImg, image1);
		}
		
		//now blur it
		///source: https://stackoverflow.com/questions/29295929/java-blur-image
		
		 	int radius = 30;
		    int size = radius * 2 + 1;
		    float weight = 1.0f / (size * size);
		    float[] data = new float[size * size];

		    for (int i = 0, datalength=data.length; i < datalength; ++i) {
		        data[i] = weight;
		    }
		    Kernel kernel = new Kernel(size, size, data);

		    BufferedImageOp op = new ConvolveOp( kernel );
		    //clone resultImg
		    ColorModel cm = resultImg.getColorModel();
		    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		    WritableRaster raster = resultImg.copyData(null);
		    BufferedImage otherImg = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		    resultImg = op.filter(otherImg, resultImg);
	
         try {
        	 System.out.println("Writing end image.");
         	ImageIO.write(resultImg,"png", new File("D:\\temp2\\Img.png"));
         	 } catch (Exception e) {
  	            // TODO Auto-generated catch block
  	            e.printStackTrace();
  	        }
	     
	}

}