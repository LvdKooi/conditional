package io.github.lvdkooi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Conditional<S, T> {

    private final List<ConditionalAction<S, T>> conditionalActions;
    private final S value;

    private Conditional(List<ConditionalAction<S, T>> conditionalActions, S value) {
        this.conditionalActions = conditionalActions;
        this.value = value;
    }

    private static <S, T> Conditional<S, T> empty() {
        return new Conditional<>(Collections.emptyList(), null);
    }

    public static <S> Conditional<S, S> of(S value) {
        return new Conditional<>(Collections.emptyList(), value);
    }

    public static <S, U> ConditionalAction<S, U> applyIf(Predicate<S> condition, Function<S, U> function) {
        return new ConditionalAction<>(condition, function);
    }

    @SafeVarargs
    public final <U> Conditional<S, U> firstMatching(ConditionalAction<S, U>... actions) {
        var actionsAsList = Arrays.stream(actions).toList();

        return new Conditional<>(actionsAsList, value);
    }

    public <U> Conditional<S, U> map(Function<T, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var updatedConditionalActions = this.conditionalActions
                .stream()
                .map(conditionalAction -> conditionalAction.copyAndExtendActionWith(mapFunction))
                .toList();

        return new Conditional<>(updatedConditionalActions, value);
    }

    public <U> Conditional<T, U> flatMap(Function<T, Conditional<T, U>> flatMapFunction) {
        Objects.requireNonNull(flatMapFunction);

        var updatedConditionalActions = this.conditionalActions
                .stream()
                .map(conditionalAction -> conditionalAction.copyAndExtendActionWith(flatMapFunction))
                .toList();

        return findMatchingFunction(updatedConditionalActions, this.value)
                .map(function -> function.apply(this.value))
                .orElseGet(Conditional::empty);
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> supplier.get())
                .apply(value);
    }

    public T orElse(T defaultValue) {
        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> defaultValue)
                .apply(value);
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> throwableSupplier) throws X {
        Objects.requireNonNull(throwableSupplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseThrow(throwableSupplier).apply(value);
    }

    private static <S, T> Optional<Function<S, T>> findMatchingFunction(List<ConditionalAction<S, T>> conditionalActions, S value) {
        return conditionalActions
                .stream()
                .filter(entry -> entry.condition().test(value))
                .findFirst()
                .map(ConditionalAction::action);
    }

    private Optional<Function<S, T>> findMatchingFunction(S value) {
        return findMatchingFunction(this.conditionalActions, value);
    }

    public record ConditionalAction<S, T>(Predicate<S> condition, Function<S, T> action) {

        public ConditionalAction {
            Objects.requireNonNull(condition);
            Objects.requireNonNull(action);
        }

        public <U> ConditionalAction<S, U> copyAndExtendActionWith(Function<T, U> mappingFunction) {
            return new ConditionalAction<>(this.condition, this.action.andThen(mappingFunction));
        }
    }
}