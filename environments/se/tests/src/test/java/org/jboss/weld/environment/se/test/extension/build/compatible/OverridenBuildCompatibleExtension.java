/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.extension.build.compatible;

import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Registration;
import jakarta.enterprise.inject.build.compatible.spi.SkipIfPortableExtensionPresent;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

/**
 * This BCE is overriden and should never be invoked
 */
// TODO change to OverridingPortableExtension.class once https://github.com/eclipse-ee4j/cdi/issues/585 is resolved
@SkipIfPortableExtensionPresent("org.jboss.weld.environment.se.test.extension.build.compatible.OverridingPortableExtension")
public class OverridenBuildCompatibleExtension implements BuildCompatibleExtension {

    public static int TIMES_INVOKED = 0;

    @Discovery
    public void discovery() {
        TIMES_INVOKED++;
    }

    @Enhancement(types = Object.class)
    public void enhancement(ClassInfo c) {
        TIMES_INVOKED++;
    }

    @Registration(types = Object.class)
    public void registration(BeanInfo b) {
        TIMES_INVOKED++;
    }

    @Synthesis
    public void synthesis() {
        TIMES_INVOKED++;
    }

    @Validation
    public void validation() {
        TIMES_INVOKED++;
    }
}
