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

import static junit.framework.Assert.assertTrue;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.gargoylesoftware.htmlunit.WebClient;

public class ProbeIntegrationTest {

    /**
     * This method iterates over input array. If the item is array as well then step to recursive check.
     * If the item is JsonObject then check its key set and try to find expected value. If the value is not found continue recursively.
     * If the item is not jsonArray nor jsonObject then cast to String and check expected value.
     *
     * @param expectedValue - checked value
     * @param key           - Json key where the value is expected
     * @param jsonArray     - array for recursive check
     * @param found         - result has to be passed across recursive invocations
     * @return true if the expected value was found with given key
     */
    protected boolean checkStringInArrayRecursively(String expectedValue, String key, JsonArray jsonArray, boolean found) {

        for (int i = 0; i < jsonArray.size(); i++) {
            String arrayItem = jsonArray.get(i).toString();
            // we have an JSONArray
            if (arrayItem.startsWith("[")) {
                found = checkStringInArrayRecursively(expectedValue, key, jsonArray.getJsonArray(i), found);
                // we have an JSONObject
            } else if (arrayItem.startsWith("{")) {
                JsonObject jsonObject = jsonArray.getJsonObject(i);
                if (jsonObject.containsKey(key)) {
                    found = jsonObject.get(key).toString().contains(expectedValue);
                    if (found) {
                        return found;
                    }
                } else {
                    Set<Map.Entry<String, JsonValue>> entries = jsonArray.getJsonObject(i).entrySet();
                    for (Map.Entry<String, JsonValue> entry : entries) {
                        if(entry.getValue().getValueType().equals(JsonValue.ValueType.OBJECT)){
                            JsonObject nested = ((JsonObject) entry.getValue());
                            if(nested.containsKey(key)){
                                found = nested.get(key).toString().contains(expectedValue);
                            }
                        }
                    }
                }
                // we have some simple object
            } else {
                if (jsonArray.get(i).toString().contains(expectedValue)) {
                    return found = true;
                }
            }
        }
        return found;
    }

    protected void assertBeanClassVisibleInProbe(Class clazz, JsonArray jsonArray) {
        assertTrue("Cannot find class " + clazz.getName() + " in Probe beans list.",
                checkStringInArrayRecursively(clazz.getName(), BEAN_CLASS, jsonArray, false));
    }

    protected WebClient invokeSimpleAction(URL url) throws IOException {
        WebClient client = new WebClient();
        client.getPage(url.toString() + "/test");
        return client;
    }
}
