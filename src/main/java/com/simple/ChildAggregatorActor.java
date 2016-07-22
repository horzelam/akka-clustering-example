package com.simple;

import com.simple.msg.SimpleMessage;

import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ChildAggregatorActor extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private int counter = 0;

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof SimpleMessage) {
            if (++counter < 3) {
                logger.info("Aggregating message:" + msg + "," + self().path());
            } else {
                logger.info("Dying for 3rd message," + self().path());
                this.self().tell(PoisonPill.getInstance(), self());
            }
        } else {
            logger.info("Unrecognized msg received : " + msg);
        }
    }
}
