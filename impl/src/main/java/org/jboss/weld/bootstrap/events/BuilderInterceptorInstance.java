/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.io.Serializable;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.util.bean.SerializableForwardingBean;

/**
 * @author Tomas Remes
 */
public class BuilderInterceptorInstance implements Serializable {

    private static final long serialVersionUID = -1623826535751475203L;

    private final SerializableForwardingBean<?> interceptedBean;

    BuilderInterceptorInstance() {
        this(null, null);
    }

    BuilderInterceptorInstance(Bean<?> interceptedBean, String contextId) {
        this.interceptedBean = interceptedBean != null ? SerializableForwardingBean.of(contextId, interceptedBean) : null;
    }

    public Bean<?> getInterceptedBean() {
        return interceptedBean;
    }

}
