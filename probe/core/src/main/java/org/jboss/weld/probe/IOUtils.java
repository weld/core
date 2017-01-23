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
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.ENCODING_UTF8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.enterprise.inject.Vetoed;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
final class IOUtils {

    private static final int DEFAULT_BUFFER = 1024 * 8;

    private IOUtils() {
    }

    static String getResourceAsString(String resourceName) {
        StringWriter writer = new StringWriter();
        BufferedReader reader = null;
        try {
            InputStream in = Resource.class.getResourceAsStream(resourceName);
            if (in == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(in, ENCODING_UTF8));
            final char[] buffer = new char[DEFAULT_BUFFER];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            writer.flush();
            return writer.toString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    static boolean writeResource(String resourceName, OutputStream out) {
        InputStream in = Resource.class.getResourceAsStream(resourceName);
        if (in == null) {
            return false;
        }
        try {
            final byte[] buffer = new byte[DEFAULT_BUFFER];
            int n = 0;
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }


}
