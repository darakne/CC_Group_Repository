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
	
	

	public static void connectJDBCToAWSEC2() {
		
		VideoConfig config = new VideoConfig();
		String PUBLIC_DNS = config.getProperty("rds_instance_hostname"); 
		String REMOTE_DATABASE_USERNAME = config.getProperty("db_user"); 
		String DATABASE_USER_PASSWORD = config.getProperty("db_user_password"); 
		String DBNAME = config.getProperty("db_name");
		
	    System.out.println("----MySQL JDBC Connection Testing -------");
	    
	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	    } catch (ClassNotFoundException e) {
	        System.out.println("Where is your MySQL JDBC Driver?");
	        e.printStackTrace();
	        return;
	    }

	    System.out.println("MySQL JDBC Driver Registered!");
	    Connection connection = null;
	    String conStr = "jdbc:mysql://" + PUBLIC_DNS + ":3306/" +DBNAME;
	    System.out.println("conStr: " + conStr);
	    try {
	    	connection = DriverManager.getConnection(conStr,REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
	    } catch (SQLException e) {
	        System.out.println("Connection Failed!:\n" + e.getMessage());
	    }

	    if (connection != null) {
	        System.out.println("SUCCESS!!!! You made it, take control     your database now!");
	    } else {
	        System.out.println("FAILURE! Failed to make connection!");
	    }

	}

	public static void main(String[] args) {
		DBConnect db = new DBConnect();
		db.connectJDBCToAWSEC2();

	}

	

}
