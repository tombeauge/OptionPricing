import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DiagramWindow extends JFrame {

    private final BinomialTreePanel treePanel;
    private final JButton toggleDisplayButton;

    // Flags to track the current display state
    private boolean isShowingStockPrices = false;

    // References to the latest data
    private double[][] currentOptionValues;
    private double[][] currentStockPrices;

    public DiagramWindow() {
        super("Binomial Tree Diagram");

        // Initialize BinomialTreePanel
        treePanel = new BinomialTreePanel();

        // Embed BinomialTreePanel inside a JScrollPane
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Optionally, set preferred viewport size
        scrollPane.setPreferredSize(new Dimension(800, 600));

        // Add JScrollPane to the content pane
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Initialize the toggle button
        toggleDisplayButton = new JButton("Show Stock Prices");
        add(toggleDisplayButton, BorderLayout.SOUTH);

        // Add ActionListener to the button
        toggleDisplayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Toggle the display state
                isShowingStockPrices = !isShowingStockPrices;

                // Update the button label based on the new state
                if (isShowingStockPrices) {
                    toggleDisplayButton.setText("Show Option Values");
                    if (currentStockPrices != null) {
                        treePanel.setStockPrices(currentStockPrices);
                    }
                } else {
                    toggleDisplayButton.setText("Show Stock Prices");
                    if (currentOptionValues != null) {
                        treePanel.setOptionValues(currentOptionValues);
                    }
                }

                // Refresh the panel to reflect changes
                treePanel.repaint();
            }
        });

        // Adjust size and position
        pack();
        setLocation(700, 100);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Update the tree with the latest option and stock prices.
     *
     * @param optionValues 2D array of option values.
     * @param stockPrices  2D array of stock prices.
     */
    public void updateTree(double[][] optionValues, double[][] stockPrices) {
        this.currentOptionValues = optionValues;
        this.currentStockPrices = stockPrices;

        // Display the appropriate data based on the current state
        if (isShowingStockPrices) {
            treePanel.setStockPrices(stockPrices);
        } else {
            treePanel.setOptionValues(optionValues);
        }

        // Refresh the panel to ensure the latest data is shown
        treePanel.repaint();
    }
}
