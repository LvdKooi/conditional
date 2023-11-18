package io.github.lvdkooi;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Conditional<T, R> {
    private final Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue;
    private final T value;

    private Conditional(Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue, T value) {
        this.actionQueue = actionQueue;
        this.value = value;
    }

    private static <T, R> Conditional<T, R> empty() {
        return new Conditional<>(new ArrayDeque<>(), null);
    }

    public static <T> Conditional<T, T> of(T value) {
        return new Conditional<>(new ArrayDeque<>(1), value);
    }

    public static <T, U> Pair<Predicate<T>, Function<T, U>> applyIf(Predicate<T> condition, Function<T, U> function) {
        assertCurrentFunctionAndPredicateAreValid(function, condition);
        return new Pair<>(condition, function);
    }

    @SafeVarargs
    public final <U> Conditional<T, U> firstMatching(Pair<Predicate<T>, Function<T, U>>... actions) {
        var arrayDeque = Arrays.stream(actions)
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(arrayDeque, value);
    }

    public <U> Conditional<T, U> map(Function<R, U> mapFunction) {
        Objects.requireNonNull(mapFunction);

        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(mapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(queue, value);
    }

    public <U> Conditional<R, U> flatMap(Function<R, Conditional<R, U>> flatMapFunction) {
        Objects.requireNonNull(flatMapFunction);

        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(flatMapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return findMatchingFunction(queue, this.value)
                .map(function -> function.apply(this.value))
                .orElseGet(Conditional::empty);
    }

    public R orElseGet(Supplier<? extends R> supplier) {
        Objects.requireNonNull(supplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> supplier.get())
                .apply(value);
    }

    public R orElse(R defaultValue) {
        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> defaultValue)
                .apply(value);
    }

    public <X extends Throwable> R orElseThrow(Supplier<? extends X> throwableSupplier) throws X {
        Objects.requireNonNull(throwableSupplier);

        return Optional.ofNullable(value)
                .flatMap(this::findMatchingFunction)
                .orElseThrow(throwableSupplier)
                .apply(value);
    }

    private static <T, R> Optional<Function<T, R>> findMatchingFunction(Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue, T t) {
        return actionQueue
                .stream()
                .filter(entry -> entry.key().test(t))
                .findFirst()
                .map(Pair::value);
    }

    private Optional<Function<T, R>> findMatchingFunction(T t) {
        return findMatchingFunction(this.actionQueue, t);
    }

    private static <T, U> void assertCurrentFunctionAndPredicateAreValid(Function<T, U> function, Predicate<T> predicate) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(predicate);
    }

    public record Pair<T, R>(T key, R value) {

        public Pair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}