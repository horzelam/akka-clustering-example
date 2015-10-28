package com.simple;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class MyRepository extends UntypedActor {
    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg.equals("writeJob")) {
            logger.info("---------------| received msg: " + msg);

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}