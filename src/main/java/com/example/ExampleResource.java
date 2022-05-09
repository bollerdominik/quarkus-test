package com.example;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.Duration;

@Path("/test")
public class ExampleResource {

    @Inject
    EventBus eventBus;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/waitForEvent")
    public Uni<String> waitForEvent() {


        return Uni.createFrom()
                .<String>emitter(emitter -> {
                    //todo what???
                    // Wait for event from the GET /send endpoint
                    // return message
                })
                .ifNoItem().after(Duration.ofSeconds(5)).failWith(new RuntimeException("timeout"));
    }

    @GET
    @Path("/send")
    public void test(@QueryParam("msg") String msg) {
        eventBus.send("test", msg);
    }

    @ConsumeEvent("test")
    public void consumeEvent(String msg) {
        System.out.println("received: " + msg);
    }
}