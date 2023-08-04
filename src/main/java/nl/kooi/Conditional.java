package nl.kooi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Conditional<T, R> {
    private final Map<Predicate<T>, Function<T, R>> actionMap;
    private final Function<T, R> currentFunction;

    private Conditional(Map<Predicate<T>, Function<T, R>> actionMap, Function<T, R> currentFunction) {
        this.actionMap = actionMap;
        this.currentFunction = currentFunction;
    }

    private Conditional<T, R> addCondition(Predicate<T> predicate) {
        var map = new HashMap<>(actionMap);
        map.put(predicate, currentFunction);

        return new Conditional<>(map, null);
    }

    public Conditional<T, R> andCondition(Predicate<T> predicate,
                                          Function<T, R> callable) {
        actionMap.put(predicate, callable);
        return this;
    }

    public static <T, R> Conditional<T, R> apply(Function<T, R> callable) {
        return new Conditional<>(new HashMap<>(), callable);
    }

    public Conditional<T, R> when(Predicate<T> condition) {
        return addCondition(condition);
    }

    public Conditional<T, R> orApply(Function<T, R> callable) {
        return new Conditional<>(this.actionMap, callable);
    }

    public R toOrElseGet(T object, Supplier<R> supplier) {
        return actionMap.entrySet().stream()
                .filter(entry -> entry.getKey().test(object))
                .map(Map.Entry::getValue)
                .map(fn -> fn.apply(object))
                .findFirst()
                .orElseGet(supplier);
    }
}
