import javax.swing.*;
import java.awt.*;

public class BinomialTreePanel extends JPanel {
    private double[][] optionValues;

    public BinomialTreePanel() {
        setPreferredSize(new Dimension(800, 600));
    }

    public void setOptionValues(double[][] optionValues) {
        this.optionValues = optionValues;
        adjustPreferredSize();
        repaint();
    }

    private void adjustPreferredSize() {
        if (optionValues == null) return;

        int steps = optionValues.length;
        int xSpacing = 150;
        int ySpacing = 80;
        int xStart = 100;
        int yStart = 100;

        int width = xStart + steps * xSpacing + 100;
        int nodesInLastStep = steps; // Each step has step + 1 nodes
        int height = yStart + nodesInLastStep * ySpacing + 100;

        setPreferredSize(new Dimension(width, height));
        revalidate(); // Notify parent containers of the size change
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (optionValues == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Layout parameters
        int xSpacing = 150;
        int ySpacing = 80;
        int nodeRadius = 25;
        int xStart = 100;
        int yStart = 100;

        int steps = optionValues.length;
        int[][] nodeX = new int[steps][];
        int[][] nodeY = new int[steps][];

        // Calculate all node positions
        for (int step = 0; step < steps; step++) {
            int nodesInStep = step + 1;
            nodeX[step] = new int[nodesInStep];
            nodeY[step] = new int[nodesInStep];

            // Center nodes vertically based on step
            double verticalOffset = (steps - 1 - step) * ySpacing / 2.0;

            for (int node = 0; node < nodesInStep; node++) {
                int x = xStart + step * xSpacing;
                int y = yStart + (int) (node * ySpacing + verticalOffset);
                nodeX[step][node] = x;
                nodeY[step][node] = y;
            }
        }

        // Draw connecting lines first (so nodes appear on top)
        g2.setColor(new Color(100, 100, 100));
        for (int step = 0; step < steps - 1; step++) {
            for (int node = 0; node < step + 1; node++) {
                // Connect to down node
                if (node < step + 2) {
                    g2.drawLine(nodeX[step][node], nodeY[step][node],
                            nodeX[step + 1][node], nodeY[step + 1][node]);
                }
                // Connect to up node
                if (node + 1 < step + 2) {
                    g2.drawLine(nodeX[step][node], nodeY[step][node],
                            nodeX[step + 1][node + 1], nodeY[step + 1][node + 1]);
                }
            }
        }

        // Draw nodes and text
        Font valueFont = new Font("SansSerif", Font.BOLD, 12);
        g2.setFont(valueFont);
        for (int step = 0; step < steps; step++) {
            for (int node = 0; node < step + 1; node++) {
                int x = nodeX[step][node];
                int y = nodeY[step][node];

                // Draw node
                g2.setColor(new Color(230, 240, 255));
                g2.fillOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
                g2.setColor(Color.BLUE);
                g2.drawOval(x - nodeRadius, y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);

                // Draw value text
                String value = String.format("$%.2f", optionValues[step][step - node]);
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(value);
                int textHeight = fm.getHeight();
                g2.setColor(Color.BLACK);
                g2.drawString(value, x - textWidth / 2, y + textHeight / 4);
            }
        }
    }
}
