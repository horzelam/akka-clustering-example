package com.simple;

import scala.Option;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope;
import akka.routing.FromConfig;

import com.simple.msg.SimpleMessage;

public class MasterActor extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    // This router is used both with lookup and deploy of routees. If you
    // have a router with only lookup of routees you can use Props.empty()
    // instead of Props.create(StatsWorker.class).
    ActorRef aggRouter = getContext().actorOf(FromConfig.getInstance().props(Props.create(MyAggActor.class)),
            "aggRouter");

    @Override
    public void onReceive(Object msg) throws Exception {

        // TODO - on start: create one repo
        // TODO - on message :
        // - create child Aggregator (but DISTRIBUTED on cluster) !!!
        // - forward to Aggregator which then writes it to this single repo
        if (msg instanceof SimpleMessage) {
            logger.info("---------------| received msg: " + msg + " in  " + this.self().path().address() + " - "
                    + this.hashCode() + " in " + Cluster.get(this.context().system()).selfAddress());
            SimpleMessage simpleMsg = (SimpleMessage) msg;

            
            // get or create relevant child
            //            String childName = "aggregator-" + simpleMsg.getValue();
            //            Option<ActorRef> possibleChild = this.context().child(childName);
            //            ActorRef child;
            //            if (possibleChild.isDefined()) {
            //                child = possibleChild.get();
            //            } else {
            //                child = this.context().actorOf(Props.create(MyAggActor.class), childName);
            //            }
            //
            //            child.tell(simpleMsg, self());
            
            
            // based on:
            // http://www.typesafe.com/activator/template/akka-sample-cluster-java?_ga=1.53593516.506721680.1434724237#code/src/main/java/sample/cluster/stats/StatsService.java
            aggRouter.tell(new ConsistentHashableEnvelope(simpleMsg.getText(), simpleMsg.getValue()), self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
