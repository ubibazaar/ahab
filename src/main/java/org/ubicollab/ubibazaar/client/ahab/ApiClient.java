package org.ubicollab.ubibazaar.client.ahab;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.ubicollab.ubibazaar.core.Device;
import org.ubicollab.ubibazaar.core.Installation;
import org.ubicollab.ubibazaar.core.Manager;

@Slf4j
public class ApiClient {

  private Client client;
  private String serverAddress;

  private final String managerId;
  private final String managerKey;

  public ApiClient(String managerId, String managerKey) {
    super();
    this.managerId = managerId;
    this.managerKey = managerKey;
  }

  protected Manager getManagerInfo() {
    WebTarget target = client
        .target(serverAddress)
        .path("resources/managers")
        .path(managerId);

    return target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(Manager.class);
  }

  protected void activateManager(Manager manager) {
    // set flag to true
    manager.setInstalled(true);

    // let api know
    client
        .target(serverAddress)
        .path("resources/managers")
        .path(manager.getId())
        .request()
        .put(Entity.entity(manager, MediaType.APPLICATION_JSON));
  }


  protected Set<Installation> findInstallationsForDevice(Device device) {
    WebTarget target = client
        .target(serverAddress)
        .path("resources/installations")
        .path("query")
        .queryParam("device", device.getId());

    return target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .get(new GenericType<Set<Installation>>() {});
  }

  protected void updateInstallation(Installation installation) {
    client
        .target(serverAddress)
        .path("resources/installations")
        .path(installation.getId())
        .request()
        .put(Entity.entity(installation, MediaType.APPLICATION_JSON));
  }

  protected void setUp() throws IOException {
    Properties prop = new Properties();
    InputStream in = Ahab.class.getResourceAsStream("/manager.properties");
    prop.load(in);
    in.close();

    // server address
    serverAddress = prop.getProperty("server_address");
    log.info("Using API on " + serverAddress);

    // set up authentication feaure
    HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basicBuilder()
        .nonPreemptive()
        .credentials(managerId, managerKey)
        .build();

    // create client with auth feature
    client = ClientBuilder.newClient().register(authFeature);
  }

}
