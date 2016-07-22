package com.simple;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.simple.msg.SimpleMessage;
import scala.Option;

/**
 * This Master is cluster-singleton.
 */
public class Master extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {

        // This Master creates children directly - all are created on the same node
        if (msg instanceof SimpleMessage) {
            logger.info("Received msg: " + msg + " in  " + this.self().path().address() + " - " + this.hashCode() + " in "
                            + Cluster.get(this.context().system()).selfAddress());
            SimpleMessage simpleMsg = (SimpleMessage) msg;

            // get or create relevant child
            String childName = "aggregator-" + simpleMsg.getValue();



            // TODO in different version: replace child with router for children
            // SCALA: context.actorOf(FromConfig.props(), name = "consumerRouter")
            // aggRouter = this.getContext().actorOf(Props.create(), "aggRouter");

            Option<ActorRef> possibleChild = this.context().child(childName);
            ActorRef child;
            if (possibleChild.isDefined()) {
                child = possibleChild.get();
            } else {
                child = this.context().actorOf(Props.create(ChildAggregatorActor.class), childName);
                System.out.println("Created a child actor: " + child.path());
            }

            child.tell(simpleMsg, self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
