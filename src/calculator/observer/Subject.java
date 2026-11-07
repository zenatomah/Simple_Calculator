package calculator.observer; // This file belongs to the "observer" package
 
/**
 * A Subject is an object that keeps a list of Observers and tells them about changes.
 * In this project the Subject is the calculator state (CalculatorModel).
 */
public interface Subject {
    /** Adds an observer to the list, so it is told about every change. */
    void registerObserver(Observer o);
 
    /** Removes an observer from the list, so it is no longer told about changes. */
    void removeObserver(Observer o);
 
    /** Calls update() on every observer in the list. */
    void notifyObservers();
}
