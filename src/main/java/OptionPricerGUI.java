import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionPricerGUI extends JFrame {

    private final JSlider initialPriceSlider;
    private final JTextField initialPriceField;

    private final JSlider strikePriceSlider;
    private final JTextField strikePriceField;

    private final JSlider probabilityUpSlider;
    private final JTextField probabilityUpField;

    private final JSlider upFactorSlider;
    private final JTextField upFactorField;

    private final JSlider downFactorSlider;
    private final JTextField downFactorField;

    private final JSlider interestRateSlider;
    private final JTextField interestRateField;

    private final JSlider stepsSlider;
    private final JTextField stepsField;

    private final JCheckBox callOptionCheckBox;

    private BinomialTreePanel treePanel;
    private final DiagramWindow diagramWindow;

    // Output components
    private final JLabel optionPriceLabel;
    private final JLabel deltaLabel;
    private final JLabel portfolioLabel;
    private final JLabel expectedValueLabel;

    private double[][] optionValues;
    private double[][] stockPrices;

    public OptionPricerGUI() {
        setTitle("Simple Binomial Tree Option Pricing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new BorderLayout());

        // Initialize panels
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(8, 3, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize components
        // Initial Price
        initialPriceSlider = createSlider(0, 200, 100, 10);
        initialPriceField = createTextField("100");
        inputPanel.add(new JLabel("Initial Price:"));
        inputPanel.add(initialPriceSlider);
        inputPanel.add(initialPriceField);

        // Strike Price
        strikePriceSlider = createSlider(0, 200, 105, 10);
        strikePriceField = createTextField("100");
        inputPanel.add(new JLabel("Strike Price:"));
        inputPanel.add(strikePriceSlider);
        inputPanel.add(strikePriceField);

        // Probability Up
        probabilityUpSlider = createSlider(0, 100, 50, 5);
        probabilityUpField = createTextField("0.50");
        inputPanel.add(new JLabel("Probability Up:"));
        inputPanel.add(probabilityUpSlider);
        inputPanel.add(probabilityUpField);

        // Up Factor
        upFactorSlider = createSlider(100, 200, 110, 5); // Represents 1.00 to 2.00
        upFactorField = createTextField("1.50");
        inputPanel.add(new JLabel("Up Factor:"));
        inputPanel.add(upFactorSlider);
        inputPanel.add(upFactorField);

        // Down Factor
        downFactorSlider = createSlider(0, 100, 90, 5); // Represents 0.00 to 1.00
        downFactorField = createTextField("0.75");
        inputPanel.add(new JLabel("Down Factor:"));
        inputPanel.add(downFactorSlider);
        inputPanel.add(downFactorField);

        // Interest Rate
        interestRateSlider = createSlider(0, 100, 0, 5); // Represents 0% to 100%
        interestRateField = createTextField("0.05");
        inputPanel.add(new JLabel("Interest Rate:"));
        inputPanel.add(interestRateSlider);
        inputPanel.add(interestRateField);

        // Steps Number
        stepsSlider = createSlider(0, 50, 3, 5);
        stepsField = createTextField("3");
        inputPanel.add(new JLabel("Number of Steps:"));
        inputPanel.add(stepsSlider);
        inputPanel.add(stepsField);

        diagramWindow = new DiagramWindow();
        diagramWindow.setVisible(true);

        // Option Type
        callOptionCheckBox = new JCheckBox("Call Option", false);
        inputPanel.add(new JLabel("Option Type:"));
        inputPanel.add(callOptionCheckBox);
        inputPanel.add(new JLabel(""));

        // Add input panel to the frame
        add(inputPanel, BorderLayout.CENTER);

        // Output panel
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new GridLayout(2, 1));
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        optionPriceLabel = new JLabel("Option Price: ");
        deltaLabel = new JLabel("Delta: ");
        portfolioLabel = new JLabel("Present Portfolio Value: ");
        expectedValueLabel = new JLabel("Expected Value: ");

        outputPanel.add(optionPriceLabel);
        outputPanel.add(deltaLabel);
        outputPanel.add(portfolioLabel);
        outputPanel.add(expectedValueLabel);

        add(outputPanel, BorderLayout.SOUTH);

        // Add listeners
        ChangeListener sliderChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateFields();
                calculateAndDisplay();
            }
        };

        initialPriceSlider.addChangeListener(sliderChangeListener);
        strikePriceSlider.addChangeListener(sliderChangeListener);
        probabilityUpSlider.addChangeListener(sliderChangeListener);
        upFactorSlider.addChangeListener(sliderChangeListener);
        downFactorSlider.addChangeListener(sliderChangeListener);
        interestRateSlider.addChangeListener(sliderChangeListener);
        stepsSlider.addChangeListener(sliderChangeListener);

        // Text field listeners
        initialPriceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int value = Integer.parseInt(initialPriceField.getText());
                    initialPriceSlider.setValue(value);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        strikePriceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int value = Integer.parseInt(strikePriceField.getText());
                    strikePriceSlider.setValue(value);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        probabilityUpField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(probabilityUpField.getText());
                    int sliderValue = (int) (value * 100);
                    probabilityUpSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        upFactorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(upFactorField.getText());
                    int sliderValue = (int) (value * 100);
                    upFactorSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        downFactorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(downFactorField.getText());
                    int sliderValue = (int) (value * 100);
                    downFactorSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        interestRateField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(interestRateField.getText());
                    int sliderValue = (int) (value * 100);
                    interestRateSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    // Invalid input, ignore
                }
            }
        });

        // Option type listener
        callOptionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateAndDisplay();
            }
        });

        // Initial calculation
        calculateAndDisplay();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OptionPricerGUI gui = new OptionPricerGUI();
            gui.setVisible(true);
        });
    }

    private JSlider createSlider(int min, int max, int initial, int step) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, initial);
        slider.setMajorTickSpacing(step * 5);
        slider.setMinorTickSpacing(step);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }

    private JTextField createTextField(String text) {
        JTextField field = new JTextField(text, 5);
        return field;
    }

    private void updateFields() {
        initialPriceField.setText(String.valueOf(initialPriceSlider.getValue()));
        strikePriceField.setText(String.valueOf(strikePriceSlider.getValue()));
        probabilityUpField.setText(String.format("%.2f", probabilityUpSlider.getValue() / 100.0));
        upFactorField.setText(String.format("%.5f", upFactorSlider.getValue() / 100.0));
        downFactorField.setText(String.format("%.2f", downFactorSlider.getValue() / 100.0));
        interestRateField.setText(String.valueOf(interestRateSlider.getValue()));
        stepsField.setText(String.format("%d", stepsSlider.getValue()));

    }

    private void calculateAndDisplay() {
        try {
            double initialPrice = initialPriceSlider.getValue();
            double strikePrice = strikePriceSlider.getValue();
            double probabilityUp = probabilityUpSlider.getValue() / 100.0;
            double upFactor = upFactorSlider.getValue() / 100.0;
            double downFactor = downFactorSlider.getValue() / 100.0;
            double interestRate = interestRateSlider.getValue() / 100.0;
            int steps = stepsSlider.getValue(); // No need for Math.round since stepsSlider is integer
            boolean isCall = callOptionCheckBox.isSelected();

            SimpleBinomialTree binomialTree = new SimpleBinomialTree(initialPrice, strikePrice, probabilityUp,
                    upFactor, downFactor, interestRate, isCall);
            MultiStepBinomialTree multiStepBinomialTree = new MultiStepBinomialTree(initialPrice, strikePrice, probabilityUp, upFactor, downFactor, interestRate, isCall, steps);

            optionValues = multiStepBinomialTree.getOptionValues();
            stockPrices = multiStepBinomialTree.getStockPrices();

            // Update the DiagramWindow with the latest data
            diagramWindow.updateTree(optionValues, stockPrices);

            // Update output labels
            optionPriceLabel.setText(String.format("Option Price: %.4f", multiStepBinomialTree.getOptionPrice()));
            deltaLabel.setText(String.format("Delta: %.4f", binomialTree.getDelta()));
            portfolioLabel.setText(String.format("Present Portfolio Value: %.4f", binomialTree.getPresentPortValue()));
            expectedValueLabel.setText(String.format("Expected Value: %.4f", binomialTree.getExpectedValue()));
        } catch (IllegalArgumentException ex) {
            optionPriceLabel.setText("Error: " + ex.getMessage());
            deltaLabel.setText("");
            portfolioLabel.setText("");
            expectedValueLabel.setText("");
        }
    }
}

