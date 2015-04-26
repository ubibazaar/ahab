import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubicollab.ubibazaar.core.Platform;

import com.google.gson.Gson;

public class UbibazaarApiClientTest {
  
  private static String serverAddress;
  
  @BeforeClass
  public static void setUp() throws IOException {
    Properties prop = new Properties();
    InputStream in = UbibazaarApiClientTest.class.getResourceAsStream("api.properties");
    prop.load(in);
    in.close();
    
    serverAddress = prop.getProperty("server_address");
  }

  Logger logger = LoggerFactory.getLogger(getClass());
  
  @Test
  public void testPing() {
    WebTarget target = ClientBuilder.newClient()
        .target(serverAddress)
        .path("/resources/ping");

    Map<String,Long> response = target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Map<String,Long>>() {});
    
    Assert.assertNotNull(response);
    
    logger.info("Ping response: " + new Gson().toJson(response));
  }
  
  @Test
  public void testPlatforms() {
    WebTarget target = ClientBuilder.newClient()
        .target(serverAddress)
        .path("/resources/platform");

    List<Platform> platforms = target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<List<Platform>>() {});
    
    Assert.assertTrue(!platforms.isEmpty());
    
    logger.info("Supported platforms: " + new Gson().toJson(platforms));
  }
}
