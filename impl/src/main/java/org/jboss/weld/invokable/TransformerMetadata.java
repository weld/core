package org.jboss.weld.invokable;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.util.Preconditions;

public class TransformerMetadata {

    private Class<?> targetClass;
    private String methodName;
    private TransformerType type;

    public TransformerMetadata(Class<?> clazz, String methodName, TransformerType type) {
        Preconditions.checkArgumentNotNull(clazz);
        Preconditions.checkArgumentNotNull(methodName);
        Preconditions.checkArgumentNotNull(type);
        this.targetClass = clazz;
        this.methodName = methodName;
        this.type = type;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public TransformerType getType() {
        return type;
    }

    public boolean isInputTransformer() {
        return type == TransformerType.INSTANCE || type == TransformerType.ARGUMENT;
    }

    public boolean isOutputTransformer() {
        return type == TransformerType.RETURN_VALUE || type == TransformerType.EXCEPTION;
    }

    @Override
    public String toString() {
        String kind = "";
        switch (this.type) {
            case WRAPPER:
                kind = "Invocation wrapper ";
                break;
            case INSTANCE:
                kind = "Target instance transformer ";
                break;
            case ARGUMENT:
                kind = "Argument transformer ";
                break;
            case RETURN_VALUE:
                kind = "Return value transformer ";
                break;
            case EXCEPTION:
                kind = "Exception transformer ";
                break;
            default:
                throw new IllegalStateException("Unknown transformer " + type);
        }
        return kind + targetClass + "#" + methodName + "()";
    }
}
