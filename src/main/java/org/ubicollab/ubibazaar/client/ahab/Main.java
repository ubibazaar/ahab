package org.ubicollab.ubibazaar.client.ahab;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) {
    // prepare executor
    ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    // schedule task at fixed rate
    //FIXME    exec.scheduleAtFixedRate(new Ahab(), 0, 20, TimeUnit.SECONDS);
    exec.schedule(new Ahab(), 0, TimeUnit.SECONDS);
    
    //FIXME close exec?
  }

}
