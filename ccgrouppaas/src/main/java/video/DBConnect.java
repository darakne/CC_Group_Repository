package video;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Just copy-paste for database testing
 *  
 *
 */
public class DBConnect {
	
	

	public static void connectJDBCToAWSEC2(int imgId, ArrayList<Integer> colors) {
		
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
	    	 try {
	    		 //ask if imid exists
	    		 //ask if color + imgid exists
	    		 	//if yes, ask occurences
	    		 
	    	  PreparedStatement ps = connection
	                    .prepareStatement("INSERT INTO Color (imgid, colorid, colorValue, occurence) VALUES (?, ?, ?, ?);");
	           
					ps.setInt(1, imgId);
					ps.setInt(2, colors.get(i));
			
	            ps.setString(2, "Meier");
	            ResultSet rs = ps.executeQuery();
	            while (rs.next())
	                System.out.println(rs.getString(1) + "\n" + rs.getString(2));
	            ps.close();
	            connection.close(); 
	        	} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	       
	    	
	        System.out.println("SUCCESS!!!! You made it, take control     your database now!");
	    } else {
	        System.out.println("FAILURE! Failed to make connection!");
	    }

	}

	public static void main(String[] args) {
		//DBConnect db = new DBConnect();
		//db.connectJDBCToAWSEC2();

	}

	

}
