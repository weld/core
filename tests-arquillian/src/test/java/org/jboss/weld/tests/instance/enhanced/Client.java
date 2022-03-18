/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.instance.enhanced;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.math.BigDecimal;

/**
 * A version of {@link Client} that uses purely CDI interfaces
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@Dependent
public class Client {

    @Inject
    Instance<Alpha> alphaInstance;

    @Inject
    Instance<Object> instance;

    @Inject
    @Juicy
    Instance<BigDecimal> bigDecimalInstance;

    Instance<Alpha> getAlphaInstance() {
        return alphaInstance;
    }

    Instance<BigDecimal> getBigDecimalInstance() {
        return bigDecimalInstance;
    }

    Instance<Object> getInstance() {
        return instance;
    }
}
