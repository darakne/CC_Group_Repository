package servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * 
 * @author a01046306 Elisabeth Wyslych, module: Cloud Computing 2018
 * The REST-servlet. ...
 */
@Path("/restapi")
public class RestServlet { //extends javax.ws.rs.core.Application{
	private static final Logger logger = LogManager.getLogger(RestServlet.class.getName());

	
/**
 * Is for testing if the servlet is online.
 * @return "hello"
 */
	@GET // http://localhost:8080/CC_Group_Project_PaaS/restapi
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHello() {	
		logger.trace("Saying hello.");
		return "Hello!";			
	}

}