package org.jboss.weld.tests.decorators.weld1110;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("facade/{message}")
public class RestFacade {

    @Inject
    private MessageSender messageSender;

    @GET
    public String send(@PathParam("message") String message) {
        return messageSender.send(message);
    }
}
