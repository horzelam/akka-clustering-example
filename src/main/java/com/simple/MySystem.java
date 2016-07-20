package com.simple;

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
import com.simple.msg.SimpleMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.RandomUtils;
import scala.concurrent.duration.FiniteDuration;

// HOW to start it - simulating multiple Cluster Nodes , while 2551 is seed node (for initial join into the cluster):
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2551"
// mvn exec:java -Dexec.mainClass="com.simple.MySystem" -Dconfig.resource=application.conf -Dexec.args="2552"
// ...
// TODO:
// config to use cluster aware routers/remote deployed routees
// currently it creates Cluster Singleton, but children are created only on the same node where singleton is created
// see:
// - cluster + cluster aware routers :
//      http://doc.akka.io/docs/akka/2.4.0/java/cluster-usage.html
//      http://doc.akka.io/docs/akka/2.1.2/cluster/cluster-usage-java.html#preparing-your-project-for-clustering
// - http://www.typesafe.com/activator/template/akka-distributed-workers-java
// - https://github.com/typesafehub/activator-akka-distributed-workers-java/blob/d0ff7f4ef4629724368a2e68aa9ef7b4e3447270/src/main/java/worker/Frontend.java
// - https://github.com/typesafehub/activator-akka-distributed-workers-java#master
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
        sysInstance.start(port);
        System.out.println("---------STOPPING THE SYSTEM in 10 sec...");
        Thread.sleep(20_000);
        System.out.println("---------STOPPING THE SYSTEM...");
        sysInstance.stop();
    }

    public void start(int port) {

        system = ActorSystem.create("example-system", config);
        Address realJoinAddress = Cluster.get(system).selfAddress();
        System.out.println("-----JOIN ADDRESS: " + realJoinAddress + "------");
        // Cluster.get(system).join(realJoinAddress);

        System.out.println("-------Starting system with config:");
        System.out.println("-------" + system.settings().config().getAnyRef("akka.remote.netty.tcp.port"));

        Cluster.get(system).registerOnMemberUp(() -> onClusterUp(port));

    }

    private void onClusterUp(int port) {
        System.out.println("Cluster is UP !");

        // create singleton Manager (not limited to any role - so all the nodes
        // can be used)
        Props managerProps = ClusterSingletonManager.props(Props.create(Master.class), PoisonPill.getInstance(),
                        ClusterSingletonManagerSettings.create(system));
        ActorRef manager = system.actorOf(managerProps, "master");
        System.out.println("-------Created singleton instance : " + manager.path() + ", " + manager.hashCode());

        // then  using proxy to access the singleton Master actor
        ActorRef proxy = system.actorOf(ClusterSingletonProxy.props("/user/master", ClusterSingletonProxySettings.create(system))
                        // ..withRole("backend")
                        , "proxy" + RandomUtils.nextInt(0, Integer.MAX_VALUE));

        system.scheduler().scheduleOnce(FiniteDuration.apply(1, "s"), () -> {
            sendMsg(0, proxy, port);
        }, system.dispatcher());

    }

    private void sendMsg(int msgNr, ActorRef proxy, int port) {
        System.out.println("------ Sending msg nr " + msgNr);
        proxy.tell(new SimpleMessage("someMsg_from_node_" + port, RandomUtils.nextInt(0, 3)), ActorRef.noSender());
        system.scheduler().scheduleOnce(FiniteDuration.apply(1, "s"), () -> {
            sendMsg(msgNr + 1, proxy, port);
        }, system.dispatcher());
    }

    private void stop() {
        system.shutdown();
    }
}
