package org.jboss.weld.tests.decorators.weld1110;

import javax.ejb.Stateful;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Stateful
@Path("message/{message}")
public class MessageSenderImpl implements MessageSender {

    @GET
	public String send(@PathParam("message") String message) {
		return message;
	}

}
