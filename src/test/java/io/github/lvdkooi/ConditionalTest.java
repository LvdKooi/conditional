package io.github.lvdkooi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionalTest {

    @Nested
    @DisplayName("Tests for conditionals with orElse")
    class orElse {

        @Test
        @DisplayName("orElse: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(2)
                    .orElse(0);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        @DisplayName("orElse: when no condition matches, then the default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .orElse(0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("orElse: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(2)
                    .map(i -> String.format("And the number is: %d", i))
                    .orElse("No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("orElse: when no condition matches and the Conditional pipeline contains a map, then the default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .map(i -> String.format("And the number is: %d", i))
                    .orElse("No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        @DisplayName("orElse: when a condition matches and the matching function evaluates to null, then a null is being returned and the default value is ignored.")
        void conditionalWithOneConditionThatEvaluatesToTrueAndFunctionThatReturnsNull() {
            var outcome = Conditional.of(2)
                    .mapWhen((Integer i) -> null, isEven())
                    .orElse(0);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("orElse: when a null is passed as the object to be evaluated, then the default value is returned.")
        void conditionalPassingInANull_orElse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(null)
                    .orElse(0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("orElse: when no condition matches and the default value is null, then this default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_orElseWithNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .orElse(null);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("orElse: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue(0)
                    .orElse(9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("orElse: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the default value is returned.")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToFalse(0)
                    .orElse(9);

            assertThat(outcome).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("Tests for conditionals with orElseGet")
    class orElseGet {

        @Test
        @DisplayName("orElseGet: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesTTrue() {
            var outcome = Conditional.of(2)
                    .mapWhen(Object::toString, isEven())
                    .orElseGet(() -> "hello world");

            assertThat(outcome).isEqualTo("2");
        }

        @Test
        @DisplayName("orElseGet: when no condition matches, then the value evaluated from the default Supplier is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = Conditional.of(2)
                    .mapWhen(Object::toString, isEven().negate())
                    .orElseGet(() -> "hello world");

            assertThat(outcome).isEqualTo("hello world");
        }

        @Test
        @DisplayName("orElseGet: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(2)
                    .map(i -> String.format("And the number is: %d", i))
                    .orElseGet(() -> "No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("orElseGet: when no condition matches and the Conditional pipeline contains a map, then the value evaluated from the default Supplier is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .map(i -> String.format("And the number is: %d", i))
                    .orElseGet(() -> "No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        @DisplayName("orElseGet: when a null is passed as the object to be evaluated, then the value evaluated from the default Supplier is returned.")
        void conditionalPassingInANull_orElseGet() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(null)
                    .orElseGet(() -> 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("orElseGet: when no condition matches and the default Supplier evaluates to null, then this null is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_orElseGetReturningNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .orElseGet(() -> null);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("orElseGet: when a condition matches and the matching function evaluates to null, then a null is being returned and the default Supplier is ignored.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingFunctionThatReturnsNull() {
            var outcome = Conditional.of(2)
                    .mapWhen(i -> null, isEven())
                    .orElseGet(() -> "hello world");

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("orElseGet: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue(0)
                    .orElseGet(() -> 9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("orElseGet: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the value evaluated from the default Supplier is returned.")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToFalse(0)
                    .orElseGet(() -> 9);

            assertThat(outcome).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("Tests for conditionals with orElseThrow")
    class orElseThrow {

        @Test
        @DisplayName("orElseThrow: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(2)
                    .orElseThrow(IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        @DisplayName("orElseThrow: when no condition matches, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven(3)
                            .orElseThrow(IllegalArgumentException::new));
        }

        @Test
        @DisplayName("orElseThrow: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven(2)
                    .map(i -> String.format("And the number is: %d", i))
                    .orElseThrow(IllegalArgumentException::new);

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("orElseThrow: when no condition matches and the Conditional pipeline contains a map, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven(3)
                            .map(i -> String.format("And the number is: %d", i))
                            .orElseThrow(IllegalArgumentException::new));
        }

        @Test
        @DisplayName("orElseThrow: when a null is passed as the object to be evaluated, then the exception supplier is evaluated (throwing an exception).")
        void conditionalPassingInANull() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven(null)
                            .orElseThrow(IllegalArgumentException::new));
        }

        @Test
        @DisplayName("orElseThrow: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue(0)
                    .orElseThrow(IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("orElseThrow: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            assertThrows(IllegalArgumentException.class, () -> conditionalWithMultipleConditionsThatEvaluateToFalse(0)
                    .orElseThrow(IllegalArgumentException::new));
        }
    }

    @Nested
    @DisplayName("Tests for wrong use of the Conditional")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Exception Handling: when a orMapWhen is placed before mapWhen in the Conditional pipeline, an IllegalArgumentException is thrown containing a clear message.")
        void orMapWhenShouldNotBePlacedBeforeMapWhen() {
            assertThat(assertThrows(IllegalArgumentException.class, () -> Conditional.of(1)
                    .orMapWhen(timesTwo(), returnTrue())
                    .mapWhen(square(), returnFalse())
                    .orElse(3456)).getMessage())
                    .isEqualTo("orMapWhen shouldn't be the first operation after calling Conditional.of(x), start with mapWhen instead");
        }

        @Test
        @DisplayName("Exception Handling: when a null is passed as the second parameter of a mapWhen, an NPE is thrown.")
        void predicateShouldNotBeNull() {
            assertThrows(NullPointerException.class, () -> Conditional.of(1)
                    .mapWhen(square(), null)
                    .orElse(3456));
        }

        @Test
        @DisplayName("Exception Handling: when an orMapWhen containing null is added to the Conditional pipeline, an NPE is thrown.")
        void functionShouldNotBeNull_orMapWhen() {
            assertThrows(NullPointerException.class, () -> Conditional.of(1)
                    .mapWhen(timesTwo(), returnTrue())
                    .orMapWhen(null, returnFalse())
                    .orElse(3456));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline starts with an.mapWhen containing null, an NPE is thrown.")
        void functionShouldNotBeNullmapWhen() {
            assertThrows(NullPointerException.class, () -> Conditional
                    .of(1).mapWhen(null, null)
                    .orMapWhen(null, null)
                    .orElse(3456));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline contains an orElseThrow containing a null throwableSupplier, an NPE is thrown.")
        void conditionalWithOneConditionThatEvaluatesToFalse_passingNullThrowableSupplierToOrElseThrow() {
            assertThrows(NullPointerException.class, () -> conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .orElseThrow(null));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline contains an orElseGet containing a null Supplier, an NPE is thrown.")
        void conditionalWithOneConditionThatEvaluatesToFalse_passingNullSupplierToOrElseGet() {
            assertThrows(NullPointerException.class, () -> conditionalThatMultipliesBy2WhenNumberIsEven(3)
                    .orElseGet(null));
        }
    }

    private static Conditional<Integer, Integer> conditionalWithMultipleConditionsThatEvaluateToTrue(Integer number) {
        return Conditional.of(number)
                .mapWhen(plus(1), returnFalse())
                .orMapWhen(plus(2), returnFalse())
                .orMapWhen(plus(3), returnTrue())
                .orMapWhen(plus(4), returnTrue())
                .orMapWhen(plus(5), returnTrue())
                .orMapWhen(plus(6), returnFalse());
    }

    private static Conditional<Integer, Integer> conditionalWithMultipleConditionsThatEvaluateToFalse(Integer number) {
        return Conditional.of(number)
                .mapWhen(plus(1), returnFalse())
                .orMapWhen(plus(2), returnFalse())
                .orMapWhen(plus(3), returnFalse())
                .orMapWhen(plus(4), returnFalse())
                .orMapWhen(plus(5), returnFalse())
                .orMapWhen(plus(6), returnFalse());
    }

    private static Conditional<Integer, Integer> conditionalThatMultipliesBy2WhenNumberIsEven(Integer number) {
        return Conditional.of(number)
                .mapWhen(timesTwo(), isEven());
    }

    private static UnaryOperator<Integer> timesTwo() {
        return i -> i * 2;
    }

    private static UnaryOperator<Integer> square() {
        return i -> i * i;
    }

    private static Predicate<Integer> isEven() {
        return i -> i % 2 == 0;
    }

    private static UnaryOperator<Integer> plus(int plus) {
        return i -> i + plus;
    }

    private static Predicate<Integer> returnFalse() {
        return i -> false;
    }

    private static Predicate<Integer> returnTrue() {
        return i -> true;
    }
}