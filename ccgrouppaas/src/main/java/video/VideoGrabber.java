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
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;


public class VideoGrabber {
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	
	private String imageFolder;
	
	public String getImageFolder() {
		return imageFolder;
	}

	public void setImageFolder(String imageFolder) {
		if(null != imageFolder) {
			if(new File(imageFolder).exists()) {
				
				if(imageFolder.endsWith(File.separator))
					this.imageFolder = imageFolder;
				else
					this.imageFolder = imageFolder+File.separator;
			}
		}
		
	}

	

	/**
	 * if img is read by ImageIO
	 * sets type of BufferedImage or else we have problems later
	 * from: https://stackoverflow.com/questions/22391353/get-color-of-each-pixel-of-an-image-using-bufferedimages
	 * @param image
	 * @return
	 */
	/* moved to ColorFromFrameExtractor
	private BufferedImage convertToARGB(BufferedImage image){
	    BufferedImage newImage = new BufferedImage(
	    image.getWidth(), image.getHeight(),
	    BUFFERED_IMAGE_TYPE);
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    return newImage;
	}
	*/
	/**
	 * 
	 * @param image A BufferedImage
	 * @return ArrayList<Color> is empty if BufferedImage was null
	 */
	/* Moved to ColorFromFrameExtractor
	public ArrayList<Color> getColorsFromBufferedImage(BufferedImage image) {
		if(null == image) {
			System.out.println("BufferedImage was null.");
			return new ArrayList<Color>();
		}
		image  = convertToARGB(image);
		
		int width=image.getWidth();
		int height = image.getHeight();
		ArrayList<Color> list = new ArrayList<>(width * height);
		// Color[][] colors = new Color[image.getWidth()][image.getHeight()];
	        for (int x = 0; x < width; x++) {
	            for (int y = 0; y < height ; y++) {
	            	list.add(new Color(image.getRGB(x, y)));
	            //	System.out.println(" adding color: "+ x + "/" + width + ", " + y + "/" + height);
	            }
	            
	           // System.out.println("row: "+ x + "/" + width);
	        }
	        
		  //now we have colors!
	        return list;
	}
	*/
	
	public  ArrayList<Integer> extractFramesAndColorsFromVideo(String videopath, int forkpool)  {

		FFmpegFrameGrabber g = new FFmpegFrameGrabber(videopath);
		try {
			g.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//from here: https://github.com/bytedeco/javacv/blob/master/platform/src/test/java/org/bytedeco/javacv/FrameConverterTest.java

		int nullCounter = 0;
    	int len = g.getLengthInFrames();

    	System.out.println("Frames to do: " + len);  
        
        //lets work with a threadpool with threads for color extracting
       // ExecutorService executorService = Executors.newFixedThreadPool(50);
         ArrayList<Integer> resultColors = new ArrayList<>();
         ForkJoinPool forkJoinPool = new ForkJoinPool(forkpool);
    	try {
    	
        
        int listLength = 1; //frames per thread
        ArrayList<Frame> imglist = new ArrayList<>(listLength);
        int imgCounter=0;
        int threadId=0;
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
             
            //collect 5 frames
            if(null != frame) {
            //	System.out.println("frame");
            	imglist.add(frame);
            	++imgCounter;
            	//list.addAll(getColorsFromBufferedImage(image));
            	
            	
             /*	try {
            	ImageIO.write(image,"png", new File(testImgfolder + i + ".png"));
            	 } catch (Exception e) {
     	            // TODO Auto-generated catch block
     	            e.printStackTrace();
     	        } */
            }else {
            	++nullCounter;
            //	System.out.println("null frame");
            }
            	
            
            if(imgCounter % listLength == 0) {
            	//create a thread to extract the 5 frames
            	ArrayList<Integer> result = forkJoinPool.invoke(new ColorFromFrameExtractor(threadId, imglist));
            	resultColors.addAll(result);
            	System.out.println("colors collected so far: " + resultColors.size());
            	
            	//executorService.execute(new ColorFromFrameExtractor(threadId, imglist));
            	
            	imglist = new ArrayList<>(); //empty list
            	imgCounter = 0;
            	++threadId;
            }
	      
       //   System.out.println("working on frame: " +i + "/" + len);
        }
        
        if(imgCounter != 0) {
        	//create a thread to extract the 5 frames
        	ArrayList<Integer> result = forkJoinPool.invoke(new ColorFromFrameExtractor(threadId, imglist));
        	resultColors.addAll(result);
        	imglist = new ArrayList<>(); //empty list
        	imgCounter = 0;
        	++threadId;
        }
        
        try {
			g.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
  
        System.out.println("Waiting to terminate threads: " + (threadId-1));
        
    	}catch(OutOfMemoryError e) {
    		e.printStackTrace();
    		//System.gc ();
    		return resultColors;
    	}
		//wait unitl one minute goes away
		try {
			forkJoinPool.awaitTermination(10,  TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		System.out.println("Null-Frames: " + nullCounter + "//" + len);
		System.out.println("Colors collected: " + resultColors.size());
		
		return resultColors;
	}
	
	/**
	 * Sorts the input list by liminescence.
	 * @param list
	 * @return
	 */
	public ArrayList<Color> sortColorListByLuminescence(ArrayList<Color> list) {
		if(null == list) {
			System.out.println("Arraylist was null.");
			return new ArrayList<Color>();
		}

		//from here: https://stackoverflow.com/questions/25680108/how-to-sort-color-codes-from-light-to-dark-in-java
        list.sort(new Comparator<Color>() {
            @Override
            public int compare(Color c1, Color c2) {
            	 return Float.compare(((float) c1.getRed() * 0.299f + (float) c1.getGreen() * 0.587f
                         + (float) c1.getBlue() * 0.114f) / 256f, ((float) c2.getRed() * 0.299f + (float) c2.getGreen()
                         * 0.587f + (float) c2.getBlue() * 0.114f) / 256f);
             }
        });
        
        return list;
	}
	/**
	 * Uses the input to draw small pieces of the gradient. Uses createGradientImage() for that
	 * @param colors
	 * @return number of images created.
	 */
	/* Moved into DrawGradientPart
	public int drawGradientImageParts(Integer[] colorInt) {
		//int colors to colors
		
		Color[] colors = new Color[colorInt.length];
		int ii=0;
		for(int ic: colorInt) {
			Color c = new Color(ic);
			colors[ii] = c;
			++ii;
		}
		
		boolean continueIt = true;
		int imgNr=0;
		for(int i=0-numberOfColorsProcessedInOneGo+1, j=0, colorLen=colors.length, testJ=0;continueIt;) {			
			if(testJ < colorLen) {
				i = i+numberOfColorsProcessedInOneGo-1;
				j = j+numberOfColorsProcessedInOneGo;
			}
			else {
				j = colorLen;
				i = colorLen-i;
				continueIt=false;
			}
			System.out.println("Working on color: "+ i + "-" + j + " / " + colorLen );
			Color[] colorsPartArr = Arrays.copyOfRange(colors, i, i +numberOfColorsProcessedInOneGo);
			BufferedImage image1 = createGradientImage(colorsPartArr);
			
			try {
				ImageIO.write(image1,"png", new File(imageFolder +"Img" + imgNr + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			System.out.println(imgNr + ".part (" + i + "/" + colorLen + ")");
			
					
			testJ = j+numberOfColorsProcessedInOneGo;
			++imgNr;
		}
		--imgNr;
		
		return imgNr;
	}
	*/
	
	/**
	 * Creates the gradient by LinearGradientPaint.
	 * @param colors colors to be drawn into the gradient.
	 * @return
	 */
	/*
	private BufferedImage createGradientImage(Color[] colors) {
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
         LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors); 
         g1.setPaint(p);
         g1.fillRect(0, 0, 200, 200);
         g1.dispose();
         return resultImg;
	}
	*/
	
	/**
	 * 
	 * @param numberOfImages Number of images to be added together (Images are named with like Img0.png)
	 * @return
	 */
	public BufferedImage addAllImagePartsTogether(int numberOfImages) {
		if(numberOfImages<=0) return null;
		//now get images and add them together
		BufferedImage resultImg = null;
		try {
			resultImg = ImageIO.read(new File(imageFolder + "Img" +0 + ".png"));
		
			joiningfor:for(int i=1; i<numberOfImages;++i) {
				BufferedImage image1 = ImageIO.read(new File(imageFolder+ "Img" +i + ".png"));
				resultImg = joinBufferedImage(resultImg, image1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("---------------");
		
		return resultImg;
	}
	
	/**
	 * adds 2 BufferedImages together
	 * @param img1 A BufferedImage
	 * @param img2 A BufferedImage
	 * @return
	 */
	//copied from https://stackoverflow.com/questions/20826216/copy-two-bufferedimages-into-one-image-side-by-side
	private BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        int newWidth = img1.getWidth()+img2.getWidth();
        int newHeight = Math.max(img1.getHeight(),img2.getHeight());
        
        BufferedImage newImage = new BufferedImage(newWidth,newHeight, BUFFERED_IMAGE_TYPE);
        Graphics2D g2 = newImage.createGraphics();
       
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth(), 0);
        g2.dispose();
        return newImage;
    }
	
	/**
	 * Blurs the input image
	 * @param image
	 */
	public BufferedImage blurImage(BufferedImage image) {
		if(null == image) return null;
		//now blur it
				///source: https://stackoverflow.com/questions/29295929/java-blur-image
				
		 	int radius = 30;
		    int size = radius * 2 + 1;
		    int sizeXsize = size * size;
		    float weight = 1.0f / (sizeXsize);
		    float[] data = new float[sizeXsize];

		    for (int i = 0, datalength=data.length; i < datalength; ++i) {
		        data[i] = weight;
		    }
		    Kernel kernel = new Kernel(size, size, data);

		    BufferedImageOp op = new ConvolveOp( kernel );
		    //clone resultImg
		    ColorModel cm = image.getColorModel();
		    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		    WritableRaster raster = image.copyData(null);
		    BufferedImage otherImg = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		    image = op.filter(otherImg, image);
			
		    return image;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		VideoGrabber vg = new VideoGrabber();
		
		String videopath = "C:/temp/randomvideo2.mp4";
		
		int partsToSplitColors = 10;
		
		
		String imgfolder = "C:/temp2/";
		vg.setImageFolder(imgfolder);
		
		String testImgfolder = "C:/temp3/";
		
		int forkpool = 20;

		//process the video
//		BufferedImage image = ImageIO.read(new File("D:\\temp\\randomvideo.mp4"));
		System.out.println("---------------");

		System.out.println("Collecting result colors.");
		//now collect the results
	    ArrayList<Integer> list = vg.extractFramesAndColorsFromVideo(videopath, forkpool);

		
		//do the rest
		//System.exit(0);
		//should be done by database
		//list = vg.sortColorListByLuminescence(list); //just for now
		Integer[] colorInt = list.toArray(new Integer[0]);
		int numberOfColorsProcessedInOneGo = (int) Math.ceil(colorInt.length  / partsToSplitColors);//100000;
		System.out.println("Colors to work with: " + list.size());
		
		//create the gradient image parts now
		ForkJoinPool forkJoinPool = new ForkJoinPool(forkpool);
		
		//int imgNr = vg.drawGradientImageParts(colors);
		//public int drawGradientImageParts(Integer[] colorInt) {
			//int colors to colors
			
			Color[] colors = new Color[colorInt.length];
			int ii=0;
			for(int ic: colorInt) {
				Color c = new Color(ic);
				colors[ii] = c;
				++ii;
			}
			

			boolean continueIt = true;
			int imgNr=0;
			for(int i=0-numberOfColorsProcessedInOneGo+1, j=0, colorLen=colors.length, testJ=0;continueIt;) {			
				if(testJ < colorLen) {
					i = i+numberOfColorsProcessedInOneGo-1;
					j = j+numberOfColorsProcessedInOneGo;
					
					
				}
				else {
					j = colorLen;
					i = colorLen-i;
					continueIt=false;
				}
				
				System.out.println("Working on color: "+ i + "-" + j + " / " + colorLen );
				if(i<0 || j<=0) break;
				
				Color[] colorsPartArr = Arrays.copyOfRange(colors, i, i +numberOfColorsProcessedInOneGo);	
				forkJoinPool.invoke(new DrawGradientPart(imgfolder, colorsPartArr, imgNr));
		    	
				System.out.println(imgNr + ".part (" + i + "/" + colorLen + ")");
										
				testJ = j+numberOfColorsProcessedInOneGo;
				++imgNr;
			}
			--imgNr;
			
		//	return imgNr;
		//}
		
		
		 
		 BufferedImage image = vg.addAllImagePartsTogether( imgNr );
		 BufferedImage result = vg.blurImage( image );
		 
		   try {
			   if(null != result) {
				   System.out.println("Writing end image: " + imgfolder+ "Img.png");
	         		ImageIO.write(result,"png", new File(imgfolder+ "Img.png"));
			   }
         	 } catch (IOException e) {
  	            // TODO Auto-generated catch block
  	            e.printStackTrace();
  	        }
		
		
		
		 //start instance
       	/*RunInstancesRequest runInstancesRequest =
       			   new RunInstancesRequest();

       			runInstancesRequest.withImageId("ami-a9d09ed1")
       			                   .withInstanceType(InstanceType.T1Micro)
       			                   .withMinCount(1)
       			                   .withMaxCount(1)
       			                   .withKeyName("my-key-pair")
       			                   .withSecurityGroups("my-security-group");
       	*/
	}

}