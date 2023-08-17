package nl.kooi;

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
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(2, 0);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(3, 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElse(2, "No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElse(3, "No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToTrueAndFunctionThatReturnsNull() {
            var outcome = Conditional
                    .apply((Integer i) -> null)
                    .when(isEven())
                    .applyToOrElse(2, 0);

            assertThat(outcome).isNull();
        }

        @Test
        void conditionalPassingInANull_applyingOrElse() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(null, 0);

            assertThat(outcome).isEqualTo(0);
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseWithNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElse(3, null);

            assertThat(outcome).isNull();
        }

        @Test
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElse(0, 9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
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
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            var outcome = Conditional
                    .apply((Integer integer) -> integer.toString())
                    .when(isEven().negate())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isEqualTo("hello world");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesTTrue() {
            var outcome = Conditional
                    .apply((Integer integer) -> integer.toString())
                    .when(isEven())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isEqualTo("2");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseGet(2, () -> "No outcome");

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseGet(3, () -> "No outcome");

            assertThat(outcome).isEqualTo("No outcome");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseGetReturningNull() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseGet(3, () -> null);

            assertThat(outcome).isNull();
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingFunctionThatReturnsNull() {
            var outcome = Conditional
                    .apply((Integer integer) -> null)
                    .when(isEven())
                    .applyToOrElseGet(2, () -> "hello world");

            assertThat(outcome).isNull();
        }

        @Test
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElseGet(0, () -> 9);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
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
        void conditionalWithOneConditionThatEvaluatesToTrue() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseThrow(2, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(4);
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .applyToOrElseThrow(3, IllegalArgumentException::new));
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToTrue_applyingMap() {
            var outcome = conditionalThatMultipliesBy2WhenNumberIsEven()
                    .map(i -> String.format("And the number is: %d", i))
                    .applyToOrElseThrow(2, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo("And the number is: 4");
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_ignoringMap() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .map(i -> String.format("And the number is: %d", i))
                            .applyToOrElseThrow(3, IllegalArgumentException::new));
        }

        @Test
        void conditionalPassingInANull() {
            assertThrows(IllegalArgumentException.class, () ->
                    conditionalThatMultipliesBy2WhenNumberIsEven()
                            .applyToOrElseThrow(null, IllegalArgumentException::new));
        }

        @Test
        void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
            var outcome = conditionalWithMultipleConditionsThatEvaluateToTrue()
                    .applyToOrElseThrow(0, IllegalArgumentException::new);

            assertThat(outcome).isEqualTo(3);
        }

        @Test
        void conditionalWithMultipleConditionsThatAllEvaluateToFalse() {
            assertThrows(IllegalArgumentException.class, () -> conditionalWithMultipleConditionsThatEvaluateToFalse()
                    .applyToOrElseThrow(0, IllegalArgumentException::new));
        }
    }

    @Nested
    @DisplayName("Tests for wrong use of the Conditional")
    class ExceptionHandlingTests {

        @Test
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
        void predicateShouldNotBeNull() {
            assertThrows(NullPointerException.class, () -> Conditional.apply((Integer i) -> i * i)
                    .when(null)
                    .applyToOrElse(1, 3456));
        }

        @Test
        void functionShouldNotBeNull_orApply() {
            assertThrows(NullPointerException.class, () -> Conditional.apply((Integer i) -> i * 2)
                    .when(returnTrue())
                    .orApply(null)
                    .when(returnFalse())
                    .applyToOrElse(1, 3456));
        }

        @Test
        void functionShouldNotBeNull_apply() {
            assertThrows(NullPointerException.class, () -> Conditional.apply(null)
                    .when(null)
                    .orApply(null)
                    .when(null)
                    .applyToOrElse(1, 3456));
        }

        @Test
        void conditionalWithOneConditionThatEvaluatesToFalse_passingNullThrowableSupplierToOrElseThrow() {
            assertThrows(NullPointerException.class, () -> conditionalThatMultipliesBy2WhenNumberIsEven()
                    .applyToOrElseThrow(3, null));
        }

        @Test
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