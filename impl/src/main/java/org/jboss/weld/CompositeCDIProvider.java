/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

import org.jboss.weld.util.collections.ImmutableList;

/**
 * Looks up {@link WeldCDIProvider} service providers and sorts them using {@link WeldCDIProvider#getPriority()}. Providers with higher priority are taken
 * first.
 * <p>
 * See also <a href= "https://issues.jboss.org/browse/WELD-2262">https://issues.jboss.org/browse/WELD-2262</a>.
 *
 * @author Martin Kouba
 */
public class CompositeCDIProvider implements CDIProvider {

    private volatile List<WeldCDIProvider> providers;

    @Override
    public CDI<Object> getCDI() {
        for (WeldCDIProvider provider : providers()) {
            CDI<Object> cdi = provider.getCDI();
            if (cdi != null) {
                return cdi;
            }
        }
        return null;
    }

    private List<WeldCDIProvider> providers() {
        if (providers == null) {
            synchronized (this) {
                if (providers == null) {
                    ImmutableList.Builder<WeldCDIProvider> builder = ImmutableList.builder();
                    for (WeldCDIProvider provider : ServiceLoader.load(WeldCDIProvider.class, CompositeCDIProvider.class.getClassLoader())) {
                        builder.add(provider);
                    }
                    providers = builder.build(new Comparator<WeldCDIProvider>() {
                        @Override
                        public int compare(WeldCDIProvider o1, WeldCDIProvider o2) {
                            return Integer.compare(o2.getPriority(), o1.getPriority());
                        }
                    });
                }
            }
        }
        return providers;
    }

}
