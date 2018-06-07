package org.jboss.weld.environment.se;

import java.util.stream.Stream;

public interface ExceptionHandler {

    Stream<Class<? extends Throwable>> getExceptionTypes();

    void handle(Throwable t);

    default boolean supports(Throwable t) {
        Class<? extends Throwable> exceptionType = t.getClass();
        return getExceptionTypes()
                .filter(exceptionType::equals)
                .map(et -> true)
                .findFirst()
                .orElse(false);
    }

}