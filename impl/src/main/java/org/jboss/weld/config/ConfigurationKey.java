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
package org.jboss.weld.config;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.configuration.spi.ExternalConfiguration;

/**
 * This enum lists all the supported configuration keys.
 *
 * @author Martin Kouba
 * @see WeldConfiguration
 * @see SystemPropertiesConfiguration
 */
public enum ConfigurationKey {

    /**
     * Indicates whether ConcurrentDeployer and ConcurrentValidator should be enabled. If enabled, ConcurrentDeployer and
     * ConcurrentValidator execute their
     * subtasks using {@link org.jboss.weld.manager.api.ExecutorServices} which can be configured separately.
     *
     * Otherwise, single-threaded version of Deployer and Validator are used.
     *
     * By default, concurrent deployment is enabled.
     */
    @Description("Indicates whether the concurrent deployment is enabled.")
    CONCURRENT_DEPLOYMENT("org.jboss.weld.bootstrap.concurrentDeployment", true),

    /**
     * The number of threads used by ContainerLifecycleEventPreloader. The ContainerLifecycleEventPreloader allows observer
     * methods for container lifecycle
     * events to be resolved upfront while the deployment is waiting for classloader or reflection API.
     *
     * ContainerLifecycleEventPreloader has its own thread pool whose size is configured by this property.
     *
     * If set to 0, ContainerLifecycleEventPreloader is not installed.
     *
     * If not specified, the value is set to Math.max(1, Runtime.getRuntime().availableProcessors() - 1)).
     *
     */
    @Description("Weld is capable of resolving observer methods for container lifecycle events in advance while bean deployer threads are blocked waiting for I/O operations. This option specifies the number of threads used for preloading. If set to 0, preloading is disabled.")
    PRELOADER_THREAD_POOL_SIZE("org.jboss.weld.bootstrap.preloaderThreadPoolSize",
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1)),

    /**
     * Allows an integrator to enable the non-portable mode. Non-portable mode is suggested by the specification to overcome
     * problems with legacy applications
     * not using CDI SPI properly.
     *
     * The non-portable mode is disabled by default.
     */
    @Description("Non-portable mode is suggested by the specification to overcome problems with legacy applications not using CDI SPI properly. The non-portable mode is disabled by default.")
    NON_PORTABLE_MODE("org.jboss.weld.nonPortableMode", false),

    /**
     * The number of threads to be used for bean loading and deployment.
     */
    @Description("The number of threads to be used by the Weld thread pool. Only used by <code>FIXED</code> and <code>FIXED_TIMEOUT</code> thread pool type.")
    EXECUTOR_THREAD_POOL_SIZE("org.jboss.weld.executor.threadPoolSize", Runtime.getRuntime().availableProcessors()),

    /**
     * If set to true, debug timing information is printed to the standard output.
     */
    @Description("If set to true, some more debug information is logged when the Weld thread pool is used.")
    EXECUTOR_THREAD_POOL_DEBUG("org.jboss.weld.executor.threadPoolDebug", false),

    /**
     * The type of the thread pool. Possible values are: FIXED, FIXED_TIMEOUT, NONE, SINGLE_THREAD, COMMON.
     */
    @Description("The type of the Weld thread pool. Possible values are: <ul><li><code>FIXED</code> - Uses a fixed number of threads. The number of threads remains the same throughout the application.</li><li><code>FIXED_TIMEOUT</code> - Uses a fixed number of threads. A thread will be stopped after a configured period of inactivity.</li><li><code>NONE</code> - No dedicated thread pool used.</li><li><code>SINGLE_THREAD</code> - A single-threaded thread pool.</li><li><code>COMMON</code> - The default ForkJoinPool.commonPool() is used.</li>")
    EXECUTOR_THREAD_POOL_TYPE("org.jboss.weld.executor.threadPoolType", ""),

    /**
     * Keep-alive time in seconds. Passed to the constructor of the ThreadPoolExecutor class, maximum time that excess idle
     * threads will wait for new tasks
     * before terminating.
     */
    @Description("The maximum time the idle threads will wait for new tasks before terminating. Only used by <code>FIXED_TIMEOUT</code> thread pool type.")
    EXECUTOR_THREAD_POOL_KEEP_ALIVE_TIME("org.jboss.weld.executor.threadPoolKeepAliveTime", 60L),

    /**
     * Weld caches resolved injection points in order to resolve them faster in the future. There exists a separate type safe
     * resolver for beans,
     * decorators, disposers, interceptors and observers. Each of them stores resolved injection points in its cache, which
     * maximum size is bounded by a default
     * value (common to all of them).
     *
     * @see <a href="https://issues.jboss.org/browse/WELD-1323">WELD-1323</a>
     */
    @Description("Weld caches already resolved injection points in order to resolve them faster in the future. There exists a separate type safe resolver for beans, decorators, disposers, interceptors and observers. Each of them stores resolved injection points in its cache, which maximum size is bounded by a common default value.")
    RESOLUTION_CACHE_SIZE("org.jboss.weld.resolution.cacheSize", 0x10000L),

    /**
     * For debug purposes, it's possible to dump the generated bytecode of proxies and subclasses.
     */
    @Description("For debugging purposes, it’s possible to dump the generated bytecode of client proxies and enhanced subclasses to the filesystem. The value represents the file path where the files should be stored.")
    PROXY_DUMP("org.jboss.weld.proxy.dump", ""),

    /**
     * Weld supports a non-standard workaround to be able to create client proxies for Java types that cannot be proxied by the
     * container, using non-portable
     * JVM APIs.
     */
    @Description("Weld supports a non-standard workaround to be able to create client proxies for Java types that cannot be proxied by the container, using non-portable JVM APIs.")
    RELAXED_CONSTRUCTION("org.jboss.weld.construction.relaxed", false),

    /**
     * Allows {@link ProxyInstantiator} to be selected explicitly. This is only intended for testing purposes and should never
     * be set by an application.
     */
    PROXY_INSTANTIATOR("org.jboss.weld.proxy.instantiator", ""),

    /**
     * Weld supports a non-standard workaround to be able to create client proxies for Java types that cannot be proxied by the
     * container, using non-portable
     * JVM APIs.
     *
     * @deprecated this option is deprecated. RELAXED_CONSTRUCTION should be used instead
     */
    @Description("This option is deprecated - <code>org.jboss.weld.construction.relaxed</code> should be used instead.")
    PROXY_UNSAFE("org.jboss.weld.proxy.unsafe", false),

    /**
     * XML descriptor validation is enabled by default.
     */
    @Description("XML descriptor validation is enabled by default.")
    DISABLE_XML_VALIDATION("org.jboss.weld.xml.disableValidating", false),

    /**
     * For certain combinations of scopes, the container is permitted to optimize an injectable reference lookup. The
     * optimization is disabled by default as it
     * does not match the {@linkjakarta.enterprise.context.spi.AlterableContext} contract.
     */
    @Description("For certain combinations of scopes, the container is permitted to optimize an injectable reference lookup. The optimization is disabled by default.")
    INJECTABLE_REFERENCE_OPTIMIZATION("org.jboss.weld.injection.injectableReferenceOptimization", false),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE("org.jboss.weld.probe.invocationMonitor.excludeType", ""),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_INVOCATION_MONITOR_SKIP_JAVABEAN_PROPERTIES("org.jboss.weld.probe.invocationMonitor.skipJavaBeanProperties", true),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_EVENT_MONITOR_EXCLUDE_TYPE("org.jboss.weld.probe.eventMonitor.excludeType", ""),

    /**
     * This optimization is used to reduce the HTTP session replication overhead. However, the inconsistency detection mechanism
     * may cause problems in some
     * development environments.
     */
    @Description("This optimization is used to reduce the HTTP session replication overhead. However, the inconsistency detection mechanism may cause problems in some development environments.")
    BEAN_IDENTIFIER_INDEX_OPTIMIZATION("org.jboss.weld.serialization.beanIdentifierIndexOptimization", true),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_EMBED_INFO_SNIPPET("org.jboss.weld.probe.embedInfoSnippet", true),

    /**
     * If set to <code>true</code>, the attributes should be fetched lazily from the backing store for some contexts (e.g.
     * attributes of an HTTP session for a
     * session context).
     */
    @Description("If set to <code>true</code>, the attributes should be fetched lazily from the backing store for some contexts (e.g. attributes of an HTTP session for a session context).")
    CONTEXT_ATTRIBUTES_LAZY_FETCH("org.jboss.weld.context.attributes.lazyFetch", true),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_JMX_SUPPORT("org.jboss.weld.probe.jmxSupport", false),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_EVENT_MONITOR_CONTAINER_LIFECYCLE_EVENTS("org.jboss.weld.probe.eventMonitor.containerLifecycleEvents", false),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_ALLOW_REMOTE_ADDRESS("org.jboss.weld.probe.allowRemoteAddress",
            "127.0.0.1|::1|::1%.+|0:0:0:0:0:0:0:1|0:0:0:0:0:0:0:1%.+"),

    /**
     * Weld supports a non-standard workaround to be able to create proxies for Java types which declare non-private non-static
     * final methods. These methods are
     * completely ignored during proxy generation, and should never be invoked upon the proxy instance!
     * <p>
     * A regular expression. If an unproxyable type matches this pattern, the type is considered proxyable and final methods are
     * ignored.
     */
    @Description("Weld supports a non-standard workaround to be able to create proxies for Java types which declare non-private non-static final methods. A regular expression. If an unproxyable type matches this pattern, the type is considered proxyable and final methods are ignored.")
    PROXY_IGNORE_FINAL_METHODS("org.jboss.weld.proxy.ignoreFinalMethods", ""),

    /**
     * Conversation timeout in milliseconds. Default value is 600 000 ms.
     */
    @Description("The maximum inactivity time of conversation in milliseconds.")
    CONVERSATION_TIMEOUT("org.jboss.weld.conversation.timeout", 10 * 60 * 1000L),

    /**
     * Conversation concurrent access timeout in milliseconds represents maximum time to wait on the conversation concurrent
     * lock. Default value is 1000 ms.
     */
    @Description("The maximum time to wait on the lock of conversation in milliseconds.")
    CONVERSATION_CONCURRENT_ACCESS_TIMEOUT("org.jboss.weld.conversation.concurrentAccessTimeout", 1000L),

    /**
     * This configuration property should only be used if experiencing problems with rolling upgrades.
     * <p>
     * The delimiter is used to abbreviate a bean archive identifier (which is usually derived from the archive name) before
     * used as a part of an identifier of
     * an internal component (such as bean). Note that the delimiter is used for all bean archives forming the application.
     * <p>
     * The abbreviation proceeds as follows:
     * <ul>
     * <li>Try to find the first occurrence of the specified delimiter</li>
     * <li>If not found, the identifier is not abbreviated</li>
     * <li>If found, try to extract the archive suffix (`.war`, `.ear`, etc.) and the final value consists of the part before
     * the delimiter and the archive
     * suffix (if extracted)</li>
     * </ul>
     * <p>
     * An example: Given an application with two versions going by the names <code>test__1.1.war</code> and
     * <code>test__1.2.war</code>. Weld normally cannot
     * support replication of <code>@SessionScoped</code> beans between these two deployments. Passing in this option with
     * delimiter "__" will allow Weld to see
     * both applications simply as test.war, hence allowing for session replication.
     */
    @Description("The delimiter is used to abbreviate a bean archive identifier before used as a part of an identifier of an internal component (such as bean).")
    ROLLING_UPGRADES_ID_DELIMITER("org.jboss.weld.clustering.rollingUpgradesIdDelimiter", ""),

    /**
     * A regular expression. If a non-empty string, then all annotated types whose
     * {@codejakarta.enterprise.inject.spi.AnnotatedType#getJavaClass().getName()} matches this pattern are vetoed if not
     * annotated with a bean defining annotation.
     */
    @Description("A regular expression. If a non-empty string, then all annotated types whose <code>jakarta.enterprise.inject.spi.AnnotatedType#getJavaClass().getName()</code> matches this pattern are vetoed if not annotated with a bean defining annotation.")
    VETO_TYPES_WITHOUT_BEAN_DEFINING_ANNOTATION("org.jboss.weld.bootstrap.vetoTypesWithoutBeanDefiningAnnotation", ""),

    /**
     * This option is deprecated and has no function since Weld 5.1.0.Final.
     * It will be removed in upcoming versions.
     */
    @Deprecated(since = "5.1.0.Final")
    @Description("This option is deprecated and has no function since Weld 5.1.0.Final.")
    PROBE_EXPORT_DATA_AFTER_DEPLOYMENT("org.jboss.weld.probe.exportDataAfterDeployment", ""),

    /**
     * If set to <code>true</code>:
     * <ul>
     * <li>Weld is allowed to perform efficient cleanup and further optimizations after bootstrap</li>
     * <li>{@link Bootstrap#endInitialization()} must be called after all EE components which support injection are installed
     * (that means all relevant {@link ProcessInjectionTarget} events were already fired)</li>
     * </ul>
     * This property can only be set by integrators through {@link ExternalConfiguration}.
     */
    ALLOW_OPTIMIZED_CLEANUP("org.jboss.weld.bootstrap.allowOptimizedCleanup", false, true),

    /**
     * A regular expression. If {@link #ALLOW_OPTIMIZED_CLEANUP} is set to true this property can be used to extend the set of
     * beans which should never be
     * considered <strong>unused</strong>. {@link Bean#getBeanClass()} is used to match the pattern.
     *
     * <p>
     * Two special values are considered. {@link UnusedBeans#ALL} (default value) means that all beans are excluded. If set to
     * {@link UnusedBeans#NONE}, no
     * beans are excluded.
     * </p>
     *
     * An unused bean:
     * <ul>
     * <li>is not excluded by this property or {@link #UNUSED_BEANS_EXCLUDE_ANNOTATION}</li>
     * <li>is not a built-in bean, session bean, extension, interceptor or decorator,</li>
     * <li>does not have a name</li>
     * <li>does not declare an observer</li>
     * <li>is not eligible for injection to any injection point</li>
     * <li>does not declare a producer which is eligible for injection to any injection point</li>
     * <li>is not eligible for injection into any {@link Instance} injection point</li>
     * </ul>
     *
     * @see ConfigurationKey#UNUSED_BEANS_EXCLUDE_ANNOTATION
     */
    UNUSED_BEANS_EXCLUDE_TYPE("org.jboss.weld.bootstrap.unusedBeans.excludeType", UnusedBeans.ALL),

    /**
     * A regular expression. If {@link #ALLOW_OPTIMIZED_CLEANUP} is set to true this property can be used to extend the set of
     * beans which should never be
     * considered <strong>unused</strong>. A bean is excluded if the corresponding {@link AnnotatedType}, or any member, is
     * annotated with an annotation which
     * matches this pattern.
     *
     * <p>
     * By default, JAX-RS annotations are considered. If undefined (an empty string), no annotations are considered.
     * </p>
     *
     * An unused bean:
     * <ul>
     * <li>is not excluded by this property or {@link #UNUSED_BEANS_EXCLUDE_ANNOTATION}</li>
     * <li>is not a built-in bean, session bean, extension, interceptor or decorator,</li>
     * <li>does not have a name</li>
     * <li>does not declare an observer</li>
     * <li>is not eligible for injection to any injection point</li>
     * <li>does not declare a producer which is eligible for injection to any injection point</li>
     * <li>is not eligible for injection into any {@link Instance} injection point</li>
     * </ul>
     *
     * @see #UNUSED_BEANS_EXCLUDE_TYPE
     */
    UNUSED_BEANS_EXCLUDE_ANNOTATION("org.jboss.weld.bootstrap.unusedBeans.excludeAnnotation", "javax\\.ws\\.rs.*"),

    /**
     * If set to true then when a contextual reference for a @SessionScoped or @ConversationScoped bean is obtained from a
     * context backed by an HTTP session
     * the instance is set again using HttpSession.setAttribute(). This allows to trigger session replication in some
     * application servers.
     */
    @Description("If set to true then when a contextual reference for a @SessionScoped or @ConversationScoped bean is obtained from a context backed by an HTTP session the instance is set again using HttpSession.setAttribute(). This allows to trigger session replication in some application servers.")
    RESET_HTTP_SESSION_ATTR_ON_BEAN_ACCESS("org.jboss.weld.context.resetHttpSessionAttributeOnBeanAccess", false),

    ;

    /**
     *
     * @param key The string representation of the key
     * @param defaultValue The default value
     */
    ConfigurationKey(String key, Object defaultValue) {
        this(key, defaultValue, false);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @param integratorOnly
     */
    ConfigurationKey(String key, Object defaultValue, boolean integratorOnly) {
        this.key = key;
        // Fail fast if a new key with unsupported value type is introduced
        if (!isValueTypeSupported(defaultValue.getClass())) {
            throw new IllegalArgumentException("Unsupported value type: " + defaultValue);
        }
        this.defaultValue = defaultValue;
        this.integratorOnly = integratorOnly;
    }

    public static final class UnusedBeans {

        public static final String ALL = "ALL";
        public static final String NONE = "NONE";

        public static final boolean isEnabled(WeldConfiguration configuration) {
            return isEnabled(configuration.getStringProperty(UNUSED_BEANS_EXCLUDE_TYPE));
        }

        public static final boolean isEnabled(String value) {
            return !ALL.equals(value) && !".*".equals(value);
        }

        public static final boolean excludeNone(String value) {
            return NONE.equals(value);
        }
    }

    private final String key;

    private final Object defaultValue;

    private final boolean integratorOnly;

    /**
     * @return the string representation of the key
     */
    public String get() {
        return key;
    }

    /**
     * @return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     * @return <code>true</code> if only values set through {@link ExternalConfiguration} are considered
     */
    public boolean isIntegratorOnly() {
        return integratorOnly;
    }

    /**
     *
     * @param value
     * @return <code>true</code> if the given value corresponds to the type of the default value, <code>false</code> otherwise
     */
    public boolean isValidValue(Object value) {
        return isValidValueType(value.getClass());
    }

    /**
     *
     * @param valueType
     * @return <code>true</code> if the given value type corresponds to the type of the default value, <code>false</code>
     *         otherwise
     */
    public boolean isValidValueType(Class<?> valueType) {
        return defaultValue.getClass().isAssignableFrom(valueType);
    }

    /**
     *
     * @param value
     * @return the converted value
     */
    public Object convertValue(String value) {
        if (defaultValue instanceof Boolean) {
            return Boolean.valueOf(value);
        } else if (defaultValue instanceof Long) {
            return Long.valueOf(value);
        } else if (defaultValue instanceof Integer) {
            return Integer.valueOf(value);
        } else {
            return value.toString();
        }
    }

    /**
     *
     * @param valueType
     * @return <code>true</code> if the given value type is supported, <code>false</code> otherwise
     */
    public static boolean isValueTypeSupported(Class<?> valueType) {
        return valueType.equals(String.class) || valueType.equals(Boolean.class) || valueType.equals(Integer.class)
                || valueType.equals(Long.class);
    }

    /**
     *
     * @param from
     * @return the key with the given value, or <code>null</code> if no such exists
     */
    public static ConfigurationKey fromString(String from) {
        for (ConfigurationKey key : values()) {
            if (key.get().equals(from)) {
                return key;
            }
        }
        return null;
    }

}
