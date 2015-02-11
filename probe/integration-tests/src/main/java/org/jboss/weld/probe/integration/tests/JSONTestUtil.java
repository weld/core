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
package org.jboss.weld.probe.integration.tests;

import java.io.IOException;
import java.net.URL;

import com.gargoylesoftware.htmlunit.WebClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JSONTestUtil {

    public static final String DATA = "data";
    public static final String ID = "id";
    public static final String BDAS = "bdas";
    public static final String BDA_ID = "bdaId";
    public static final String KIND = "kind";
    public static final String SCOPE = "scope";
    public static final String QUALIFIERS = "qualifiers";
    public static final String TYPES = "types";
    public static final String DEPENDENTS = "dependents";
    public static final String DEPENDENCIES = "dependencies";
    public static final String BEAN_CLASS = "beanClass";
    public static final String METHOD_NAME = "methodName";
    public static final String EVENT_INFO = "eventInfo";
    public static final String INSTANCES = "instances";
    public static final String BEAN_DISCOVERY_MODE = "beanDiscoveryMode";
    public static final String DEPLOYMENT_PATH = "weld-probe/deployment";
    public static final String INVOCATIONS_PATH = "weld-probe/invocations";
    public static final String EVENTS_PATH = "weld-probe/events";
    public static final String BEANS_PATH = "weld-probe/beans";
    public static final String SESSION_CONTEXTS_PATH = "weld-probe/contexts/session";
    public static final String APPLICATION_CONTEXTS_PATH = "weld-probe/contexts/application";
    public static final String BEANS_PATH_ALL = "weld-probe/beans?pageSize=0";

    private JSONTestUtil() {
    }

    public static JsonObject getPageAsJSONObject(String path, URL url) throws IOException {
        return getPageAsJSONObject(path, url, null);
    }

    public static JsonObject getPageAsJSONObject(String path, URL url, WebClient client) throws IOException {

        if (client == null) {
            client = new WebClient();
        }

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(client.getPage(url.toString().concat(path)).getWebResponse().getContentAsString(), JsonObject.class);
        return jsonObject;
    }

    public static JsonObject getDeploymentByName(String path, String name, URL url) throws IOException {
        JsonObject deploymentJSON = getPageAsJSONObject(path, url);
        JsonArray deployments = deploymentJSON.getAsJsonArray(BDAS);
        JsonObject result = null;
        for (int i = 0; i < deployments.size(); i++) {
            String bdaId = deployments.get(i).getAsJsonObject().get(BDA_ID).getAsString();
            if (bdaId.contains(name)) {
                result = deployments.get(i).getAsJsonObject();
            }
        }
        return result;
    }

    public static JsonObject getBeanDetail(String path, Class clazz, URL url) throws IOException {
        JsonObject beans = getPageAsJSONObject(path, url);
        JsonArray beansArray = beans.getAsJsonArray(DATA);
        String id = null;
        for (int i = 0; i < beansArray.size(); i++) {
            JsonObject bean = beansArray.get(i).getAsJsonObject();
            if (bean.get(BEAN_CLASS).getAsString().equals(clazz.getName())) {
                id = bean.get(ID).getAsString();
            }
        }

        if (id != null) {
            String beanDetailURL = BEANS_PATH.concat("/".concat(id));
            return getPageAsJSONObject(beanDetailURL, url);
        } else {
            return null;
        }
    }

    public static enum BeanType {

        MANAGED,
        INTERCEPTOR,
        DECORATOR;
    }

}
