/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.literal;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.util.AnnotationLiteral;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class BeforeDestroyedLiteral extends AnnotationLiteral<BeforeDestroyed> implements BeforeDestroyed {

    public static final BeforeDestroyedLiteral REQUEST = of(RequestScoped.class);

    public static final BeforeDestroyedLiteral CONVERSATION = of(ConversationScoped.class);

    public static final BeforeDestroyedLiteral SESSION = of(SessionScoped.class);

    public static final BeforeDestroyedLiteral APPLICATION = of(ApplicationScoped.class);

    private static final long serialVersionUID = 1L;

    private final Class<? extends Annotation> value;

    public static BeforeDestroyedLiteral of(Class<? extends Annotation> value) {
        return new BeforeDestroyedLiteral(value);
    }

    private BeforeDestroyedLiteral(Class<? extends Annotation> value) {
        this.value = value;
    }

    public Class<? extends Annotation> value() {
        return value;
    }

}
