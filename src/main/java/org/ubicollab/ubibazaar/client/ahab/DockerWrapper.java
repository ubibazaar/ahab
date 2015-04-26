package org.ubicollab.ubibazaar.client.ahab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.ubicollab.ubibazaar.core.App;
import org.ubicollab.ubibazaar.core.Installation;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

@Slf4j
public class DockerWrapper {

  public void synchronize(List<Installation> installations) {
    // FIXME find running
    try {
      findRunning();
    } catch (DockerException | InterruptedException e) {
      log.error(e.getMessage(), e);
    }

    // FIXME stop newly removed
    // FIXME start newly added
    for (Installation installation : installations) {
      try {
        startApp(installation.getApp(), installation.getId().toString());
      } catch (DockerException | InterruptedException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public void startApp(App app, String uuid) throws DockerException, InterruptedException {
    // attach to docker
    try (DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock")) {
      // load app info
      String imageName = app.getProperties().get("docker_hub_repo"); // TODO extract property name,
      //FIXME String[] ports = app.getProperties().get("ports").split(":"); // TODO extract property name
      
      String[] ports = {"22"};

      // pull image
      docker.pull(imageName);

      // Create container with exposed ports
      ContainerConfig config = ContainerConfig.builder()
          .image(imageName)
          .exposedPorts(ports)
          .build();

      // Bind container ports to host ports
      // FIXME binding must be done some other way
      Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
      for (String port : ports) {
        List<PortBinding> hostPorts = new ArrayList<PortBinding>();
        hostPorts.add(PortBinding.of("0.0.0.0", port));
        portBindings.put(port, hostPorts);
      }
      
      HostConfig hostConfig = HostConfig.builder()
          .portBindings(portBindings)
          .build();

      String name = "ubibazaar_app_" + uuid;
      log.info("naming container {}", name);
      
      ContainerCreation creation = docker.createContainer(config, name);
      String id = creation.id();

      // Inspect container
      ContainerInfo info = docker.inspectContainer(id);

      // Start container
      docker.startContainer(id, hostConfig);

      // FIXME return any of this?
      log.info(info.toString());
      log.info(info.id());
      log.info(info.image());
      log.info(info.state().startedAt().toString());
      log.info(info.state().pid().toString());
      log.info(info.id().toString());
    }
  }

  public void findRunning() throws DockerException, InterruptedException {
    // attach to docker
    try (DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock")) {
      for (Container container : docker.listContainers(ListContainersParam.allContainers())) {
        log.info("container {} is {}", container.id(), container.image());
      }

    }
  }

}
