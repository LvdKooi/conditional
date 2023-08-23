package io.github.lvdkooi;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The `Conditional` class offers a way to organize conditional actions using a monadic approach. It allows chaining
 * and applying conditional actions based on predicates and functions. It supports building a sequence of conditions and
 * corresponding actions to be executed based on those conditions and has the capability to default to a predefined
 * action or value when no conditions are met.
 *
 * @param <T> The type of input object.
 * @param <R> The type of result produced by the actions.
 * @author Laurens van der Kooi
 */
public class Conditional<T, R> {
    private final Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue;
    private final Function<T, R> currentFunction;

    /**
     * Constructs a `Conditional` instance with an initial action queue and a current function.
     *
     * @param actionQueue     The queue of predicate-function pairs representing conditions and actions.
     * @param currentFunction The current function associated with this `Conditional` instance.
     */
    private Conditional(Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue, Function<T, R> currentFunction) {
        this.actionQueue = actionQueue;
        this.currentFunction = currentFunction;
    }

    /**
     * Defines the initial action for the 'Conditional' pipeline. This method should consistently be
     * succeeded by a corresponding when method, containing the condition that has to match for this function to be evaluated.
     *
     * @param function The initial function to be associated with the `Conditional` instance.
     * @param <T>      The type of input object.
     * @param <R>      The type of result produced by the function.
     * @return A new `Conditional` instance with the provided function.
     * @throws NullPointerException If the provided function is null.
     */
    public static <T, R> Conditional<T, R> apply(Function<T, R> function) {
        Objects.requireNonNull(function);
        return new Conditional<>(new ArrayDeque<>(1), function);
    }

    /**
     * Incorporates a condition-action pair into the Conditional instance, utilizing the provided condition and the
     * action that was previously specified in the apply/orApply chain before this when method.
     *
     * @param condition The condition to be checked before applying the current function.
     * @return A new `Conditional` instance with the added condition-action pair.
     * @throws NullPointerException If the provided condition is null, or if the current function is not set.
     */
    public Conditional<T, R> when(Predicate<T> condition) {
        assertCurrentFunctionAndPredicateAreValid(condition);

        var queue = new ArrayDeque<>(this.actionQueue);
        queue.add(new Pair<>(condition, currentFunction));

        return new Conditional<>(queue, null);
    }

    /**
     * Defines a subsequent action to be executed when the subsequent condition evaluates to true,
     * but only if none of the preceding conditions have evaluated to true.  This method should consistently be
     * succeeded by a corresponding when method, containing the condition that has to match for this function
     * to be evaluated.
     *
     * @param function The alternative function to be associated with a succeeding condition.
     * @return A new `Conditional` instance with the provided alternative function.
     * @throws NullPointerException If the provided function is null.
     */
    public Conditional<T, R> orApply(Function<T, R> function) {
        Objects.requireNonNull(function);
        return new Conditional<>(this.actionQueue, function);
    }

    /**
     *  If a condition matches, applies the provided function to the outcome of the matching action.
     *
     * @param mapFunction The mapping function to transform the current function's result.
     * @param <U>         The type of result produced by the mapping function.
     * @return A new `Conditional` instance with the mapped result type.
     * @throws NullPointerException If the provided mapping function is null.
     */
    public <U> Conditional<T, U> map(Function<R, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(mapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(queue, null);
    }

    /**
     * Applies the actions associated with conditions to the provided object, or else returns a default value
     * supplied by the given supplier.
     *
     * @param object   The object to which the actions are applied.
     * @param supplier The supplier providing the default value if no condition matches.
     * @return The result of applying the appropriate action to the object, or the default value if no match is found.
     * @throws NullPointerException If the provided supplier is null.
     */
    public R applyToOrElseGet(T object, Supplier<? extends R> supplier) {
        Objects.requireNonNull(supplier);

        return Optional.ofNullable(object)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> supplier.get())
                .apply(object);
    }

    /**
     * Applies the actions associated with conditions to the provided object, or else returns a default value.
     *
     * @param object       The object to which the actions are applied.
     * @param defaultValue The default value to be returned if no condition matches.
     * @return The result of applying the appropriate action to the object, or the default value if no match is found.
     */
    public R applyToOrElse(T object, R defaultValue) {
        return Optional.ofNullable(object)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> defaultValue)
                .apply(object);
    }

    /**
     * Applies the actions associated with conditions to the provided object, or else throws an exception
     * supplied by the given throwable supplier.
     *
     * @param object            The object to which the actions are applied.
     * @param throwableSupplier The supplier providing the throwable to be thrown if no condition matches.
     * @param <X>               The type of the exception to be thrown.
     * @return The result of applying the appropriate action to the object.
     * @throws X                    If no condition matches and the throwable supplier provides an exception.
     * @throws NullPointerException If the provided throwable supplier is null.
     */
    public <X extends Throwable> R applyToOrElseThrow(T object, Supplier<? extends X> throwableSupplier) throws X {
        Objects.requireNonNull(throwableSupplier);

        return Optional.ofNullable(object)
                .flatMap(this::findMatchingFunction)
                .orElseThrow(throwableSupplier)
                .apply(object);
    }

    private Optional<Function<T, R>> findMatchingFunction(T t) {
        return actionQueue
                .stream()
                .filter(entry -> entry.key().test(t))
                .findFirst()
                .map(Pair::value);
    }

    private void assertCurrentFunctionAndPredicateAreValid(Predicate<T> predicate) {
        Objects.requireNonNull(currentFunction, "The function that belongs to this condition is not yet set. " +
                "A predicate can only be added after an apply(Function<T, R> function) or orApply(Function<T, R> function).");
        Objects.requireNonNull(predicate);
    }

    /**
     * Represents a pair of key and value.
     *
     * @param <T> The type of the key.
     * @param <R> The type of the value.
     */
    private record Pair<T, R>(T key, R value) {

        private Pair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}