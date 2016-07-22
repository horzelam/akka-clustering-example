package com.simple.worker;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Worker App starter.
 */
public class WorkerApp {

    private static ActorSystem system;

    private LoggingAdapter logger;

    private Config config;

    public WorkerApp(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[MAIN] Starting the app...");

        // load config defined in variable "config.resource"
        Config config = ConfigFactory.load()
                                     // common config:
                                     .withFallback(ConfigFactory.load("common/reference.conf"));

        WorkerApp sysInstance = new WorkerApp(config);
        sysInstance.start();
        System.out.println("[MAIN] STOPPING THE SYSTEM in 20 sec...");
        Thread.sleep(20_000);
        System.out.println("[MAIN] STOPPING THE SYSTEM...");
        sysInstance.stop();
    }

    public void start() {
        system = ActorSystem.create("example-system", config);
        this.logger = Logging.getLogger(system, this);
        Address realJoinAddress = Cluster.get(system)
                                         .selfAddress();
        logger.info("[MAIN] JOIN ADDRESS: " + realJoinAddress + "------");
        // Cluster.get(system).join(realJoinAddress);

        logger.info("[MAIN] Starting system with config:");
        logger.info("[MAIN] " + system.settings()
                                      .config()
                                      .getAnyRef("akka.remote.netty.tcp.port"));

        Cluster.get(system)
               .registerOnMemberUp(() -> onClusterUp());

    }

    private void onClusterUp() {
        logger.info("[MAIN] Cluster is UP !");

    }

    private void stop() {
        system.shutdown();
    }
}
