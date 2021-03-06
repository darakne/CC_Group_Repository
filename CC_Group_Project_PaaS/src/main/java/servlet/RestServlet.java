package servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	@Path("/upload")
	@POST 
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {	
			logger.debug("Im in the upload");
			if (uploadedInputStream == null || fileDetail == null) {
				logger.error("Invalid form data - status 400.");
				return Response.status(400).entity("Invalid form data").build();
			}
				
			String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
			try {
				saveToDisk(uploadedInputStream, uploadedFileLocation);
			} catch (IOException e) {
				logger.error("status 500 - Cannot save file");
				return Response.status(500).entity("Cannot save file").build();
			}
			logger.traceExit("File saved to " + uploadedFileLocation);
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