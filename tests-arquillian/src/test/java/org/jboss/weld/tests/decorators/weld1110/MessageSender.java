package org.jboss.weld.tests.decorators.weld1110;

import javax.ejb.Stateful;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Stateful
@Path("message")
public class MessageSender {

	@GET
	public Message send(Message message) {
		return message;
	}

}
