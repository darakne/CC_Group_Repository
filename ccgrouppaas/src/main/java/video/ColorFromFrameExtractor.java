package video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import database.DBConnect;

public class ColorFromFrameExtractor implements Callable<Boolean>{

	final static int TIMEOUT_MILLISECONDS = 10000;
	final int FORKPOOL = 3;
	private static final long serialVersionUID = 1L;
	
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	ArrayList<BufferedImage> frames;
	int threadId;
	int imgId;
	ArrayList<Color> result;
	

	public ColorFromFrameExtractor(int threadId, int imgId, ArrayList<BufferedImage> framesToExtractColorsList) {
		frames = framesToExtractColorsList;
		this.threadId = threadId;
		this.imgId = imgId;
	}
	/*
	/**
	 * if img is read by ImageIO
	 * sets type of BufferedImage or else we have problems later
	 * from: https://stackoverflow.com/questions/22391353/get-color-of-each-pixel-of-an-image-using-bufferedimages
	 * @param image
	 * @return
	 * */

	private BufferedImage convertToARGB(BufferedImage image){
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
	 * @param image A BufferedImage
	 * @return ArrayList<Color> is empty if BufferedImage was null
	 * */

	private void getColorsFromBufferedImage(BufferedImage image) {
		if(null == image) {
			System.out.println("BufferedImage was null.");
			return ;
		}
		if(image.getType() != BufferedImage.TYPE_INT_ARGB)
			image  = convertToARGB(image);
		
		
		int width=image.getWidth();
		int height = image.getHeight();
		//ArrayList<Integer> list = new ArrayList<>(width * height);
		// Color[][] colors = new Color[image.getWidth()][image.getHeight()];
		DBConnect db = new DBConnect();

		int value=0;
		ArrayList<Integer> colorvalues = new ArrayList<>();
		System.out.println("image: " + width + " x " + height);
	        for (int x = 0, y = 0; x < width; x++) {
	            for (y = 0; y < height ; y++) {
	            	value = image.getRGB(x, y);
					colorvalues.add(value);
	           
	            	//System.out.println(threadId + ", " + imgId  + " at (" + x + "/" + y + ") of ("+width+"/" +height+"), value: " + value);
					if(y % 30 == 0) {
						 try {
					        	db.createOrUpdateColors(imgId, colorvalues);
					        	System.out.println(threadId + ", " + imgId  + " at (" + x + "/" + y + ") of ("+width+"/" +height+"), " + colorvalues);
					        	colorvalues = new ArrayList<>(); 
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
	            }
	            try {
		        	db.createOrUpdateColors(imgId, colorvalues);
		        	System.out.println(threadId + ", " + imgId  + " at (" + x + "/" + y + ") of ("+width+"/" +height+")");
		        	colorvalues = new ArrayList<>(); 
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	           
	        }
	}
	
	
	@Override
	public Boolean call() throws Exception {
		System.out.println(threadId + ".thread is starting ...");		
		
		ExecutorService executor = Executors.newFixedThreadPool(FORKPOOL);
		ArrayList<Future<Boolean>> futures = new ArrayList<>();
		int i=1;
		int interval = 100;
		int fromWidth=0, toWidth=0;
		int fromHeight=0, toHeight=0;
		for(BufferedImage frame: frames) {
			//System.out.println(threadId + " works on frame " + i);
			//++i;
			if(null != frame) {
				/*
				for(int size=frame.getWidth(); fromWidth<size; fromWidth+=interval, toWidth+=interval) {
					if(toWidth > size) toWidth = size-1;
				
					for(int size2=frame.getHeight(); i<size2; fromHeight+=interval, toHeight+=interval) {
						if(toHeight > size) toHeight = size2-1;
						
						Future<Boolean> result = executor.submit(new ColorFromFrameExtractorMini(threadId + "_" + i, imgId, frame, fromWidth, fromHeight, toWidth, toHeight));		    	
						++i;
						futures.add(result);
					}
				}*/
					getColorsFromBufferedImage(frame);
				}
		}
		
		/*
		//wait unitl one minute goes away
				try {
					executor.awaitTermination(10,  TimeUnit.SECONDS);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				

				i=0;
				for(Future<Boolean> f:futures) {
					try {
						if(!f.isDone()) {
							System.out.println(i++ +".Opening Future Mini: " + f.get(20, TimeUnit.SECONDS));
							f.cancel(true);
						}
					}catch (TimeoutException e)	{
						System.out.println(i++ + ".thread was terminated.");
					} catch (InterruptedException | ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				executor.shutdownNow();
		*/

		System.out.println(threadId + ".thread: is finished.");
		return true;
		
	}


	
}
