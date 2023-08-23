package nl.laurensvanderkooi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionalTest {

    @Nested
    @DisplayName("Tests for conditionals with applyToOrElse")
    class applyToOrElse {

        @Test
        @DisplayName("applyToOrElse: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(2, 0);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        @DisplayName("applyToOrElse: when no condition matches, then the default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(3, 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("applyToOrElse: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElse(2, "No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("applyToOrElse: when no condition matches and the Conditional pipeline contains a map, then the default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElse(3, "No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        @DisplayName("applyToOrElse: when a condition matches and the matching function evaluates to null, then a null is being returned and the default value is ignored.")
        void conditionalWithOneConditionThatEvaluatesToTrueAndFunctionThatReturnsNull() {
            var outcome = Conditional
                    .apply((Integer i) -> null)
                    .when(isEven())
                    .applyToOrElse(2, 0);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("applyToOrElse: when a null is passed as the object to be evaluated, then the default value is returned.")
        void conditionalPassingInANull_applyingOrElse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(null, 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("applyToOrElse: when no condition matches and the default value is null, then this default value is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseWithNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(3, null);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("applyToOrElse: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElse(0, 9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("applyToOrElse: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the default value is returned.")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToFalse()
                    .applyToOrElse(0, 9);

            assertThat(outcome).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("Tests for conditionals with applyToOrElseGet")
    class applyToOrElseGet {

        @Test
        @DisplayName("applyToOrElseGet: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesTTrue() {
            var outcome = Conditional
                    .apply((Integer integer) -> integer.toString())
                    .when(isEven())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isEqualTo("2");
        }

        @Test
        @DisplayName("applyToOrElseGet: when no condition matches, then the value evaluated from the default Supplier is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = Conditional
                    .apply((Integer integer) -> integer.toString())
                    .when(isEven().negate())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isEqualTo("hello world");
        }

        @Test
        @DisplayName("applyToOrElseGet: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseGet(2, () -> "No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("applyToOrElseGet: when no condition matches and the Conditional pipeline contains a map, then the value evaluated from the default Supplier is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseGet(3, () -> "No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        @DisplayName("applyToOrElseGet: when a null is passed as the object to be evaluated, then the value evaluated from the default Supplier is returned.")
        void conditionalPassingInANull_applyingOrElse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseGet(null, () -> 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        @DisplayName("applyToOrElseGet: when no condition matches and the default Supplier evaluates to null, then this null is returned.")
        void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseGetReturningNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseGet(3, () -> null);

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("applyToOrElseGet: when a condition matches and the matching function evaluates to null, then a null is being returned and the default Supplier is ignored.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingFunctionThatReturnsNull() {
            var outcome = Conditional
                    .apply((Integer integer) -> null)
                    .when(isEven())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isNull();
        }

        @Test
        @DisplayName("applyToOrElseGet: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElseGet(0, () -> 9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("applyToOrElseGet: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the value evaluated from the default Supplier is returned.")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToFalse()
                    .applyToOrElseGet(0, () -> 9);

            assertThat(outcome).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("Tests for conditionals with applyToOrElseThrow")
    class applyToOrElseThrow {
        @Test
        @DisplayName("applyToOrElseThrow: when a condition matches, then the matching function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseThrow(2, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        @DisplayName("applyToOrElseThrow: when no condition matches, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .applyToOrElseThrow(3, IllegalArgumentException::new));
        }

        @Test
        @DisplayName("applyToOrElseThrow: when a condition matches and the Conditional pipeline contains a map, then the matching function is first applied and then the map function is applied to the object.")
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseThrow(2, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        @DisplayName("applyToOrElseThrow: when no condition matches and the Conditional pipeline contains a map, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .map(i -> String.format("And the number is: %d", i))
                            .applyToOrElseThrow(3, IllegalArgumentException::new));
        }

        @Test
        @DisplayName("applyToOrElseThrow: when a null is passed as the object to be evaluated, then the exception supplier is evaluated (throwing an exception).")
        void conditionalPassingInANull() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .applyToOrElseThrow(null, IllegalArgumentException::new));
        }

        @Test
        @DisplayName("applyToOrElseThrow: when multiple conditions would return true, then only the function belonging to the first condition that evaluated to true is applied.")
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElseThrow(0, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        @DisplayName("applyToOrElseThrow: when the Conditional pipeline contain multiple conditions that all evaluate to false, then the exception supplier is evaluated (throwing an exception).")
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            assertThrows(IllegalArgumentException.class, () -> conditionalWithMultipleConditionsThatEvaluateToFalse()
                    .applyToOrElseThrow(0, IllegalArgumentException::new));
        }
    }

    @Nested
    @DisplayName("Tests for wrong use of the Conditional")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Exception Handling: when a when is placed before orApply in the Conditional pipeline, an NPE is thrown containing a clear message.")
        void whenShouldNotBePlacedBeforeOrApply() {
            assertThat(assertThrows(NullPointerException.class, () -> Conditional.apply((Integer i) -> i * i)
                    .when(returnTrue())
                    .when(returnTrue())
                    .orApply(i -> i * 2)
                    .applyToOrElse(1, 3456)).getMessage())
                    .isEqualTo("The function that belongs to this condition is not yet set. A predicate can " +
                            "only be added after an apply(Function<T, R> function) or orApply(Function<T, R> function).");
        }

        @Test
        @DisplayName("Exception Handling: when a when containing null is added to the Conditional pipeline, an NPE is thrown.")
        void predicateShouldNotBeNull() {
            assertThrows(NullPointerException.class, () -> Conditional.apply((Integer i) -> i * i)
                    .when(null)
                    .applyToOrElse(1, 3456));
        }

        @Test
        @DisplayName("Exception Handling: when an orApply containing null is added to the Conditional pipeline, an NPE is thrown.")
        void functionShouldNotBeNull_orApply() {
            assertThrows(NullPointerException.class, () -> Conditional.apply((Integer i) -> i * 2)
                    .when(returnTrue())
                    .orApply(null)
                    .when(returnFalse())
                    .applyToOrElse(1, 3456));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline starts with an apply containing null, an NPE is thrown.")
        void functionShouldNotBeNull_apply() {
            assertThrows(NullPointerException.class, () -> Conditional.apply(null)
                    .when(null)
                    .orApply(null)
                    .when(null)
                    .applyToOrElse(1, 3456));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline contains an applyToOrElseThrow containing a null throwableSupplier, an NPE is thrown.")
        void conditionalWithOneConditionThatEvaluatesToFalse_passingNullThrowableSupplierToOrElseThrow() {
            assertThrows(NullPointerException.class, () -> conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseThrow(3, null));
        }

        @Test
        @DisplayName("Exception Handling: when the Conditional pipeline contains an applyToOrElseGet containing a null Supplier, an NPE is thrown.")
        void conditionalWithOneConditionThatEvaluatesToFalse_passingNullSupplierToOrElseGet() {
            assertThrows(NullPointerException.class, () -> conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseGet(3, null));
        }
    }

    private static Conditional<Integer, Integer> conditionalWithMultipleConditionsThatEvaluateToTrue() {
        return Conditional
                .apply(plus(1)).when(returnFalse())
                .orApply(plus(2)).when(returnFalse())
                .orApply(plus(3)).when(returnTrue())
                .orApply(plus(4)).when(returnTrue())
                .orApply(plus(5)).when(returnTrue())
                .orApply(plus(6)).when(returnFalse());
    }

    private static Conditional<Integer, Integer> conditionalWithMultipleConditionsThatEvaluateToFalse() {
        return Conditional
                .apply(plus(1)).when(returnFalse())
                .orApply(plus(2)).when(returnFalse())
                .orApply(plus(3)).when(returnFalse())
                .orApply(plus(4)).when(returnFalse())
                .orApply(plus(5)).when(returnFalse())
                .orApply(plus(6)).when(returnFalse());
    }

    private static Conditional<Integer, Integer> conditionalThatMultipliesBy2WhenNumberIsEven() {
        return Conditional
                .apply(timesTwo())
                .when(isEven());
    }

    private static UnaryOperator<Integer> timesTwo() {
        return i -> i * 2;
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