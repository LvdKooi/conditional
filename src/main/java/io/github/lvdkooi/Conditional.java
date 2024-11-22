package io.github.lvdkooi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A monad-like container for applying conditional logic in a functional way.
 * The {@code Conditional} class encapsulates a value and a list of conditional actions that can be applied to it.
 * It provides various methods for transforming the value or performing operations depending on whether conditions are met.
 *
 * <p>This class allows chaining conditions and actions in a functional pipeline, making the code more readable
 * and reusable compared to traditional imperative {@code if-else} statements.
 *
 * @param <S> The type of the input value.
 * @param <T> The type of the resulting value after the conditions and actions are applied.
 */
public final class Conditional<S, T> {

    /**
     * The value wrapped by this {@code Conditional} container.
     */
    private final S value;

    /**
     * A list of {@code ConditionalAction} instances, each containing a condition and an action.
     * These actions are applied in sequence based on the value and their respective conditions.
     */
    private final List<ConditionalAction<S, T>> conditionalActions;

    /**
     * Constructs a new {@code Conditional} instance with the given value and a list of conditional actions.
     *
     * @param value   The value to wrap in the {@code Conditional}.
     * @param actions The list of conditional actions to apply.
     */
    private Conditional(S value, List<ConditionalAction<S, T>> actions) {
        this.value = value;
        this.conditionalActions = actions;
    }

    /**
     * Creates a new {@code Conditional} instance with the given value.
     * The value will be wrapped in the container with no initial conditions.
     *
     * @param value The value to wrap in the {@code Conditional}.
     * @param <S>   The type of the value.
     * @return A new {@code Conditional} instance.
     */
    public static <S> Conditional<S, S> of(S value) {
        return new Conditional<>(value, Collections.emptyList());
    }

    /**
     * Creates an empty {@code Conditional} with a {@code null} value and no conditional actions.
     *
     * @param <S> The type of the value.
     * @param <T> The type of the resulting value.
     * @return An empty {@code Conditional}.
     */
    private static <S, T> Conditional<S, T> empty() {
        return new Conditional<>(null, Collections.emptyList());
    }

    /**
     * Creates a {@code ConditionalAction} that applies the given condition and function.
     *
     * @param condition The condition to check.
     * @param function  The function to apply if the condition is true.
     * @param <S>       The type of the input value.
     * @param <U>       The type of the result after the function is applied.
     * @return A new {@code ConditionalAction} instance.
     */
    public static <S, U> ConditionalAction<S, U> applyIf(Predicate<S> condition, Function<S, U> function) {
        return new ConditionalAction<>(condition, function);
    }

    /**
     * Applies the given conditional actions in order, and returns a new {@code Conditional} instance with the
     * value transformed based on the first matching condition.
     *
     * @param actions The conditional actions to apply.
     * @param <U>     The type of the resulting value.
     * @return A new {@code Conditional} instance with the transformed value.
     */
    @SafeVarargs
    public final <U> Conditional<S, U> firstMatching(ConditionalAction<S, U>... actions) {
        var actionsAsList = Arrays.stream(actions).toList();
        return new Conditional<>(value, actionsAsList);
    }

    /**
     * Transforms the value inside the {@code Conditional} using the given mapping function.
     *
     * @param mapFunction The function to apply to the value inside the {@code Conditional}.
     * @param <U>         The type of the resulting value.
     * @return A new {@code Conditional} instance with the transformed value.
     */
    public <U> Conditional<S, U> map(Function<T, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var updatedConditionalActions = conditionalActions
                .stream()
                .map(condAction -> condAction.and(mapFunction))
                .toList();

        return new Conditional<>(value, updatedConditionalActions);
    }

    /**
     * Applies the given flatMap function to the value inside the {@code Conditional} and returns a new
     * {@code Conditional} based on the result of the transformation.
     *
     * @param flatMapFunction The function to apply that returns another {@code Conditional}.
     * @param <U>             The type of the resulting value.
     * @return A new {@code Conditional} instance containing the transformed value.
     */
    public <U> Conditional<T, U> flatMap(Function<T, Conditional<T, U>> flatMapFunction) {
        return map(flatMapFunction)
                .orElseGet(Conditional::empty);
    }

    /**
     * Returns the value inside the {@code Conditional}, or invokes the given supplier if no condition matches.
     *
     * @param supplier The supplier to invoke if no conditions are met.
     * @return The value inside the {@code Conditional}, or the value from the supplier.
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElse(obj -> supplier.get())
                .apply(value);
    }

    /**
     * Returns the value inside the {@code Conditional}, or the provided default value if no condition matches.
     *
     * @param defaultValue The default value to return if no conditions are met.
     * @return The value inside the {@code Conditional}, or the default value.
     */
    public T orElse(T defaultValue) {
        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElse(obj -> defaultValue)
                .apply(value);
    }

    /**
     * Returns the value inside the {@code Conditional}, or throws an exception provided by the exception supplier
     * if no condition matches.
     *
     * @param exceptionSupplier The supplier to provide the exception to throw.
     * @param <X>               The type of the exception to throw.
     * @return The value inside the {@code Conditional}.
     * @throws X If no condition matches, the exception is thrown.
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseThrow(exceptionSupplier)
                .apply(value);
    }

    /**
     * Finds the first matching function for the given value.
     *
     * @param value The value to check against the conditions.
     * @return An {@code Optional} containing the matching function, or empty if no conditions match.
     */
    private Optional<Function<S, T>> findMatchingFunction(S value) {
        return conditionalActions.stream()
                .filter(entry -> entry.condition().test(value))
                .findFirst()
                .map(ConditionalAction::action);
    }

    /**
     * A record that represents a conditional action consisting of a condition and a function.
     * The condition is evaluated, and if it is {@code true}, the function is applied to the value.
     *
     * @param <S> The type of the value to check.
     * @param <T> The type of the resulting value after the function is applied.
     */
    public record ConditionalAction<S, T>(Predicate<S> condition, Function<S, T> action) {

        /**
         * Constructs a new {@code ConditionalAction} with the given condition and action.
         *
         * @param condition The condition to check.
         * @param action    The function to apply if the condition is {@code true}.
         */
        public ConditionalAction {
            Objects.requireNonNull(condition);
            Objects.requireNonNull(action);
        }

        /**
         * Combines this {@code ConditionalAction} with an additional action.
         * The new action is applied after the original one.
         *
         * @param extraAction The extra function to apply after the original action.
         * @param <U>         The type of the new resulting value.
         * @return A new {@code ConditionalAction} with the combined actions.
         */
        public <U> ConditionalAction<S, U> and(Function<T, U> extraAction) {
            return new ConditionalAction<>(condition, action.andThen(extraAction));
        }
    }
}
