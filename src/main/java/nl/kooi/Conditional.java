package nl.kooi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Conditional<T, R> {
    private final Map<Predicate<T>, Function<T, R>> actionMap;
    private final Function<T, R> currentFunction;

    private Conditional(Map<Predicate<T>, Function<T, R>> actionMap, Function<T, R> currentFunction) {
        this.actionMap = actionMap;
        this.currentFunction = currentFunction;
    }

    public static <T, R> Conditional<T, R> apply(Function<T, R> callable) {
        return new Conditional<>(new LinkedHashMap<>(), callable);
    }

    public Conditional<T, R> when(Predicate<T> condition) {
        assertCurrentFunctionAndPredicateAreValid(condition);

        var map = new LinkedHashMap<>(actionMap);
        map.put(condition, currentFunction);

        return new Conditional<>(map, null);
    }

    public Conditional<T, R> orApply(Function<T, R> callable) {
        return new Conditional<>(this.actionMap, callable);
    }

    public <U> Conditional<T, U> map(Function<R, U> mapFunction) {
        var updatedMap = this.actionMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().andThen(mapFunction), (x, y) -> y, LinkedHashMap::new));

        return new Conditional<>(updatedMap, null);
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
        return actionMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(t))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private void assertCurrentFunctionAndPredicateAreValid(Predicate<T> predicate) {
        Objects.requireNonNull(currentFunction, "A predicate can only be added after an apply(Function<T, R> callable) or orApply(Function<T, R> callable)");
        Objects.requireNonNull(predicate, "Predicate is not nullable");
    }
}