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
package org.jboss.weld.probe;

import java.io.IOException;
import java.util.Map;

import javax.enterprise.inject.Vetoed;

import org.jboss.weld.util.collections.ImmutableMap;

/**
 * Various string constants.
 *
 * @author Martin Kouba
 */
@Vetoed
public final class Strings {

    public static final String ID = "id";
    public static final String BDA_ID = "bdaId";
    public static final String BDA = "bda";
    public static final String BEAN_DISCOVERY_MODE = "beanDiscoveryMode";
    public static final String WARNING = "warning";
    public static final String EXCLUDE = "exclude";
    public static final String INCLUDE = "include";
    public static final String ACTIVATIONS = "activations";
    public static final String SCAN = "scan";
    public static final String BEANS_XML = "beansXml";
    public static final String TRIMMED = "trimmed";
    public static final String URL = "url";
    public static final String MARKER = "marker";
    public static final String BDAS = "bdas";
    public static final String CONFIGURATION = "configuration";
    public static final String CONFLICTS = "conflicts";
    public static final String SCOPE = "scope";
    public static final String TYPES = "types";
    public static final String TYPE = "type";
    public static final String QUALIFIER = "qualifier";
    public static final String QUALIFIERS = "qualifiers";
    public static final String NAME = "name";
    public static final String STEREOTYPES = "stereotypes";
    public static final String KIND = "kind";
    public static final String IS_ALTERNATIVE = "isAlternative";
    public static final String EJB_NAME = "ejbName";
    public static final String SESSION_BEAN_TYPE = "sessionBeanType";
    public static final String DEPENDENCIES = "dependencies";
    public static final String DEPENDENTS = "dependents";
    public static final String DECLARING_BEAN = "declaringBean";
    public static final String DISPOSAL_METHOD = "disposalMethod";
    public static final String PRODUCER_METHOD = "producerMethod";
    public static final String PRODUCER_FIELD = "producerField";
    public static final String PRODUCER_INFO = "producerInfo";
    public static final String BEAN_CLASS = "beanClass";
    public static final String BEAN_TYPE = "beanType";
    public static final String OBSERVED_TYPE = "observedType";
    public static final String RECEPTION = "reception";
    public static final String TX_PHASE = "txPhase";
    public static final String PRIORITY = "priority";
    public static final String PRIORITY_RANGE = "priorityRange";
    public static final String ANNOTATED_METHOD = "annotatedMethod";
    public static final String VALUE = "value";
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String PROPERTIES = "properties";
    public static final String INSTANCES = "instances";
    public static final String OBJECT_TO_STRING = "objectToString";
    public static final String AS_STRING = "asString";
    public static final String REQUIRED_TYPE = "requiredType";
    public static final String METHOD_NAME = "methodName";
    public static final String START = "start";
    public static final String TIME = "time";
    public static final String DECLARED_OBSERVERS = "declaredObservers";
    public static final String DECLARED_PRODUCERS = "declaredProducers";
    public static final String REMOVED_INVOCATIONS = "removedInvocations";
    public static final String CHILDREN = "children";
    public static final String INTERCEPTED_BEAN = "interceptedBean";
    public static final String DECLARING_CLASS = "declaringClass";
    public static final String ENABLEMENT = "enablement";
    public static final String INTERCEPTORS = "interceptors";
    public static final String DECORATORS = "decorators";
    public static final String ALTERNATIVES = "alternatives";
    public static final String ACCESSIBLE_BDAS = "accessibleBdas";
    public static final String BEANS = "beans";
    public static final String SEARCH = "search";
    public static final String TIMESTAMP = "ts";
    public static final String CONTEXTS = "contexts";
    public static final String CIDS = "cids";
    public static final String CONTAINER = "container";
    public static final String CONTAINER_EVENT = "containerEvent";
    public static final String EVENT_INFO = "eventInfo";
    public static final String OBSERVERS = "observers";
    public static final String METHOD = "method";
    public static final String CLASS = "class";
    public static final String APPLICATION = "application";
    public static final String CONTEXT_ID = "contextId";
    public static final String VERSION = "version";
    public static final String PROBE_COMPONENT = "probeComponent";
    public static final String DESCRIPTION = "description";
    public static final String INFO = "info";
    public static final String IS_POTENTIAL = "isPotential";
    public static final String IS_UNSATISFIED = "isUnsatisfied";
    public static final String IS_AMBIGUOUS = "isAmbiguous";
    public static final String BINDINGS = "bindings";
    public static final String DELEGATE_TYPE = "delegateType";
    public static final String DELEGATE_QUALIFIERS = "delegateQualifiers";
    public static final String DECORATED_TYPES = "decoratedTypes";
    public static final String CLASS_INTERCEPTOR_BINDINGS = "classInterceptorBindings";
    public static final String ASSOCIATED_TO = "associatedTo";
    public static final String REMOVED_EVENTS = "removedEvents";
    public static final String INIT_TS = "initTs";
    public static final String DASHBOARD = "dashboard";
    public static final String BOOSTRAP_STATS = "bootstrapStats";
    public static final String FIRED = "fired";
    public static final String INVOCATIONS = "invocations";
    public static final String RESOLVE = "resolve";
    public static final String HASH = "hash";
    public static final String UNUSED = "unused";
    public static final String INVERTED = "inverted";
    public static final String CLASS_AVAILABILITY = "classAvailability";
    public static final String SYS_PROPERTY = "sysProperty";

    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "pageSize";
    public static final String TOTAL = "total";
    public static final String LAST_PAGE = "lastPage";
    public static final String DATA = "data";
    public static final String FILTERS = "filters";
    public static final String REPRESENTATION = "representation";
    public static final String ERROR = "error";

    public static final String PARAM_TRANSIENT_DEPENDENCIES = "transientDependencies";
    public static final String PARAM_TRANSIENT_DEPENDENTS = "transientDependents";

    // Internet media types
    static final String APPLICATION_JSON = "application/json";
    static final String APPLICATION_JAVASCRIPT = "application/javascript";
    // otf, ttf fonts
    static final String APPLICATION_FONT_SFNT = "application/font-sfnt";
    static final String APPLICATION_FONT_WOFF = "application/font-woff";
    // eot
    static final String APPLICATION_FONT_MS = "application/vnd.ms-fontobject";
    static final String TEXT_JAVASCRIPT = "text/javascript";
    static final String TEXT_CSS = "text/css";
    static final String TEXT_HTML = "text/html";
    static final String TEXT_PLAIN = "text/plain";
    static final String IMG_PNG = "image/png";
    static final String IMG_SVG = "image/svg+xml";
    static final String IMG_ICO = " image/x-icon";

    static final String ENCODING_UTF8 = "UTF-8";

    static final String SUFFIX_HTML = "html";
    static final String SUFFIX_CSS = "css";
    static final String SUFFIX_JS = "js";
    static final String SUFFIX_PNG = "png";
    static final String SUFFIX_TTF = "ttf";
    static final String SUFFIX_OTF = "otf";
    static final String SUFFIX_EOT = "eot";
    static final String SUFFIX_SVG = "svg";
    static final String SUFFIX_WOFF = "woff";
    static final String SUFFIX_ICO = "ico";

    static final String SLASH = "/";
    static final String QUTATION_MARK = "\"";
    static final String EQUALS = "=";
    static final String COMMA = ",";
    static final String PARENTHESES_LEFT = "(";
    static final String PARENTHESES_RIGHT = ")";
    static final String CHEVRONS_LEFT = "<";
    static final String CHEVRONS_RIGHT = ">";
    static final String WILDCARD = "?";
    static final String WILDCARD_EXTENDS = WILDCARD + " extends";
    static final String WILDCARD_SUPER = WILDCARD + " super";
    static final String ARRAY = "[]";
    static final String AMPERSAND = "@";

    static final String FILE_CLIENT_HTML = "probe.html";
    static final String PATH_META_INF_CLIENT = "/META-INF/client/";

    static final String RESOURCE_PARAM_START = "{";
    static final String RESOURCE_PARAM_END = "}";

    static final String ADDITIONAL_BDA_SUFFIX = ".additionalClasses";
    static final String WEB_INF_CLASSES = "WEB-INF/classes";

    static final String GET_PREFIX = "get";
    static final String IS_PREFIX = "is";
    static final String SET_PREFIX = "set";

    static final String HTTP_HEADER_CACHE_CONTROL = "Cache-Control";

    static final String INFO_FETCHING_LAZILY = "Most likely lazy fetching - Instance.get()";

    static final String WARNING_UNRESTRICTED_PAT_OBSERVER = "BOOTSTRAP PERFORMANCE TIP: This observer method receives events for all annotated types! Consider restricting events using @WithAnnotations or a generic type with bounds.";

    static final String WARNING_CONFLICTING_ENABLEMENT = "WARNING - There is additional local enablement in: ";
    static final String EMPTY = "";

    private static final int ABR_MIN_LIMIT = 4;
    private static final String ABR_MARKER = "...";

    private static final Map<Character, String> REPLACEMENTS = ImmutableMap.<Character, String> builder().put('"', "&quot;").put('\'', "&#39;")
            .put('&', "&amp;").put('<', "&lt;").put('>', "&gt;").build();

    private Strings() {
    }

    static String abbreviate(String value, int limit) {
        if (limit < ABR_MIN_LIMIT) {
            throw new IllegalArgumentException("Minimum limit is 4");
        }
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit - ABR_MARKER.length()) + ABR_MARKER;
    }

    /**
     *
     * @param value
     * @return an escaped value
     */
    static String escape(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        for (int i = 0; i < value.length(); i++) {
            String replacement = REPLACEMENTS.get(value.charAt(i));
            if (replacement != null) {
                StringBuilder result = new StringBuilder();
                result.append(value.substring(0, i));
                result.append(replacement);
                try {
                    escapeNext(value, i, result);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                return result.toString();
            }
        }
        return value;
    }

    private static void escapeNext(String value, int index, Appendable appendable) throws IOException {
        int length = value.length();
        while (++index < length) {
            char c = value.charAt(index);
            String replacement = REPLACEMENTS.get(c);
            if (replacement != null) {
                appendable.append(replacement);
            } else {
                appendable.append(c);
            }
        }
    }


}
