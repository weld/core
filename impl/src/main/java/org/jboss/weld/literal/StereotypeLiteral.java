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

import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation literal for {@link Stereotype}?
 *
 * @author Pete Muir
 */
@SuppressWarnings("all")
public class StereotypeLiteral extends AnnotationLiteral<Stereotype> implements Stereotype {

    private static final long serialVersionUID = -974277187448157814L;

    public static final Stereotype INSTANCE = new StereotypeLiteral();

    private StereotypeLiteral() {
    }

}
