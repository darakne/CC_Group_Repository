package video;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Just copy-paste for database testing
 *  
 *
 */
public class DBConnect {

	private static final String PUBLIC_DNS = null;
	private static final String PORT = null;
	private static final String DATABASE = null;
	private static final String REMOTE_DATABASE_USERNAME = null;
	private static final String DATABASE_USER_PASSWORD = null;

	public static void main(String[] args) {
		
		  System.out.println("----MySQL JDBC Connection Testing -------");
		    
		    try {
		        Class.forName("com.mysql.jdbc.Driver");
		    } catch (ClassNotFoundException e) {
		        System.out.println("Where is your MySQL JDBC Driver?");
		        e.printStackTrace();
		        return;
		    }

		    System.out.println("MySQL JDBC Driver Registered!");
		    Connection connection = null;

		    try {
		        connection = DriverManager.
		                getConnection("jdbc:mysql://" + PUBLIC_DNS + ":" + PORT + "/" + DATABASE, REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
		    } catch (SQLException e) {
		        System.out.println("Connection Failed!:\n" + e.getMessage());
		    }

		    if (connection != null) {
		        System.out.println("SUCCESS!!!! You made it, take control     your database now!");
		    } else {
		        System.out.println("FAILURE! Failed to make connection!");
		    }

	}

}
