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
package org.jboss.weld.probe.tests.integration;

import static org.jboss.weld.probe.Strings.BDAS;
import static org.jboss.weld.probe.Strings.BDA_ID;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.DATA;
import static org.jboss.weld.probe.Strings.ID;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import com.gargoylesoftware.htmlunit.WebClient;

public class JSONTestUtil {

    public static final String DEPLOYMENT_PATH = "weld-probe/deployment";
    public static final String INVOCATIONS_PATH = "weld-probe/invocations";
    public static final String EVENTS_PATH = "weld-probe/events";
    public static final String OBSERVERS_PATH_ALL = "weld-probe/observers?pageSize=0";
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

        JsonReader jsonReader = Json.createReader(client.getPage(url.toString().concat(path)).getWebResponse().getContentAsStream());
        return jsonReader.readObject();
    }

    public static JsonObject getDeploymentByName(String path, String name, URL url) throws IOException {
        JsonObject deploymentJSON = getPageAsJSONObject(path, url);
        JsonArray deployments = deploymentJSON.getJsonArray(BDAS);
        JsonObject result = null;
        for (int i = 0; i < deployments.size(); i++) {
            String bdaId = deployments.getJsonObject(i).get(BDA_ID).toString();
            if (bdaId.contains(name)) {
                result = deployments.getJsonObject(i);
            }
        }
        return result;
    }

    private static String getBeanDetailUrl(String path, Class clazz, URL url) throws IOException {
        JsonObject beans = getPageAsJSONObject(path, url);
        JsonArray beansArray = beans.getJsonArray(DATA);
        String id = null;
        for (int i = 0; i < beansArray.size(); i++) {
            JsonObject bean = beansArray.getJsonObject(i);
            if (bean.getString(BEAN_CLASS).equals(clazz.getName())) {
                id = bean.getString(ID);
            }
        }

        if (id != null) {
            String beanDetailURL = BEANS_PATH.concat("/".concat(id));
            return beanDetailURL;
        } else {
            return null;
        }

    }

    public static JsonObject getBeanDetail(String path, Class clazz, URL url) throws IOException {
        String beanDetailUrl = getBeanDetailUrl(path, clazz, url);
        return getPageAsJSONObject(beanDetailUrl, url);
    }

    public static JsonObject getBeanInstanceDetail(String path, Class clazz, URL url, WebClient webClient, String... param) throws IOException {
        String beanDetailUrl = getBeanDetailUrl(path, clazz, url);
        for (int i = 0; i < param.length; i++) {
            beanDetailUrl = beanDetailUrl + "/instance/" + param[i];
        }
        return getPageAsJSONObject(beanDetailUrl, url, webClient);
    }

    public static List<JsonObject> getAllJsonObjectsByClass(Class clazz, JsonArray array) {
        List<JsonObject> result = new ArrayList<>();

        for (JsonValue jsonElement : array) {
            if (((JsonObject) jsonElement).getString(BEAN_CLASS).equals(clazz.getName())) {
                result.add(((JsonObject) jsonElement));
            }
        }
        return result;
    }

    public static enum BeanType {
        MANAGED,
        INTERCEPTOR,
        DECORATOR,
        PRODUCER_FIELD,
        PRODUCER_METHOD,
        EXTENSION,
        SESSION;
    }

    public static enum SessionBeanType {
        STATEFUL,
        STATELESS;
    }

}
