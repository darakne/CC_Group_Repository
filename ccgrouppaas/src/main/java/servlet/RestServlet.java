package servlet;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import database.DBConnect;
import video.DrawGradientPart;
import video.VideoGrabber;


/**
 * 
 * @author a01046306 Elisabeth Wyslych, module: Cloud Computing 2018
 * The REST-servlet. ...
 */

//only http://localhost:8080/CC_Group_Project_PaaS/home is index.jsp
@Path("/home")
public class RestServlet extends Application{ 
	private static final String UPLOAD_FOLDER ="C:/temp/";
	private static final Logger logger = LogManager.getLogger(RestServlet.class.getName());
	

	@Context
	private UriInfo context;
	
	

	/**
	 * Is for testing if the servlet is online.
	 * @return "hello"
	 */
	@Path("/hello")
	@GET // http://localhost:8080/CC_Group_Project_PaaS/home/hello
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHello() {	
		logger.trace("Saying hello.");
		return "Hello!";			
	}
	//https://www.geekmj.org/jersey/jax-rs-single-file-upload-example-406/
	@Path("/upload/{linkToVideo}")
	@POST 
	public Response uploadFile(@PathParam("linkToVideo") String linkToVideo) {	
			logger.debug("Im in the upload");
			if (linkToVideo == null) {
				logger.error("Invalid form data - status 400.");
				return Response.status(400).entity("Invalid form data").build();
			}
				
			String imgSource = "videoSource/";
			if(!new File(imgSource).exists() ) new File(imgSource).mkdir();
			
			logger.debug("linkToVideo: " + linkToVideo);
			String videourl = linkToVideo;//"http://mirrors.standaloneinstaller.com/video-sample/small.mp4"; //"C:/temp/randomvideo.mp4";
			//String videourl = "http://mirrors.standaloneinstaller.com/video-sample/small.mp4"; 
			String videoname = imgSource + "video.mp4";
			//download video
			try (BufferedInputStream in = new BufferedInputStream(new URL(videourl).openStream());
					  FileOutputStream fileOutputStream= new FileOutputStream(videoname)) {
					    byte dataBuffer[] = new byte[1024];
					    int bytesRead;
					    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
					        fileOutputStream.write(dataBuffer, 0, bytesRead);
					    }
					    
					    
					} catch (IOException e) {
					    e.printStackTrace();
					}
			String videopath = ""+videoname;

			int partsToSplitColors = 100;
			
			
			String imgfolder = "video/";
			if(!new File(imgfolder).exists() ) new File(imgfolder).mkdir();
			VideoGrabber vg = new VideoGrabber();		
			vg.setImageFolder(imgfolder);
			
			String testImgfolder = "C:/temp3/";
			
			int forkpool = 40;
			int waitingtimePerFrameInSeconds= 5;

			//process the video
//			BufferedImage image = ImageIO.read(new File("D:\\temp\\randomvideo.mp4"));
			System.out.println("---------------");
			DBConnect db = new DBConnect();
			
			int imgId = 0;
			boolean imgIdExists = true;
			while(imgIdExists) {
				imgId = (int)(Math.random()*10000000);
				if(imgId == 0) ++imgId;
				try {
					imgIdExists = db.checkImgId(imgId);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			System.out.println("ImgId created.");

			System.out.println("Collecting result colors.");
			//now collect the results
		
		    vg.extractFramesAndColorsFromVideo(videopath, imgId, forkpool, waitingtimePerFrameInSeconds);

			
			//do the rest
			//System.exit(0);
			//should be done by database
			//list = vg.sortColorListByLuminescence(list); //just for now

			
			
			//create the gradient image parts now
			//attention ArrayLists are empty now > colors saved at database
			ForkJoinPool forkJoinPool = new ForkJoinPool(forkpool);
			
			//int imgNr = vg.drawGradientImageParts(colors);
			//public int drawGradientImageParts(Integer[] colorInt) {
				//int colors to colors
				
			//to do: get some colors from database and send them to thread	
				int colorsize=0;
				try {
					colorsize = db.countColors(imgId);
					
					if(colorsize <1) {
						System.out.println("Zero colors to continue.");
						System.exit(1);
					}
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				boolean continueIt = true;
				int imgNr=0;
				int numberOfColorsProcessedInOneGo = (int) Math.ceil(colorsize  / partsToSplitColors);//100000;
				System.out.println("Colors to work with: " + colorsize);
				for(int i=0-numberOfColorsProcessedInOneGo+1, j=0, colorLen=colorsize, testJ=0;continueIt;) {			
					if(testJ < colorLen) {
						i = i+numberOfColorsProcessedInOneGo-1;
						j = j+numberOfColorsProcessedInOneGo;
					}
					else {
						j = colorLen;
						i = colorLen-i;
						continueIt=false;
					}
					
					
					
					System.out.println("Adding color to gradientpart: "+ i + "-" + j + " / " + colorLen );
					if(i<0 || j<=0) break;
				
					ArrayList<Color> colorsPartArr = new ArrayList<>();
					try {
						colorsPartArr = db.getColorsOrdered(imgId, i,j);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					forkJoinPool.submit(new DrawGradientPart(imgfolder, colorsPartArr, imgNr));
			    	
					System.out.println(imgNr + ".part (" + i + "/" + colorLen + ")");
											
					testJ = j+numberOfColorsProcessedInOneGo;
					++imgNr;
				}
				--imgNr;
				
			//	return imgNr;
			//}
			
				//delete colors from database by DBConnect deleteAllColorsOfImage(int imgId, Connection connection)
			try {
				db.deleteAllColorsOfImage(imgId);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 
			 BufferedImage result = vg.addAllImagePartsTogether( imgNr );
			//result = vg.blurImage( result );
			 
			   try {
				   if(null != result) {
					   System.out.println("Writing end image: " + imgfolder+ "Img.png");
		         		ImageIO.write(result,"png", new File(imgfolder+ "Img.png"));
				   }
	         	 } catch (IOException e) {
	  	            // TODO Auto-generated catch block
	  	            e.printStackTrace();
	  	        }
			
			System.out.println("Finished.");

			return Response.status(200)
					.entity("Delivering file ...").build();
				
	}
	
	
}