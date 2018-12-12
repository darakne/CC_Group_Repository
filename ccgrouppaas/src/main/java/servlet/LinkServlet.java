package servlet;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



@WebServlet("/upload")
public class LinkServlet extends HttpServlet {
	private static final String UPLOAD_FOLDER ="C:/temp/";
	

	//https://www.geekmj.org/jersey/jax-rs-single-file-upload-example-406/

	/**
	 * 
	 */
	private static final long serialVersionUID = 7720710681846464323L;

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getParameter("link");
        
       if (url != null) {
    	   String videoname = "video.mp4";
    	   System.out.println(url);
    	   String location = UPLOAD_FOLDER + videoname;
    		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
  				  FileOutputStream fileOutputStream= new FileOutputStream(location)) {
  				    byte dataBuffer[] = new byte[1024];
  				    int bytesRead;
  				    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
  				        fileOutputStream.write(dataBuffer, 0, bytesRead);
  				    }
  				  request.getRequestDispatcher("/image.jsp").forward(request, response);
  				    
  				    
  				} catch (IOException e) {
  				    e.printStackTrace();
  				}
				    	  
       	   }
	       else {
	               request.setAttribute("error", "Unknown user, please try again");
	               request.getRequestDispatcher("/index.jsp").forward(request, response);
	           }
       

        
    }
	
}
