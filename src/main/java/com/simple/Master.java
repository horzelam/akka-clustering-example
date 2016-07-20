package com.simple;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.simple.msg.SimpleMessage;
import scala.Option;

public class Master extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {

        // TODO
        // - create child Aggregator (but DISTRIBUTED on cluster)  - via router configured to do so
        if (msg instanceof SimpleMessage) {
            logger.info("Received msg: " + msg + " in  " + this.self().path().address() + " - " + this.hashCode() + " in "
                            + Cluster.get(this.context().system()).selfAddress());
            SimpleMessage simpleMsg = (SimpleMessage) msg;

            // get or create relevant child
            String childName = "aggregator-" + simpleMsg.getValue();
            Option<ActorRef> possibleChild = this.context().child(childName);
            ActorRef child;
            if (possibleChild.isDefined()) {
                child = possibleChild.get();
            } else {
                child = this.context().actorOf(Props.create(MyAggActor.class), childName);
                System.out.println("Created a child actor: " + child.path());
            }

            child.tell(simpleMsg, self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
