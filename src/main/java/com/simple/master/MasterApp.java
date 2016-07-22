package com.simple.master;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.simple.common.SimpleMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.RandomUtils;
import scala.concurrent.duration.FiniteDuration;

/**
 * Frontend App starter.
 */
public class MasterApp {

    private static ActorSystem system;

    private LoggingAdapter logger;

    private Config config;

    public MasterApp(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("[MAIN] Starting the app...");

        // Override the configuration of the port when specified as program
        // argument
        int port = (args.length == 0) ? 0 : Integer.parseInt(args[0]);
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                                     // .withFallback(ConfigFactory.parseString("akka.cluster.roles = [managerRole]"))
                                     .withFallback(ConfigFactory.load())
                                     // common config:
                                     //.withFallback(

                                     //                ConfigFactory.load("common/reference.conf")

        //                             )
        ;

        MasterApp sysInstance = new MasterApp(config);
        sysInstance.start(port);
        System.out.println("[MAIN] STOPPING THE SYSTEM in 20 sec...");
        Thread.sleep(20_000);
        System.out.println("[MAIN] STOPPING THE SYSTEM...");
        sysInstance.stop();
    }

    public void start(int port) {
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
               .registerOnMemberUp(() -> onClusterUp(port));

    }

    private void onClusterUp(int port) {
        logger.info("[MAIN] Cluster is UP !");

        // create singleton Manager (not limited to any role - so all the nodes
        // can be used)
        final ActorRef proxy = createMasterAsSingleton();

        // generate Input messages to Master
        system.scheduler()
              .scheduleOnce(FiniteDuration.apply(1, "s"), () -> {
                  sendMsg(0, proxy, port);
              }, system.dispatcher());

    }

    private ActorRef createMasterAsSingleton() {
        Props managerProps = ClusterSingletonManager.props(Props.create(Master.class), PoisonPill.getInstance(),
                        ClusterSingletonManagerSettings.create(system)
                                                       // role:
                                                       .withRole("master")

        );
        ActorRef manager = system.actorOf(managerProps, "master");
        logger.info("[MAIN] Created singleton instance : " + manager.path() + ", " + manager.hashCode());

        // then  using proxy to access the singleton Master actor
        return system.actorOf(ClusterSingletonProxy.props("/user/master", ClusterSingletonProxySettings.create(system)
                                                                                                       .withRole("master"))
                        // ..withRole("worker")
                        , "proxy" + RandomUtils.nextInt(0, Integer.MAX_VALUE));
    }

    private void sendMsg(int msgNr, ActorRef proxy, int port) {
        logger.info("[MAIN] Sending msg nr " + msgNr);
        proxy.tell(new SimpleMessage("message from master node " + port, RandomUtils.nextInt(0, 3)), ActorRef.noSender());
        system.scheduler()
              .scheduleOnce(FiniteDuration.apply(1, "s"), () -> {
                  sendMsg(msgNr + 1, proxy, port);
              }, system.dispatcher());
    }

    private void stop() {
        system.shutdown();
    }
}
