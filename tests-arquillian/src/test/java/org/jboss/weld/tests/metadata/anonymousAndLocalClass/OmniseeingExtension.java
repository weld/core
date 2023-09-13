/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.metadata.anonymousAndLocalClass;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;

/**
 * Observes everything and everyone - this triggers creation of AT for all classes.
 * This way we make sure it won't happen for anonymous/local classes.
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class OmniseeingExtension implements Extension {

    public static int LOCAL_CLASS_OBSERVED = 0;
    public static int ANONYMOUS_CLASS_OBSERVED = 0;

    public void observePAT(@Observes ProcessAnnotatedType<?> pat) {
        if (pat.getAnnotatedType().getJavaClass().isLocalClass()) {
            LOCAL_CLASS_OBSERVED++;
        }
        if (pat.getAnnotatedType().getJavaClass().isAnonymousClass()) {
            ANONYMOUS_CLASS_OBSERVED++;
        }
    }
}
