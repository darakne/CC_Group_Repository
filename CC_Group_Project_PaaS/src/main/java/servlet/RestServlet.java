package servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;



/**
 * 
 * @author a01046306 Elisabeth Wyslych, module: Cloud Computing 2018
 * The REST-servlet. ...
 */

@Path("/home")
public class RestServlet { 
	private static final String UPLOAD_FOLDER ="C:/Users/betul/Documents/temp/";
	
	public RestServlet() {
		
	}
	@Context
	private UriInfo context;
	
	@POST 
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormParam("file") InputStream uploadedInputStream,
			@FormParam("file") FormDataContentDisposition fileDetail) {	
			System.out.println("Im in the upload");
			if (uploadedInputStream == null || fileDetail == null)
				return Response.status(400).entity("Invalid form data").build();
			
			String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
			try {
				saveToDisk(uploadedInputStream, uploadedFileLocation);
			} catch (IOException e) {
				return Response.status(500).entity("Can not save file").build();
			}
			return Response.status(200)
					.entity("File saved to " + uploadedFileLocation).build();
				
	}
	
	private void saveToDisk(InputStream uploadedInputStream,String target)throws IOException {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[1024];
			out = new FileOutputStream(new File(target));
			while((read = uploadedInputStream.read(bytes))!=-1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
	}
}