// This file belongs to the "observer" package (the Observer design pattern)
package calculator.observer;
 
/**
 * An Observer is an object that wants to be told when a Subject changes.
 * In this project the Observer is the window (CalculatorView).
 */
public interface Observer {
    /** Called by the Subject every time its state changes. */
    void update(); // no body here: every class that implements Observer must write its own update()
}
