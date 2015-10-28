package com.simple;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyManagerActor extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg.equals("someMsg")) {
            logger.info("---------------| received msg: " + msg + " in  " + this.self().path().address() + " - "
                    + this.hashCode() + " in " + Cluster.get(this.context().system()).selfAddress());

            ActorRef child = this.context().actorOf(Props.create(MyRepository.class), "repository");

            child.tell("writeJob", self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
