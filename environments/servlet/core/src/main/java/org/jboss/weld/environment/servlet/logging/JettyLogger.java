/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.logging;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.weld.environment.logging.WeldEnvironmentLogger;

/**
 *
 * Message IDs: 001200 - 001299
 *
 * @author Kirill Gaevskii
 *
 */
@MessageLogger(projectCode = WeldEnvironmentLogger.WELD_ENV_PROJECT_CODE)
public interface JettyLogger extends WeldEnvironmentLogger {
    JettyLogger LOG = Logger.getMessageLogger(JettyLogger.class, Category.JETTY.getName());

    @LogMessage(level = Level.INFO)
    @Message(id = 1200, value = "Jetty 7.2+ detected, CDI injection will be available in Servlets and Filters. Injection into Listeners should work on Jetty 9.1.1 and newer.")
    void jettyDetectedListenersInjectionIsSupported();

    @LogMessage(level = Level.INFO)
    @Message(id = 1201, value = "Jetty 7.2+ detected, CDI injection will be available in Servlets and Filters. Injection into Listeners is not supported.")
    void jettyDetectedListenersInjectionIsNotSupported();

    @LogMessage(level = Level.ERROR)
    @Message(id = 1202, value = "Unable to create JettyWeldInjector. CDI injection will not be available in Servlets, Filters or Listeners.")
    void unableToCreateJettyWeldInjector(@Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 1203, value = "GWTHostedMode detected, JSR-299 injection will be available in Servlets and Filters. Injection into Listeners is not supported.")
    void gwtHostedModeDetected();

    @LogMessage(level = Level.WARN)
    @Message(id = 1204, value = "Can't find Injector in the servlet context so injection is not available for {0}.", format = Format.MESSAGE_FORMAT)
    void cantFindInjector(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 1205, value = "Missing jetty-env.xml, no BeanManager present in JNDI.")
    void missingJettyEnv();

    @LogMessage(level = Level.INFO)
    @Message(id = 1206, value = "Cannot find matching WebApplicationContext, no default CDI support: use jetty-web.xml")
    void cantFindWebApplicationContext();

    @LogMessage(level = Level.WARN)
    @Message(id = 1207, value = "Can't find Injector in the servlet context so injection is not available for {0}.", format = Format.MESSAGE_FORMAT)
    void cantFindInjectior(Object param1);

    @LogMessage(level = Level.WARN)
    @Message(id = 1208, value = "Missing jetty-env.xml, no BeanManager present in JNDI.")
    void missingJettyEnvXml();

    @LogMessage(level = Level.INFO)
    @Message(id = 1209, value = "Cannot find matching WebApplicationContext, no default CDI support: use jetty-web.xml")
    void cantFindMatchingWebApplicationContext();

    @Message(id = 1210, value = "No such Jetty injector found in servlet context attributes.")
    IllegalStateException noSuchJettyInjectorFound();
}