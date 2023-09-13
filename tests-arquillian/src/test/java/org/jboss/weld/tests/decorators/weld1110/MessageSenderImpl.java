package org.jboss.weld.tests.decorators.weld1110;

import jakarta.ejb.Stateless;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Stateless
@Path("message/{message}")
public class MessageSenderImpl implements MessageSender {

    @GET
    public String send(@PathParam("message") String message) {
        return message;
    }

}
