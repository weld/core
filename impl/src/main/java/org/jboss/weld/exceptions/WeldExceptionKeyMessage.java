/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.exceptions;

import java.io.Serializable;

import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

/**
 * Exception message based on an enumerated key and optional object arguments.
 * This includes localization of the message based on the settings in the JVM
 * when the {@link java.lang.Throwable#getMessage()} or
 * {@link java.lang.Throwable#getLocalizedMessage()} methods are invoked on a
 * Weld exception.
 *
 * @author David Allen
 */
public class WeldExceptionKeyMessage implements WeldExceptionMessage, Serializable {
    private static final long serialVersionUID = 3474682221381024558L;
    private Enum<?> messageKey;
    private String[] messageArguments;

    /**
     * <p>
     * Creates a new exception message based on an enumerated message key. This
     * message will not be localized until it is actually logged or other
     * software invokes the {@link #getMessage()} method.
     * </p>
     *
     * @param <E>  the message key enumeration
     * @param key  the message key from the above enumeration
     * @param args optional arguments for the message
     */
    public <E extends Enum<?>> WeldExceptionKeyMessage(E key, Object... args) {
        this.messageKey = key;
        if ((args != null) && (args.length > 0)) {
            this.messageArguments = new String[args.length];
            int index = 0;
            for (Object arg : args) {
                messageArguments[index++] = arg == null ? "null" : arg.toString();
            }
        }
    }

    /*
    * (non-Javadoc)
    * @see org.jboss.weld.exceptions.WeldExceptionMessage#getAsString()
    */
    public String getAsString() {
        String result = null;
        try {
            result = loggerFactory().getMessageConveyor().getMessage(messageKey, (Object[]) messageArguments);
        } catch (Exception e) {
            // We want the using exception to be processed, but also include
            // this one in its message
            result = "Exception message for key " + messageKey + " not found due to " + e.getLocalizedMessage();
        }
        if (result == null) {
            result = "Exception message for key " + messageKey + " not found";
        }
        return result;
    }

}
