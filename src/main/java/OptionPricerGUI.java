import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class OptionPricerGUI extends JFrame {

    // Existing components
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

    private final DiagramWindow diagramWindow;
    private final String filePath = "src/main/resources/EvolutionOfOptionPrice.csv";

    // Output components
    private final JLabel optionPriceLabel;
    private final JLabel deltaLabel;
    private final JLabel portfolioLabel;
    private final JLabel expectedValueLabel;

    private double[][] optionValues;
    private double[][] stockPrices;

    // New components for running Python script
    private final JButton runPythonButton;
    private final JTextArea pythonOutputArea;

    public OptionPricerGUI() {
        setTitle("Simple Binomial Tree Option Pricing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700); // Increased size to accommodate output area
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
        outputPanel.setLayout(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new GridLayout(4, 1));

        optionPriceLabel = new JLabel("Option Price: ");
        deltaLabel = new JLabel("Delta: ");
        portfolioLabel = new JLabel("Present Portfolio Value: ");
        expectedValueLabel = new JLabel("Expected Value: ");

        labelsPanel.add(optionPriceLabel);
        labelsPanel.add(deltaLabel);
        labelsPanel.add(portfolioLabel);
        labelsPanel.add(expectedValueLabel);

        outputPanel.add(labelsPanel, BorderLayout.NORTH);

        // Initialize the Run Python Script button
        runPythonButton = new JButton("Run Python Script");
        outputPanel.add(runPythonButton, BorderLayout.CENTER);

        // Initialize the Python Output Area
        pythonOutputArea = new JTextArea(10, 50);
        pythonOutputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(pythonOutputArea);
        outputPanel.add(scrollPane, BorderLayout.SOUTH);

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

        // Add ActionListener to the Run Python Script button
        runPythonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runPythonScript();
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
        upFactorField.setText(String.format("%.2f", upFactorSlider.getValue() / 100.0));
        downFactorField.setText(String.format("%.2f", downFactorSlider.getValue() / 100.0));
        interestRateField.setText(String.format("%.2f", interestRateSlider.getValue() / 100.0));
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

            try (FileWriter writer = new FileWriter(filePath)) {
                writer.append("Step,OptionPrice\n");

                for (int i = 1; i <= 100; i++) {
                    MultiStepBinomialTree largeBinomialTree = new MultiStepBinomialTree(initialPrice, strikePrice, probabilityUp, upFactor, downFactor, interestRate, isCall, i);
                    writer.append(String.valueOf(i))
                            .append(",")
                            .append(String.valueOf(largeBinomialTree.getOptionPrice()))
                            .append("\n");
                }
                System.out.println("Data exported successfully to " + filePath);
            } catch (IOException e) {
                System.err.println("Error writing to CSV: " + e.getMessage());
            }

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

    private void runPythonScript() {
        // Define the path to the Python interpreter
        String pythonInterpreter = "python3"; // Assumes 'python3' is in the system PATH

        // Define the relative path to the Python script
        String pythonScript = "src/main/python/RunGUI.py";

        // Create a ProcessBuilder instance with relative paths
        ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScript);

        // Set the working directory to the project's root directory
        // (Assuming the Java application is run from the project's root)
        processBuilder.directory(new File(System.getProperty("user.dir")));

        // Optional: Redirect error stream to standard output
        processBuilder.redirectErrorStream(true);

        try {
            // Start the process
            Process process = processBuilder.start();

            // Read the output from the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            // Display the output and errors in the JTextArea
            if (exitCode == 0) {
                pythonOutputArea.setText("Python script executed successfully:\n" + output.toString());
            } else {
                pythonOutputArea.setText("Python script execution failed with exit code " + exitCode + ":\n" + output.toString());
            }

        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            pythonOutputArea.setText("An error occurred while executing the Python script:\n" + ex.getMessage());
        }
    }
}
