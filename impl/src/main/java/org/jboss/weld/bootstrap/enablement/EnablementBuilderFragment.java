/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.enablement;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.ERROR_LOADING_BEANS_XML_ENTRY;
import static org.jboss.weld.logging.messages.BootstrapMessage.PRIORITY_OUTSIDE_OF_RECOMMENDED_RANGE;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_NOT_CLASS;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE;
import static org.jboss.weld.logging.messages.ValidatorMessage.ENABLED_FLAG_USED_WITHOUT_PRIORITY_SET;
import static org.jboss.weld.logging.messages.ValidatorMessage.NO_GLOBALLY_ENABLED_CLASS_MATCHING_LOCAL_DISABLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.EnabledClass;
import org.jboss.weld.bootstrap.spi.EnabledStereotype;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.ValidatorMessage;
import org.jboss.weld.metadata.MetadataImpl;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.collections.DefaultValueMap;
import org.slf4j.cal10n.LocLogger;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

/**
 * Holds information about globally and locally enabled classes of a given kind (either interceptors, decorators or alternatives)
 *
 * @author Jozef Hartinger
 *
 */
class EnablementBuilderFragment {

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);
    private static final int LOCAL_RECORD_FIRST_ITEM_PRIORITY = 1000;
    private static final int LOCAL_RECORD_PRIORITY_STEP = 10;

    private final ValidatorMessage duplicateRecordMessage;
    private final ValidatorMessage globallyEnabledItemNotInBeanArchiveMessage;

    /*
     * <class priority="100">org.mycompany.myfwk.TimeStampLogger</class>
     */
    private final Map<String, GlobalEnablementRecord> globallyEnabledRecords;
    /*
     * <class enabled="false" priority="100">org.mycompany.myfwk.TimeStampLogger</class>
     */
    private final Map<String, GlobalEnablementRecord> globallySetPriorities;

    // classes enabled locally for a given bean archive
    private final Map<BeanDeployment, LocalOverrides> localOverrides;

    EnablementBuilderFragment(ValidatorMessage duplicateRecordMessage, ValidatorMessage globallyEnabledItemNotInBeanArchiveMessage) {
        this.duplicateRecordMessage = duplicateRecordMessage;
        this.globallyEnabledItemNotInBeanArchiveMessage = globallyEnabledItemNotInBeanArchiveMessage;
        this.globallyEnabledRecords = new HashMap<String, GlobalEnablementRecord>();
        this.globallySetPriorities = new HashMap<String, GlobalEnablementRecord>();
        this.localOverrides = DefaultValueMap.hashMapWithDefaultValue(LocalOverrides.SUPPLIER);
    }

    protected void processGlobalRecords(BeanDeployment deployment, List<Metadata<EnabledClass>> records) {
        Map<String, Metadata<EnabledClass>> duplicateCheck = new HashMap<String, Metadata<EnabledClass>>();
        Collection<String> beanArchiveClasses = deployment.getBeanDeploymentArchive().getBeanClasses();

        for (Metadata<EnabledClass> item : records) {
            EnabledClass record = item.getValue();
            Metadata<EnabledClass> duplicateItem = duplicateCheck.put(record.getValue(), item);
            if (duplicateItem != null) {
                throw new DeploymentException(duplicateRecordMessage, record.getValue(), duplicateItem.getLocation());
            }
            if (record.getPriority() != null) {
                // load priority
                int priority = record.getPriority();
                validatePriority(priority, item.getLocation());
                // load class
                Class<?> enabledClass = loadClass(item, deployment.getBeanDeployer()
                        .getResourceLoader());
                boolean enabledClassFoundWithinArchive = beanArchiveClasses.contains(item.getValue().getValue());
                if (!enabledClassFoundWithinArchive) {
                    log.warn(globallyEnabledItemNotInBeanArchiveMessage, item.getValue(), deployment.getBeanDeploymentArchive());
                }
                if (record.isEnabled() == null || record.isEnabled().equals(true)) {
                    // this is a global enabler
                    // TODO check for duplicates on global level
                    globallyEnabledRecords.put(record.getValue(),
                            new GlobalEnablementRecord(item.getLocation(), enabledClass, priority, deployment.getBeanDeploymentArchive()));
                } else {
                    // this is a global priority setter
                    // TODO check for duplicates on global level
                    globallySetPriorities.put(record.getValue(),
                            new GlobalEnablementRecord(item.getLocation(), enabledClass, priority, deployment.getBeanDeploymentArchive()));
                }
            }
        }
    }

    protected void processLocalRecords(BeanDeployment deployment, List<Metadata<EnabledClass>> records) {
        for (Metadata<EnabledClass> item : records) {
            EnabledClass record = item.getValue();
            if (record.getPriority() == null && record.isEnabled() != null) {
                Class<?> enabledClass = loadClass(item, deployment.getBeanDeployer()
                        .getResourceLoader());
                if (record.isEnabled().equals(true)) {
                    // this is a local enabler of a disabled global record with priority set
                    LocalEnablementRecord enabler = new LocalEnablementRecord(item.getLocation(), enabledClass);
                    GlobalEnablementRecord disabledRecordWithPriority = globallySetPriorities.get(record.getValue());
                    if (disabledRecordWithPriority == null) {
                        throw new DeploymentException(ENABLED_FLAG_USED_WITHOUT_PRIORITY_SET, record.getValue(), item.getLocation());
                    }
                    localOverrides.get(deployment).locallyEnabledRecordsWithDefaultPriority.put(enabler,
                            disabledRecordWithPriority);
                } else {
                    // this is a local disabler of a globally enabled record
                    LocalEnablementRecord disabler = new LocalEnablementRecord(item.getLocation(), enabledClass);
                    GlobalEnablementRecord disabledGlobalRecord = globallyEnabledRecords.get(record.getValue());
                    if (disabledGlobalRecord == null) {
                        log.warn(NO_GLOBALLY_ENABLED_CLASS_MATCHING_LOCAL_DISABLE, record.getValue(), item.getLocation());
                        continue;
                    }
                    localOverrides.get(deployment).locallyDisabledRecords.put(disabledGlobalRecord, disabler);
                }
            }
        }
    }

    public void processLegacyRecords(BeanDeployment deployment, List<Metadata<EnabledClass>> records) {
        Map<String, LegacyEnablementRecord> legacyRecords = localOverrides.get(deployment).legacyRecords;
        int priority = LOCAL_RECORD_FIRST_ITEM_PRIORITY;
        for (Metadata<EnabledClass> item : records) {
            EnabledClass record = item.getValue();
            if (record.getPriority() == null && record.isEnabled() == null) {
                Class<?> enabledClass = loadClass(item, deployment.getBeanDeployer()
                        .getResourceLoader());
                legacyRecords.put(enabledClass.getName(), new LegacyEnablementRecord(item.getLocation(), enabledClass, priority));
                priority += LOCAL_RECORD_PRIORITY_STEP;
            }
        }
    }

    protected void validatePriority(int priority, String location) {
        if (priority < 0 || priority > 3099) {
            log.warn(PRIORITY_OUTSIDE_OF_RECOMMENDED_RANGE, priority, location);
        }
    }

    protected Class<?> loadClass(Metadata<EnabledClass> enabledClassMetadata, ResourceLoader loader) {
        Class<?> clazz = null;
        try {
            clazz = loader.classForName(enabledClassMetadata.getValue().getValue());
        } catch (ResourceLoadingException e) {
            throw new DeploymentException(ERROR_LOADING_BEANS_XML_ENTRY, e.getCause(), enabledClassMetadata.getValue().getValue(), enabledClassMetadata.getLocation());
        } catch (Exception e) {
            throw new DeploymentException(ERROR_LOADING_BEANS_XML_ENTRY, e, enabledClassMetadata.getValue().getValue(), enabledClassMetadata.getLocation());
        }
        if (enabledClassMetadata.getValue() instanceof EnabledStereotype) {
            if (!clazz.isAnnotation()) {
                throw new DeploymentException(ALTERNATIVE_STEREOTYPE_NOT_STEREOTYPE, clazz);
            }
        } else {
            if (clazz.isAnnotation() || clazz.isInterface() || clazz.isEnum()) {
                throw new DeploymentException(ALTERNATIVE_BEAN_CLASS_NOT_CLASS, clazz);
            }
        }
        return clazz;
    }

    /**
     * Returns a list of classes representing total ordering in the given bean archive. The bean archive is identified
     * {@link BeanDeployment} object. The resulting list is mutable and may be modified later in the deployment.
     */
    public List<Metadata<Class<?>>> create(BeanDeployment deployment) {
        LocalOverrides overrides = localOverrides.get(deployment);
        List<EnablementRecordWithPriority> localRecords = new ArrayList<EnablementRecordWithPriority>();

        // add all globally enabled records
        for (GlobalEnablementRecord globallyEnabledRecord : globallyEnabledRecords.values()) {
            if (overrides.legacyRecords.containsKey(globallyEnabledRecord.getEnabledClass().getName())) {
                // this globally enabled record is overridden by a legacy-style record
                continue;
            }
            if (overrides.locallyDisabledRecords.containsKey(globallyEnabledRecord)) {
                // this globally enabled record is disabled in this module
                continue;
            }
            if (deployment.getBeanDeploymentArchive().getBeanDeploymentArchives().contains(globallyEnabledRecord.getArchive())) {
                // only apply this global enablement if the BDA that defines it is accessible from the current BDA
                localRecords.add(globallyEnabledRecord);
            }
        }
        // add locally enabled records that reference a global record that is not globally enabled but has default priority
        localRecords.addAll(overrides.locallyEnabledRecordsWithDefaultPriority.values());

        // add legacy enabled records
        localRecords.addAll(overrides.legacyRecords.values());

        Collections.sort(localRecords);
        return new ArrayList<Metadata<Class<?>>>(Lists.transform(localRecords, EnablementRecordWithPriorityToClassMetadataFunction.INSTANCE));
    }

    /**
     * Holds information about local enablements affecting a given bean archive only.
     *
     * @author Jozef Hartinger
     *
     */
    private static class LocalOverrides {

        public static final Supplier<LocalOverrides> SUPPLIER = new Supplier<LocalOverrides>() {
            @Override
            public LocalOverrides get() {
                return new LocalOverrides();
            }
        };

        /*
         * <class>org.mycompany.myfwk.TimeStampLogger</class>
         */
        private final Map<String, LegacyEnablementRecord> legacyRecords;
        /*
         * <class enabled="false">org.mycompany.myfwk.TimeStampLogger</class>
         */
        private final Map<GlobalEnablementRecord, LocalEnablementRecord> locallyDisabledRecords;
        /*
         * <class enabled="true">org.mycompany.myfwk.TimeStampLogger</class>
         */
        private final Map<LocalEnablementRecord, GlobalEnablementRecord> locallyEnabledRecordsWithDefaultPriority;

        public LocalOverrides() {
            this.legacyRecords = new HashMap<String, LegacyEnablementRecord>();
            this.locallyDisabledRecords = new HashMap<GlobalEnablementRecord, LocalEnablementRecord>();
            this.locallyEnabledRecordsWithDefaultPriority = new HashMap<LocalEnablementRecord, GlobalEnablementRecord>();
        }
    }

    private static class EnablementRecordWithPriorityToClassMetadataFunction implements
            Function<EnablementRecordWithPriority, Metadata<Class<?>>> {

        private static final EnablementRecordWithPriorityToClassMetadataFunction INSTANCE = new EnablementRecordWithPriorityToClassMetadataFunction();

        @Override
        public Metadata<Class<?>> apply(EnablementRecordWithPriority input) {
            return new MetadataImpl<Class<?>>(input.getEnabledClass(), input.getLocation());
        }
    }

    public void clear() {
        globallyEnabledRecords.clear();
        globallySetPriorities.clear();
        localOverrides.clear();
    }

    /*
     * Local structures used for enablement proessing
     */

    private abstract static class EnablementRecord {

        private final String location;
        private final Class<?> enabledClass;

        public EnablementRecord(String location, Class<?> enabledClass) {
            this.location = location;
            this.enabledClass = enabledClass;
        }

        public String getLocation() {
            return location;
        }

        public Class<?> getEnabledClass() {
            return enabledClass;
        }
    }

    private abstract static class EnablementRecordWithPriority extends EnablementRecord implements Comparable<EnablementRecordWithPriority> {

        private final int priority;

        public EnablementRecordWithPriority(String location, Class<?> enabledClass, int priority) {
            super(location, enabledClass);
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(EnablementRecordWithPriority o) {
            if (priority == o.getPriority()) {
                /*
                 * The spec does not specify what happens if two records have the same priority.
                 * Instead of giving random results, we compare the records based on their class name lexicographically.
                 */
                return getEnabledClass().getName().compareTo(o.getEnabledClass().getName());
            }
            return priority - o.priority;
        }
    }

    /**
     * A beans.xml records with global effect. This is either a global enablement definition (enabled == true) or a global priority
     * setter (enabled == false).
     *
     * @author Jozef Hartinger
     *
     */
    private static class GlobalEnablementRecord extends EnablementRecordWithPriority {

        private final BeanDeploymentArchive archive;

        public GlobalEnablementRecord(String location, Class<?> enabledClass, int priority, BeanDeploymentArchive archive) {
            super(location, enabledClass, priority);
            this.archive = archive;
        }

        /**
         * Returns the {@link BeanDeploymentArchive} which contains the beans.xml file in which this record is enabled globally.
         *
         * @return
         */
        public BeanDeploymentArchive getArchive() {
            return archive;
        }
    }

    /**
     * A beans.xml record that only affects a single bean archive. This can either be a disabler of a globally enabled record
     * (enabled == false) or an enabler of a record that has been given a default priority.
     *
     * @author Jozef Hartinger
     *
     */
    private static class LocalEnablementRecord extends EnablementRecord {

        public LocalEnablementRecord(String location, Class<?> enabledClass) {
            super(location, enabledClass);
        }
    }

    /**
     * Represents a class (interceptor/decorator/alternative) that is enabled for a bean archive as defined in CDI 1.0.
     *
     * @author Jozef Hartinger
     *
     */
    private static class LegacyEnablementRecord extends EnablementRecordWithPriority {

        public LegacyEnablementRecord(String location, Class<?> enabledClass, int priority) {
            super(location, enabledClass, priority);
        }
    }
}
