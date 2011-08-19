/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.tests.bundle1.impl;

import org.jboss.weld.osgi.tests.bundle1.api.Name;
import org.jboss.weld.osgi.tests.bundle1.api.PropertyService;
import org.jboss.weld.environment.osgi.api.annotation.Publish;

@Publish
@Name("2")
public class PropertyServiceImpl3 implements PropertyService {

    @Override
    public String whoAmI() {
        return getClass().getName();
    }
}
