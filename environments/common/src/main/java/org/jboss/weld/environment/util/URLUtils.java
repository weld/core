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
package org.jboss.weld.environment.util;

/**
 * Utils for working with URLs and URIs
 *
 * @author Jozef Hartinger
 *
 */
public class URLUtils {

    public static final String PROCOTOL_FILE = "file";
    public static final String PROCOTOL_JAR = "jar";
    public static final String PROCOTOL_JRT = "jrt";
    public static final String PROCOTOL_WAR = "war";
    public static final String PROCOTOL_HTTP = "http";
    public static final String PROCOTOL_HTTPS = "https";
    public static final String PROTOCOL_FILE_PART = PROCOTOL_FILE + ":";
    public static final String PROTOCOL_JRT_PART = PROCOTOL_JRT + ":/";
    public static final String PROTOCOL_WAR_PART = PROCOTOL_WAR + ":";
    // according to JarURLConnection api doc, the separator is "!/"
    public static final String JAR_URL_SEPARATOR = "!/";

    private URLUtils() {
    }
}
