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
import javax.inject.Qualifier;

/**
 * Annotation literal for {@link Qualifier}?
 *
 * @author Pete Muir
 */
@SuppressWarnings("all")
public class QualifierLiteral extends AnnotationLiteral<Qualifier> implements Qualifier {

    private static final long serialVersionUID = -1865461877555038671L;

    public static final Qualifier INSTANCE = new QualifierLiteral();

    private QualifierLiteral() {
    }

}
