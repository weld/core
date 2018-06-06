package org.jboss.weld.environment.se;

import org.jboss.logging.Logger;

public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger logger = Logger.getLogger(DefaultExceptionHandler.class);

	@Override
	public Class<? extends Throwable>[] getExceptionTypes() {
		return null;
	}

	@Override
	public void handle(Throwable t) {
        logger.error("Weld-SE application exited with an exception", t);
	}

}