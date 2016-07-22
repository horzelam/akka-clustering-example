### How to start it simulating multiple Cluster Nodes , while 2551 is seed node (for initial join into the cluster):

 * 1 node: mvn exec:java -Dexec.mainClass="com.simple.App" -Dconfig.resource=application.conf -Dexec.args="2551"
 * 2 node: mvn exec:java -Dexec.mainClass="com.simple.App" -Dconfig.resource=application.conf -Dexec.args="2552"
 * ...

### Description
 * Master actor initialized only if we have at least 2 nodes.
 
### App versions:
 * cluster-singleton Master + application.conf  - creating Child actors (but only on the same node as Cluster singleton), 2 nodes generating input messages </LI>
 * cluster-singleton MasterVer2 + application.ver2.conf - router creating Child actors (but only on the same node as Cluster singleton), 2 nodes generating input messages </LI>
 
### NOTE
In akka-remoting remote you need to create remote actors - it's not done by the router automatically,
even if router is configured to use remote group of actors - see http://doc.akka.io/docs/akka/2.4.8/scala/remoting.html

### TODO
* children are created only on the same node where cluster singleton parent (Master) is created - configure it to create child actors distributed in cluster

### TO CHECK
* config to use cluster aware routers/remote deployed routees

* Possible approach with akka-clustering - create roles: common/backend/fronted
      * backend will have port 0
      * frontend will create cluster-router which creates workers with role backend
        
        see: 
        http://stackoverflow.com/questions/18798531/how-to-create-remote-actors-dynamically-and-control-them-by-using-akka

* example where single producer is started and single consumer per consumer-node are started, 
  then router is sending data with load balancing to consumers

      * http://blog.kamkor.me/Akka-Cluster-Load-Balancing/
      
* https://blog.codecentric.de/en/2016/01/getting-started-akka-cluster/

* local sending msg to remote actor - http://alvinalexander.com/scala/simple-akka-actors-remote-example



 
 * see - cluster + cluster aware routers :
    * http://doc.akka.io/docs/akka/2.4.0/java/cluster-usage.html
    * http://doc.akka.io/docs/akka/2.1.2/cluster/cluster-usage-java.html#preparing-your-project-for-clustering
        * http://www.typesafe.com/activator/template/akka-distributed-workers-java
        * https://github.com/typesafehub/activator-akka-distributed-workers-java/blob/d0ff7f4ef4629724368a2e68aa9ef7b4e3447270/src/main/java/worker/Frontend.java
        * https://github.com/typesafehub/activator-akka-distributed-workers-java#master
        * http://www.typesafe.com/activator/template/akka-distributed-workers?_ga=1.99394842.506721680.1434724237#code/src/main/scala/worker/Main.scala
        * http://www.typesafe.com/activator/template/akka-distributed-workers-java#code/src/main/java/worker/Main.java
