/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.weld1174;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.interceptor.Interceptors;

/**
 * @author <a href="ron.sigal@jboss.com">Ron Sigal</a>
 * @version $Revision: 1.1 $
 *          <p/>
 *          Copyright May 7, 2012
 */
@RequestScoped
@Interceptors({ Interceptor0.class })
@ClassTestBinding
public class InterceptedManagedBean implements Serializable {

    @Interceptors({ Interceptor1.class })
    @MethodTestBinding
    public String test() {
        return null;
    }
}
