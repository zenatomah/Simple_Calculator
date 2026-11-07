package calculator; // the main package (the root of the project)
 
import calculator.controller.CalculatorController; // C
import calculator.model.CalculatorModel;           // M
import calculator.view.CalculatorView;             // V
 
import javax.swing.SwingUtilities; // runs code on Swing's own thread
 
/** Starts the program and connects Model, View and Controller. */
public class Main {
    // Java starts every program here
    public static void main(String[] args) {
        // run the UI code on the Swing thread
        SwingUtilities.invokeLater(() -> {
            // get the ONE model (Singleton)
            CalculatorModel model = CalculatorModel.getInstance();
            // create the window; it observes the model
            CalculatorView view = new CalculatorView(model);
            // create the controller
            CalculatorController controller = new CalculatorController(model);
            // button clicks go to the controller
            view.addButtonListener(controller);
            view.setVisible(true);                                    // show the window
        });
    }
}
