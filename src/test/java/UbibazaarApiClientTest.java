import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.ubicollab.ubibazaar.core.Instance;

public class UbibazaarApiClientTest {

  @Test
  public void testInstances() {
    WebTarget target =
        ClientBuilder.newClient().target("http://10.0.0.114:8080")
            .path("ubibazaar-api/resources/instance");

    List<Instance> instances =
        target.request(MediaType.APPLICATION_JSON_TYPE).get(new GenericType<List<Instance>>() {});

    for (Instance instance : instances) {
      System.out.println(instance.getApp().getName() + " on " + instance.getDevice().getName()
          + instance.getApp().getExtra());
    }
  }
}
