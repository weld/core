/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.generic;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * @author Marius Bogoevici
 */
@Dependent
public class TestBean {
    @Inject
    GenericInterface<String> genericStringField;

    @Inject
    GenericInterface<Integer> genericIntegerField;

    @Inject
    BoundedGenericBean<Subclass> boundedGenericSubclassField;

    @Inject
    BoundedGenericBean<BaseClass> boundedGenericBaseField;


    public String echo(String param) {
        return genericStringField.echo(param);
    }

    public Integer echo(Integer param) {
        return genericIntegerField.echo(param);
    }

    public Subclass echo(Subclass param) {
        return boundedGenericSubclassField.echo(param);
    }

    public BaseClass echo(BaseClass param) {
        return boundedGenericBaseField.echo(param);
    }

}
