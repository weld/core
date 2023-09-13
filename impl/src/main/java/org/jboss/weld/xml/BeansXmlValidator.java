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
package org.jboss.weld.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jboss.weld.logging.XmlLogger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This validator is thread safe and suitable for sharing between threads.
 *
 * @author Martin Kouba
 */
public class BeansXmlValidator implements ErrorHandler {

    private static final StreamSource[] EMPTY_SOURCE_ARRAY = new StreamSource[0];
    private static final String ROOT_ELEMENT_NAME = "beans";
    // See also http://www.w3.org/TR/2001/REC-xmlschema-1-20010502/#cvc-elt
    private static final String VALIDATION_ERROR_CODE_CVC_ELT_1 = "cvc-elt.1";

    private final Schema cdi11Schema;

    private final Schema cdi20Schema;

    public BeansXmlValidator() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        cdi11Schema = initSchema(factory, XmlSchema.CDI11_SCHEMAS);
        cdi20Schema = initSchema(factory, XmlSchema.CDI20_SCHEMAS);
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "False positive, see https://github.com/spotbugs/spotbugs/issues/259")
    public void validate(URL beansXml, ErrorHandler errorHandler) {
        if (beansXml == null) {
            throw XmlLogger.LOG.loadError("unknown", null);
        }
        if (errorHandler == null) {
            errorHandler = this;
        }
        Schema schema = cdi20Schema;

        // First quick check of beans.xml to find out version
        try (InputStream in = beansXml.openStream()) {
            if (in.available() == 0) {
                // The file is just acting as a marker file
                return;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, Charset.forName(StandardCharsets.UTF_8.name())))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(XmlSchema.CDI11.getFileName())) {
                        schema = cdi11Schema;
                        break;
                    } else if (line.contains(XmlSchema.CDI20.getFileName())) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw XmlLogger.LOG.loadError(beansXml, e);
        }

        if (schema == null) {
            return;
        }
        Validator validator = schema.newValidator();
        validator.setErrorHandler(errorHandler);
        try (InputStream in = beansXml.openStream()) {
            validator.validate(new StreamSource(in));
        } catch (SAXException | IOException e) {
            // No-op - validation is optional
        }
    }

    public void validate(URL beansXml) {
        validate(beansXml, this);
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        XmlLogger.LOG.xsdValidationWarning(e.getSystemId(), e.getLineNumber(), e.getMessage());
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        if (e.getMessage().startsWith(VALIDATION_ERROR_CODE_CVC_ELT_1) && e.getMessage().contains(ROOT_ELEMENT_NAME)) {
            // Ignore the errors we get when there is no schema defined
            return;
        }
        XmlLogger.LOG.xsdValidationError(e.getSystemId(), e.getLineNumber(), e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        throw e;
    }

    private static StreamSource[] loadXsds(XmlSchema[] schemas) {
        List<Source> xsds = new ArrayList<>();
        for (XmlSchema schema : schemas) {
            Source source = loadXsd(schema.getFileName(), schema.getClassLoader());
            if (source != null) {
                xsds.add(source);
            }
        }
        return xsds.toArray(EMPTY_SOURCE_ARRAY);
    }

    private static StreamSource loadXsd(String name, ClassLoader classLoader) {
        InputStream in = classLoader.getResourceAsStream(name);
        if (in == null) {
            return null;
        } else {
            return new StreamSource(in);
        }
    }

    private Schema initSchema(SchemaFactory factory, XmlSchema[] schemas) {
        StreamSource[] sources = null;
        try {
            sources = loadXsds(schemas);
            return factory.newSchema(sources);
        } catch (SAXException e) {
            XmlLogger.LOG.warnf("Error initializing schema from %s", Arrays.toString(schemas));
            return null;
        } finally {
            if (sources != null) {
                for (StreamSource source : sources) {
                    try {
                        source.getInputStream().close();
                    } catch (IOException e) {
                        XmlLogger.LOG.warn("Error closing schema resource", e);
                    }
                }
            }
        }
    }

}
