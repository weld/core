/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.lite.extension.translator.logging;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.Category;

/**
 * Logger for Lite extension translator module.
 *
 * @author Matej Novotny
 */
@MessageLogger(projectCode = "LITE-EXTENSION-TRANSLATOR-")
public interface LiteExtensionTranslatorLogger extends BasicLogger {
    LiteExtensionTranslatorLogger LOG = Logger.getMessageLogger(LiteExtensionTranslatorLogger.class, Category.LITE_EXTENSION_TRANSLATOR.getName());

    @Message(id = 0, value = "Unable to instantiate object from class {0} via no-args constructor. The exception was: {1}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException unableToInstantiateObject(Class<?> classToInstantiate, String exception);

    @Message(id = 1, value = "Unexpected extension method argument: {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException unexpectedMethodArgument(Object argument);

    @Message(id = 2, value = "{0} methods can't declare a parameter of type {1}, found at {2}. Method name - {3}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidMethodParameter(Object phaseString, Object paramTypeString, Object declaringClassName, Object methodName);

    @Message(id = 3, value = "Unknown declaration {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException unknownDeclaration(Object cdiDeclaration);

    @Message(id = 4, value = "Observer method without an @Observes parameter: {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException missingObservesAnnotation(Object cdiDeclaration);

    @Message(id = 5, value = "Unknown primitive type: {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException unknownPrimitiveType(Object typeFound);

    @Message(id = 6, value = "Zero or more than one parameter of type {0} for method {1} " +
            "with declaring class {2}", format = Message.Format.MESSAGE_FORMAT)
    DefinitionException incorrectParameterCount(String validParameterTypes, Object method, Object declaringClass);

    @Message(id = 7, value = "Unknown query parameter: {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException unknownQueryParameter(Object query);

    @Message(id = 8, value = "Unable to invoke extension method {0} with arguments {1}. The exception was: {2}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException unableToInvokeExtensionMethod(Object method, Object arguments, String exception);

    @Message(id = 9, value = "Unable to load class with name {0}. The exception was: {1}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException cannotLoadClassByName(Object className, String exception);

    @Message(id = 10, value = "Unrecognized parameter of type {0} declared in class {1}#{2}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidExtensionMethodParameterType(Object type, Object declaringClass, Object methodName);

    @Message(id = 11, value = "Unknown reflection type {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException unknownReflectionType(Object reflectionType);

    @Message(id = 12, value = "Unknown annotation member {0}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException unknownAnnotationMember(Object annotationMember);

    @Message(id = 13, value = "Not {0}:{1}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException kindNotEqual(Object kind, Object value);

    @Message(id = 14, value = "Unable to access annotation member(s) for annotation {0}. The exception was: {1}", format = Message.Format.MESSAGE_FORMAT)
    DefinitionException unableToAccessAnnotationMembers(Object annotation, String exception);

    @Message(id = 15, value = "Provided type {0} is illegal because it doesn't match an of known annotation member types.", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException illegalAnnotationMemberType(Object type);

}
