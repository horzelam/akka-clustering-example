package com.simple;

import com.simple.msg.SimpleMessage;

import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyAggActor extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private int counter = 0;

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof SimpleMessage || msg.toString().startsWith("someMsg")) {
            if (++counter < 3) {
                logger.info("---------------| aggregating message:" + msg + "," + self().path());
            } else {
                logger.info("---------------| dying for 3rd message," + self().path());
                this.self().tell(PoisonPill.getInstance(), self());
            }
        } else {
            logger.info("Unrecognized msg received : " + msg);
        }
    }
}