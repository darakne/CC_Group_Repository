package image;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import org.bytedeco.javacv.FrameGrabber.Exception;

public class ImageGradient {

	public static void main(String[] args) throws Exception {
		  Point2D start = new Point2D.Float(0, 0);
		     Point2D end = new Point2D.Float(50, 50);
		     float[] dist = {0.0f, 0.2f, 1.0f};
		     Color[] colors = {Color.RED, Color.WHITE, Color.BLUE};
		     LinearGradientPaint p = new LinearGradientPaint(start, end, dist, colors);
	}
}
