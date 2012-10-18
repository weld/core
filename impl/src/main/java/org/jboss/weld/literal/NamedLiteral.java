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
package org.jboss.weld.literal;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;

/**
 * Annotation literal for {@link Named}
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
@SuppressWarnings("all")
public class NamedLiteral extends AnnotationLiteral<Named> implements Named {

    private static final long serialVersionUID = 5089199348756765779L;

    private final String value;

    public String value() {
        return value;
    }

    public NamedLiteral(String value) {
        this.value = value;
    }

    public static final Named DEFAULT = new NamedLiteral("");

}
