package org.jboss.weld.invokable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.invoke.AsyncHandler;

import org.jboss.weld.resources.DefaultResourceLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AsyncHandlerRegistryTest {
    @Rule
    public TemporaryFolder files = new TemporaryFolder();

    private void discover(AsyncHandlerRegistry registry, Class<?>... providers) throws Exception {
        discoverService(registry, AsyncHandler.ReturnType.class, providers);
    }

    private void discoverService(AsyncHandlerRegistry registry, Class<?> service, Class<?>... providers) throws Exception {
        var file = files.newFile().toPath();
        Files.writeString(file, Stream.of(providers).map(Class::getName).collect(Collectors.joining("\n")));
        URL descriptor = file.toUri().toURL();
        registry.discoverHandlers(new DefaultResourceLoader() {
            @Override
            public Collection<URL> getResources(String name) {
                return name.equals("META-INF/services/" + service.getName())
                        ? List.of(descriptor)
                        : List.of();
            }
        });
    }

    @Test
    public void builtinCollisionRequiresConfiguration() throws Exception {
        var registry = new AsyncHandlerRegistry();
        discover(registry, StageHandler.class);
        assertThrows(DeploymentException.class, registry::validateHandlers);
    }

    @Test
    public void selectsCustomBuiltinReplacement() throws Exception {
        var registry = new AsyncHandlerRegistry(CompletionStage.class.getName() + "=" + StageHandler.class.getName());
        discover(registry, StageHandler.class);
        registry.validateHandlers();
        assertEquals(StageHandler.class, registry.getHandler(CompletionStage.class).getHandlerClass());
    }

    @Test
    public void selectsBuiltinOverCustomProvider() throws Exception {
        var registry = new AsyncHandlerRegistry(CompletionStage.class.getName() + "="
                + AsyncHandlerRegistry.BuiltinCompletionStageHandler.class.getName());
        discover(registry, StageHandler.class);
        registry.validateHandlers();
        assertEquals(AsyncHandlerRegistry.BuiltinCompletionStageHandler.class,
                registry.getHandler(CompletionStage.class).getHandlerClass());
        assertTrue(registry.getHandler(CompletionStage.class).isReturnType());
    }

    @Test
    public void selectsMultipleHandlersWithWhitespace() throws Exception {
        var registry = new AsyncHandlerRegistry(" \t" + String.class.getName() + " = "
                + SecondStringHandler.class.getName() + " , " + Integer.class.getName() + " = "
                + IntegerHandler.class.getName() + " \t");
        discover(registry, StringHandler.class, SecondStringHandler.class);
        discoverService(registry, AsyncHandler.ParameterType.class, IntegerHandler.class, SecondIntegerHandler.class);
        registry.validateHandlers();
        assertEquals(SecondStringHandler.class, registry.getHandler(String.class).getHandlerClass());
        assertTrue(registry.getHandler(String.class).isReturnType());
        assertEquals(IntegerHandler.class, registry.getHandler(Integer.class).getHandlerClass());
        assertFalse(registry.getHandler(Integer.class).isReturnType());
    }

    @Test
    public void selectsOneOfDuplicateCustomProvidersAfterAllArchives() throws Exception {
        var registry = new AsyncHandlerRegistry(String.class.getName() + "=" + SecondStringHandler.class.getName());
        discover(registry, StringHandler.class);
        discover(registry, SecondStringHandler.class);
        registry.validateHandlers();
        assertEquals(SecondStringHandler.class, registry.getHandler(String.class).getHandlerClass());
    }

    @Test
    public void duplicateCustomProvidersFailByDefault() throws Exception {
        var registry = new AsyncHandlerRegistry();
        discover(registry, StringHandler.class, SecondStringHandler.class);
        assertThrows(DeploymentException.class, registry::validateHandlers);
    }

    @Test
    public void repeatedDiscoveryIsNotADuplicate() throws Exception {
        var registry = new AsyncHandlerRegistry();
        discover(registry, StringHandler.class);
        discover(registry, StringHandler.class);
        registry.validateHandlers();
        assertEquals(StringHandler.class, registry.getHandler(String.class).getHandlerClass());
    }

    @Test
    public void missingConfiguredProviderFails() {
        var registry = new AsyncHandlerRegistry(CompletionStage.class.getName() + "=missing.Handler");
        var failure = assertThrows(DeploymentException.class, registry::validateHandlers);
        assertTrue(failure.getMessage().contains("WELD-002031"));
    }

    @Test
    public void wrongConfiguredAsyncTypeFails() throws Exception {
        var registry = new AsyncHandlerRegistry(CompletionStage.class.getName() + "=" + StringHandler.class.getName());
        discover(registry, StringHandler.class);
        assertThrows(DeploymentException.class, registry::validateHandlers);
    }

    @Test
    public void undiscoveredAsyncTypeFails() {
        var registry = new AsyncHandlerRegistry("missing.Type=missing.Handler");
        assertThrows(DeploymentException.class, registry::validateHandlers);
    }

    @Test
    public void malformedAndDuplicateMappingsFail() {
        for (String configuration : List.of("type", "=handler", "type=", "type=one,type=two", "type=handler,",
                "type=handler=extra", ",type=handler", "type=handler,,other=provider", " \t=handler", "type= \t",
                "type=one, type =two")) {
            var failure = assertThrows(configuration, DeploymentException.class, () -> new AsyncHandlerRegistry(configuration));
            assertTrue(configuration, failure.getMessage().contains("WELD-002032"));
        }
    }

    @Test
    public void bothKindsWithDifferentTypesFailThroughEitherDescriptor() {
        for (Class<?> service : List.of(AsyncHandler.ReturnType.class, AsyncHandler.ParameterType.class)) {
            assertThrows(DefinitionException.class,
                    () -> discoverService(new AsyncHandlerRegistry(), service, BothKinds.class));
        }
    }

    @Test
    public void bothKindsWithSameTypeFailThroughSingleDescriptor() {
        assertThrows(DefinitionException.class, () -> discover(new AsyncHandlerRegistry(), BothKindsSameType.class));
    }

    public static class BothKinds implements AsyncHandler.ReturnType<String>, AsyncHandler.ParameterType<Integer> {
        public String transform(String original, Runnable completion) {
            return original;
        }

        public Integer transformArgument(Integer original, Runnable completion) {
            return original;
        }
    }

    public static class BothKindsSameType implements AsyncHandler.ReturnType<String>, AsyncHandler.ParameterType<String> {
        public String transform(String original, Runnable completion) {
            return original;
        }

        public String transformArgument(String original, Runnable completion) {
            return original;
        }
    }

    public static class StageHandler implements AsyncHandler.ReturnType<CompletionStage<?>> {
        public CompletionStage<?> transform(CompletionStage<?> original, Runnable completion) {
            return original.whenComplete((value, failure) -> completion.run());
        }
    }

    public static class IntegerHandler implements AsyncHandler.ParameterType<Integer> {
        @Override
        public Integer transformArgument(Integer original, Runnable completion) {
            completion.run();
            return original;
        }
    }

    public static class SecondIntegerHandler implements AsyncHandler.ParameterType<Integer> {
        @Override
        public Integer transformArgument(Integer original, Runnable completion) {
            completion.run();
            return original;
        }
    }

    public static class StringHandler implements AsyncHandler.ReturnType<String> {
        public String transform(String original, Runnable completion) {
            completion.run();
            return original;
        }
    }

    public static class SecondStringHandler implements AsyncHandler.ReturnType<String> {
        public String transform(String original, Runnable completion) {
            completion.run();
            return original;
        }
    }
}
