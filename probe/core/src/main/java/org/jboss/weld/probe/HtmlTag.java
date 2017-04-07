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

import static org.jboss.weld.probe.Strings.CHEVRONS_LEFT;
import static org.jboss.weld.probe.Strings.CHEVRONS_RIGHT;
import static org.jboss.weld.probe.Strings.EQUALS;
import static org.jboss.weld.probe.Strings.ID;
import static org.jboss.weld.probe.Strings.QUTATION_MARK;
import static org.jboss.weld.probe.Strings.SLASH;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A very simple HTML tag builder.
 *
 * @author Martin Kouba
 */
class HtmlTag {

    static final String HTML = "html";
    static final String BODY = "body";
    static final String TITLE = "title";
    static final String H = "h";
    static final String P = "p";
    static final String PRE = "pre";
    static final String DIV = "div";
    static final String TABLE = "table";
    static final String TR = "tr";
    static final String TD = "td";
    static final String TH = "th";
    static final String STYLE = "style";
    static final String HEAD = "head";
    static final String A = "a";
    static final String OL = "ol";
    static final String LI = "li";
    static final String STRONG = "strong";
    static final SafeString BR = SafeString.of("<br>");
    static final String CLASS = "class";

    static HtmlTag of(String name) {
        return new HtmlTag(name);
    }

    static HtmlTag html() {
        return of(HTML);
    }

    static HtmlTag head() {
        return of(HEAD);
    }

    static HtmlTag style() {
        return of(STYLE);
    }

    static HtmlTag body() {
        return of(BODY);
    }

    static HtmlTag title(String value) {
        return of(TITLE).add(value);
    }

    static HtmlTag h1(String value) {
        return h(1, value);
    }

    static HtmlTag h2(String value) {
        return h(2, value);
    }

    static HtmlTag h(int level, String value) {
        return of(H + level).add(value);
    }

    static HtmlTag p(String value) {
        return of(P).add(value);
    }

    static HtmlTag pre(String value) {
        return of(PRE).add(value);
    }

    static HtmlTag div(String id) {
        return div().attr(ID, id);
    }

    static HtmlTag div() {
        return of(DIV);
    }

    static HtmlTag table() {
        return of(TABLE);
    }

    static HtmlTag stripedTable() {
        return of(TABLE).attr(CLASS, "table-striped");
    }

    static HtmlTag tr() {
        return of(TR);
    }

    static HtmlTag th(String value) {
        return of(TH).add(value);
    }

    static HtmlTag td() {
        return of(TD);
    }

    static HtmlTag td(String value) {
        return td().add(value);
    }

    static HtmlTag a(String href) {
        return of(A).attr("href", href);
    }

    static HtmlTag aname(String name) {
        return of(A).attr(Strings.NAME, name);
    }

    static HtmlTag ol() {
        return of(OL);
    }

    static HtmlTag li() {
        return of(LI);
    }

    static HtmlTag strong(String text) {
        return of(STRONG).add(text);
    }

    private final String name;

    private Map<String, String> attrs;

    private final List<Object> contents;

    private HtmlTag(String name) {
        this.name = name;
        this.contents = new LinkedList<>();
    }

    /**
     * Attribute name and value are not escaped or modified in any way.
     *
     * @param name
     * @param value
     * @return self
     */
    HtmlTag attr(String name, String value) {
        if (attrs == null) {
            attrs = new HashMap<>();
        }
        attrs.put(name, value);
        return this;
    }

    /**
     * Add the contents.
     * <p>
     * {@link Object#toString()} is used when generating the string representation of the tag. The value is escaped using {@link Strings#escape(String)} for
     * instances which are neither {@link HtmlTag} nor {@link SafeString}.
     *
     * @param content
     * @return self
     */
    HtmlTag add(Object... contents) {
        for (Object content : contents) {
            this.contents.add(content);
        }
        return this;
    }

    /**
     * Append this tag to the contents of the given parent tag.
     *
     * @param parent
     * @return self
     */
    HtmlTag appendTo(HtmlTag parent) {
        parent.add(this);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(CHEVRONS_LEFT);
        builder.append(name);
        if (attrs != null && !attrs.isEmpty()) {
            for (Entry<String, String> attr : attrs.entrySet()) {
                builder.append(" ");
                builder.append(attr.getKey());
                builder.append(EQUALS);
                builder.append(QUTATION_MARK);
                builder.append(attr.getValue());
                builder.append(QUTATION_MARK);
            }
        }
        builder.append(CHEVRONS_RIGHT);
        for (Object content : contents) {
            if (content instanceof HtmlTag || content instanceof SafeString) {
                builder.append(content.toString());
            } else {
                builder.append(Strings.escape(content.toString()));
            }
        }
        builder.append(CHEVRONS_LEFT);
        builder.append(SLASH);
        builder.append(name);
        builder.append(CHEVRONS_RIGHT);
        return builder.toString();
    }

    static class SafeString {

        static SafeString of(String value) {
            return new SafeString(value);
        }

        private final String value;

        private SafeString(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }

}