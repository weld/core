/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.util;

import java.io.CharArrayReader;
import java.util.Optional;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.weld.probe.JsonDataProvider;

public class ProbeJMXUtil {

    public final static String JMX_CONNECTION_URL = "service:jmx:rmi:///jndi/rmi://127.0.0.1:9999/jmxrmi";

    private static JMXConnector getConnector(String connectionUrl) throws Exception {
        JMXConnector jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(connectionUrl), null);
        return jmxConnector;
    }

    private static ObjectInstance getProbeMBeanInstance(MBeanServerConnection connection) throws Exception {
        Set<ObjectInstance> mbeans = connection.queryMBeans(null, null);
        Optional<ObjectInstance> probeMBean = mbeans.stream().filter(p -> p.getClassName().contains(JsonDataProvider.class.getPackage().getName())).findFirst();
        ObjectInstance probeMBeanInstance = probeMBean.get();
        return probeMBeanInstance;
    }

    public static JsonObject invokeMBeanOperation(String name, Object[] params, String[] signature) throws Exception {
        try (JMXConnector connector = getConnector(JMX_CONNECTION_URL)) {
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            ObjectInstance mBeanInstance = getProbeMBeanInstance(connection);
            Object o = connection.invoke(mBeanInstance.getObjectName(), name, params, signature);
            CharArrayReader arrayReader = new CharArrayReader(o.toString().toCharArray());
            JsonReader reader = Json.createReader(arrayReader);
            return reader.readObject();
        }
    }

}
