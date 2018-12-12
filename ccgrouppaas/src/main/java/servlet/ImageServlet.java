package servlet;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/picture")
public class ImageServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException  {
		try{
	         String fileName = "animal.png";            
	         FileInputStream fis = new FileInputStream(new File("C:\\temp2\\"+fileName));
	         BufferedInputStream bis = new BufferedInputStream(fis);             
	         response.setContentType("image/png");
	         BufferedOutputStream output = new BufferedOutputStream(response.getOutputStream());
	         for (int data; (data = bis.read()) > -1;) {
	           output.write(data);
	         }             
	      }
	      catch(IOException e){

	      }finally{
	          // close the streams
	      }
	  		
	   
        
    }
}
