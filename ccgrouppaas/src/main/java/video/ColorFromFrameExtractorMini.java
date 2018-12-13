package video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import database.DBConnect;

public class ColorFromFrameExtractorMini implements Callable<Boolean>{
	private static final Logger logger = LogManager.getLogger(ColorFromFrameExtractorMini.class.getName());
	final static int TIMEOUT_MILLISECONDS = 10000;
	private static final long serialVersionUID = 1L;
	
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	private BufferedImage oneframe;
	private String threadId;
	private int imgId;

	private int fromWidth;
	private int toWidth;
	private int fromHeight;
	private int toHeight;
	

	public ColorFromFrameExtractorMini(String threadId, int imgId, BufferedImage frame, int fromWidth, int fromHeight, int toWidth, int toHeight) {
		oneframe = frame;
		this.threadId = threadId;
		this.imgId = imgId;
		
		this.fromWidth=fromWidth;
		this.fromHeight=fromHeight;
		
		this.toWidth=toWidth;
		this.toHeight=toHeight;
		
		logger.traceExit();
	}
	
	/**
	 * if img is read by ImageIO
	 * sets type of BufferedImage or else we have problems later
	 * from: https://stackoverflow.com/questions/22391353/get-color-of-each-pixel-of-an-image-using-bufferedimages
	 * @param image
	 * @return
	 */
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
	 */
	private void getColorsFromBufferedImage(BufferedImage image) {
		//image  = convertToARGB(image);
		
		
		int width=image.getWidth();
		int height = image.getHeight();
		//ArrayList<Integer> list = new ArrayList<>(width * height);
		// Color[][] colors = new Color[image.getWidth()][image.getHeight()];
		DBConnect db = new DBConnect();

		int value=0;
		ArrayList<Integer> colorvalues = new ArrayList<>();
	        for (int x = fromWidth, y = 0; x < toWidth && y <width; x++) {
	            for (y = fromHeight; y < toHeight && y <height; y++) {

	            //	if (Thread.interrupted()) break;
	            	
	            	value = image.getRGB(x, y);
					colorvalues.add(value);
	           
	            	System.out.println(threadId + ", " + imgId  + " at (" + x + "/" + y + ") of ("+width+"/" +height+"), value: " + value);

	            }
	            System.out.println(threadId + ", " + imgId  + " has:" + colorvalues);
	            try {
		        	db.createOrUpdateColors(imgId, colorvalues);
		        	logger.debug(threadId + ", " + imgId  + " at (" + x + "/" + y + ") of ("+width+"/" +height+")");
		        	colorvalues = new ArrayList<>(); 
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.catching(e);
				}      
	        }
	}
	
	@Override
	public Boolean call() {
		System.out.println(threadId + "-Mini is starting ...");
		
		//long startTime = System.currentTimeMillis();
		//long elapsedTime;
		

		int i=0;
		
			if(null == oneframe) 
				System.out.println("Mini-Frame is null.");
			
			if(oneframe.getWidth() > 0 && oneframe.getHeight() >0) {  
			//System.out.println(threadId + " works on frame " + i++);

				getColorsFromBufferedImage(oneframe);
			
			}
			
			//elapsedTime = System.currentTimeMillis() - startTime;
			//if (elapsedTime > TIMEOUT_MILLISECONDS) break;
		
		logger.debug(threadId + ".thread: is finished.");
		return true;
		
	}


	
}
