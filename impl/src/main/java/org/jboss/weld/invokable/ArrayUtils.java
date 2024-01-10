package org.jboss.weld.invokable;

class ArrayUtils {
    private ArrayUtils() {
    }

    static Object[] trimArrayToSize(Object[] array, int size) {
        if (array == null || array.length <= size) {
            return array;
        }
        Object[] result = new Object[size];
        System.arraycopy(array, 0, result, 0, size);
        return result;
    }
}
