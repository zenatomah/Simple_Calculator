package calculator.model; // This file belongs to the "model" package
 
import calculator.observer.Observer; // the interface the window implements
import calculator.observer.Subject;  // the interface this class implements
 
import java.math.BigDecimal; // exact decimal numbers
import java.util.ArrayList;  // resizable list
import java.util.HashMap;    // fast key -> value table (used for the cache)
import java.util.List;       // general list type
import java.util.Map;        // general key -> value type
 
/**
 * The single calculator state (Singleton pattern) and all calculator rules.
 * It is the Subject of the Observer pattern: it tells the window whenever it changes.
 * This class does not know anything about Swing.
 */
public class CalculatorModel implements Subject {
 
    // ===================== Singleton =====================
 
    // the one and only object; static = belongs to the class
    private static CalculatorModel instance;
 
    private CalculatorModel() {
        // private constructor: no other class can write "new CalculatorModel()"
    }
 
    /** @return the single CalculatorModel, creating it the first time it is needed */
    public static CalculatorModel getInstance() {
        if (instance == null) {                // has the object been created yet?
            instance = new CalculatorModel();  // no: create it now (lazy instantiation)
        }
        return instance;                       // always return the same object
    }
 
    // ===================== State =====================
 
    private static final int MAX_DIGITS = 15; // a number may have at most 15 digits
    public static final String DIVIDE_BY_ZERO_MESSAGE = "Cannot divide by zero"; // error text
 
    private final List<String> tokens = new ArrayList<>(); // what was typed, e.g. ["12", "+", "3"]
    private String resultText = "";       // the result shown after "="
    private String errorMessage = "";     // the error shown in red ("" means no error)
    private boolean showingResult = false; // true right after "=" was pressed
 
    // Cache: remembers results of expressions that were already calculated.
    // HashMap finds a key in O(1) time on average, using hashCode() then equals().
    private final Map<Expression, BigDecimal> resultCache = new HashMap<>();
 
    // ===================== Subject (Observer pattern) =====================
 
    private final List<Observer> observers = new ArrayList<>(); // everyone who wants updates
 
    @Override
    public void registerObserver(Observer o) {
        // ignore null and avoid adding the same observer twice
        if (o != null && !observers.contains(o)) {
            observers.add(o);                       // add it to the list
        }
    }
 
    @Override
    public void removeObserver(Observer o) {
        observers.remove(o); // take it out of the list
    }
 
    @Override
    public void notifyObservers() {
        for (Observer o : observers) { // for every registered observer...
            o.update();                // ...tell it that the state changed
        }
    }
 
    // ===================== Actions (called by the Controller) =====================
 
    /**
     * Adds a digit to the current number.
     * @param digit requires '0' to '9'
     */
    public void appendDigit(char digit) {
        if (showingResult) {                             // a result is on the screen?
            resetState();                                // a new digit starts a new expression
        }
        errorMessage = "";                               // typing clears any old error
        if (lastTokenIsNumber()) {                       // are we in the middle of a number?
            String number = lastToken();                 // the number typed so far, e.g. "12"
            if (countDigits(number) >= MAX_DIGITS) {     // already 15 digits?
                // ignore the extra digit
            } else if (number.equals("0")) {             // the number is just "0"?
                setLastToken(String.valueOf(digit));     // replace it: "0" then "7" becomes "7"
            } else {                                     // normal case
                setLastToken(number + digit);            // "12" then "3" becomes "123"
            }
        // nothing typed yet, or the last token is an operator
        } else {
            tokens.add(String.valueOf(digit));           // start a new number
        }
        notifyObservers();                               // tell the window to redraw
    }
 
    /** Adds a decimal point to the current number (at most one per number). */
    public void appendDecimalPoint() {
        if (showingResult) {                             // a result is on the screen?
            resetState();                                // start a new expression
        }
        errorMessage = "";                               // clear any old error
        if (lastTokenIsNumber()) {                       // in the middle of a number?
            if (!lastToken().contains(".")) {            // only if it has no point yet
                setLastToken(lastToken() + ".");         // "12" becomes "12."
            }
        } else {                                         // no number yet
            tokens.add("0.");                            // "." alone becomes "0."
        }
        notifyObservers();                               // tell the window to redraw
    }
 
    /**
     * Adds an operator after the current number.
     * Ignored when nothing is typed or a result is shown.
     * A second operator in a row replaces the first one.
     */
    public void appendOperator(Operator op) {
        if (showingResult || tokens.isEmpty()) {         // nothing to operate on?
            return;                                      // ignore the key
        }
        if (lastTokenIsOperator()) {                     // "5 +" then "*" ?
            setLastToken(op.getSymbol());                // replace: "5 *"
        } else if (wouldDivideByZero()) {                // "5 / 0" then another operator?
            errorMessage = DIVIDE_BY_ZERO_MESSAGE;       // block it and show the error
        } else {                                         // normal case
            tokens.add(op.getSymbol());                  // add the operator
        }
        notifyObservers();                               // tell the window to redraw
    }
 
    /** The "=" key: builds an Expression and shows its value. */
    public void calculate() {
        if (showingResult || tokens.isEmpty() || lastTokenIsOperator()) { // incomplete, e.g. "5 +"?
            return;                                                     // ignore the key
        }
        if (wouldDivideByZero()) {                       // ends with "/ 0"?
            errorMessage = DIVIDE_BY_ZERO_MESSAGE;       // show the error instead of a result
            notifyObservers();                           // tell the window to redraw
            return;                                      // stop here
        }
        // turn the typed tokens into an immutable Expression
        Expression expression = buildExpression();
        BigDecimal value;                                // will hold the result
        // calculated before? (uses hashCode() and equals())
        if (resultCache.containsKey(expression)) {
            value = resultCache.get(expression);         // cache hit: reuse the stored result
        } else {                                         // first time we see this expression
            value = expression.evaluate();               // calculate it
            resultCache.put(expression, value);          // store it for next time
        }
        resultText = format(value);                      // turn the number into display text
        showingResult = true;                            // remember that a result is on screen
        notifyObservers();                               // tell the window to redraw
    }
 
    /** The "C" key: empties everything. */
    public void clear() {
        resetState();       // forget the expression, the result and the error
        notifyObservers();  // tell the window to redraw
    }
 
    /** The "DEL" key: removes the last character or the last operator. */
    public void backspace() {
        if (showingResult || tokens.isEmpty()) {         // nothing to delete, or a result is shown?
            return;                                      // ignore the key
        }
        errorMessage = "";                               // clear any old error
        if (lastTokenIsOperator()) {                     // last thing typed was an operator?
            tokens.remove(tokens.size() - 1);            // remove the whole operator
        } else {                                         // last thing typed was a number
            String number = lastToken();                 // e.g. "123"
            String shorter = number.substring(0, number.length() - 1); // e.g. "12"
            if (shorter.isEmpty()) {                     // the number is now empty?
                tokens.remove(tokens.size() - 1);        // remove it completely
            } else {
                setLastToken(shorter);                   // keep the shorter number
            }
        }
        notifyObservers();                               // tell the window to redraw
    }
 
    // ===================== Getters (called by the View) =====================
 
    /** @return the top line, e.g. "2 + 3 * 4", or "2 + 3 * 4 =" after "=" */
    public String getExpressionText() {
        String text = String.join(" ", tokens);          // join the tokens with spaces
        return showingResult ? text + " =" : text;       // add " =" when a result is shown
    }
 
    /** @return the bottom line: the error message, the result, or "" */
    public String getResultText() {
        if (hasError()) {                                // an error is waiting?
            return errorMessage;                         // show the error
        }
        return showingResult ? resultText : "";          // show the result, or nothing
    }
 
    /** @return true if an error message is shown */
    public boolean hasError() {
        return !errorMessage.isEmpty();                  // any text means there is an error
    }
 
    // ===================== Private helpers =====================
 
    private void resetState() {
        tokens.clear();          // forget everything typed
        resultText = "";         // forget the result
        errorMessage = "";       // forget the error
        showingResult = false;   // no result on screen
    }
 
    private String lastToken() {
        return tokens.get(tokens.size() - 1);            // the last item in the list
    }
 
    private void setLastToken(String value) {
        tokens.set(tokens.size() - 1, value);            // replace the last item in the list
    }
 
    private boolean lastTokenIsOperator() {
        // list not empty AND last item is an operator
        return !tokens.isEmpty() && isOperator(lastToken());
    }
 
    private boolean lastTokenIsNumber() {
        // list not empty AND last item is a number
        return !tokens.isEmpty() && !isOperator(lastToken());
    }
 
    private static boolean isOperator(String token) {
        for (Operator op : Operator.values()) {          // check all four operators
            if (op.getSymbol().equals(token)) {          // does the token match this symbol?
                return true;                             // yes: it is an operator
            }
        }
        return false;                                    // no match: it is a number
    }
 
    private static int countDigits(String number) {
        // count characters, ignoring the decimal point
        return number.replace(".", "").length();
    }
 
    /** @return true if the last number is zero and comes right after "/" */
    private boolean wouldDivideByZero() {
        if (tokens.size() < 2 || !lastTokenIsNumber()) { // need at least "x / y"
            return false;
        }
        String operator = tokens.get(tokens.size() - 2); // the token before the last number
        BigDecimal number = new BigDecimal(lastToken()); // the last number as a BigDecimal
        return operator.equals(Operator.DIVIDE.getSymbol())       // it is a "/"...
            // ...and the number is zero (0, 0.0, ...)
            && number.compareTo(BigDecimal.ZERO) == 0;
    }
 
    /** Turns the tokens into an Expression. Requires: the last token is a number. */
    private Expression buildExpression() {
        List<BigDecimal> numbers = new ArrayList<>();    // the numbers we find
        List<Operator> ops = new ArrayList<>();          // the operators we find
        for (String token : tokens) {                    // go through what was typed
            if (isOperator(token)) {                     // an operator?
                ops.add(Operator.fromSymbol(token));     // convert "+" to Operator.ADD and keep it
            } else {                                     // a number
                // convert "12.5" to a BigDecimal and keep it
                numbers.add(new BigDecimal(token));
            }
        }
        // the Expression copies both lists defensively
        return new Expression(numbers, ops);
    }
 
    /** @return the number as plain text, e.g. "10" (never "1E+1") */
    private static String format(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString(); // plain digits, no scientific notation
    }
}
