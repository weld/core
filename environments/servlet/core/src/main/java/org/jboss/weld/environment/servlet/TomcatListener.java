/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet;

import org.jboss.weld.environment.servlet.deployment.URLScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Set;

/**
 * Tomcat listener.
 * Use this if deploying app as packed archive.
 *
 * @author Ales Justin
 */
public class TomcatListener extends Listener {
    private static final Logger log = LoggerFactory.getLogger(TomcatListener.class);

    protected URLScanner createUrlScanner(ClassLoader classLoader, ServletContext context) {
        return new TomcatScanner(classLoader);
    }

    private static class TomcatScanner extends URLScanner {
        private TomcatScanner(ClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        protected void handleURL(URL url, Set<String> classes, Set<URL> urls) {
            try {
                Object content = url.getContent();
                if (content instanceof DirContext) {
                    recurse((DirContext) content, classes, urls, "");
                } else {
                    log.warn("Cannot scan URL, content not javax.naming.Context instance.");
                }
            } catch (Exception e) {
                log.error("Cannot scan URL: " + url, e);
            }
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    protected static void recurse(DirContext context, Set<String> classes, Set<URL> urls, String prefix) throws Exception {
        if (prefix.length() > 0)
            prefix += ".";

        NamingEnumeration ne = context.listBindings("");
        while (ne.hasMoreElements()) {
            Binding next = (Binding) ne.nextElement();
            String name = prefix + next.getName();
            if (name.endsWith(".class")) {
                classes.add(name.substring(0, name.length() - 6));
                continue;
            }

            Object nextObject = next.getObject();
            if (nextObject instanceof DirContext) {
                recurse((DirContext) nextObject, classes, urls, name);
            }
        }
    }
}