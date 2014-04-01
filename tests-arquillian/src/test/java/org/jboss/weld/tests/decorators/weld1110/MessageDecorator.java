package org.jboss.weld.tests.decorators.weld1110;

import java.io.Serializable;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

@Decorator
public abstract class MessageDecorator implements MessageSender, Serializable {

	@Inject @Delegate
	private MessageSender sender;

	public String send(String message) {
		String msg = "Decorated " + message;
		return sender.send(msg);
	}
}
