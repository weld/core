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
package org.jboss.weld.tests.unit.logging.messages;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.logging.MessageId;
import org.jboss.weld.logging.messages.BeanManagerMessage;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.logging.messages.ContextMessage;
import org.jboss.weld.logging.messages.ConversationMessage;
import org.jboss.weld.logging.messages.ElMessage;
import org.jboss.weld.logging.messages.EventMessage;
import org.jboss.weld.logging.messages.JsfMessage;
import org.jboss.weld.logging.messages.MetadataMessage;
import org.jboss.weld.logging.messages.ReflectionMessage;
import org.jboss.weld.logging.messages.ServletMessage;
import org.jboss.weld.logging.messages.UtilMessage;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.logging.messages.VersionMessage;
import org.jboss.weld.logging.messages.XmlMessage;
import org.junit.Assert;
import org.junit.Test;

public class LoggingMessageUniquenessTest {

    @Test
    public void testLoggingMessagesUnique() {
        assertMessageIdsUnique(BeanManagerMessage.class);
        assertMessageIdsUnique(BeanMessage.class);
        assertMessageIdsUnique(BootstrapMessage.class);
        assertMessageIdsUnique(ContextMessage.class);
        assertMessageIdsUnique(ConversationMessage.class);
        assertMessageIdsUnique(ElMessage.class);
        assertMessageIdsUnique(EventMessage.class);
        assertMessageIdsUnique(JsfMessage.class);
        assertMessageIdsUnique(MetadataMessage.class);
        assertMessageIdsUnique(ReflectionMessage.class);
        assertMessageIdsUnique(ServletMessage.class);
        assertMessageIdsUnique(UtilMessage.class);
        assertMessageIdsUnique(ValidatorMessage.class);
        assertMessageIdsUnique(VersionMessage.class);
        assertMessageIdsUnique(XmlMessage.class);
    }

    private void assertMessageIdsUnique(Class<? extends Enum<?>> enumClass) {
        Set<String> ids = new HashSet<String>();
        for (Field field : enumClass.getDeclaredFields()) {
            MessageId annotation = field.getAnnotation(MessageId.class);
            if (annotation != null && !field.isAnnotationPresent(Deprecated.class)) {
                String id = annotation.value();
                if (!ids.add(id)) {
                    Assert.fail("ID " + id + " is not unique. Field: " + field.getName() + ", " + enumClass);
                }
            }
        }
    }
}
