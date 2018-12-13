package database;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private static final Logger logger = LogManager.getLogger(DBConnect.class.getName());
	
	VideoConfig config;
	VideoConfig dbconfig;
	
	public DBConnect() {
		config = new VideoConfig("config.properties");	
		dbconfig = new VideoConfig("database.properties");	
	}
	
	/*
	public void createColorTable() throws SQLException {
		Connection con=createDBConnection();
		Statement setupStatement = con.createStatement();
	    String createTable = dbconfig.getProperty("create_colortable");
	 //   String insertRow1 = "INSERT INTO colortable (imgid, colorvalue, occurence) VALUES (12, 12345453, 1);";

	    setupStatement.addBatch(createTable);
	 //   setupStatement.addBatch(insertRow1);
	    setupStatement.executeBatch();
	    setupStatement.close();
	    con.close(); 
	}
	*/
	
	/**
	 * Created from: https://www.mkyong.com/jdbc/how-to-connect-to-mysql-with-jdbc-driver-java/
	 * @return
	 * @throws SQLException 
	 */
	private Connection createDBConnection() throws SQLException {
		String PUBLIC_DNS = config.getProperty("rds_instance_hostname"); 
		String REMOTE_DATABASE_USERNAME = config.getProperty("db_user"); 
		String DATABASE_USER_PASSWORD = config.getProperty("db_user_password"); 
		String DBNAME = config.getProperty("db_name");
		
		  try {
		        Class.forName("com.mysql.cj.jdbc.Driver");
		    } catch (ClassNotFoundException e) {
		        logger.error("No jdbc driver.");
		        e.printStackTrace();
		    }

		    //logger.debug("MySQL JDBC Driver Registered!");
		    Connection connection = null;
		    String conStr = "jdbc:mysql://" + PUBLIC_DNS + ":3306/" +DBNAME;
		    logger.debug("Database Connection String: " + conStr);
		   
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
	
	public boolean checkImgId(int imgId) throws SQLException{
		Connection connection = createDBConnection();
			
		String statemenet = dbconfig.getProperty("check_ImgId");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, imgId);
        logger.debug(ps);
        
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
         
         logger.trace("img id is: " + imgId1 + "==" +imgId);
         
         if(imgId1 != -1)
			 return true;
		
		return false;
	}

	
	private int checkImgIdAndColor_GetColorID(int imgId, int colorValue, Connection connection) throws SQLException{
		if(null == connection) 
			connection = createDBConnection();
	//	String hex = Integer.toHexString(colorValue & 0xffffff);
		String statemenet = dbconfig.getProperty("check_ImgIdAndColorValue");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, imgId);
        ps.setInt(2, colorValue);
        logger.debug(ps);
        
        int colorid=-1;
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
        	 String coloridStr = rs.getString(1);
        	 if(null != coloridStr) {
        		 colorid = Integer.parseInt(coloridStr);	 
        	 }
         }
         ps.close();
     //    connection.close(); 
         
        // logger.traceExit("imgid: " + imgId + ", Returning colorid: " + colorid);
	return colorid;

	}
	
	
	private int getOccurrence(int colorId, Connection connection) throws SQLException {
		if(null == connection) 
			connection = createDBConnection();
		
		String statemenet = dbconfig.getProperty("get_Occurence");
		
		PreparedStatement ps = connection.prepareStatement(statemenet);
        ps.setInt(1, colorId);
        logger.debug(ps);
        
        int occurrence=0;
         ResultSet rs = ps.executeQuery();
         if (rs.next()) {
        	 String occurrenceStr = rs.getString(1);
        	 if(null != occurrenceStr) {
        		 occurrence = Integer.parseInt(occurrenceStr);	 
        	 }
         }
         ps.close();
      //   connection.close(); 
         
         return occurrence;
	}
	
	/**
	 * Normally a batch number is 1000
	 * @throws SQLException 
	 */
	private boolean createOrUpdateColors_Batchnumber(int imgId, List<Integer> list, Connection connection) throws SQLException {
		if(null == list || list.size() < 1) return false;
		if(null == connection) 
			connection = createDBConnection();		
		
		int colorid;
		String statemenet = dbconfig.getProperty("insert_New_Color");
		PreparedStatement ps = connection.prepareStatement(statemenet);
		
		String statemenet2 = dbconfig.getProperty("update_Color");
		PreparedStatement ps2 = connection.prepareStatement(statemenet2);
		
		int cvalue;
		int occurence=0;
		for(int i=0, size=list.size(); i<size; ++i) {
			cvalue = list.get(i);
			colorid = checkImgIdAndColor_GetColorID(imgId, cvalue, connection);
			logger.trace("Adding color to batch: " + cvalue);
			if(colorid == -1) {
				//color is not there yet
				ps.setInt(1, imgId);
		        ps.setInt(2, cvalue);
		        ps.setInt(3, 1);
		        ps.addBatch();
			}else {
				 occurence = getOccurrence(colorid, connection)+1;
				 ps2.setInt(1, occurence);
			     ps2.setInt(2, colorid);
			     ps2.addBatch();
			}
			
		}
		//int[] batch = 
		ps.executeBatch();
		//System.out.println("batch - " + Arrays.asList(batch));
		//int[] batch2 = 
		ps2.executeBatch();
		//System.out.println("batch2 - " + Arrays.asList(batch2));

	 	ps.close();
	 	ps2.close();
	 	
	 	return true;
	}
	
	public boolean createOrUpdateColors(int imgId, ArrayList<Integer> colorValues) throws SQLException {
		if(colorValues.size() < 1 ) return false;
		Connection connection = createDBConnection();
		int count = 500;
		int i=0;
		int size=colorValues.size();
		for(int j=0+count; j<size; i=+count, j+=count)	
			createOrUpdateColors_Batchnumber(imgId, colorValues.subList(i, j), connection);		
		if(i < size) 
			createOrUpdateColors_Batchnumber(imgId, colorValues.subList(i, size-1), connection);
			
        connection.close(); 
        
        logger.debug(imgId + ", batches sent.");
        
		return true;
	}
	
	public ArrayList<Color> getColorsOrdered(int imgId, int from, int to) throws SQLException{
		PreparedStatement ps = null;
		Connection connection = createDBConnection();
		
		String statemenet = dbconfig.getProperty("getAllColorsOrdered");
		ps = connection.prepareStatement(statemenet);
		ps.setInt(1, imgId);
		ps.setInt(2, from);
		ps.setInt(3, to);
		logger.debug(ps);
		
		int difference = to-from;
		if(difference<=0) return null; 
		
		ArrayList<Color> colors = new ArrayList<Color>(to-from+1);
		//Color[] colors = new Color[to-from+1];
		Color c;
		boolean hasColorValue=false;
        ResultSet rs = ps.executeQuery();
        int occurence = 1;
        while(rs.next()) {
        	String occurenceStr =rs.getString(1);
       	 	String imgIdStr = rs.getString(2);
       	 if(null != imgIdStr) {
       		//System.out.println("decoding: " + imgIdStr);
       		c = Color.decode(imgIdStr);
       		if(null != c) {
       			if(null != occurenceStr)
       				occurence = Integer.parseInt(occurenceStr);
       		    			
       			for(int i=0; i< occurence; ++i)
       				colors.add(c);
       			hasColorValue=true;
       		}
       	 }
        }
        ps.close();
        connection.close(); 
        
        if(!hasColorValue) return null;
        
        logger.debug("Returning Colors: " + colors);
        
		return colors;		
	}
	
	public int countColors(int imgId) throws SQLException {
		
		Connection connection = createDBConnection();
		PreparedStatement ps = null;
		
		String statemenet = dbconfig.getProperty("count_Colors");
		ps = connection.prepareStatement(statemenet);
		ps.setInt(1, imgId);
		logger.debug(ps);
		
        ResultSet rs = ps.executeQuery();
        int result=0;
        if(rs.next()) {
       	 String imgIdStr = rs.getString(1);
       	 	if(null != imgIdStr) {
       	 	result=(Integer.parseInt(imgIdStr));	
       	
       	 	}
        }
        ps.close();
        connection.close(); 
        
        return result;
	}
	
	/**
	 * 
	 * @param imgId
	 * @param colorValue
	 * @return
	 * @throws SQLException
	 */
	public boolean deleteAllColorsOfImage(int imgId) throws SQLException {
		PreparedStatement ps = null;
		int deleted = 0;
		Connection connection = createDBConnection();
		String statemenet = dbconfig.getProperty("delete_Color");
		ps = connection.prepareStatement(statemenet);
		ps.setInt(1, imgId);
		logger.debug(ps);


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
		int imgId = 6586137;
		int colorValue=1;
		 System.out.println("res: checkImgId " + imgId + ": " + db.checkImgId(imgId));
		// System.out.println("res: checkImgIdAndColor_GetColorID: " + db.checkImgIdAndColor_GetColorID(imgId, colorValue, null));
		// db.createOrUpdateColor(imgId, colorValue, null);
	
		 System.out.println("res: countColors(): " +db.countColors(imgId));
	//	 System.out.println("res: getColorsOrdered():"+  db.getColorsOrdered(imgId, 1, 10));
	//	 System.out.println("res: deleteAllColorsOfImage(): "+ db.deleteAllColorsOfImage(imgId, null));
	}

	

}
