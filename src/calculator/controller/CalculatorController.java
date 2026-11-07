package calculator.controller; // This file belongs to the "controller" package (the C in MVC)
 
import calculator.model.CalculatorModel; // the model we send actions to
import calculator.model.Operator;        // to turn "+" into Operator.ADD
 
import java.awt.event.ActionEvent;    // information about a click
import java.awt.event.ActionListener; // "something that reacts to clicks"
 
/**
 * Turns one button click into ONE call on the model.
 * It decides WHICH model method to call, never WHETHER the input is allowed.
 */
public class CalculatorController implements ActionListener {
 
    private final CalculatorModel model; // the model this controller talks to
 
    /** @param model the calculator model (the Singleton) */
    public CalculatorController(CalculatorModel model) {
        this.model = model; // remember the model
    }
 
    /** Swing calls this method automatically whenever a button is clicked. */
    @Override
    public void actionPerformed(ActionEvent e) {
        handleCommand(e.getActionCommand()); // the action command is the button's text, e.g. "7"
    }
 
    /**
     * Sends the command to the right model method.
     * @param command the text of the clicked button
     * @throws IllegalArgumentException if the command is unknown
     */
    public void handleCommand(String command) {
        // one character and it is 0-9?
        if (command.length() == 1 && Character.isDigit(command.charAt(0))) {
            model.appendDigit(command.charAt(0));                         // add the digit
        } else if (command.equals(".")) {                                 // decimal point?
            model.appendDecimalPoint();                                   // add the point
        } else if (command.equals("=")) {                                 // equals?
            model.calculate();                                            // calculate the result
        } else if (command.equals("C")) {                                 // clear?
            model.clear();                                                // empty everything
        } else if (command.equals("DEL")) {                               // delete?
            // remove the last character
            model.backspace();
        // otherwise it must be an operator
        } else {
            // "+" -> Operator.ADD (throws if unknown)
            model.appendOperator(Operator.fromSymbol(command));
        }
    }
}
