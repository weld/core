package org.jboss.weld.tests.decorators.weld1110;

import java.io.Serializable;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;


@Decorator
public abstract class MessageDecorator extends MessageSender implements Serializable {
	
	@Inject @Delegate
	private MessageSender sender;
	
	public Message send(Message message) {
		Message msg = new Message("Decorated " + message.getContent());
		return sender.send(msg);
	}
}
