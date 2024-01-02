package io.github.lvdkooi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Conditional<S, T> {

    private final Map<Predicate<S>, Function<S, T>> actionMap;
    private final S value;

    private Conditional(Map<Predicate<S>, Function<S, T>> actionMap, S value) {
        this.actionMap = actionMap;
        this.value = value;
    }

    private static <S, T> Conditional<S, T> empty() {
        return new Conditional<>(new LinkedHashMap<>(), null);
    }

    public static <S> Conditional<S, S> of(S value) {
        return new Conditional<>(new LinkedHashMap<>(1), value);
    }

    public static <S, U> ConditionalAction<S, U> applyIf(Predicate<S> condition, Function<S, U> function) {
        assertCurrentFunctionAndPredicateAreValid(function, condition);
        return new ConditionalAction<>(condition, function);
    }

    @SafeVarargs
    public final <U> Conditional<S, U> firstMatching(ConditionalAction<S, U>... actions) {
        var arrayDeque = Arrays.stream(actions)
                .collect(Collectors.toMap(ConditionalAction::condition, ConditionalAction::action, (x, y) -> x, LinkedHashMap::new));

        return new Conditional<>(arrayDeque, value);
    }

    public <U> Conditional<S, U> map(Function<T, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var updatedActionMap = this.actionMap
                .entrySet()
                .stream()
                .map(entry -> new ConditionalAction<>(entry.getKey(), entry.getValue().andThen(mapFunction)))
                .collect(Collectors.toMap(ConditionalAction::condition, ConditionalAction::action, (x, y) -> x, LinkedHashMap::new));

        return new Conditional<>(updatedActionMap, value);
    }

    public <U> Conditional<T, U> flatMap(Function<T, Conditional<T, U>> flatMapFunction) {
        Objects.requireNonNull(flatMapFunction);

        var updatedActionMap = this.actionMap
                .entrySet()
                .stream()
                .map(entry -> new ConditionalAction<>(entry.getKey(), entry.getValue().andThen(flatMapFunction)))
                .collect(Collectors.toMap(ConditionalAction::condition, ConditionalAction::action, (x, y) -> x, LinkedHashMap::new));

        return findMatchingFunction(updatedActionMap, this.value)
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
                .orElseThrow(throwableSupplier)
                .apply(value);
    }

    private static <S, T> Optional<Function<S, T>> findMatchingFunction(Map<Predicate<S>, Function<S, T>> actionMap, S value) {
        return actionMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(value))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private Optional<Function<S, T>> findMatchingFunction(S value) {
        return findMatchingFunction(this.actionMap, value);
    }

    private static <S, U> void assertCurrentFunctionAndPredicateAreValid(Function<S, U> function, Predicate<S> predicate) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(predicate);
    }

    public record ConditionalAction<S, T>(Predicate<S> condition, Function<S, T> action) {

        public ConditionalAction {
            Objects.requireNonNull(condition);
            Objects.requireNonNull(action);
        }
    }
}