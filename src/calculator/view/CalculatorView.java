package calculator.view; // This file belongs to the "view" package (the V in MVC)
 
import calculator.model.CalculatorModel; // the model we read from
import calculator.model.Operator;        // to get the operator symbols for the buttons
import calculator.observer.Observer;     // the interface this window implements
 
import javax.swing.BorderFactory;       // creates empty borders (padding)
import javax.swing.JButton;             // a clickable button
import javax.swing.JFrame;              // a window
import javax.swing.JLabel;              // a piece of text on the screen
import javax.swing.JPanel;              // a container that holds other components
import javax.swing.SwingConstants;      // alignment constants such as RIGHT
import java.awt.BorderLayout;           // layout: NORTH, CENTER, SOUTH...
import java.awt.Color;                  // colors
import java.awt.Dimension;              // a width and height in pixels
import java.awt.Font;                   // fonts
import java.awt.GridLayout;             // layout: a grid of equal cells
import java.awt.event.ActionListener;   // "something that reacts to clicks"
import java.util.ArrayList;             // resizable list
import java.util.List;                  // general list type
 
/**
 * The calculator window. It only SHOWS data and FORWARDS clicks.
 * It never calculates anything.
 */
// a window that is also an Observer
public class CalculatorView extends JFrame implements Observer {
 
    // where the text comes from
    private final CalculatorModel model;
    // top line (the expression)
    private final JLabel expressionLabel = new JLabel(" ", SwingConstants.RIGHT);
    // bottom line (the result)
    private final JLabel resultLabel = new JLabel(" ", SwingConstants.RIGHT);
    // all buttons, to connect them later
    private final List<JButton> buttons = new ArrayList<>();
    // big font for results
    private final Font resultFont = new Font("SansSerif", Font.BOLD, 30);
    // smaller font so error messages fit
    private final Font errorFont = new Font("SansSerif", Font.BOLD, 20);
 
    /** Builds the window and registers it as an observer of the model. */
    public CalculatorView(CalculatorModel model) {
        super("Simple Calculator");                     // the window title
        this.model = model;                             // remember the model
        buildLayout();                                  // create the labels and buttons
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closing the window ends the program
        setSize(340, 460);                              // width and height in pixels
        setLocationRelativeTo(null);                    // center the window on the screen
        // subscribe to model changes (labels exist now)
        model.registerObserver(this);
        update();                                       // show the initial (empty) state
    }
 
    /** Connects every button to the given listener (the controller). */
    public void addButtonListener(ActionListener listener) {
        for (JButton button : buttons) {         // for every button...
            button.addActionListener(listener);  // ...send its clicks to the listener
        }
    }
 
    /** Called by the model after every change: redraw the two lines. */
    @Override
    public void update() {
        // top line from the model
        expressionLabel.setText(textOrSpace(model.getExpressionText()));
        // bottom line from the model
        resultLabel.setText(textOrSpace(model.getResultText()));
        // red for errors, black otherwise
        resultLabel.setForeground(model.hasError() ? Color.RED : Color.BLACK);
        // smaller text for errors so they fit
        resultLabel.setFont(model.hasError() ? errorFont : resultFont);
    }
 
    private void buildLayout() {
        // ---- Display: two lines ----
        // smaller font for the top line
        expressionLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        // gray text for the top line
        expressionLabel.setForeground(Color.GRAY);
        JPanel display = new JPanel(new GridLayout(2, 1));              // 2 rows, 1 column
        // padding around the text
        display.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        display.setBackground(Color.WHITE);                             // white background
        // fixed height so the window never jumps
        display.setPreferredSize(new Dimension(340, 110));
        display.add(expressionLabel);                                   // row 1: expression
        display.add(resultLabel);                                       // row 2: result
 
        // ---- Buttons: a 4 x 4 grid ----
        String[] labels = {                                             // button texts, row by row
            "C", "DEL", Operator.DIVIDE.getSymbol(), Operator.MULTIPLY.getSymbol(),
            "7", "8", "9", Operator.SUBTRACT.getSymbol(),
            "4", "5", "6", Operator.ADD.getSymbol(),
            "1", "2", "3", "."
        };
        // 4 rows, 4 columns, 6px gaps
        JPanel grid = new JPanel(new GridLayout(4, 4, 6, 6));
        for (String label : labels) {                                   // for every text...
            // ...create a button and add it
            grid.add(createButton(label));
        }
 
        // ---- Last row: "0" and "=" (wider buttons) ----
        JPanel lastRow = new JPanel(new GridLayout(1, 2, 6, 6));        // 1 row, 2 columns
        lastRow.add(createButton("0"));                                 // left: 0
        lastRow.add(createButton("="));                                 // right: =
 
        // holds the grid and the last row
        JPanel keypad = new JPanel(new BorderLayout(6, 6));
        keypad.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));  // padding around the keypad
        keypad.add(grid, BorderLayout.CENTER);                          // grid in the middle
        keypad.add(lastRow, BorderLayout.SOUTH);                        // last row at the bottom
 
        // layout of the whole window
        setLayout(new BorderLayout());
        add(display, BorderLayout.NORTH);                               // display at the top
        add(keypad, BorderLayout.CENTER);                               // keypad fills the rest
    }
 
    private JButton createButton(String label) {
        JButton button = new JButton(label);                    // a button showing the label
        // the text the controller will receive
        button.setActionCommand(label);
        button.setFont(new Font("SansSerif", Font.BOLD, 20));   // big bold text
        button.setFocusable(false);                             // no focus rectangle after clicking
        buttons.add(button);                                    // remember it for addButtonListener
        return button;                                          // give it back to the caller
    }
 
    /** An empty JLabel shrinks to zero height, so we show a space instead of "". */
    private static String textOrSpace(String text) {
        return text.isEmpty() ? " " : text; // "" becomes " ", anything else stays the same
    }
}
