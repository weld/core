package org.jboss.weld.tests.decorators.weld1110;

import javax.ejb.Local;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public interface MessageSender {

    String send(String message);

}
