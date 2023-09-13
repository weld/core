package org.jboss.weld.tests.arrays;

/**
 *
 */
public class Bar<T> {

    private T value;

    public Bar(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Bar bar = (Bar) o;

        if (value != null ? !value.equals(bar.value) : bar.value != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
