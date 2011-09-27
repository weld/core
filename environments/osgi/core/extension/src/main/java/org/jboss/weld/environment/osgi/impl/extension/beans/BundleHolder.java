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
package org.jboss.weld.environment.osgi.impl.extension.beans;

import org.jboss.weld.environment.osgi.api.BundleState;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import javax.enterprise.context.ApplicationScoped;

/**
 * This the reference to the current bundle used by Weld-OSGi. It wraps the OSGi
 * {@link Bundle}. It is aware of the new {@link BundleState}s for bean bundles.
 * It may be injected anywhere in the bean bundle.
 * <b/>
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
@ApplicationScoped
public class BundleHolder {
    private BundleState state = BundleState.INVALID;

    private Bundle bundle;

    private BundleContext context;

    /**
     * Get the OSGi {@link Bundle} object wrapped by this BundleHolder.
     * @return the Bundle object for the current bean bundle.
     */
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public BundleContext getContext() {
        return context;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public BundleState getState() {
        return state;
    }

    public void setState(BundleState state) {
        this.state = state;
    }

}
