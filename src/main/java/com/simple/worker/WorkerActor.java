package com.simple.worker;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.simple.common.SimpleMessage;

/**
 * Worker actor which aggregates incoming messages
 */
public class WorkerActor extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof SimpleMessage) {
            logger.info("<-- Aggregating message:" + msg + "," + self().path());
        } else {
            //logger.info("Dying for unknown message," + self().path());
            //this.self().tell(PoisonPill.getInstance(), self());
            logger.info("Unrecognized msg received : " + msg);
        }
    }
}
