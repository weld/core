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
package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import java.lang.invoke.MethodHandles;

import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.jboss.weld.exceptions.AmbiguousResolutionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.UnsatisfiedResolutionException;

/**
 * Log messages for bean manager and related support classes.
 *
 * Message IDs: 001300 - 001399
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface BeanManagerLogger extends WeldLogger {

    BeanManagerLogger LOG = Logger.getMessageLogger(MethodHandles.lookup(), BeanManagerLogger.class,
            Category.BEAN_MANAGER.getName());

    @Message(id = 1300, value = "Unable to locate BeanManager")
    NamingException cannotLocateBeanManager();

    @Message(id = 1301, value = "Annotation {0} is not a qualifier", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidQualifier(Object param1);

    @Message(id = 1302, value = "Duplicate qualifiers:  {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException duplicateQualifiers(Object param1);

    @SuppressWarnings("weldlog:method-retType")
    @Message(id = 1303, value = "No active contexts for scope type {0}", format = Format.MESSAGE_FORMAT)
    ContextNotActiveException contextNotActive(Object param1);

    @Message(id = 1304, value = "More than one context active for scope type {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException duplicateActiveContexts(Object param1);

    @Message(id = 1305, value = "The given type {0} is not a type of the bean {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException specifiedTypeNotBeanType(Object param1, Object param2);

    @Message(id = 1307, value = "Unable to resolve any beans of type {0} with qualifiers {1}", format = Format.MESSAGE_FORMAT)
    UnsatisfiedResolutionException unresolvableType(Object param1, Object param2);

    @Message(id = 1308, value = "Unable to resolve any beans for {0}", format = Format.MESSAGE_FORMAT)
    UnsatisfiedResolutionException unresolvableElement(Object param1);

    @Message(id = 1310, value = "No decorator types were specified in the set")
    IllegalArgumentException noDecoratorTypes();

    @Message(id = 1311, value = "Interceptor bindings list cannot be empty")
    IllegalArgumentException interceptorBindingsEmpty();

    @Message(id = 1312, value = "Duplicate interceptor binding type {0} found", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException duplicateInterceptorBinding(Object param1);

    @Message(id = 1313, value = "Trying to resolve interceptors with non-binding type {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException interceptorResolutionWithNonbindingType(Object param1);

    @Message(id = 1314, value = "{0} is expected to be a normal scope type", format = Format.MESSAGE_FORMAT)
    String nonNormalScope(Object param1);

    @Message(id = 1316, value = "{0} is not an interceptor binding type", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException notInterceptorBindingType(Object param1);

    @Message(id = 1317, value = "{0} is not a stereotype", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException notStereotype(Object param1);

    @Message(id = 1318, value = "Cannot resolve an ambiguous dependency between: {0}", format = Format.MESSAGE_FORMAT)
    AmbiguousResolutionException ambiguousBeansForDependency(Object param1);

    @Message(id = 1319, value = "Bean manager ID must not be null")
    IllegalArgumentException nullBeanManagerId();

    @Message(id = 1325, value = "No instance of an extension {0} registered with the deployment", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException noInstanceOfExtension(Object param1);

    @Message(id = 1326, value = "Cannot create bean attributes - the argument must be either an AnnotatedField or AnnotatedMethod but {0} is not", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException cannotCreateBeanAttributesForIncorrectAnnotatedMember(Object param1);

    @Message(id = 1327, value = "Unable to identify the correct BeanManager. The calling class {0} is placed in multiple bean archives", format = Format.MESSAGE_FORMAT)
    IllegalStateException ambiguousBeanManager(Object param1);

    @Message(id = 1328, value = "Unable to identify the correct BeanManager. The calling class {0} is not placed in bean archive", format = Format.MESSAGE_FORMAT)
    IllegalStateException unsatisfiedBeanManager(Object param1);

    @Message(id = 1329, value = "Unable to identify the correct BeanManager")
    IllegalStateException unableToIdentifyBeanManager();

    @Message(id = 1330, value = "BeanManager is not available.")
    IllegalStateException beanManagerNotAvailable();

    @Message(id = 1331, value = "Declaring bean cannot be null for the non-static member {0}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException nullDeclaringBean(Object param1);

    @Message(id = 1332, value = "BeanManager method {0} is not available during application initialization. Container state: {1}", format = Format.MESSAGE_FORMAT)
    IllegalStateException methodNotAvailableDuringInitialization(Object param1, Object state);

    @Message(id = 1333, value = "BeanManager method {0} is not available after shutdown", format = Format.MESSAGE_FORMAT)
    IllegalStateException methodNotAvailableAfterShutdown(Object param1);

    @Message(id = 1334, value = "Unsatisfied dependencies for type {1} with qualifiers {0} {2}", format = Format.MESSAGE_FORMAT)
    UnsatisfiedResolutionException injectionPointHasUnsatisfiedDependencies(Object param1, Object param2, Object param3);

    @Message(id = 1335, value = "Ambiguous dependencies for type {1} with qualifiers {0}\n Possible dependencies: {2}", format = Format.MESSAGE_FORMAT)
    AmbiguousResolutionException injectionPointHasAmbiguousDependencies(Object param1, Object param2, Object param3);

    @Message(id = 1336, value = "InjectionTargetFactory.configure() may not be called after createInjectionTarget() invocation. AnnotatedType used: {0}", format = Format.MESSAGE_FORMAT)
    IllegalStateException unableToConfigureInjectionTargetFactory(Object param1);

    @Message(id = 1337, value = "BeanContainer#{0} requires all parameters to be non-null.", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException assignabilityMethodIllegalArgs(Object param1);

    @Message(id = 1338, value = "All annotations passed into BeanContainer#{0} have to be CDI Qualifiers. Following annotation was not recognized as a qualifier: {1}", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException annotationNotAQualifier(Object param1, Object param2);

    @Message(id = 1339, value = "Provided event type {0} cannot contain unresolvable type parameter", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException eventTypeUnresolvableWildcard(Object param1);

}
