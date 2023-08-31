package org.jboss.weld.invokable;

class ValueCarryingException extends Exception {

    // we use this method to filter return values of exception transformer into excepted types
    static Object hideReturnValue(Object methodRetType) throws ValueCarryingException {
        // rethrow method return type inside exception
        throw new ValueCarryingException(methodRetType);
    }


    private final Object methodReturnValue;
    public ValueCarryingException(Object methodReturnValue) {
        this.methodReturnValue = methodReturnValue;
    }

    public Object getMethodReturnValue() {
        return methodReturnValue;
    }
}
