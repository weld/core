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
package org.jboss.weld.bootstrap;

/**
 * A simple tracker used to monitor bootstrap operations. It is not thread-safe and may not be shared between threads.
 *
 * @author Martin Kouba
 */
interface Tracker extends AutoCloseable {

    String OP_BOOTSTRAP = "bootstrap";
    String OP_START_CONTAINER = "startContainer";
    String OP_INIT_SERVICES = "initServices";
    String OP_CONTEXTS = "builtinContexts";
    String OP_READ_DEPLOYMENT = "readDeploymentStructure";
    String OP_START_INIT = "startInitialization";
    String OP_DEPLOY_BEANS = "deployBeans";
    String OP_VALIDATE_BEANS = "validateBeans";
    String OP_END_INIT = "endInitialization";
    String OP_BBD = "BeforeBeanDiscovery";
    String OP_ATD = "AfterTypeDiscovery";
    String OP_ABD = "AfterBeanDiscovery";
    String OP_ADV = "AfterDeploymentValidation";

    /**
     * Starts an operation - push.
     *
     * @param operation
     * @return self
     */
    Tracker start(String operation);

    /**
     * Ends the last operation on the stack - pop.
     *
     * @return self
     */
    Tracker end();

    /**
     * Could be used to monitor the current operation progress.
     *
     * @param info
     */
    void split(String info);

    /**
     * End all operations.
     */
    void close();

}
