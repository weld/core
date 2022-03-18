/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.interceptors.signature;

import jakarta.enterprise.context.Dependent;
import org.junit.Assert;

import org.jboss.weld.bean.proxy.InterceptionDecorationContext;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Lifecycle
@Dependent
public class LifecycleInterceptedBean {

    public String foo() {
        Stack stack = InterceptionDecorationContext.getStack();
        Assert.assertNotNull(stack);
        Assert.assertEquals(1, stack.size());
        return "foo";
    }
}
