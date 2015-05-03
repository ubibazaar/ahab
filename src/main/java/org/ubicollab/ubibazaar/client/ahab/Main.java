package org.ubicollab.ubibazaar.client.ahab;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) throws IOException {
    if(args.length < 1) {
      throw new RuntimeException("Properties file name must be specified as an afrgument.");
    }
    
    Properties prop = new Properties();
    InputStream in = new FileInputStream(args[0]);
    prop.load(in);
    in.close();
    
    String id = prop.getProperty("manager_id");
    String key = prop.getProperty("manager_key");
    
    // prepare executor
    ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    // schedule task at fixed rate
    exec.scheduleAtFixedRate(new Ahab(id, key), 0, 20, TimeUnit.SECONDS);
  }

}
