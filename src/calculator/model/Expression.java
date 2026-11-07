package calculator.model; // This file belongs to the "model" package
 
import java.math.BigDecimal;   // exact decimal numbers
import java.math.RoundingMode; // how to round the final result
import java.util.ArrayList;    // a resizable list we can build step by step
import java.util.List;         // the general "list" type
import java.util.Objects;      // helper that combines several hash codes into one
 
/**
 * An IMMUTABLE arithmetic expression such as 2 + 3 * 4.
 * Once created, it can never change.
 * Its value uses standard precedence: * and / first, then + and -, each from left to right.
 */
public final class Expression { // "final": no other class may extend it and add changeable fields
 
    private final List<BigDecimal> operands; // the numbers, in order, e.g. [2, 3, 4]
    private final List<Operator> operators;  // the operators between them, e.g. [ADD, MULTIPLY]
// Rep invariant (RI):
    //   operands != null and operators != null
    //   operands.size() >= 1
    //   operands.size() == operators.size() + 1
    //   no element of operands or operators is null
    //   every operand is in canonical form: operand.equals(operand.stripTrailingZeros())
    //   for every index i, if operators.get(i) == DIVIDE then
    //       operands.get(i + 1).compareTo(BigDecimal.ZERO) != 0
    //
    // Abstraction function (AF):
    //   AF(operands, operators) = the arithmetic expression
    //       operands[0] operators[0] operands[1] ... operators[n-1] operands[n]
    //   where n = operators.size(), whose value is found by applying every
    //   MULTIPLY and DIVIDE from left to right, then every ADD and SUBTRACT from left to right.
    //   Example: ([2, 3, 4], [ADD, MULTIPLY]) represents 2 + 3 * 4, whose value is 14.
    //
    // Safety from rep exposure:
    //   Both fields are private and final.
    //   The constructor stores unmodifiable copies (List.copyOf) of its inputs,
    //   so the caller's lists are never shared with this object (defensive copying).
    //   No method returns either list; observers return one element at a time.
    //   BigDecimal and Operator are immutable, so returning one of them exposes nothing.
    //   The class is final, so no subclass can add mutable state.
 
    /**
     * Creates an expression.
     * @param operands  the numbers, in order; at least one
     * @param operators the operators between the numbers; exactly operands.size() - 1 of them
     * @throws IllegalArgumentException if an input is null, the sizes do not match,
     *         or a number that comes right after DIVIDE is zero
     */
    public Expression(List<BigDecimal> operands, List<Operator> operators) {
        if (operands == null || operators == null) {                       // was a list missing?
            // refuse to create the object
            throw new IllegalArgumentException("Lists must not be null");
        }
        // wrong shape, e.g. "2 +"?
        if (operands.isEmpty() || operands.size() != operators.size() + 1) {
            // refuse
            throw new IllegalArgumentException("Need exactly one more number than operators");
        }
        // a NEW list that will hold cleaned-up numbers
        List<BigDecimal> canonical = new ArrayList<>();
        for (BigDecimal number : operands) {             // look at every number we were given
            if (number == null) {                        // a missing number?
                throw new IllegalArgumentException("Numbers must not be null"); // refuse
            }
            // store 2.50 as 2.5 so equal numbers look the same
            canonical.add(number.stripTrailingZeros());
        }
        for (int i = 0; i < operators.size(); i++) {     // look at every operator by its position i
            Operator op = operators.get(i);              // the operator at position i
            if (op == null) {                            // a missing operator?
                throw new IllegalArgumentException("Operators must not be null"); // refuse
            }
            // is the number after it zero? (compareTo: 0.0 counts as 0)
            boolean nextIsZero = canonical.get(i + 1).compareTo(BigDecimal.ZERO) == 0;
            if (op == Operator.DIVIDE && nextIsZero) {   // dividing by zero?
                throw new IllegalArgumentException("Cannot divide by zero"); // refuse
            }
        }
        // defensive copy: unmodifiable, not shared with anyone
        this.operands = List.copyOf(canonical);
        // defensive copy: later changes to the caller's list cannot reach us
        this.operators = List.copyOf(operators);
        checkRep();                              // double-check that the RI holds
    }
 
    /** Checks the rep invariant. The checks run only when Java is started with -ea. */
    private void checkRep() {
        assert operands != null && operators != null;           // RI line 1
        assert operands.size() >= 1;                            // RI line 2
        assert operands.size() == operators.size() + 1;         // RI line 3
        for (BigDecimal number : operands) {                    // for every number...
            assert number != null;                              // RI line 4 (numbers)
            assert number.equals(number.stripTrailingZeros());  // RI line 5 (canonical form)
        }
        for (int i = 0; i < operators.size(); i++) {            // for every operator...
            assert operators.get(i) != null;                    // RI line 4 (operators)
            if (operators.get(i) == Operator.DIVIDE) {          // if it is a division...
                // RI line 6: the divisor is not zero
                assert operands.get(i + 1).compareTo(BigDecimal.ZERO) != 0;
            }
        }
    }
 
    /**
     * Computes the value of this expression with operator precedence.
     * @return the value, rounded to at most 10 decimal places
     */
    public BigDecimal evaluate() {
        // Pass 1: do every * and / from left to right; collect the results ("terms").
        // temporary list, lives only inside this method
        List<BigDecimal> terms = new ArrayList<>();
        // the + and - operators found between the terms
        List<Operator> addOps = new ArrayList<>();
        BigDecimal current = operands.get(0);         // start with the first number
        // walk through the operators from left to right
        for (int i = 0; i < operators.size(); i++) {
            Operator op = operators.get(i);           // the operator at position i
            BigDecimal next = operands.get(i + 1);    // the number that comes after it
            if (op.getPrecedence() == 2) {            // is it * or / ?
                current = op.apply(current, next);    // yes: calculate it right now
            } else {                                  // it is + or -
                terms.add(current);                   // the current term is finished: save it
                addOps.add(op);                       // remember the + or - for pass 2
                current = next;                       // start a new term with the next number
            }
        }
        terms.add(current);                           // save the last term
 
        // Pass 2: do every + and - from left to right.
        BigDecimal result = terms.get(0);             // start with the first term
        for (int i = 0; i < addOps.size(); i++) {     // walk through the + and - operators
            // apply it to the running result
            result = addOps.get(i).apply(result, terms.get(i + 1));
        }
        checkRep();                                   // evaluating must not break the RI
        return result.setScale(10, RoundingMode.HALF_UP) // round to 10 decimal places
                     // and remove useless zeros (3.0000000000 -> 3)
                     .stripTrailingZeros();
    }
 
    /** @return how many numbers this expression has */
    public int getOperandCount() {
        return operands.size(); // the size of the numbers list
    }
 
    /**
     * @param index requires 0 <= index < getOperandCount()
     * @return the number at that position
     */
    public BigDecimal getOperand(int index) {
        return operands.get(index); // safe: one immutable BigDecimal, not the whole list
    }
 
    /**
     * @param index requires 0 <= index < getOperandCount() - 1
     * @return the operator at that position
     */
    public Operator getOperator(int index) {
        return operators.get(index); // safe: one immutable Operator, not the whole list
    }
 
    /** @return the expression as text, for example "2 + 3 * 4" */
    @Override
    public String toString() {
        // start with the first number
        StringBuilder text = new StringBuilder(operands.get(0).toPlainString());
        // then for every operator...
        for (int i = 0; i < operators.size(); i++) {
            text.append(' ').append(operators.get(i).getSymbol())                 // add " +"
                .append(' ').append(operands.get(i + 1).toPlainString());         // add " 3"
        }
        return text.toString(); // turn the builder into a normal String
    }
 
    /**
     * Two expressions are equal when they have the same abstract value according to the AF:
     * the same numbers (2.5 and 2.50 count as the same number) and the same operators,
     * in the same order. So 2 + 3 equals 2 + 3, but 2 + 3 does NOT equal 5.
     */
    @Override
    // the parameter MUST be Object, otherwise this is not an override
    public boolean equals(Object that) {
        if (!(that instanceof Expression)) {         // not an Expression at all (or null)?
            return false;                            // then it cannot be equal
        }
        Expression other = (Expression) that;        // it is an Expression: treat it as one
        return operands.equals(other.operands)       // same numbers in the same order...
            && operators.equals(other.operators);    // ...and same operators in the same order
    }
 
    /** Equal expressions always return the same hash code (required by the Object contract). */
    @Override
    public int hashCode() {
        // built from the SAME fields that equals() compares
        return Objects.hash(operands, operators);
    }
}
