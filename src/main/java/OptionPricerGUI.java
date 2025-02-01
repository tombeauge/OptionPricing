import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final JLabel memoryUsageLabel;

    private double[][] optionValues;
    private double[][] stockPrices;

    // New components for running Python script
    private final JButton runPythonButton;
    private final JTextArea pythonOutputArea;

    private int numberStepsGraph = 100; // Default value

    // Logger for debugging
    private static final Logger LOGGER = Logger.getLogger(OptionPricerGUI.class.getName());

    public OptionPricerGUI() {
        setTitle("Simple Binomial Tree Option Pricing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 800); // Increased size to accommodate output area
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
        interestRateSlider = createSlider(-25, 25, 0, 5); // Now allows -25% to 25%
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
        labelsPanel.setLayout(new GridLayout(5, 1)); // Changed to 5 rows

        optionPriceLabel = new JLabel("Option Price: ");
        deltaLabel = new JLabel("Delta: ");
        portfolioLabel = new JLabel("Present Portfolio Value: ");
        expectedValueLabel = new JLabel("Expected Value: ");
        memoryUsageLabel = new JLabel("Memory Usage: 0 MB");

        labelsPanel.add(optionPriceLabel);
        labelsPanel.add(deltaLabel);
        labelsPanel.add(portfolioLabel);
        labelsPanel.add(expectedValueLabel);
        labelsPanel.add(memoryUsageLabel);

        outputPanel.add(labelsPanel, BorderLayout.NORTH);

        JButton benchmarkButton = new JButton("Run 10s Benchmark");
        benchmarkButton.setToolTipText("Click to benchmark how many steps can be computed in 10 seconds.");

        outputPanel.add(benchmarkButton, BorderLayout.EAST);

        benchmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                benchmarkFor10Seconds(
                        initialPriceSlider.getValue(),
                        strikePriceSlider.getValue(),
                        probabilityUpSlider.getValue() / 100.0,
                        upFactorSlider.getValue() / 100.0,
                        downFactorSlider.getValue() / 100.0,
                        interestRateSlider.getValue() / 100.0,
                        callOptionCheckBox.isSelected()
                );
            }
        });

        // Initialize the Run Python Script button
        runPythonButton = new JButton("Run Python Script");
        runPythonButton.setToolTipText("Click to run the Python script with the specified number of steps.");
        outputPanel.add(runPythonButton, BorderLayout.CENTER);

        // Initialize the Python Output Area
        pythonOutputArea = new JTextArea(10, 50);
        pythonOutputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(pythonOutputArea);
        outputPanel.add(scrollPane, BorderLayout.SOUTH);

        add(outputPanel, BorderLayout.SOUTH);

        // Configure Logger to display messages in pythonOutputArea
        LOGGER.setLevel(Level.ALL);
        LOGGER.addHandler(new java.util.logging.Handler() {
            @Override
            public void publish(java.util.logging.LogRecord record) {
                if (record.getLevel().intValue() >= Level.INFO.intValue()) {
                    SwingUtilities.invokeLater(() -> {
                        pythonOutputArea.append(record.getLevel() + ": " + record.getMessage() + "\n");
                    });
                }
            }

            @Override
            public void flush() {}

            @Override
            public void close() throws SecurityException {}
        });

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

        // Text field listeners with input validation and logging
        initialPriceField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int value = Integer.parseInt(initialPriceField.getText());
                    initialPriceSlider.setValue(value);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Invalid Initial Price input: " + initialPriceField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Initial Price! Please enter a valid integer.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
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
                    LOGGER.log(Level.WARNING, "Invalid Strike Price input: " + strikePriceField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Strike Price! Please enter a valid integer.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        probabilityUpField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(probabilityUpField.getText());
                    if (value < 0.0 || value > 1.0) {
                        throw new NumberFormatException("Probability must be between 0 and 1.");
                    }
                    int sliderValue = (int) (value * 100);
                    probabilityUpSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Invalid Probability Up input: " + probabilityUpField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Probability Up! Please enter a valid decimal between 0 and 1.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        upFactorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(upFactorField.getText());
                    if (value <= 0.0) {
                        throw new NumberFormatException("Up Factor must be positive.");
                    }
                    int sliderValue = (int) (value * 100);
                    upFactorSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Invalid Up Factor input: " + upFactorField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Up Factor! Please enter a valid positive decimal.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        downFactorField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(downFactorField.getText());
                    if (value <= 0.0 || value >= 1.0) {
                        throw new NumberFormatException("Down Factor must be between 0 and 1.");
                    }
                    int sliderValue = (int) (value * 100);
                    downFactorSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Invalid Down Factor input: " + downFactorField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Down Factor! Please enter a valid decimal between 0 and 1.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        interestRateField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double value = Double.parseDouble(interestRateField.getText());
                    if (value < -0.25 || value > 0.25) {
                        throw new NumberFormatException("Interest Rate must be between -0.25 and 0.25.");
                    }
                    int sliderValue = (int) (value * 100); // Convert to match slider scale
                    interestRateSlider.setValue(sliderValue);
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, "Invalid Interest Rate input: " + interestRateField.getText());
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Invalid Interest Rate! Please enter a value between -0.25 and 0.25.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
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

        // Add ActionListener to the Run Python Script button with input prompt
        runPythonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // Prompt the user for an integer input
                String input = JOptionPane.showInputDialog(
                        OptionPricerGUI.this,
                        "Enter an integer value:",
                        "Input Required",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (input != null) { // Check if the user didn't cancel the dialog
                    try {
                        numberStepsGraph = Integer.parseInt(input.trim());

                        // Validate the input (optional)
                        if (numberStepsGraph <= 0) {
                            throw new NumberFormatException("Value must be a positive integer.");
                        }

                        // Optional: Confirm large inputs
                        if (numberStepsGraph > 1000) { // Example threshold
                            int response = JOptionPane.showConfirmDialog(
                                    OptionPricerGUI.this,
                                    "You have entered a large number of steps (" + numberStepsGraph + "). This may take a significant amount of time. Do you wish to proceed?",
                                    "Confirm Large Input",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );

                            if (response != JOptionPane.YES_OPTION) {
                                return; // Abort the operation
                            }
                        }

                        // Generate CSV in the background
                        generateCsvInBackground(
                                numberStepsGraph,
                                initialPriceSlider.getValue(),
                                strikePriceSlider.getValue(),
                                probabilityUpSlider.getValue() / 100.0,
                                upFactorSlider.getValue() / 100.0,
                                downFactorSlider.getValue() / 100.0,
                                interestRateSlider.getValue() / 100.0,
                                callOptionCheckBox.isSelected()
                        );


                    } catch (NumberFormatException ex) {
                        // Inform the user about invalid input
                        JOptionPane.showMessageDialog(
                                OptionPricerGUI.this,
                                "Invalid input! Please enter a valid positive integer.",
                                "Input Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        LOGGER.log(Level.WARNING, "Invalid input for numberStepsGraph: " + input);
                    }
                }
            }
        });

        // Create a Swing Timer that updates the memory usage every 250 milliseconds
        Timer memoryTimer = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the runtime instance
                Runtime runtime = Runtime.getRuntime();

                // Calculate memory usage in bytes
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long usedMemoryBytes = totalMemory - freeMemory;

                // Convert to megabytes
                long usedMemoryMB = usedMemoryBytes / (1024 * 1024);

                // Update the label
                memoryUsageLabel.setText("Memory Usage: " + usedMemoryMB + " MB");
            }
        });
        memoryTimer.start(); // Start the timer


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
        interestRateField.setText(String.format("%.2f", interestRateSlider.getValue() / 100.0)); // Now supports negative values
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
            int steps = stepsSlider.getValue();
            boolean isCall = callOptionCheckBox.isSelected();

            SimpleBinomialTree binomialTree = new SimpleBinomialTree(initialPrice, strikePrice, probabilityUp,
                    upFactor, downFactor, interestRate, isCall);
            MultiStepBinomialTree multiStepBinomialTree = new MultiStepBinomialTree(initialPrice, strikePrice, probabilityUp,
                    upFactor, downFactor, interestRate, isCall, steps);

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
            LOGGER.log(Level.SEVERE, "IllegalArgumentException: " + ex.getMessage());
        }
    }

    private void runPythonScript(double computationTime) {
        // Disable the button to prevent multiple clicks
        runPythonButton.setEnabled(false);
        pythonOutputArea.setText("Running Python script...\n");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Define the path to the Python interpreter based on OS
                String pythonInterpreter;
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("mac") || osName.contains("nix") || osName.contains("nux")) {
                    pythonInterpreter = "venv/bin/python"; // macOS/Linux path
                } else if (osName.contains("win")) {
                    pythonInterpreter = "venv\\Scripts\\python.exe"; // Windows path
                } else {
                    // Default to macOS/Linux path
                    pythonInterpreter = "venv/bin/python";
                }

                // Define the relative path to the Python script
                String pythonScript = "src/main/python/OptionPriceBinomialTreeGraph.py";

                // Create a ProcessBuilder instance with the computation time as an argument
                ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScript, String.valueOf(computationTime));

                // Set the working directory to the project's root directory
                processBuilder.directory(new File(System.getProperty("user.dir")));

                // Redirect error stream to standard output for easier handling
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

                    // Update the JTextArea on the Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        if (exitCode == 0) {
                            pythonOutputArea.setText("Python script executed successfully:\n" + output.toString());
                        } else {
                            pythonOutputArea.setText("Python script execution failed with exit code " + exitCode + ":\n" + output.toString());
                        }
                        // Re-enable the button
                        runPythonButton.setEnabled(true);
                    });

                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        pythonOutputArea.setText("An error occurred while executing the Python script:\n" + ex.getMessage());
                        runPythonButton.setEnabled(true);
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private void generateCsvInBackground(int numberStepsGraph, double initialPrice, double strikePrice,
                                         double probabilityUp, double upFactor, double downFactor,
                                         double interestRate, boolean isCall) {

        runPythonButton.setEnabled(false);

        long startTime = System.currentTimeMillis();

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private long maxUsedMemory = 0; // Track max memory usage

            @Override
            protected Void doInBackground() throws Exception {
                LOGGER.log(Level.INFO, "number of steps graph: " + numberStepsGraph);

                try (FileWriter writer = new FileWriter(filePath)) {
                    writer.append("Step,OptionPrice,ComputationTime\n");

                    for (int i = 1; i <= numberStepsGraph; i++) {
                        if (isCancelled()) break;

                        long stepStartTime = System.nanoTime();

                        MultiStepBinomialTree largeBinomialTree = new MultiStepBinomialTree(
                                initialPrice, strikePrice, probabilityUp, upFactor, downFactor,
                                interestRate, isCall, i);

                        double stepOptionPrice = largeBinomialTree.getOptionPrice();

                        long stepEndTime = System.nanoTime();
                        double computationTime = (stepEndTime - stepStartTime) / 1_000_000.0;

                        writer.append(String.valueOf(i))
                                .append(",")
                                .append(String.valueOf(stepOptionPrice))
                                .append(",")
                                .append(String.format("%.6f", computationTime))
                                .append("\n");

                        // Update max memory usage
                        Runtime runtime = Runtime.getRuntime();
                        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                        if (usedMemory > maxUsedMemory) {
                            maxUsedMemory = usedMemory;
                        }

                        publish(i);
                    }

                    long endTime = System.currentTimeMillis();
                    double computationTime = (endTime - startTime) / 1000.0;
                    LOGGER.log(Level.INFO, "Total computation time: " + computationTime + " seconds");

                    runPythonScript(computationTime);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {

                //TODO add new label
                int latestStep = chunks.get(chunks.size() - 1);
                expectedValueLabel.setText("Writing step " + latestStep + "/" + numberStepsGraph);
            }

            @Override
            protected void done() {
                long endTime = System.currentTimeMillis();
                double totalComputationTime = (endTime - startTime) / 1000.0;

                // Convert max memory to MB and log
                long maxUsedMemoryMB = maxUsedMemory / (1024 * 1024);
                LOGGER.log(Level.INFO, "Max memory used during CSV generation: " + maxUsedMemoryMB + " MB");
                pythonOutputArea.append("Max memory used during CSV generation: " + maxUsedMemoryMB + " MB\n");

                LOGGER.log(Level.INFO, "Total computation time: " + totalComputationTime + " seconds");

                runPythonButton.setEnabled(true);
                try {
                    get();
                    LOGGER.log(Level.INFO, "Data exported successfully to " + filePath);
                    pythonOutputArea.append("Data exported successfully to " + filePath + "\n");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error writing to CSV: " + ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Error writing to CSV: " + ex.getMessage(),
                            "File Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void benchmarkFor10Seconds(double initialPrice, double strikePrice,
                                       double probabilityUp, double upFactor, double downFactor,
                                       double interestRate, boolean isCall) {

        runPythonButton.setEnabled(false);
        pythonOutputArea.setText("Starting benchmark for 10 seconds...\n");

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            private long maxUsedMemory = 0;
            private int totalStepsComputed = 0;

            @Override
            protected Void doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + 10_000; // Run for 10 seconds

                try (FileWriter writer = new FileWriter(filePath)) {
                    writer.append("Step,OptionPrice,ComputationTime\n");

                    int step = 1;
                    while (System.currentTimeMillis() < endTime) {
                        long stepStartTime = System.nanoTime();

                        MultiStepBinomialTree binomialTree = new MultiStepBinomialTree(
                                initialPrice, strikePrice, probabilityUp, upFactor,
                                downFactor, interestRate, isCall, step);

                        double optionPrice = binomialTree.getOptionPrice();
                        long stepEndTime = System.nanoTime();
                        double computationTime = (stepEndTime - stepStartTime) / 1_000_000.0;

                        writer.append(step + "," + optionPrice + "," + computationTime + "\n");

                        totalStepsComputed = step;
                        step++;

                        // Track max memory usage
                        Runtime runtime = Runtime.getRuntime();
                        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                        if (usedMemory > maxUsedMemory) {
                            maxUsedMemory = usedMemory;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                long maxUsedMemoryMB = maxUsedMemory / (1024 * 1024);

                pythonOutputArea.append("Benchmark completed.\n");
                pythonOutputArea.append("Total Steps Computed in 10s: " + totalStepsComputed + "\n");
                pythonOutputArea.append("Max Memory Used: " + maxUsedMemoryMB + " MB\n");

                runPythonButton.setEnabled(true);

                try {
                    get(); // Ensure no exceptions occurred
                    LOGGER.log(Level.INFO, "Benchmark completed successfully.");
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error during benchmarking: " + ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(
                            OptionPricerGUI.this,
                            "Error during benchmarking: " + ex.getMessage(),
                            "Benchmark Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

}
