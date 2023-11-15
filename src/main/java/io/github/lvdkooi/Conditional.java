package io.github.lvdkooi;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
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

    public static <T> Conditional<T, T> of(T value) {
        return new Conditional<>(new ArrayDeque<>(1), value);
    }

    public <U> Conditional<T, U> mapWhen(Function<T, U> function, Predicate<T> condition) {
        if (!actionQueue.isEmpty()) {
            throw new IllegalArgumentException("mapWhen should be the first operation after calling Conditional.of(x)");
        }

        assertCurrentFunctionAndPredicateAreValid(function, condition);

        var arrayDeque = new ArrayDeque<Pair<Predicate<T>, Function<T, U>>>(1);
        arrayDeque.add(new Pair<>(condition, function));

        return new Conditional<>(arrayDeque, value);
    }

    public Conditional<T, R> orMapWhen(Function<T, R> function, Predicate<T> condition) {
        if (actionQueue.isEmpty()) {
            throw new IllegalArgumentException("orMapWhen shouldn't be the first operation after calling Conditional.of(x), start with mapWhen instead");
        }

        assertCurrentFunctionAndPredicateAreValid(function, condition);
        var arrayDeque = new ArrayDeque<>(actionQueue);
        arrayDeque.add(new Pair<>(condition, function));

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

    private Optional<Function<T, R>> findMatchingFunction(T t) {
        return actionQueue
                .stream()
                .filter(entry -> entry.key().test(t))
                .findFirst()
                .map(Pair::value);
    }

    private <U> void assertCurrentFunctionAndPredicateAreValid(Function<T, U> function, Predicate<T> predicate) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(predicate);
    }

    private record Pair<T, R>(T key, R value) {

        private Pair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}