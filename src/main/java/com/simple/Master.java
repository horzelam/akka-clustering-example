package com.simple;

import org.apache.commons.lang3.RandomUtils;

import scala.Option;

import com.simple.msg.SimpleMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class Master extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

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
            String childName = "aggregator-" + simpleMsg.getValue();
            Option<ActorRef> possibleChild = this.context().child(childName);
            ActorRef child;
            if (possibleChild.isDefined()) {
                child = possibleChild.get();
            } else {
                child = this.context().actorOf(Props.create(MyAggActor.class), childName);
            }

            child.tell(simpleMsg, self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
