package io.github.lvdkooi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Conditional<S, T> {

    private final Queue<Pair<Predicate<S>, Function<S, T>>> actionQueue;
    private final S value;

    private Conditional(Queue<Pair<Predicate<S>, Function<S, T>>> actionQueue, S value) {
        this.actionQueue = actionQueue;
        this.value = value;
    }

    private static <S, T> Conditional<S, T> empty() {
        return new Conditional<>(new ArrayDeque<>(), null);
    }

    public static <S> Conditional<S, S> of(S value) {
        return new Conditional<>(new ArrayDeque<>(1), value);
    }

    public static <S, U> Pair<Predicate<S>, Function<S, U>> applyIf(Predicate<S> condition, Function<S, U> function) {
        assertCurrentFunctionAndPredicateAreValid(function, condition);
        return new Pair<>(condition, function);
    }

    @SafeVarargs
    public final <U> Conditional<S, U> firstMatching(Pair<Predicate<S>, Function<S, U>>... actions) {
        var arrayDeque = Arrays.stream(actions)
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(arrayDeque, value);
    }

    public <U> Conditional<S, U> map(Function<T, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(mapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(queue, value);
    }

    public <U> Conditional<T, U> flatMap(Function<T, Conditional<T, U>> flatMapFunction) {
        Objects.requireNonNull(flatMapFunction);

        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(flatMapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return findMatchingFunction(queue, this.value)
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

    private static <S, T> Optional<Function<S, T>> findMatchingFunction(Queue<Pair<Predicate<S>, Function<S, T>>> actionQueue, S value) {
        return actionQueue
                .stream()
                .filter(entry -> entry.key().test(value))
                .findFirst()
                .map(Pair::value);
    }

    private Optional<Function<S, T>> findMatchingFunction(S value) {
        return findMatchingFunction(this.actionQueue, value);
    }

    private static <S, U> void assertCurrentFunctionAndPredicateAreValid(Function<S, U> function, Predicate<S> predicate) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(predicate);
    }

    public record Pair<S, T>(S key, T value) {

        public Pair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}