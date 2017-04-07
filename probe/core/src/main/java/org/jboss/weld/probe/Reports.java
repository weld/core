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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.AccessController;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.Description;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.HtmlTag.SafeString;
import org.jboss.weld.security.GetSystemPropertyAction;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 */
class Reports {

    private static final String VALIDATION_REPORT_FILE_NAME = "weld-validation-report.html";

    private static final String TITLE = "Weld - Validation Report";
    private static final String TITLE_EXCEPTION = "Validation Issue";
    private static final String TITLE_BDAS = "Deployment - Bean Archives";
    private static final String TITLE_DEPS = "Dependency Issues";
    private static final String TITLE_CONFIG = "Weld Configuration - Modified Values";
    private static final String TITLE_BEANS = "Enabled Beans";

    private static final String EXCEPTION = "exception";
    private static final String BDAS = "bdas";
    private static final String DEPS = "deps";
    private static final String CONFIG = "config";
    private static final String BEANS = "beans";

    private Reports() {
    }

    static void generateValidationReport(Probe probe, Exception exception, Environment environment, BeanManagerImpl manager) {

        HtmlTag html = HtmlTag.html();
        HtmlTag head = HtmlTag.head().appendTo(html);
        head.add(HtmlTag.title(TITLE));
        head.add(HtmlTag.style().add(SafeString.of(IOUtils.getResourceAsString("/report.css"))));

        HtmlTag body = HtmlTag.body().appendTo(html);

        body.add(HtmlTag.h1(TITLE));
        HtmlTag meta = HtmlTag.stripedTable().appendTo(body);
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Generated at:")), HtmlTag.td(LocalDateTime.now().toString())));
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Weld Version:")), HtmlTag.td(Formats.getSimpleVersion())));
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Weld Environment:")), HtmlTag.td(environment.toString())));
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Java Version:")),
                HtmlTag.td(AccessController.doPrivileged(new GetSystemPropertyAction("java.version")))));
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Java Vendor:")),
                HtmlTag.td(AccessController.doPrivileged(new GetSystemPropertyAction("java.vendor")))));
        meta.add(HtmlTag.tr().add(HtmlTag.td().add(HtmlTag.strong("Operating System:")),
                HtmlTag.td(AccessController.doPrivileged(new GetSystemPropertyAction("os.name")))));

        HtmlTag contents = HtmlTag.ol().appendTo(body);
        contents.add(HtmlTag.li().add(HtmlTag.a("#" + EXCEPTION).add(TITLE_EXCEPTION)));
        contents.add(HtmlTag.li().add(HtmlTag.a("#" + BDAS).add(TITLE_BDAS)));
        contents.add(HtmlTag.li().add(HtmlTag.a("#" + DEPS).add(TITLE_DEPS)));
        contents.add(HtmlTag.li().add(HtmlTag.a("#" + BEANS).add(TITLE_BEANS)));
        contents.add(HtmlTag.li().add(HtmlTag.a("#" + CONFIG).add(TITLE_CONFIG)));

        body.add(HtmlTag.aname(EXCEPTION));
        body.add(HtmlTag.h2(TITLE_EXCEPTION));
        body.add(HtmlTag.p(exception.getMessage()).attr(HtmlTag.STYLE, "font-size: large;color: red;font-weight:bold;"));
        body.add(HtmlTag.h(3, "Exception Stack:"));
        final StringWriter stackWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackWriter));
        body.add(HtmlTag.div(EXCEPTION).add(HtmlTag.pre(stackWriter.toString())));

        Map<BeanDeploymentArchive, BeanManagerImpl> beanDeploymentArchivesMap = Container.instance(manager).beanDeploymentArchives();
        List<BeanDeploymentArchive> bdas = new ArrayList<BeanDeploymentArchive>(beanDeploymentArchivesMap.keySet());
        Collections.sort(bdas, probe.getBdaComparator());

        addBeanArchives(body, bdas);
        addInvalidDependencies(probe, body);
        addBeans(probe, body, bdas);
        addConfiguration(body, manager);

        String export = manager.getServices().get(WeldConfiguration.class).getStringProperty(ConfigurationKey.PROBE_EXPORT_DATA_AFTER_DEPLOYMENT);
        File exportPath;
        if (!export.isEmpty()) {
            exportPath = new File(export);
        } else {
            exportPath = new File(System.getProperty("user.dir"));
        }
        if (!exportPath.canWrite()) {
            ProbeLogger.LOG.invalidExportPath(exportPath);
            return;
        }
        try {
            File exportFile = new File(exportPath, VALIDATION_REPORT_FILE_NAME);
            Files.write(exportFile.toPath(), html.toString().getBytes(Charset.forName("UTF-8")));
            ProbeLogger.LOG.validationReportExported("file://" + exportFile.getAbsolutePath());

        } catch (IOException e) {
            ProbeLogger.LOG.unableToExportData(exportPath, e.getCause() != null ? e.getCause() : e);
            ProbeLogger.LOG.catchingTrace(e);
        }
    }

    private static void addInvalidDependencies(Probe probe, HtmlTag body) {

        body.add(HtmlTag.aname(DEPS));
        body.add(HtmlTag.h2(TITLE_DEPS));

        HtmlTag table = HtmlTag.stripedTable().appendTo(HtmlTag.div(DEPS).appendTo(body));
        HtmlTag.tr().add(HtmlTag.th(""), HtmlTag.th("Declaring Bean"), HtmlTag.th("Problem")).appendTo(table);
        int idx = 0;

        for (Bean<?> bean : probe.getBeans()) {

            final BeanManagerImpl beanManager = probe.getBeanManager(bean);
            // Don't process built-in beans
            if (beanManager == null) {
                continue;
            }

            Set<InjectionPoint> injectionPoints = bean.getInjectionPoints();
            if (injectionPoints != null && !injectionPoints.isEmpty()) {
                for (InjectionPoint injectionPoint : injectionPoints) {
                    if (injectionPoint.isDelegate()) {
                        // Do not process delegate injection points
                        continue;
                    }

                    Set<Bean<?>> beans = beanManager.getBeans(injectionPoint.getType(),
                            injectionPoint.getQualifiers().toArray(new Annotation[injectionPoint.getQualifiers().size()]));
                    if (beans.isEmpty()) {
                        // Unsatisfied
                        HtmlTag.tr().add(HtmlTag.td(++idx + "."), HtmlTag.td(bean.toString()).add(HtmlTag.td("Unsatisfied dependency at " + injectionPoint)))
                                .appendTo(table);
                        try {
                            beanManager.resolve(beans);
                        } catch (AmbiguousResolutionException e) {
                            // Ambiguous
                            HtmlTag.tr().add(HtmlTag.td(++idx + "."), HtmlTag.td(bean.toString()).add(HtmlTag.td("Ambiguous dependency at " + injectionPoint)))
                                    .appendTo(table);
                        }
                    }
                }
            }
        }
    }

    private static void addBeanArchives(HtmlTag body, List<BeanDeploymentArchive> bdas) {

        body.add(HtmlTag.aname(BDAS));
        body.add(HtmlTag.h2(TITLE_BDAS));

        HtmlTag table = HtmlTag.stripedTable().appendTo(HtmlTag.div(BDAS).appendTo(body));
        HtmlTag.tr().add(HtmlTag.th(""), HtmlTag.th("Identifier"), HtmlTag.th("Bean Discovery Mode"), HtmlTag.th("beans.xml")).appendTo(table);
        int idx = 0;

        for (BeanDeploymentArchive bda : bdas) {
            HtmlTag tr = HtmlTag.tr().add(HtmlTag.td(++idx + "."), HtmlTag.td(bda.getId())).appendTo(table);

            BeansXml beansXml = bda.getBeansXml();
            tr.add(HtmlTag.td(beansXml != null ? beansXml.getBeanDiscoveryMode().toString() : BeanDiscoveryMode.ANNOTATED.toString()));

            if (beansXml != null && !beansXml.equals(BeansXml.EMPTY_BEANS_XML)) {
                HtmlTag.div().attr(HtmlTag.TITLE, beansXml.getUrl() != null ? beansXml.getUrl().toString() : "URL not available")
                        .add(beansXml.getVersion() != null ? beansXml.getVersion() : "Version not defined").appendTo(HtmlTag.td().appendTo(tr));
            } else {
                tr.add(HtmlTag.td("No beans.xml"));
            }
        }

    }

    private static void addConfiguration(HtmlTag body, BeanManagerImpl manager) {

        body.add(HtmlTag.aname(CONFIG));
        body.add(HtmlTag.h2(TITLE_CONFIG));

        HtmlTag table = HtmlTag.stripedTable().appendTo(HtmlTag.div(CONFIG).appendTo(body));
        HtmlTag.tr().add(HtmlTag.th(""), HtmlTag.th("Key"), HtmlTag.th("Default Value"), HtmlTag.th("Value"), HtmlTag.th("Description")).appendTo(table);
        int idx = 0;

        WeldConfiguration configuration = manager.getServices().get(WeldConfiguration.class);
        for (ConfigurationKey key : getSortedConfigurationKeys()) {
            Object defaultValue = key.getDefaultValue();
            Object value = getValue(key, configuration);
            if (value == null) {
                // Unsupported property type
                continue;
            }
            if (!defaultValue.equals(value)) {
                String desc = getDesc(key);
                table.add(HtmlTag.tr().add(HtmlTag.td(++idx + "."), HtmlTag.td(key.get()), HtmlTag.td(defaultValue.toString()), HtmlTag.td(value.toString()),
                        HtmlTag.td().add(desc != null ? SafeString.of(desc) : "")));
            }
        }
    }

    private static void addBeans(Probe probe, HtmlTag body, List<BeanDeploymentArchive> bdas) {

        body.add(HtmlTag.aname(BEANS));
        body.add(HtmlTag.h2(TITLE_BEANS));

        HtmlTag table = HtmlTag.stripedTable().appendTo(HtmlTag.div(BEANS).appendTo(body));
        HtmlTag.tr().add(HtmlTag.th(""), HtmlTag.th("Archive"), HtmlTag.th("Kind"), HtmlTag.th("Bean Class"), HtmlTag.th("Types"), HtmlTag.th("Qualifiers"),
                HtmlTag.th("Scope")).appendTo(table);
        int idx = 0;

        List<Bean<?>> beans = probe.getBeans();

        for (int i = 0; i < bdas.size(); i++) {
            String bdaId = bdas.get(i).getId();
            for (Bean<?> bean : beans) {
                final BeanManagerImpl beanManager = probe.getBeanManager(bean);
                // Don't process built-in beans
                if (beanManager == null) {
                    continue;
                }
                if (bdaId.equals(beanManager.getId())) {

                    HtmlTag types = HtmlTag.td();
                    for (Iterator<Type> iterator = JsonObjects.sortTypes(bean.getTypes()).iterator(); iterator.hasNext();) {
                        // Omit java.lang.Object
                        Type type = iterator.next();
                        String formatted = Strings.escape(Formats.formatType(type, false));
                        if (!Object.class.equals(type)) {
                            types.add(HtmlTag.div().attr(HtmlTag.TITLE, formatted).add(abbreviateType(formatted)));
                            if (iterator.hasNext()) {
                                types.add(HtmlTag.BR);
                            }
                        }
                    }

                    HtmlTag qualifiers = HtmlTag.td();
                    if (bean.getQualifiers() != null && !bean.getQualifiers().isEmpty()) {
                        for (Iterator<Annotation> iterator = bean.getQualifiers().iterator(); iterator.hasNext();) {
                            Annotation qualifier = iterator.next();
                            if (Any.class.equals(qualifier.annotationType())) {
                                // Omit javax.enterprise.inject.Any
                                continue;
                            } else if (Default.class.equals(qualifier.annotationType())) {
                                qualifiers.add(JsonObjects.simplifiedAnnotation(qualifier));
                            } else {
                                qualifiers.add(HtmlTag.div().attr(HtmlTag.TITLE, qualifier.toString()).add(abbreviateAnnotation(qualifier.toString())));
                            }
                            if (iterator.hasNext()) {
                                qualifiers.add(HtmlTag.BR);
                            }
                        }
                    }
                    String beanClass = Formats.formatType(bean.getBeanClass(), false);
                    HtmlTag.tr()
                            .add(HtmlTag.td(++idx + "."), HtmlTag.td().add(HtmlTag.a("#" + BDAS).add(" " + (i + 1))),
                                    HtmlTag.td(BeanKind.from(bean).toString()),
                                    HtmlTag.td().add(HtmlTag.div().attr(HtmlTag.TITLE, beanClass).add(abbreviateType(beanClass))), types, qualifiers,
                                    HtmlTag.td(JsonObjects.simplifiedScope(bean.getScope())))
                            .appendTo(table);
                }
            }
        }

    }

    static List<ConfigurationKey> getSortedConfigurationKeys() {
        List<ConfigurationKey> configurationKeys = new ArrayList<>();
        Collections.addAll(configurationKeys, ConfigurationKey.values());
        Collections.sort(configurationKeys, new Comparator<ConfigurationKey>() {
            @Override
            public int compare(ConfigurationKey o1, ConfigurationKey o2) {
                return o1.get().compareTo(o2.get());
            }
        });
        return configurationKeys;
    }

    static Object getValue(ConfigurationKey key, WeldConfiguration configuration) {
        Object defaultValue = key.getDefaultValue();
        Object value = null;
        if (defaultValue instanceof Boolean) {
            value = configuration.getBooleanProperty(key);
        } else if (defaultValue instanceof Long) {
            value = configuration.getLongProperty(key);
        } else if (defaultValue instanceof Integer) {
            value = configuration.getIntegerProperty(key);
        } else if (defaultValue instanceof String) {
            value = configuration.getStringProperty(key);
        }
        return value;
    }

    static String getDesc(ConfigurationKey key) {
        try {
            Field field = ConfigurationKey.class.getDeclaredField(key.toString());
            if (field != null && field.isEnumConstant()) {
                Description description = field.getAnnotation(Description.class);
                if (description == null) {
                    // Don't show config options without description
                    return null;
                }
                return description.value();
            }
        } catch (NoSuchFieldException | SecurityException | NullPointerException ignored) {
        }
        return null;
    }

    private static String abbreviateType(String type) {
        StringBuilder builder = new StringBuilder();
        String[] parts = type.split("\\.");
        for (int i = 0; i < parts.length; i++) {
            if (i == (parts.length - 1)) {
                builder.append(parts[i]);
            } else {
                builder.append(parts[i].charAt(0));
                builder.append(".");
            }
        }
        return builder.toString();
    }

    private static String abbreviateAnnotation(String annotation) {
        if (annotation.indexOf('(') != -1) {
            annotation = annotation.substring(1, annotation.indexOf('('));
        } else {
            annotation = annotation.substring(1);
        }
        return "@" + abbreviateType(annotation);
    }

}
