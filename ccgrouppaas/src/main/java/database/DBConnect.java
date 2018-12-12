package database;

import java.awt.Color;
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

		    //logger.debug("MySQL JDBC Driver Registered!");
		    Connection connection = null;
		    String conStr = "jdbc:mysql://" + PUBLIC_DNS + ":3306/" +DBNAME;
		  //  System.out.println("conStr: " + conStr);
		   
		    connection = DriverManager.getConnection(conStr,REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
		    //logger.debug("MySQL Connection created!");
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
	        	//logger.debug("MySql version nr: " + rs.getString(1));
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
        //logger.debug("prepared statement" + ps);
        
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
         
         logger.info("img id is: " + imgId1 + "==" +imgId);
         
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
        //logger.debug("prepared statement" + ps);
        
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
         
         logger.info("Returning colorid: " + colorid);
	return colorid;

	}
	
	
	public int getOccurrence(int colorId, Connection connection) throws SQLException {
		if(null == connection) 
			connection = createDBConnection();
		
		String statemenet = dbconfig.getProperty("get_Occurence");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, colorId);
        //logger.debug("prepared statement" + ps);
        
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
	
	public boolean createOrUpdateColor(int imgId, int colorValue, Connection connection) throws SQLException {
		
		int colorid = checkImgIdAndColor_GetColorID(imgId, colorValue, connection);
		 int update= -1;
		if(null == connection) 
			connection = createDBConnection();
		if(colorid == -1) {
			//color is not there yet
			String statemenet = dbconfig.getProperty("insert_New_Color");
			
			PreparedStatement ps = connection.prepareStatement(statemenet);
	        ps.setInt(1, imgId);
	        ps.setInt(2, colorValue);
	        ps.setInt(3, 1);
	        //logger.debug("prepared statement" + ps);
	        
	        ps.executeUpdate();
	        ps.close();
	        connection.close(); 
		}else {
			//color is there
			
			//get the occurrence first
			int occurence= getOccurrence(colorid, connection)+1;
			
			
			connection = createDBConnection();
			
			//now update table			
			String statemenet = dbconfig.getProperty("update_Color");
			PreparedStatement ps = connection.prepareStatement(statemenet);
	        ps.setInt(1, occurence);
	        ps.setInt(2, colorid);
	        //logger.debug("prepared statement" + ps);
	        
	        update = ps.executeUpdate();
	       logger.debug("color updates: " + update);
	       
	        ps.close();
	        connection.close(); 
	        
	    	
		}
		
		if(update==1) return true;
		
		return false;
		// System.out.println("colorid in method: " + colorid);
	}
	
	public Color[] getColorsOrdered(int imgId, int from, int to, Connection connection) throws SQLException{
		PreparedStatement ps = null;
	
		if(null == connection) 
			connection = createDBConnection();
		
		
		String statemenet = dbconfig.getProperty("getAllColorsOrdered");
		ps = connection.prepareStatement(statemenet);
		ps.setInt(1, imgId);
		ps.setInt(2, from);
		ps.setInt(3, to);
		
		Color[] colors = new Color[to-from];
		int colorInt;
		Color c;
        ResultSet rs = ps.executeQuery();
        for(int i=0;rs.next();++i) {
       	 String imgIdStr = rs.getString(1);
       	 if(null != imgIdStr) {
       		colorInt=(Integer.parseInt(imgIdStr));	
       		c = new Color(colorInt);
       		colors[i] = c; 
       	 }
        }
        ps.close();
        connection.close(); 
        
		return colors;		
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
		int imgId = 0;
		int colorValue=1;
		 System.out.println("res: checkImgId " + imgId + ": " + db.checkImgId(imgId, null));
		// System.out.println("res: checkImgIdAndColor_GetColorID: " + db.checkImgIdAndColor_GetColorID(imgId, colorValue, null));
		// db.createOrUpdateColor(imgId, colorValue, null);
		  db.deleteAllColorsOfImage(imgId, null);
		 System.out.println("res: getColorsOrdered():"+  db.getColorsOrdered(12, 55, 66, null));
	}

	

}
