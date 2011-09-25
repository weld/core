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

import javax.enterprise.context.NormalScope;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Annotation literal for {@link NormalScope}
 *
 * @author Pete Muir
 */
@SuppressWarnings("all")
public class NormalScopeLiteral extends AnnotationLiteral<NormalScope> implements NormalScope {

    private static final long serialVersionUID = -411873333591249571L;

    private final boolean passivating;

    public NormalScopeLiteral(boolean passivating) {
        this.passivating = passivating;
    }

    public boolean passivating() {
        return passivating;
    }

}
