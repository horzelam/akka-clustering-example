package com.simple.master;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.routing.ClusterRouterPool;
import akka.cluster.routing.ClusterRouterPoolSettings;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingPool;
import akka.routing.ConsistentHashingRouter;
import com.simple.worker.WorkerActor;
import com.simple.common.SimpleMessage;

/**
 * This Master is cluster-singleton.
 */
public class Master extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ActorRef router;

    final ConsistentHashingRouter.ConsistentHashMapper hashMapper = new
                    ConsistentHashingRouter.ConsistentHashMapper() {
        @Override
        public Object hashKey(Object message) {
            if (message instanceof SimpleMessage) {
                return ((SimpleMessage) message).getValue();
            } else {
                return null;
            }
        }
    };

    @Override
    public void preStart() {

        router = getContext().actorOf(new ClusterRouterPool( //
                        new ConsistentHashingPool(0) // with 0 initial number of routees in pool
                                        // with following way to route the messages - see hashMapper
                                        .withHashMapper(hashMapper), //
                        new ClusterRouterPoolSettings(4 //totalInstances
                                        , 2 //maxInstancesPerNode,
                                        , false //allowLocalRoutees
                                        , "worker" //useRole
                        )).props(Props.create(WorkerActor.class)), "workerRouter");

        //
        //        router = context().actorOf(Props.create(WorkerActor.class)
        //                                          .withRouter(new ClusterRouterConfig(new ConsistentHashingRouter(0),
        //                                                                          new ClusterRouterSettings(
        //                                                                                          10,//totalInstances
        //                                                                                          3, //maxInstancesPerNode
        //                                                                                          false, // allowLocalRoutees
        //                                                                                          "worker" //useRole = Some("worker")
        //                                                                          )
        //                                                          )
        //                                          ),
        //                        "childAggName");

        // 1 normal router
        // router = getContext().actorOf(FromConfig.getInstance().props(Props.create(WorkerActor.class)), "aggRouter");

        // 2 normal router - programmatically
        //        router = getContext().actorOf(new RoundRobinPool(3).props(Props.create(WorkerActor.class)),
        //                        "aggRouter");

        // 3 router with remote actors as routees :
        //        Address[] remoteAdressesList = { new Address("akka.tcp", "remotesys", "otherhost", 1234),
        //                        AddressFromURIString.parse("akka.tcp://othersys@anotherhost:1234") };
        //        router = getContext().actorOf(new RemoteRouterConfig(new RoundRobinPool(5), remoteAdressesList).props(Props.create(. class)));

    }

    @Override
    public void onReceive(Object msg) throws Exception {

        // This Master creates children directly - all are created on the same node
        if (msg instanceof SimpleMessage) {
            logger.info("Received msg: " + msg + " in  " + this.self()
                                                               .path()
                                                               .address() + " - " + this.hashCode() + " in " + Cluster.get(this.context()
                                                                                                                               .system())
                                                                                                                      .selfAddress());
            SimpleMessage simpleMsg = (SimpleMessage) msg;

            router.tell(simpleMsg, self());


        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
