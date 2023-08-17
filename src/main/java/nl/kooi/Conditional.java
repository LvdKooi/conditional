package nl.kooi;

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
    private final Function<T, R> currentFunction;

    private Conditional(Queue<Pair<Predicate<T>, Function<T, R>>> actionQueue, Function<T, R> currentFunction) {
        this.actionQueue = actionQueue;
        this.currentFunction = currentFunction;
    }

    public static <T, R> Conditional<T, R> apply(Function<T, R> callable) {
        return new Conditional<>(new ArrayDeque<>(1), callable);
    }

    public Conditional<T, R> when(Predicate<T> condition) {
        assertCurrentFunctionAndPredicateAreValid(condition);

        var queue = new ArrayDeque<>(this.actionQueue);
        queue.add(new Pair<>(condition, currentFunction));

        return new Conditional<>(queue, null);
    }

    public Conditional<T, R> orApply(Function<T, R> callable) {
        return new Conditional<>(this.actionQueue, callable);
    }

    public <U> Conditional<T, U> map(Function<R, U> mapFunction) {
        var queue = this.actionQueue
                .stream()
                .map(pair -> new Pair<>(pair.key(), pair.value().andThen(mapFunction)))
                .collect(Collectors.toCollection(ArrayDeque::new));

        return new Conditional<>(queue, null);
    }

    public R applyToOrElseGet(T object, Supplier<? extends R> supplier) {
        Objects.requireNonNull(supplier);

        return findMatchingFunction(object)
                .orElseGet(() -> obj -> supplier.get())
                .apply(object);
    }

    public R applyToOrElse(T object, R defaultValue) {
        return Optional.ofNullable(object)
                .flatMap(this::findMatchingFunction)
                .orElseGet(() -> obj -> defaultValue)
                .apply(object);
    }

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
        Objects.requireNonNull(currentFunction, "A predicate can only be added after an apply(Function<T, R> callable) or orApply(Function<T, R> callable)");
        Objects.requireNonNull(predicate, "Predicate is not nullable");
    }

    private record Pair<T, R>(T key, R value) {

        private Pair {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
        }
    }
}