package com.simple;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.RoundRobinPool;
import com.simple.msg.SimpleMessage;

/**
 * This Master is cluster-singleton.
 * It uses router to distribute the work on child actors
 */
public class MasterVer2 extends UntypedActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    private ActorRef router;

    @Override
    public void preStart() {

        // 1 normal router
        // router = getContext().actorOf(FromConfig.getInstance().props(Props.create(ChildAggregatorActor.class)), "aggRouter");

        // 2 normal router - programmatically
        router = getContext().actorOf(new RoundRobinPool(3).props(Props.create(ChildAggregatorActor.class)),
                        "aggRouter");

        // 3 router with remote actors as routees :
//        Address[] remoteAdressesList = { new Address("akka.tcp", "remotesys", "otherhost", 1234),
//                        AddressFromURIString.parse("akka.tcp://othersys@anotherhost:1234") };
//        router = getContext().actorOf(new RemoteRouterConfig(new RoundRobinPool(5), remoteAdressesList).props(Props.create(. class)));

    }

    @Override
    public void onReceive(Object msg) throws Exception {

        // TODO
        // - create child Aggregator (but DISTRIBUTED on cluster)  - via router configured to do so
        if (msg instanceof SimpleMessage) {
            logger.info("Received msg: " + msg + " in  " + this.self().path().address() + " - " + this.hashCode() + " in " + Cluster.get(
                            this.context().system()).selfAddress());
            SimpleMessage simpleMsg = (SimpleMessage) msg;

            // TODO: replace child with router for children
            // SCALA: context.actorOf(FromConfig.props(), name = "consumerRouter")
            // aggRouter = this.getContext().actorOf(Props.create(), "aggRouter");
            router.tell(simpleMsg, self());

            // OLD implementation:
            // get or create relevant child
            // String childName = "aggregator-" + simpleMsg.getValue();
            //            Option<ActorRef> possibleChild = this.context().child(childName);
            //            ActorRef child;
            //            if (possibleChild.isDefined()) {
            //                child = possibleChild.get();
            //            } else {
            //                child = this.context().actorOf(Props.create(ChildAggregatorActor.class), childName);
            //                System.out.println("Created a child actor: " + child.path());
            //            }
            //
            //            child.tell(simpleMsg, self());

        } else {
            logger.info("Unrecognized msg received : " + msg);
        }

    }

}
