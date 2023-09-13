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

import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

/**
 * Exception message that produces a list of exceptions and their stack traces
 * for logging. This is typically used in lifecycle events which accumulate
 * exceptions across observers.
 *
 * @author David Allen
 */
public class WeldExceptionListMessage implements WeldExceptionMessage, Serializable {

    private static final long serialVersionUID = 3445187707771082346L;

    private List<? extends Throwable> causes;
    private String message;

    public WeldExceptionListMessage(List<? extends Throwable> throwables) {
        this.causes = throwables;
    }

    public String getAsString() {
        if (message == null) {
            generateMessage();
        }
        return message;
    }

    private void generateMessage() {
        StringWriter writer = new StringWriter();
        PrintWriter messageBuffer = new PrintWriter(writer);
        messageBuffer.print("Exception List with ");
        messageBuffer.print(causes.size());
        messageBuffer.print(" exceptions:\n");
        int i = 0;
        for (Throwable throwable : causes) {
            messageBuffer.print("Exception ");
            messageBuffer.print(i++);
            messageBuffer.print(" :\n");
            throwable.printStackTrace(messageBuffer);
        }
        messageBuffer.flush();
        message = writer.toString();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new WeldExceptionStringMessage(getAsString());
    }

}
