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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.WeldException;

/**
 * Log messages relating to reflection
 *
 * Message ids: 000600 - 000699
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface ReflectionLogger extends WeldLogger {

    ReflectionLogger LOG = Logger.getMessageLogger(ReflectionLogger.class, Category.REFLECTION.getName());

    @LogMessage(level = Level.DEBUG)
    @Message(id = 600, value = "{0} is missing @Retention(RUNTIME). Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingRetention(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 601, value = "{0} is missing @Target. Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingTarget(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 602, value = "{0} is not declared @Target(TYPE, METHOD) or @Target(TYPE). Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingTargetTypeMethodOrTargetType(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 604, value = "{0} is not declared @Target(METHOD, FIELD, TYPE). Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingTargetMethodFieldType(Object param1);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 605, value = "{0} is not declared @Target(METHOD, FIELD, TYPE, PARAMETER), @Target(METHOD, TYPE), @Target(METHOD), @Target(TYPE) or @Target(FIELD). Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingTargetMethodFieldTypeParameterOrTargetMethodTypeOrTargetMethodOrTargetTypeOrTargetField(Object param1);

    @Message(id = 606, value = "Unable to determine name of parameter")
    IllegalArgumentException unableToGetParameterName();

    @Message(id = 607, value = "annotationMap cannot be null")
    WeldException annotationMapNull();

    @Message(id = 608, value = "declaredAnnotationMap cannot be null")
    WeldException declaredAnnotationMapNull();

    @Message(id = 610, value = "Unable to deserialize constructor. Declaring class {0}, index {1}", format = Format.MESSAGE_FORMAT)
    WeldException unableToGetConstructorOnDeserialization(Object param1, Object param2, @Cause Throwable cause);

    @Message(id = 611, value = "Unable to deserialize method. Declaring class {0}, index {1}", format = Format.MESSAGE_FORMAT)
    WeldException unableToGetMethodOnDeserialization(Object param1, Object param2, @Cause Throwable cause);

    @Message(id = 612, value = "Unable to deserialize field. Declaring class {0}, field name {1}", format = Format.MESSAGE_FORMAT)
    WeldException unableToGetFieldOnDeserialization(Object param1, Object param2, @Cause Throwable cause);

    @Message(id = 614, value = "Incorrect number of AnnotatedParameters {0} on AnnotatedMethod {1}. AnnotatedMethod has {2} as parameters but should have {3} as parameters", format = Format.MESSAGE_FORMAT)
    DefinitionException incorrectNumberOfAnnotatedParametersMethod(Object param1, Object param2, Object param3, Object param4);

    @Message(id = 616, value = "Instantiation through ReflectionFactory of {0} failed", format = Format.MESSAGE_FORMAT)
    WeldException reflectionfactoryInstantiationFailed(Object param1, @Cause Throwable cause);

    @Message(id = 617, value = "Instantiation through Unsafe of {0} failed", format = Format.MESSAGE_FORMAT)
    WeldException unsafeInstantiationFailed(Object param1, @Cause Throwable cause);

    @LogMessage(level = Level.WARN)
    @Message(id = 619, value = "A lifecycle callback interceptor declares an interceptor binding with target other than ElementType.TYPE\n  {0}\n  Binding: {1}\n  Target: {2}", format = Format.MESSAGE_FORMAT)
    void lifecycleCallbackInterceptorWithInvalidBindingTarget(Object interceptor, Object binding, Object elementTypes);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 620, value = "{0} is not declared @Target(METHOD, FIELD, PARAMETER, TYPE). Weld will use this annotation, however this may make the application unportable.", format = Format.MESSAGE_FORMAT)
    void missingTargetMethodFieldParameterType(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 621, value = "Interceptor binding {0} with @Target defined as {1} should not be applied on interceptor binding {2} with @Target definition: {3}", format = Format.MESSAGE_FORMAT)
    void invalidInterceptorBindingTargetDeclaration(Object param1, Object param2, Object param3, Object param4);

    // LOGTOOL-104 we had to change the cause type
    @Message(id = 622, value = "IllegalArgumentException invoking {2} on {1} ({0}) with parameters {3}", format = Format.MESSAGE_FORMAT)
    WeldException illegalArgumentExceptionOnReflectionInvocation(Class<?> clazz, Object instance, Method method,
            String parameters, @Cause Throwable cause);

    @Message(id = 623, value = "Unknown type {0}.", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException unknownType(Type type);

    @Message(id = 624, value = "Invalid type argument combination: {0}; {1}.", format = Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidTypeArgumentCombination(Type type1, Type type2);

    @Message(id = 625, value = "Unable to locate method: {0}", format = Format.MESSAGE_FORMAT)
    WeldException noSuchMethodWrapper(@Cause NoSuchMethodException cause, String message);

}