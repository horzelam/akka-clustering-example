package com.simple;

import org.apache.commons.lang3.RandomUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.contrib.pattern.ClusterSingletonManager;
import akka.contrib.pattern.ClusterSingletonProxy;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

// HOW to start it - simulating multiple Cluster Nodes , while 2551 is seed node (for initial join into the cluster):
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2551"
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2552"
// ...
public class MySystem {

    private ActorSystem system;
    private Config config;

    public MySystem(Config config) {
        this.config = config;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("starting...");

        // Override the configuration of the port when specified as program
        // argument
        int port = (args.length == 0) ? 0 : Integer.parseInt(args[0]);
        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)
                //.withFallback(ConfigFactory.parseString("akka.cluster.roles = [managerRole]"))
                .withFallback(ConfigFactory.load());

        MySystem sysInstance = new MySystem(config);
        ActorRef manager = sysInstance.start();

        for (int i = 0; i < 10; i++) {
            manager.tell("someMsg", ActorRef.noSender());
            Thread.sleep(1000);
        }

        Thread.sleep(10000);
        System.out.println("---------STOPPING THE SYSTEM...");
        sysInstance.stop();
    }

    public ActorRef start() {

        system = ActorSystem.create("gjallarhorn-webhook-notifier-system", config);
        Address realJoinAddress = Cluster.get(system).selfAddress();
        System.out.println("-----JOIN ADDRESS: " + realJoinAddress + "------");
        // Cluster.get(system).join(realJoinAddress);

        System.out.println("-------Starting system with config:");
        System.out.println("-------" + system.settings().config().getAnyRef("akka.remote.netty.tcp.port"));

        // create singleton Manager (not limited to any role - so all the nodes
        // can be used)
        Props managerProps = ClusterSingletonManager.defaultProps(Props.create(MyManagerActor.class),
                "singleton-my-manager", PoisonPill.getInstance(), null);
        ActorRef manager = system.actorOf(managerProps, "my-manager");
        System.out.println("-------Created singleton instance : " + manager.path() + ", " + manager.hashCode());

        ActorRef proxy = system.actorOf(ClusterSingletonProxy.defaultProps("/user/my-manager", null), "proxy"+RandomUtils.nextInt(0, Integer.MAX_VALUE));

        return proxy;
        // manager = system.actorOf(Props.create(MyManagerActor.class),
        // "manager");

    }

    private void stop() {
        system.shutdown();
    }
}
