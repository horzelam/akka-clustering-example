### TODO

* make the Master actor initialized only if we have at least 2 nodes - so maybe it shouldn't be started by external app
* Master actor should be cluster-singleton
* configure it to create child actors distributed in cluster

### TO CHECK

* example where single producer is started and single consumer per consumer-node are started, then router is sending data with load balancing to consumers

      * http://blog.kamkor.me/Akka-Cluster-Load-Balancing/
      
* https://blog.codecentric.de/en/2016/01/getting-started-akka-cluster/
