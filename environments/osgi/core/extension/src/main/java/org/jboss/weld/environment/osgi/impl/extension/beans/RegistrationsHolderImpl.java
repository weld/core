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

import org.jboss.weld.environment.osgi.api.RegistrationHolder;
import org.osgi.framework.ServiceRegistration;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link RegistrationHolder}.
 *
 * @author Mathieu ANCELIN - SERLI (mathieu.ancelin@serli.com)
 * @author Matthieu CLOCHARD - SERLI (matthieu.clochard@serli.com)
 */
@ApplicationScoped
public class RegistrationsHolderImpl implements RegistrationHolder {

    private List<ServiceRegistration> registrations =
                                      new ArrayList<ServiceRegistration>();

    @Override
    public List<ServiceRegistration> getRegistrations() {
        return registrations;
    }

    @Override
    public void addRegistration(ServiceRegistration reg) {
        registrations.add(reg);
    }

    @Override
    public void removeRegistration(ServiceRegistration reg) {
        registrations.remove(reg);
    }

    @Override
    public void clear() {
        registrations.clear();
    }

    @Override
    public int size() {
        return registrations.size();
    }

}
