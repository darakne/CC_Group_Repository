package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import servlet.LinkServlet;
import video.VideoConfig;


/**
 * Just copy-paste for database testing
 *  
 *
 */
public class DBConnect {
	private static final Logger logger = LogManager.getLogger(LinkServlet.class.getName());
	
	VideoConfig config;
	VideoConfig dbconfig;
	
	public DBConnect() {
		config = new VideoConfig("config.properties");	
		dbconfig = new VideoConfig("database.properties");	
	}
	
	
	public void createColorTable() throws SQLException {
		Connection con=createDBConnection();
		Statement setupStatement = con.createStatement();
	    String createTable = dbconfig.getProperty("create_colortable");
	    String insertRow1 = "INSERT INTO colortable (imgid, colorvalue, occurence) VALUES (12, 12345453, 1);";

	    setupStatement.addBatch(createTable);
	    setupStatement.addBatch(insertRow1);
	    setupStatement.executeBatch();
	    setupStatement.close();
	    con.close(); 
	}
	
	public boolean dropColorTable() throws SQLException {
		Connection connection=createDBConnection();
		Statement statement = connection.createStatement();

		String dropTable = dbconfig.getProperty("drop_colortable");
		
        return statement.execute(dropTable);
	}
	

	
	/**
	 * Created from: https://www.mkyong.com/jdbc/how-to-connect-to-mysql-with-jdbc-driver-java/
	 * @return
	 * @throws SQLException 
	 */
	public Connection createDBConnection() throws SQLException {
		String PUBLIC_DNS = config.getProperty("rds_instance_hostname"); 
		String REMOTE_DATABASE_USERNAME = config.getProperty("db_user"); 
		String DATABASE_USER_PASSWORD = config.getProperty("db_user_password"); 
		String DBNAME = config.getProperty("db_name");
		
		  try {
		        Class.forName("com.mysql.cj.jdbc.Driver");
		    } catch (ClassNotFoundException e) {
		        System.out.println("No jdbc driver.");
		        e.printStackTrace();
		    }

		    logger.debug("MySQL JDBC Driver Registered!");
		    Connection connection = null;
		    String conStr = "jdbc:mysql://" + PUBLIC_DNS + ":3306/" +DBNAME;
		  //  System.out.println("conStr: " + conStr);
		   
		    connection = DriverManager.getConnection(conStr,REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
		    logger.debug("MySQL Connection created!");
		    return connection;
   
	}
	
	/**
	 * Checks the MySqlVersion number as test
	 * @return
	 */
	public boolean checkConnection() {
		boolean foundSomething=false;
		
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = createDBConnection();
			ps = connection.prepareStatement(dbconfig.getProperty("check_Connection"));
			ResultSet rs = ps.executeQuery();
			
	        while (rs.next()) {
	        	logger.debug("MySql version nr: " + rs.getString(1));
	            foundSomething=true;
	        }
	        
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					ps.close();
					connection.close(); 
					if(foundSomething) return true;
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		return false;
	}
	
	public boolean checkImgId(int imgId, Connection connection) throws SQLException{
		if(null == connection) 
			connection = createDBConnection();
			
		String statemenet = dbconfig.getProperty("check_ImgId");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, imgId);
        logger.debug("prepared statement" + ps);
        
        int imgId1=-1;
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
        	 String imgIdStr = rs.getString(1);
        	 if(null != imgIdStr) {
        		 imgId1 = Integer.parseInt(imgIdStr);	 
        	 }
         }
         ps.close();
         connection.close(); 
         
         if(imgId1 == imgId)
			 return true;
		
		return false;
	}
	
	public int checkImgIdAndColor_GetColorID(int imgId, int colorValue, Connection connection) throws SQLException{
		if(null == connection) 
			connection = createDBConnection();
			
		String statemenet = dbconfig.getProperty("check_ImgIdAndColorValue");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, imgId);
        ps.setInt(2, colorValue);
        logger.debug("prepared statement" + ps);
        
        int colorid=-1;
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
        	 String coloridStr = rs.getString(1);
        	 if(null != coloridStr) {
        		 colorid = Integer.parseInt(coloridStr);	 
        	 }
         }
         ps.close();
         connection.close(); 
         
         logger.debug("Returning colorid: " + colorid);
	return colorid;

	}
	
	
	public int getOccurrence(int colorId, Connection connection) throws SQLException {
		if(null == connection) 
			connection = createDBConnection();
		
		String statemenet = dbconfig.getProperty("get_Occurence");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, colorId);
        logger.debug("prepared statement" + ps);
        
        int occurrence=0;
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
        	 String occurrenceStr = rs.getString(1);
        	 if(null != occurrenceStr) {
        		 occurrence = Integer.parseInt(occurrenceStr);	 
        	 }
         }
         ps.close();
         connection.close(); 
         
         return occurrence;
	}
	
	public void createOrUpdateColor(int imgId, int colorValue, Connection connection) throws SQLException {
		
		int colorid = checkImgIdAndColor_GetColorID(imgId, colorValue, connection);
	
	
			connection = createDBConnection();
		if(colorid == -1) {
			//color is not there yet
			String statemenet = dbconfig.getProperty("insert_New_Color");
			
			PreparedStatement ps = connection.prepareStatement(statemenet);
	        ps.setInt(1, imgId);
	        ps.setInt(2, colorValue);
	        ps.setInt(3, 1);
	        logger.debug("prepared statement" + ps);
	        
	        ps.executeUpdate();
	        ps.close();
	        connection.close(); 
		}else {
			//color is there
			
			//get the occurrence first
			int occurence= getOccurrence(colorid, connection)+1;
			
			
			//now update table			
			String statemenet = dbconfig.getProperty("update_Color");
			PreparedStatement ps = connection.prepareStatement(statemenet);
	        ps.setInt(1, occurence);
	        ps.setInt(2, colorid);
	        logger.debug("prepared statement" + ps);
	        
	       int update = ps.executeUpdate();
	       logger.debug("updates: " + update);
	       
	        ps.close();
	        connection.close(); 
		}
		// System.out.println("colorid in method: " + colorid);
	}
	
	/**
	 * Not tested
	 * @param imgId
	 * @param colorValue
	 * @param connection
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteAllColorsOfImage(int imgId, Connection connection) throws SQLException {
		PreparedStatement ps = null;
		int deleted = 0;
		if(null == connection) 
			connection = createDBConnection();
		String statemenet = dbconfig.getProperty("delete_Color");
		ps = connection.prepareStatement(statemenet);
		ps.setInt(1, imgId);

		deleted = ps.executeUpdate();

		System.out.println("Record is deleted!");

		ps.close();
		connection.close();
		
		if(deleted ==1) return true;
		return false;
			
	}


	public static void main(String[] args) throws SQLException {
		DBConnect db = new DBConnect();
		db.checkConnection();
		//db.createColorTable(); //only for table init
		// System.out.println("db.dropColorTable(): " + db.dropColorTable());
		int imgId = 12;
		int colorValue=1;
		 System.out.println("res: checkImgId " + imgId + ": " + db.checkImgId(imgId, null));
		 System.out.println("res: checkImgIdAndColor_GetColorID: " + db.checkImgIdAndColor_GetColorID(imgId, colorValue, null));
		 db.createOrUpdateColor(imgId, colorValue, null);
		 // db.deleteAllColorsOfImage(imgId, null);
		 
	}

	

}
