package org.ubicollab.ubibazaar.client.ahab;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import org.ubicollab.ubibazaar.core.Device;
import org.ubicollab.ubibazaar.core.Installation;
import org.ubicollab.ubibazaar.core.Manager;

@Slf4j
public class ApiClient {

  private String serverAddress;
  private String managerUUID;

  protected Manager getManagerInfo() {
    WebTarget target = ClientBuilder.newClient()
        .target(serverAddress)
        .path("/resources/manager/")
        .path(managerUUID);

    return target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(Manager.class);
  }


  protected Set<Installation> findInstallationsForDevice(Device device) {
    WebTarget target = ClientBuilder.newClient()
        .target(serverAddress)
        .path("/resources/installation")
        .path("query")
        .queryParam("device", device.getId());

    return target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Set<Installation>>() {});
  }


  protected void setUp() throws IOException {
    Properties prop = new Properties();
    InputStream in = Ahab.class.getResourceAsStream("/api.properties");
    prop.load(in);
    in.close();

    // server address
    serverAddress = prop.getProperty("server_address");
    log.info("Using API on " + serverAddress);

    managerUUID = prop.getProperty("manager_uuid");
    log.info("This is manager " + managerUUID);
  }

}
