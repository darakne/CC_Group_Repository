package video;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.RecursiveTask;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.freenect2.Logger;

public class DrawGradientPart  implements Callable<Boolean>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static int BUFFERED_IMAGE_TYPE = BufferedImage.TYPE_INT_ARGB;
	//private int numberOfColorsProcessedInOneGo;
	private ArrayList<Color> colors;
	private String imageFolder;
	private int imgNr;
	
	public DrawGradientPart(String imageFolder, ArrayList<Color> colors, int imageNr) {
		this.imageFolder = imageFolder;
		//this.numberOfColorsProcessedInOneGo = numberOfColorsProcessedInOneGo;
		this.colors = colors;
		this.imgNr = imageNr;
	}
	
	/*
	public int drawGradientImageParts() {
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
	} */
	
	/**
	 * Creates the gradient by LinearGradientPaint.
	 * @param colors colors to be drawn into the gradient.
	 * @return
	 */
	private BufferedImage createGradientImage() {
		if(null == colors) return null;
		if(colors.size() < 2) {
			colors.add(colors.get(0));
		}
		
		BufferedImage resultImg = new BufferedImage(10, 100, BUFFERED_IMAGE_TYPE);
				
         float[] dist = new float[colors.size()];
         for(int i=0, j=colors.size();i<j;++i) {
        	 float x = (float)i/(float)j;
        	 if(x < (float)1)  dist[i] = x;
        	 else x=1;       	
        	// System.out.println("dist[" + i +" / " + j + "]: " + dist[i]);       	 
         }
         Graphics2D g1 = resultImg.createGraphics();
         Point2D start = new Point2D.Float(0, 100);
         Point2D end = new Point2D.Float(200,100);
         //not enough heap space for this
         Color[] colorArr = colors.toArray(new Color[0]);
         LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colorArr); 
         g1.setPaint(p);
         g1.fillRect(0, 0, 300, 300);
         g1.dispose();
         
         
         return resultImg;
	}

	@Override
	public Boolean call() {
		BufferedImage image1 = createGradientImage();
		if(null == image1) {
			System.out.println("DrawGradientPart: image is null." );
			return false;
		}
		try {
			ImageIO.write(image1,"png", new File(imageFolder +"Img" + imgNr + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}
	


}
