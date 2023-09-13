/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.cid;

import org.jboss.weld.module.jsf.FacesUrlTransformer;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Simple unit test to check conversation ID parameter detection/addition
 *
 * @see WELD-2512
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class CidParamMatchingTest {

    @Test
    public void testPatternMatching() {
        String cidParamName = "cid";
        String cidValue = "1";
        String appendedString = "cid=" + cidValue;

        Assert.assertTrue(createTransformer("localhost:8080")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertTrue(createTransformer("localhost:8080?myparam=foo&bar=true&myowncid=5")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertTrue(createTransformer("localhost:8080?myParam=foo&bar=true")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertFalse(createTransformer("localhost:8080?myparam=foo&bar=true&cid=2")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertFalse(createTransformer("localhost:8080?cid=5&myparam=foo&bar=true")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertFalse(createTransformer("localhost:8080?myparam=foo;bar=true;cid=5")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
        Assert.assertTrue(createTransformer("localhost:8080?myowncid=5;myparam=foo;bar=true")
                .appendConversationIdIfNecessary(cidParamName, cidValue).getUrl().contains(appendedString));
    }

    private FacesUrlTransformer createTransformer(String url) {
        return new FacesUrlTransformer(url, null);
    }
}
