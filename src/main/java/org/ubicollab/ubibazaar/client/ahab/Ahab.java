package org.ubicollab.ubibazaar.client.ahab;

import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.ubicollab.ubibazaar.core.Device;
import org.ubicollab.ubibazaar.core.Installation;
import org.ubicollab.ubibazaar.core.Manager;

@Slf4j
public class Ahab implements Runnable {
  
  private final String managerId;
  private final String managerKey;
  
  public Ahab(String managerId, String managerKey) {
    super();
    this.managerId = managerId;
    this.managerKey = managerKey;
  }

  @Override
  public void run() {
    try {
      // set up api client
      ApiClient api = new ApiClient(managerId, managerKey);
      api.setUp();

      // set up docker wrapper
      DockerWrapper docker = new DockerWrapper();

      // look up manager info
      Manager manager = api.getManagerInfo();
      
      // if this is the first time after installation, activate
      if(!manager.getInstalled()) {
        api.activateManager(manager);
      }

      // manage devices
      for (Device device : manager.getDevices()) {
        try {
          // processing device
          log.info("Synchronizing installations on device {} (id={})", device.getName(),
              device.getId());

          // look up installations
          Set<Installation> installations = api.findInstallationsForDevice(device);

          // synch installations with docker
          // this starts whatever is not running
          // and stops whatever should not be
          docker.synchronize(installations);

          log.info("Successfully synchronized installations.");

          // update ports on API side
          installations.stream()
              .forEach(i -> api.updateInstallation(i));
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        }
      }

    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
  }

}
