package com.simple;

import org.apache.commons.lang3.RandomUtils;

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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

// HOW to start it - simulating multiple Cluster Nodes , while 2551 is seed node (for initial join into the cluster):
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2551"
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2552"
// ...
// see:
//
// - http://www.typesafe.com/activator/template/akka-distributed-workers-java
// - https://github.com/typesafehub/activator-akka-distributed-workers-java/blob/d0ff7f4ef4629724368a2e68aa9ef7b4e3447270/src/main/java/worker/Frontend.java
// -https://github.com/typesafehub/activator-akka-distributed-workers-java#master
// - http://www.typesafe.com/activator/template/akka-distributed-workers?_ga=1.99394842.506721680.1434724237#code/src/main/scala/worker/Main.scala
// - http://www.typesafe.com/activator/template/akka-distributed-workers-java#code/src/main/java/worker/Main.java
public class MySystem {

    private static ActorSystem system;
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
        // .withFallback(ConfigFactory.parseString("akka.cluster.roles = [managerRole]"))
                .withFallback(ConfigFactory.load());

        MySystem sysInstance = new MySystem(config);
        ActorRef proxy = sysInstance.start();

        Thread.sleep(2000);
        System.out.println("---------Sending msgs to Manager Proxy...");
        for (int i = 0; i < 10; i++) {
            proxy.tell("someMsg-"+port, ActorRef.noSender());
            // singletonSelection.tell("someMsg", ActorRef.noSender());
            // selection.tell("someMsg", ActorRef.noSender());
            Thread.sleep(1000);
        }

        Thread.sleep(20000);
        System.out.println("---------STOPPING THE SYSTEM...");
        sysInstance.stop();
    }

    public ActorRef start() {

        system = ActorSystem.create("example-system", config);
        Address realJoinAddress = Cluster.get(system).selfAddress();
        System.out.println("-----JOIN ADDRESS: " + realJoinAddress + "------");
        // Cluster.get(system).join(realJoinAddress);

        System.out.println("-------Starting system with config:");
        System.out.println("-------" + system.settings().config().getAnyRef("akka.remote.netty.tcp.port"));

        // create singleton Manager (not limited to any role - so all the nodes
        // can be used)
        Props managerProps = ClusterSingletonManager.props(Props.create(Master.class), PoisonPill.getInstance(),
                ClusterSingletonManagerSettings.create(system));
        ActorRef manager = system.actorOf(managerProps, "master");
        System.out.println("-------Created singleton instance : " + manager.path() + ", " + manager.hashCode());

        ActorRef proxy = system.actorOf(
                ClusterSingletonProxy.props("/user/master", ClusterSingletonProxySettings.create(system))
                // ..withRole("backend")
                , "proxy" + RandomUtils.nextInt(0, Integer.MAX_VALUE));

        return proxy;

    }

    private void stop() {
        system.shutdown();
    }
}
