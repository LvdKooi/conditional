package nl.kooi;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConditionalTest {

    @Test
    void callableWithOneConditionThatEvaluatesToTrue() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(2, 0);

        assertThat(outcome).isEqualTo(4);
    }

    @Test
    void callableWithOneConditionThatEvaluatesToFalse_applyingOrElse() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(3, 0);

        assertThat(outcome).isEqualTo(0);
    }

    @Test
    void callablePassingInANull_applyingOrElse() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(null, 0);

        assertThat(outcome).isEqualTo(0);
    }

    @Test
    void callableWithOneConditionThatEvaluatesToFalse_applyingOrElseWithNull() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElse(3, null);

        assertThat(outcome).isNull();
    }

    @Test
    void callableWithOneConditionThatEvaluatesToFalse_applyingOrElseGetReturningNull() {
        var outcome = Conditional
                .apply(timesTwo())
                .when(isEven())
                .applyToOrElseGet(3, () -> null);

        assertThat(outcome).isNull();
    }

    @Test
    void callableWithOneConditionThatEvaluatesToFalse_applyingOrElseThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                Conditional
                        .apply(timesTwo())
                        .when(isEven())
                        .applyToOrElseThrow(3, IllegalArgumentException::new));
    }

    @Test
    void callablePassingInANull_applyingOrElseThrow() {
        assertThrows(IllegalArgumentException.class, () ->
                Conditional
                        .apply(timesTwo())
                        .when(isEven())
                        .applyToOrElseThrow(null, IllegalArgumentException::new));
    }

    @Test
    void callableWithOneConditionThatEvaluatesToFalse_applyingOrElseGet() {
        var outcome = Conditional
                .apply((Integer integer) -> integer.toString())
                .when(isEven().negate())
                .applyToOrElseGet(2, () -> "hello world");

        assertThat(outcome).isEqualTo("hello world");
    }

    @Test
    void callableWithMultipleConditionsThatEvaluateToTrue_functionOfFirstTrueIsEvaluated() {
        var outcome = Conditional
                .apply((Integer i) -> i + 1)
                .when(i -> false)
                .orApply(i -> i + 2)
                .when(i -> false)
                .orApply(i -> i + 3)
                .when(i -> true)
                .orApply(i -> i + 4)
                .when(i -> true)
                .orApply(i -> i + 5)
                .when(i -> true)
                .orApply(i -> i + 6)
                .when(i -> false)
                .applyToOrElse(0, 9);

        assertThat(outcome).isEqualTo(3);
    }

    @Test
    void callableWithOneConditionThatEvaluatesToTrue_applyingAMapFunction() {
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
}