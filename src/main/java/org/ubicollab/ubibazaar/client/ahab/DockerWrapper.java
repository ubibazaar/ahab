package org.ubicollab.ubibazaar.client.ahab;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.ubicollab.ubibazaar.core.Installation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;

@Slf4j
public class DockerWrapper {

  private static final String CONTAINER_PREFIX = "ubibazaar_inst_";
  private static final Integer KILL_TIMEOUT = 20;
  private static final String DOCKER_BINDING = "unix:///var/run/docker.sock";

  public void synchronize(Set<Installation> installations) {
    try {
      Set<String> shouldBeInstalled = installations.stream()
          .map(i -> i.getId())
          .collect(Collectors.toSet());

      Set<String> areInstalled = findInstalled();

      if (shouldBeInstalled.equals(areInstalled)) {
        log.info("All installations in sync {}", shouldBeInstalled);
      } else {
        log.info("Sync required. Should be installed {}. Are installed {}.",
            shouldBeInstalled, areInstalled);

        // uninstall whatever is running, but should not
        for (String instId : Sets.difference(areInstalled, shouldBeInstalled)) {
          try {
            uninstall(instId);
            log.info("Uninstalled {}", instId);
          } catch (DockerException | InterruptedException e) {
            log.error("Could not uninstall " + instId + ". Reason: " + e.getMessage(), e);
          }
        }

        // install whatever should be running, but is not
        for (String instId : Sets.difference(shouldBeInstalled, areInstalled)) {
          Installation installation = installations.stream()
              .filter(i -> i.getId().equals(instId))
              .findFirst().get();

          try {
            install(installation);
            log.info("Installed {}", instId);
          } catch (DockerException | InterruptedException e) {
            log.error("Could not install " + instId + ". Reason: " + e.getMessage(), e);
          }
        }
      }

    } catch (DockerException | InterruptedException e) {
      log.error(e.getMessage(), e);
    }
  }

  protected Set<String> findInstalled() throws DockerException, InterruptedException {
    try (DockerClient docker = new DefaultDockerClient(DOCKER_BINDING)) {
      return docker.listContainers(ListContainersParam.allContainers()).stream()
          .flatMap(container -> container.names().stream())
          .filter(x -> x.startsWith("/" + CONTAINER_PREFIX))
          .map(x -> x.replace("/" + CONTAINER_PREFIX, ""))
          .collect(Collectors.toSet());
    }
  }

  protected void install(Installation installation) throws DockerException, InterruptedException {
    try (DockerClient docker = new DefaultDockerClient(DOCKER_BINDING)) {
      // load app info
      Map<?, ?> props = new Gson().fromJson(installation.getApp().getProperties(), Map.class);
      String image = (String) props.get("docker_hub_repo");

      // pull the image
      docker.pull(image);

      // create container of the image
      ContainerConfig config = ContainerConfig.builder()
          .image(image)
          .build();

      // publishing all ports, i.e. assigning ports randomly necessary if we want to automate and
      // run multiple containers which might want to use the same ports
      HostConfig hostConfig = HostConfig.builder()
          .publishAllPorts(true)
          .build();

      // create a container, name it after the installation and use a common prefix, so that we know
      // which containers belong to ubibazaar
      ContainerCreation creation =
          docker.createContainer(config, CONTAINER_PREFIX + installation.getId());

      // inspect the container, if needed more properties
      // ContainerInfo info = docker.inspectContainer(creation.id());

      // start the container
      docker.startContainer(creation.id(), hostConfig);
      
      // set manager feedback
      StringBuffer sb = new StringBuffer();
      docker.listContainers(ListContainersParam.allContainers()).stream()
          .filter(c -> c.id().equals(creation.id())) // find container
          .flatMap(c -> c.ports().stream()) // find exposed ports
          .forEach(p -> sb.append(p.toString())); // store ports in properties
      
      Map<String, String> installationProps = Maps.newHashMap();
      installationProps.put("ports", sb.toString());
      installation.setProperties(new Gson().toJson(installationProps));
    }
  }

  protected void uninstall(String installationId) throws DockerException, InterruptedException {
    try (DockerClient docker = new DefaultDockerClient(DOCKER_BINDING)) {
      // find the container of this installation
      Container container = docker.listContainers(ListContainersParam.allContainers()).stream()
          .filter(c -> c.names().contains("/" + CONTAINER_PREFIX + installationId))
          .findFirst()
          .get();

      // stop it (or kill it)
      docker.stopContainer(container.id(), KILL_TIMEOUT);

      // remove it
      docker.removeContainer(container.id());
    }
  }

}
