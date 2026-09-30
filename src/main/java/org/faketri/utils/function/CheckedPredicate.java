package org.faketri.utils.function;

import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T> {

    /**
     * A specialized version of a condition check for
     * cases where an error might occur.
     *
     * @return boolean true if the condition evaluates to true.
     * or boolean false if the condition is false or if
     * an exception is thrown during execution.
     */
    static <T> Predicate<T> unchecked(CheckedPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                return false;
            }
        };
    }

    boolean test(T t);
}
