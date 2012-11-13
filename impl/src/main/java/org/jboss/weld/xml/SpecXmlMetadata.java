/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.xml;

import java.net.URL;

import org.jboss.weld.bootstrap.spi.EnabledClass;

/**
 * Metadata about a beans.xml entry.
 * @author Jozef Hartinger
 *
 */
public class SpecXmlMetadata extends XmlMetadata<EnabledClass> {

    public SpecXmlMetadata(String qName, EnabledClass record, URL file, int lineNumber) {
        super(qName, record, file, lineNumber);
    }

    @Override
    public String getLocation() {
        StringBuilder builder = new StringBuilder();
        EnabledClass record = getValue();
        if (record == null) {
            builder.append("<");
            builder.append(getQName());
            builder.append("/>");
        } else {
            builder.append("<");
            builder.append(getQName());
            if (record.isEnabled() != null) {
                builder.append(" enabled=");
                builder.append(record.isEnabled().toString());
            }
            if (record.getPriority() != null) {
                builder.append(" priority=");
                builder.append(record.getPriority().toString());
            }
            builder.append(">");
            builder.append(record.getValue());
            builder.append("</");
            builder.append(getQName());
            builder.append("<");
        }
        // location suffix
        builder.append(" in ");
        builder.append(getFile().toString());
        builder.append("@");
        builder.append(getLineNumber());
        return builder.toString();
    }
}
