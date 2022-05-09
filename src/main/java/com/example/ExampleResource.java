package com.example;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.quarkus.vertx.LocalEventBusCodec;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.MessageConsumer;

@Path("/test")
public class ExampleResource {

    @Inject
    EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.getDelegate().registerDefaultCodec(EventData.class, new LocalEventBusCodec<>());
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/waitForEvent")
    public Uni<EventData> waitForEvent(@QueryParam("eventId") String eventId) {
        return Uni.createFrom()
                .<EventData>emitter(uniEmitter -> setupEmitter(eventId, uniEmitter))
                .ifNoItem().after(Duration.ofSeconds(5))
                .failWith(new RuntimeException("timeout"));
    }

    @GET
    @Path("/send")
    public void test(@QueryParam("msg") String msg, @QueryParam("eventId") String eventId) {
        eventBus.publish("authorizationEvent", new EventData(eventId, msg));
    }

    private void setupEmitter(String eventId, UniEmitter<? super EventData> uniEmitter) {
        MessageConsumer<EventData> consumer = eventBus.consumer("authorizationEvent");
        consumer.handler(m -> {
            System.out.println("Received: " + m.body());

            if (m.body().getEventId().equals(eventId)) {
                uniEmitter.complete(m.body());
                consumer.unregisterAndForget();
            }

        });
    }
}
