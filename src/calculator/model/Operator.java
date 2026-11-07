package calculator.model; // This file belongs to the "model" package (the M in MVC)
 
import java.math.BigDecimal;  // BigDecimal stores exact decimal numbers (no rounding errors)
import java.math.MathContext; // MathContext tells BigDecimal how many digits to keep when dividing
 
/**
 * The four arithmetic operators of the calculator.
 * An enum is a type with a FIXED list of values, so it is immutable by nature.
 */
public enum Operator {
    ADD("+", 1),       // addition: symbol "+", low precedence (1)
    SUBTRACT("-", 1),  // subtraction: symbol "-", low precedence (1)
    MULTIPLY("*", 2),  // multiplication: symbol "*", high precedence (2) -> done first
    DIVIDE("/", 2);    // division: symbol "/", high precedence (2) -> done first
 
    private final String symbol;   // the text shown on the button, e.g. "+"
    private final int precedence;  // 2 = calculate first, 1 = calculate later
 
    /** Constructor: Java calls it once for each of the four values above. */
    Operator(String symbol, int precedence) {
        this.symbol = symbol;         // remember the symbol for this operator
        this.precedence = precedence; // remember the precedence for this operator
    }
 
    /** @return the symbol of this operator, for example "+" */
    public String getSymbol() {
        return symbol; // give back the stored symbol
    }
 
    /** @return 2 for * and /, 1 for + and - */
    public int getPrecedence() {
        return precedence; // give back the stored precedence
    }
 
    /**
     * Applies this operator to two numbers.
     * @param left  the number on the left of the operator
     * @param right the number on the right; must not be zero when this is DIVIDE
     * @return the result of: left (operator) right
     */
    public BigDecimal apply(BigDecimal left, BigDecimal right) {
        // choose what to do based on which operator this is
        switch (this) {
            case ADD:      return left.add(right);        // left + right
            case SUBTRACT: return left.subtract(right);   // left - right
            case MULTIPLY: return left.multiply(right);   // left * right
            // left / right, keeping 34 digits (1/3 never ends)
            case DIVIDE:   return left.divide(right, MathContext.DECIMAL128);
            // can never happen; the compiler requires it
            default:       throw new AssertionError("Unknown operator: " + this);
        }
    }
 
    /**
     * Finds the operator that has the given symbol.
     * @param symbol one of "+", "-", "*", "/"
     * @return the matching operator
     * @throws IllegalArgumentException if no operator has this symbol
     */
    public static Operator fromSymbol(String symbol) {
        // values() returns all four operators; check each one
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) {    // is this the operator whose symbol was asked for?
                return op;                     // yes: return it immediately
            }
        }
        // no match: report the error
        throw new IllegalArgumentException("Unknown operator: " + symbol);
    }
}
