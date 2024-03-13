package org.jboss.weld.logging;

import static org.jboss.weld.logging.WeldLogger.WELD_PROJECT_CODE;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;

/**
 * Log messages for {@link jakarta.enterprise.invoke.Invoker}, {@link jakarta.enterprise.invoke.InvokerBuilder}
 * and related Weld classes.
 *
 * Message IDs: 002000 - 002099
 */
@MessageLogger(projectCode = WELD_PROJECT_CODE)
public interface InvokerLogger extends WeldLogger {

    InvokerLogger LOG = Logger.getMessageLogger(InvokerLogger.class, Category.INVOKER.getName());

    @Message(id = 2000, value = "Cannot apply {0} to method argument with position {1}; total number of method parameters is {2}", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException invalidArgumentPosition(String kindOfTransformer, int position, int argLookupLength);

    @Message(id = 2001, value = "{0} transformer is already set! InvokerBuilder transformers cannot be set repeatedly.", format = Message.Format.MESSAGE_FORMAT)
    IllegalArgumentException settingTransformerRepeatedly(String kindOfTransformer);

    @Message(id = 2002, value = "Invalid {0} transformer method: {1}", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException invalidTransformerMethod(String kindOfTransformer, Object transformerMetadata);

    @Message(id = 2003, value = "No matching transformer method found for {0}. There has to be exactly one matching method.", format = Message.Format.MESSAGE_FORMAT)
    IllegalStateException noMatchingTransformerMethod(Object transformerMetadata);

    @Message(id = 2004, value = "Multiple matching transformer methods found for {0}. There has to be exactly one matching method; instead, following methods were found: {1}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException multipleMatchingTransformerMethod(Object transformerMetadata, Object listOfMatches);

    @Message(id = 2005, value = "Unable to create method handle for method: {0}", format = Message.Format.MESSAGE_FORMAT)
    RuntimeException cannotCreateMethodHandle(Object method, @Cause Throwable cause);

    @Message(id = 2006, value = "All invocation transformers need to be public - {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException nonPublicTransformer(Object transformerMetadata);

    @Message(id = 2007, value = "Input transformer {0} has a return value that is not assignable to expected class: {1}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException inputTransformerNotAssignable(Object transformerMetadata, Object clazz);

    @Message(id = 2008, value = "Non-static input transformers are expected to have zero input parameters! Transformer: {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException nonStaticInputTransformerHasParams(Object transformerMetadata);

    @Message(id = 2009, value = "Static input transformers can only have one or two parameters! Transformer: {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException staticInputTransformerParams(Object transformerMetadata);

    @Message(id = 2010, value = "Static input transformers with two parameters can only have Consumer<Runnable> as their second parameter! Transformer: {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException staticInputTransformerIncorrectParams(Object transformerMetadata);

    @Message(id = 2011, value = "Non-static output transformers are expected to have zero input parameters! Transformer: {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException nonStaticOutputTransformerHasParams(Object transformerMetadata);

    @Message(id = 2012, value = "Static output transformers are expected to have one input parameter! Transformer: {0}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException staticOutputTransformerParams(Object transformerMetadata);

    @Message(id = 2013, value = "Output transformer {0} parameter is not assignable to the expected class: {1}", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException outputTransformerNotAssignable(Object transformerMetadata, Object clazz);

    @Message(id = 2014, value = "Invocation wrapper has unexpected parameters: {0} \nExpected param types are: {1}, Object[], Invoker.class", format = Message.Format.MESSAGE_FORMAT)
    DeploymentException wrapperUnexpectedParams(Object transformerMetadata, Object clazz);
}
