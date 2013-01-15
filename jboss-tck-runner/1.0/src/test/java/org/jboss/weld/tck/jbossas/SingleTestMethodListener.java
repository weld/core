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
package org.jboss.weld.tck.jbossas;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class SingleTestMethodListener implements IMethodInterceptor {
    public static final String TEST_CLASS_PROPERTY = "tckTest";

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        String test = System.getProperty(TEST_CLASS_PROPERTY);
        if (test == null || test.length() == 0) {
            return methods;
        }
        List<IMethodInstance> ret = new ArrayList<IMethodInstance>();
        if (test.contains(".")) {
            for (IMethodInstance method : methods) {
                if (method.getMethod().getTestClass().getName().equals(test)) {
                    ret.add(method);
                }
            }
        } else {
            for (IMethodInstance method : methods) {
                if (method.getMethod().getTestClass().getName().endsWith("." + test)) {
                    ret.add(method);
                }
            }
        }
        return ret;
    }
}
