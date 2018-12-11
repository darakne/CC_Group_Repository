package video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

public class ColorFromFrameExtractor extends RecursiveTask<ArrayList<Integer>> {//implements Callable<ArrayList<Integer>>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	ArrayList<Frame> frames;
	int threadId;
	ArrayList<Color> result;

	public ColorFromFrameExtractor(int threadId, ArrayList<Frame> framesToExtractColorsList) {
		frames = framesToExtractColorsList;
		this.threadId = threadId;
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
	public ArrayList<Integer> getColorsFromBufferedImage(BufferedImage image) {
		if(null == image) {
			System.out.println("BufferedImage was null.");
			return new ArrayList<Integer>();
		}
		image  = convertToARGB(image);
		
		
		int width=image.getWidth();
		int height = image.getHeight();
		ArrayList<Integer> list = new ArrayList<>(width * height);
		// Color[][] colors = new Color[image.getWidth()][image.getHeight()];
	        for (int x = 0; x < width; x++) {
	            for (int y = 0; y < height ; y++) {
	            		list.add(image.getRGB(x, y));
	            //	System.out.println(" adding color: "+ x + "/" + width + ", " + y + "/" + height);
	            }
	            
	           // System.out.println("row: "+ x + "/" + width);
	        }
	        
		  //now we have colors!
	        return list;
	}
	
	@Override
	public ArrayList<Integer> compute() {
	//	System.out.println(threadId + ".thread is starting ...");
		ArrayList<Integer> result = new ArrayList<>();
		
		Java2DFrameConverter converter = new Java2DFrameConverter(); 
		for(Frame frame: frames) {
			BufferedImage image = converter.convert(frame);    
			//System.out.println(threadId + " works on frame " + i);
			if(null != image)
				result.addAll(getColorsFromBufferedImage(image));
		}
	//	System.out.println(threadId + ".thread: returns colors: " + result.size());
		return result;
		
	}


	
}