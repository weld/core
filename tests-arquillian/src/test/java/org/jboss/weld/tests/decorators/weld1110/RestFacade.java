package org.jboss.weld.tests.decorators.weld1110;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("facade/{message}")
public class RestFacade {

    @Inject
    private MessageSender messageSender;

    @GET
    public String send(@PathParam("message") String message) {
        return messageSender.send(message);
    }
}
