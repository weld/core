/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.servlet;

import static org.jboss.weld.util.reflection.Reflections.cast;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

import org.jboss.weld.Container;
import org.jboss.weld.Weld;
import org.jboss.weld.manager.BeanManagerImpl;

public class StaticWeldProvider implements CDIProvider {

    private static class WeldSingleton {
        private static final Weld<Object> WELD_INSTANCE = new EnhancedWeld<Object>();
    }

    private static class EnhancedWeld<T> extends Weld<T> {

        @Override
        protected BeanManagerImpl unsatisfiedBeanManager(String callerClassName) {
            /*
             * In certain scenarios we use flat deployment model (weld-se, weld-servlet). In that case
             * we return the only BeanManager we have.
             */
            if (Container.instance().beanDeploymentArchives().values().size() == 1) {
                return Container.instance().beanDeploymentArchives().values().iterator().next();
            }
            return super.unsatisfiedBeanManager(callerClassName);
        }
    }

    @Override
    public <T> CDI<T> getCDI() {
        return cast(WeldSingleton.WELD_INSTANCE);
    }
}
