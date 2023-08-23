# The Conditional Monad: Structuring Conditional Actions

## Introduction
Drawing inspiration from Java's Optional, the Conditional Monad offers a way to organize conditional actions using a monadic approach. It also provides the capability to default to a predefined action or value when no conditions are met.

An illustrative example is as follows:

```
    private static final Double ZERO = 0.0;

    public Double calculateNumber(int number) {
        return Conditional
                .apply((Integer i) -> i * 2.5)
                .when(i -> i % 2 == 0)
                .orApply(i -> i / 2.0)
                .when(i -> i > 100)
                .applyToOrElse(number, ZERO);
    }
```

In this instance, the scenario can be distilled as follows:

- Take an integral number;
- If the number is even, multiply the number by 2.5;
- Otherwise, if the number is greater than 100, halve it;
- If none of the conditions are satisfied, default to 0.0.

In order to maintain code readability, it is recommended to use this monad with conditions (that are used in the _when_ methods) and actions (which are used in the _apply/orApply_ methods) that are encapsulated within separate methods.

This would look like this:

```
    private static final Double ZERO = 0.0;

    public Double calculateNumber(int number) {
        return Conditional.apply(timesTwoAndAHalf())
                .when(isEven())
                .orApply(halfIt())
                .when(isLargerThan(100))
                .applyToOrElse(number, ZERO);
    }
    
    private static Function<Integer, Double> timesTwoAndAHalf() {
        return i -> i * 2.5;
    }

    private static Function<Integer, Double> halfIt() {
        return i -> i / 2.0;
    }

    private static Predicate<Integer> isEven() {
        return i -> i % 2 == 0;
    }

    private static Predicate<Integer> isLargerThan(int number) {
        return i -> i > number;
    }
```

A Conditional pipeline is composed of intermediate operations and a terminal operation. The following provides descriptions of each.

## Intermediate operations

### Defining action/condition pairs using _apply_, _when_ and _orApply_
The Conditional encompasses 3 intermediate operations that serve to define an action alongside the corresponding condition, which must evaluate as true for the action to be executed.

These are:
- **apply**: which is necessary to create the Conditional. This method receives a Function<T, R>, where T is the type of the value which is the input for the Conditonal, and R is the value that is returned by the function.
- **when**: which is the condition that must evaluate to true for the previous function to be applied. This method takes a Predicate<T>, where T denotes the input value type for the Conditional.
- **orApply**: which outlines the subsequent action to be executed exclusively when the subsequent condition evaluates to true, under the condition that none of the preceding conditions have evaluated to true. Just like _apply_, this method receives a Function<T, R>.

The _apply_ should consistently be succeeded by a corresponding when, and similarly, this pattern applies to _orApply_ as well.

Conditions are assessed in the same order as they were chained within the Conditional pipeline. Please be aware that conditions and actions are lazy evaluated, meaning that only the action of the first condition that evaluates to true is executed, leaving all other conditions and actions unevaluated.

An example containing one action/condition pair:

```
Conditional
  .apply(timesTwoAndAHalf())
  .when(isEven())
```

An example containing multiple action/condition pairs:

```
Conditional
  .apply(timesTwoAndAHalf())
  .when(isEven())
  .orApply(halfIt())
  .when(isLargerThan(100))
```

### Transforming the value coming from the applied function with _map_
The Conditional includes a single intermediate operation responsible for converting the value derived from the applied action into a value of the subsequent type. Just like Java's Optional and Stream, this function is called _map_.

- **map**: which accepts a Function<R, U>, wherein R represents the potential value returned from the _apply_ or _orApply_ methods, and U denotes the type of the value to which this method is mapping. This method is an optional part of the Conditional pipeline.

An example containing a map function:

```
Conditional
  .apply(timesTwoAndAHalf())
  .when(isEven())
  .orApply(halfIt())
  .when(isLargerThan(100))
  .map(d -> String.format("The outcome was: %f", d))
```

In this example, if any of the conditions evaluates to true, the map function takes the Double originating from the action linked with that condition and maps it to a String.

## Terminal operations
The Conditional contains 3 terminal operations: _applyToOrElse_, _applyToOrElseGet_, _applyToOrElseThrow_. A terminal operation is an operation that marks the end of the Conditional pipeline. When a terminal operation is invoked on a Conditional, it triggers the actual processing of the intermediate operations and produces a result or a side-effect. A Conditional pipeline may only contain one terminal operation, whereas multiple intermediate operations are allowed.

### applyToOrElse
The applyToOrElse method takes 2 arguments: the first is the value that will be processed by the Conditional pipeline, the second a default value of the same type that would otherwise be returned by the pipeline if any condition in the pipeline evaluates to true. The default value is also returned when the first argument (the value to be processed) is null. This method is recommended if the default value is already in scope or is a constant. Putting a method call in the second argument is discouraged, since the second argument of this method is not lazy evaluated.  

```
    private static final Double ZERO = 0.0;

    public Double calculateNumber(int number) {
        return Conditional.apply(timesTwoAndAHalf())
                .when(isEven())
                .orApply(halfIt())
                .when(isLargerThan(100))
                .applyToOrElse(number, ZERO);
    }
```

### applyToOrElseGet
The applyToOrElseGet method takes 2 arguments: the first argument is the value that will be processed by the Conditional pipeline, the second a Supplier that returns an object of the same type that would otherwise be returned by the pipeline if any condition in the pipeline matches. The supplier is also invoked when the first argument (the value to be processed) is null. The second argument to this method is lazy evaluated, and suitable for operations that you would only like to perform when none of the conditions evaluates to true.

```
    public Double calculateNumber(int number) {
        return Conditional.apply(timesTwoAndAHalf())
                .when(isEven())
                .orApply(halfIt())
                .when(isLargerThan(100))
                .applyToOrElseGet(number, () -> doAlternateCalculation(number));
    }
```

### applyToOrElseThrow
The applyToOrElseThrow method takes 2 arguments: the first argument is the value that will be processed by the Conditional pipeline, the second a Supplier that returns a Throwable that will be thrown if no condition in the pipeline matches. This throwable is also thrown when the first argument (the value to be processed) is null. The second argument to this method is lazy evaluated.

```
    public Double calculateNumber(int number) {
        return Conditional.apply(timesTwoAndAHalf())
                .when(isEven())
                .orApply(halfIt())
                .when(isLargerThan(100))
                .applyToOrElseThrow(number, CalculationException::new);
    }
```
