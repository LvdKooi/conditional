package nl.kooi;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionalTest {

    @Test
    void conditionalWithOneConditionThatEvaluatesToTrue() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(2, 0);

        assertThat(outcome).isEqualTo(4);
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
    void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElse() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(3, 0);

        assertThat(outcome).isEqualTo(0);
    }

    @Test
    void conditionalPassingInANull_applyingOrElse() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(null, 0);

        assertThat(outcome).isEqualTo(0);
    }

    @Test
    void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseWithNull() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(3, null);

        assertThat(outcome).isNull();
    }

    @Test
    void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseGetReturningNull() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElseGet(3, () -> null);

        assertThat(outcome).isNull();
    }

    @Test
    void conditionalWithOneConditionThatEvaluatesToTrue_applyingOrElseThrow() {
        var outcome =
                Conditional
                        .apply(timesTwo())
                        .when(isEven())
                        .applyToOrElseThrow(2, IllegalArgumentException::new);

        assertThat(outcome).isEqualTo(4);
    }

    @Test
    void conditionalPassingInANull_applyingOrElseThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                Conditional
                        .apply(timesTwo())
                        .when(isEven())
                        .applyToOrElseThrow(null, IllegalArgumentException::new));
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
    void conditionalWithOneConditionThatEvaluatesToFalse_applyingOrElseGet() {
        var outcome = Conditional
                .apply((Integer integer) -> integer.toString())
                .when(isEven().negate())
                .applyToOrElseGet(2, () -> "hello world");

        assertThat(outcome).isEqualTo("hello world");
    }

    @Test
    void conditionalWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
        var outcome = Conditional
                .apply(plus(1))
                .when(returnFalse())
                .orApply(plus(2))
                .when(returnFalse())
                .orApply(plus(3))
                .when(returnTrue())
                .orApply(plus(4))
                .when(returnTrue())
                .orApply(plus(5))
                .when(returnTrue())
                .orApply(plus(6))
                .when(returnFalse())
                .applyToOrElse(0, 9);

        assertThat(outcome).isEqualTo(3);
    }

    @Test
    void conditionalWithOneConditionThatEvaluatesToTrue_applyingAMapFunction() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .map(i -> String.format("And the number is: %d", i))
                .applyToOrElseGet(2, () -> "No outcome");

        assertThat(outcome).isEqualTo("And the number is: 4");
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