package video;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
 
/**
 * @author Crunchify.com
 * 
 */
 
// from: https://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/
public class VideoConfig extends Properties{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String result = "";
	InputStream inputStream;
	
	
	public VideoConfig() {

		try {
			getPropValues();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getPropValues() throws IOException {
 
		try {
			String propFileName = "config.properties";
 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				super.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		} finally {
			inputStream.close();
		}
		return result;
	
	}
	 

	public static void main(String[] args) {
		VideoConfig f = new VideoConfig();
		System.out.println(f.get("company3"));

	}

}
