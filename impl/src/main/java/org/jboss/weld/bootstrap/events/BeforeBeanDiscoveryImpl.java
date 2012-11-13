/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.BeanDeployer;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.EnablementBuilder;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.literal.InterceptorBindingTypeLiteral;
import org.jboss.weld.literal.NormalScopeLiteral;
import org.jboss.weld.literal.QualifierLiteral;
import org.jboss.weld.literal.ScopeLiteral;
import org.jboss.weld.literal.StereotypeLiteral;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;

public class BeforeBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements BeforeBeanDiscovery {

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts, EnablementBuilder enablementBuilder) {
        new BeforeBeanDiscoveryImpl(beanManager, deployment, beanDeployments, contexts, enablementBuilder).fire();
    }

    protected BeforeBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts, EnablementBuilder enablementBuilder) {
        super(beanManager, BeforeBeanDiscovery.class, beanDeployments, deployment, contexts, enablementBuilder);
    }

    public void addQualifier(Class<? extends Annotation> bindingType) {
        getTypeStore().add(bindingType, QualifierLiteral.INSTANCE);
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(bindingType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(bindingType);
    }

    public void addInterceptorBinding(Class<? extends Annotation> bindingType, Annotation... bindingTypeDef) {
        TypeStore typeStore = getTypeStore();
        typeStore.add(bindingType, InterceptorBindingTypeLiteral.INSTANCE);
        for (Annotation a : bindingTypeDef) {
            typeStore.add(bindingType, a);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(bindingType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(bindingType);
    }

    public void addScope(Class<? extends Annotation> scopeType, boolean normal, boolean passivating) {
        if (normal) {
            getTypeStore().add(scopeType, new NormalScopeLiteral(passivating));
        } else if (passivating) {
            throw new DefinitionException(BootstrapMessage.PASSIVATING_NON_NORMAL_SCOPE_ILLEGAL, scopeType);
        } else {
            getTypeStore().add(scopeType, ScopeLiteral.INSTANCE);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(scopeType);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(scopeType);
    }

    public void addStereotype(Class<? extends Annotation> stereotype, Annotation... stereotypeDef) {
        TypeStore typeStore = getTypeStore();
        typeStore.add(stereotype, StereotypeLiteral.INSTANCE);
        for (Annotation a : stereotypeDef) {
            typeStore.add(stereotype, a);
        }
        getBeanManager().getServices().get(ClassTransformer.class).clearAnnotationData(stereotype);
        getBeanManager().getServices().get(MetaAnnotationStore.class).clearAnnotationData(stereotype);
    }

    public void addAnnotatedType(AnnotatedType<?> type) {
        if (Beans.isVetoed(type)) {
            return;
        }
        Object receiver = getReceiver();
        if (!(receiver instanceof Extension)) {
            throw new IllegalStateException("BeforeBeanDiscovery receiver is not an extension");
        }
        BeanDeployer deployer = getOrCreateBeanDeployment(type.getJavaClass()).getBeanDeployer();
        deployer.addSyntheticClass(type, (Extension) receiver);
    }

}
