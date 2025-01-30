import javax.swing.*;

public class DiagramWindow extends JFrame {

    private BinomialTreePanel treePanel;

    public DiagramWindow() {
        super("Binomial Tree Diagram");

        // Initialize BinomialTreePanel
        treePanel = new BinomialTreePanel();

        // Embed BinomialTreePanel inside a JScrollPane
        JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Optionally, set preferred viewport size
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));

        // Add JScrollPane to the content pane
        getContentPane().add(scrollPane);

        // Adjust size/position as desired:
        pack();
        setLocation(700, 100);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Pass the latest option-values array and repaint the diagram.
     */
    public void updateTree(double[][] values) {
        treePanel.setOptionValues(values);
    }
}
