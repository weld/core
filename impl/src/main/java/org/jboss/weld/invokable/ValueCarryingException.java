package org.jboss.weld.invokable;

class ValueCarryingException extends Exception {

    // we use this method to filter return values of exception transformer into excepted types
    static Object throwReturnValue(Object returnValue) throws ValueCarryingException {
        // rethrow method return type inside exception
        throw new ValueCarryingException(returnValue);
    }

    private final Object methodReturnValue;

    public ValueCarryingException(Object methodReturnValue) {
        this.methodReturnValue = methodReturnValue;
    }

    public Object getMethodReturnValue() {
        return methodReturnValue;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // no need to capture stack trace, this exception type is only used to carry a return value
        return this;
    }
}
