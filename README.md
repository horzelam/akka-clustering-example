### Description
 This is example how to start akka cluster with several nodes setup.
   
 Master node has just single Master actor - its defined as cluster singleton, so even if we start more master nodes 
 there should always be single Master.
   
 Backend nodes are prepared to host a ChildAggregator actors.
 These actors are actually routees (children) of router created in Master actor.
 
 Master actor initialized only on cluster Up event - only cluster achieved minimum setup.
 
 If we start additional worker node - it should also produce the messages
 

### How to start it simulating multiple Cluster Nodes , while 2551 is seed node (for initial join into the cluster):
To form a minimal cluster of 2 worker nodes + 1 master node run following scripts:
 * runWorker.sh
 * runWorker.sh
 * runMaster_1.sh
 
Optionally you can run: 
 * runMaster_2.sh
Which just adds another Master node producing messages, but Master actor should be single in the whole cluster.

### How the messages are distributed in a cluster of 2 Master + 2 Worker nodes:
 All messages from both master nodes (2551, 2552) are first propagated 
 to singleton Master actor and then to router.
  
 * message from master node 2551, value=0 --> worker c4 on worker node 1
 * message from master node 2552, value=0 --> worker c4 on worker node 1
 * message from master node 2551, value=2 --> worker c2 on worker node 1
 * message from master node 2552, value=2 --> worker c2 on worker node 1
 
 * message from master node 2552, value=1 --> worker c1 on worker node 2
 * message from master node 2551, value=2 --> worker c3 on worker node 2
 * message from master node 2551, value=1 --> worker c1 on worker node 2
 * message from master node 2552, value=2 --> worker c3 on worker node 2
 
  
   
### TODO:
 * example of nice finish aggregation / stop the workers.
 * example of Master node failure - Master actor should be started on 2nd awailable node.
 
### NOTE
See: http://doc.akka.io/docs/akka/2.4.8/scala/remoting.html
In akka-remoting (without using clustering, ClusterRouterPool) you need to create remote actors on remote nodes to be able to use it in router group.
It's not done by the router automatically.
Even if router is configured to use remote group of actors - these actors must be created on remote node. 


Without ClusterRouterPool and defined roles to be used when creating routees - children are created only on the same node 
where Master is created.

### TO CHECK

* Possible approach with akka-clustering - create roles: common/backend/fronted
      * backend will have port 0
      * frontend will create cluster-router which creates workers with role backend        
        see: 
        http://stackoverflow.com/questions/18798531/how-to-create-remote-actors-dynamically-and-control-them-by-using-akka
* example where single producer is started and single consumer per consumer-node are started, 
  then router is sending data with load balancing to consumers
      * http://blog.kamkor.me/Akka-Cluster-Load-Balancing/
* config to use cluster aware routers/remote deployed routees    
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
