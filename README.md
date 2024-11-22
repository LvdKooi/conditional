# Conditional: A Monad-Inspired Container for Functional Conditional Logic in Java

The `Conditional` class is a monad-like container designed to facilitate the management of conditional operations in a functional way. Drawing inspiration from Java's `Optional`, `Stream`, and `CompletableFuture`, `Conditional` allows you to compose conditional logic into a functional pipeline, making it a clean and reusable alternative to traditional imperative `if-then-else` statements.

**Author:** _Laurens van der Kooi_

## Motivation

In traditional Java code, conditional business logic often requires multiple nested `if-else` statements: 

```
public static BigDecimal calculateNumber(int number) {
    if (number % 2 == 0) {
        return BigDecimal.valueOf(number * 2.5);
    }

    if (number > 100) {
        return BigDecimal.valueOf(number * 0.5);
    }

    return BigDecimal.ZERO;
}

```

This approach can become hard to read and maintain, especially when the logic involves many conditions. Inspired by monads in functional programming, I designed the `Conditional` type to express conditional logic more elegantly and compose it in a functional manner.



### The Conditional Class

The `Conditional` class is a custom monad-like container designed specifically for conditional operations. It offers a functional alternative to `if-then-else` statements, allowing you to chain conditions and their corresponding actions into a pipeline. This approach not only makes the code more readable but also more flexible and reusable.

### Key Operations

The `Conditional` class supports a variety of intermediate and terminal operations, including:

- **Intermediate Operations**:
  - `firstMatching`: Takes a list of conditions and actions, applying the first action whose condition matches the value.
  - `map`: Transforms the value in the container using a provided function.
  - `flatMap`: Allows chaining conditional operations by returning another `Conditional` from a transformation.

- **Terminal Operations**:
  - `orElse`: Returns the value inside the `Conditional`, or a default value if no conditions match.
  - `orElseGet`: Returns the value or lazily evaluates a fallback function if no conditions match.
  - `orElseThrow`: Throws an exception if no conditions match.

## Example Usage

Here’s an example of how to use `Conditional` to apply conditional logic functionally:

```java
public static BigDecimal calculateNumber(int number) {
    return Conditional.of(number)
            .firstMatching(
                    applyIf(isEven(), times(2.5)),
                    applyIf(isLargerThan(100), times(0.5))
            )
            .map(BigDecimal::valueOf)
            .orElse(BigDecimal.ZERO);
}

private static Function<Integer, Double> times(double factor) {
    return i -> i * factor;
}

private static Predicate<Integer> isEven() {
    return i -> i % 2 == 0;
}

private static Predicate<Integer> isLargerThan(int threshold) {
    return i -> i > threshold;
}
```

In this example:
- We use `Conditional.of(number)` to wrap the `number` in a `Conditional`.
- We apply two conditions using `applyIf` (whether the number is even or greater than 100) and their corresponding actions (multiply by 2.5 or 0.5).
- We then transform the result into a `BigDecimal` using `map`.
- If no condition matches, we return a default value (`BigDecimal.ZERO`) using `orElse`.

## How It Works

The `Conditional` class follows the concept of monads, encapsulating values and offering methods to transform them. Here are the key components:

1. **Unit Operation**: `Conditional.of(value)` wraps a value in a `Conditional` container.
2. **FlatMap**: The `flatMap` method allows chaining operations that return other `Conditional` containers, enabling more complex transformations.
3. **Binding Functions**: `map` and `firstMatching` are used to apply functions and conditions to the value inside the `Conditional`.
4. **Terminal Operations**: Operations like `orElse`, `orElseGet`, and `orElseThrow` provide ways to retrieve the value or handle situations where no conditions match.

## Benefits of Using `Conditional`

- **Functional Composition**: Conditional logic can be composed into a pipeline, making it more readable and maintainable.
- **Immutability**: The `Conditional` class is immutable, ensuring that transformations don't alter the original value.
- **Extensibility**: New conditions and actions can be added without changing the existing logic, making the code more flexible.

## Installation

**Apache Maven**

If you’re using Maven to build your project, add the following to your `pom.xml` to use the Conditional:

```xml
<dependency>
    <groupId>io.github.lvdkooi</groupId>
    <artifactId>conditional</artifactId>
    <version>2.0.0</version>
</dependency>
```

**Gradle**

If you’re using Gradle to build your project, add the following to your `build.gradle` to use the Conditional:

```gradle
implementation 'io.github.lvdkooi:conditional:2.0.0'
```

## Conclusion

The `Conditional` class is a powerful tool for developers who want to leverage functional programming techniques in Java. By encapsulating conditional logic into a monad-like container, it simplifies complex conditional operations and makes the code more declarative and maintainable. Try it out in your next project to see how functional pipelines can improve your conditional logic!