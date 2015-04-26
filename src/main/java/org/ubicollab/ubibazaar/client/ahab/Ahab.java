package org.ubicollab.ubibazaar.client.ahab;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.ubicollab.ubibazaar.core.Device;
import org.ubicollab.ubibazaar.core.Installation;
import org.ubicollab.ubibazaar.core.Manager;

@Slf4j
public class Ahab implements Runnable {

  @Override
  public void run() {
    try {
      // set up api client
      ApiClient api = new ApiClient();
      api.setUp();
      
      // set up docker wrapper
      DockerWrapper docker = new DockerWrapper();

      // look up manager info
      Manager manager = api.getManagerInfo();

      for (Device device : manager.getDevices()) {
        try {
          // processing device
          log.info("Starting: device {} (id={})", device.getName(), device.getId());

          // look up installations
          List<Installation> installations = api.findInstallationsForDevice(device);

          // synch installations with docker
          // this starts whatever is not running 
          // and stops whatever should not be
          docker.synchronize(installations);
          
          log.info("Successfully synchronized device {} (id={})", device.getName(), device.getId());
        } catch (Throwable t) {
          log.error(t.getMessage(), t);
        }
      }

    } catch (Throwable t) {
      log.error(t.getMessage(), t);
    }
  }

}
