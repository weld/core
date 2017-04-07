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
package org.jboss.weld.probe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HtmlTagTest {

    @Test
    public void testHtmlTag() {
        assertEquals("<html><h1>Foo</h1><div id=\"bla\" class=\"blabla\">Hello!</div></html>",
                HtmlTag.html().add(HtmlTag.h1("Foo")).add(HtmlTag.div("bla").attr("class", "blabla").add("Hello!")).toString());
        HtmlTag html = HtmlTag.html();
        assertEquals("<div>Hello!</div>", HtmlTag.div().add("Hello!").appendTo(html).toString());
        assertEquals("<html><div>Hello!</div></html>", html.toString());
    }

    @Test
    public void testEscaping() {
        assertEquals("<div name=\"&value\"\"></div>", HtmlTag.div().attr("name", "&value\"").toString());
        assertEquals("<div>&lt;&amp;value&quot;&gt;<p>&amp;</p></div>", HtmlTag.div().add("<&value\">", HtmlTag.p("&")).toString());
    }

}
