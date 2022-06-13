/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet.jsf;

import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationFactory;


/**
 * @author pmuir
 * @author alesj
 */
public class WeldApplicationFactory extends ApplicationFactory {

    private volatile Application application;

    public WeldApplicationFactory(ApplicationFactory applicationFactory) {
        super(applicationFactory);
    }

    protected ApplicationFactory delegate() {
        return getWrapped();
    }

    @Override
    public Application getApplication() {
        if (application == null) {
            synchronized (this) {
                if (application == null) {
                    application = new WeldApplication(delegate().getApplication());
                }
            }
        }
        return application;
    }

    @Override
    public void setApplication(Application application) {
        synchronized (this) {
            this.application = null; // invalidate the instance, so it picks up new application
            delegate().setApplication(application);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }
}
