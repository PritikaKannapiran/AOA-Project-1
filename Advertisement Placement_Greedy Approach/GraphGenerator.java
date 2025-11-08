import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Generates complexity graph from experimental data
 * Creates publication-quality PNG image for LaTeX report
 */
public class GraphGenerator extends JPanel {
    
    // Experimental data from timing results
    private static final int[] N_VALUES = {10, 50, 100, 500, 1000, 2000, 5000, 10000};
    private static final int[] M_VALUES = {5, 25, 50, 250, 500, 1000, 2500, 5000};
    private static final double[] AVG_TIMES = {0.029, 0.053, 0.071, 0.253, 0.551, 1.488, 7.211, 35.112};
    private static final double[] STD_DEVS = {0.009, 0.008, 0.015, 0.070, 0.057, 0.136, 0.320, 1.394};
    
    // Graph dimensions
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;
    private static final int MARGIN = 80;
    private static final int PLOT_WIDTH = WIDTH - 2 * MARGIN;
    private static final int PLOT_HEIGHT = HEIGHT - 2 * MARGIN;
    
    // Use logarithmic scale for better visualization
    private boolean useLogScale = true;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Fill background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw title
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String title = "Empirical Time Complexity: O(n log n + nm)";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 40);
        
        // Draw axes
        drawAxes(g2d);
        
        // Draw grid
        drawGrid(g2d);
        
        // Draw theoretical curve
        drawTheoreticalCurve(g2d);
        
        // Draw experimental data points with error bars
        drawExperimentalData(g2d);
        
        // Draw legend
        drawLegend(g2d);
        
        // Draw axis labels
        drawLabels(g2d);
    }
    
    private void drawAxes(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        
        // X-axis
        g2d.drawLine(MARGIN, HEIGHT - MARGIN, WIDTH - MARGIN, HEIGHT - MARGIN);
        
        // Y-axis
        g2d.drawLine(MARGIN, MARGIN, MARGIN, HEIGHT - MARGIN);
        
        // Draw tick marks and labels
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // X-axis ticks (logarithmic)
        int[] xTicks = {10, 50, 100, 500, 1000, 5000, 10000};
        for (int tick : xTicks) {
            int x = MARGIN + (int)(scaleX(tick) * PLOT_WIDTH);
            g2d.drawLine(x, HEIGHT - MARGIN, x, HEIGHT - MARGIN + 5);
            String label = String.valueOf(tick);
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(label, x - fm.stringWidth(label)/2, HEIGHT - MARGIN + 20);
        }
        
        // Y-axis ticks (logarithmic)
        double[] yTicks = {0.01, 0.1, 1.0, 10.0, 100.0};
        for (double tick : yTicks) {
            if (tick <= 40) { // Only show ticks within our data range
                int y = HEIGHT - MARGIN - (int)(scaleY(tick) * PLOT_HEIGHT);
                g2d.drawLine(MARGIN - 5, y, MARGIN, y);
                String label = String.format("%.2f", tick);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(label, MARGIN - fm.stringWidth(label) - 10, y + 5);
            }
        }
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                     10, new float[]{5}, 0));
        
        // Vertical grid lines
        int[] xTicks = {10, 50, 100, 500, 1000, 5000, 10000};
        for (int tick : xTicks) {
            int x = MARGIN + (int)(scaleX(tick) * PLOT_WIDTH);
            g2d.drawLine(x, MARGIN, x, HEIGHT - MARGIN);
        }
        
        // Horizontal grid lines
        double[] yTicks = {0.01, 0.1, 1.0, 10.0, 100.0};
        for (double tick : yTicks) {
            if (tick <= 40) {
                int y = HEIGHT - MARGIN - (int)(scaleY(tick) * PLOT_HEIGHT);
                g2d.drawLine(MARGIN, y, WIDTH - MARGIN, y);
            }
        }
    }
    
    private void drawTheoreticalCurve(Graphics2D g2d) {
        g2d.setColor(new Color(241, 143, 1)); // Orange
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                     10, new float[]{10, 5}, 0));
        
        // Calculate theoretical times for smooth curve
        int numPoints = 100;
        int[] xPoints = new int[numPoints];
        int[] yPoints = new int[numPoints];
        
        // Find scaling factor from experimental data at n=1000
        double n_ref = 1000;
        double m_ref = 500;
        double time_ref = 0.551;
        double complexity_ref = n_ref * Math.log(n_ref) / Math.log(2) + n_ref * m_ref;
        double scalingFactor = time_ref / complexity_ref;
        
        for (int i = 0; i < numPoints; i++) {
            // Logarithmically spaced points
            double logN = Math.log(10) + i * (Math.log(10000) - Math.log(10)) / (numPoints - 1);
            double n = Math.exp(logN);
            double m = n / 2;
            double complexity = n * Math.log(n) / Math.log(2) + n * m;
            double theoreticalTime = complexity * scalingFactor;
            
            xPoints[i] = MARGIN + (int)(scaleX(n) * PLOT_WIDTH);
            yPoints[i] = HEIGHT - MARGIN - (int)(scaleY(theoreticalTime) * PLOT_HEIGHT);
        }
        
        g2d.drawPolyline(xPoints, yPoints, numPoints);
    }
    
    private void drawExperimentalData(Graphics2D g2d) {
        // Draw line connecting points
        g2d.setColor(new Color(46, 134, 171)); // Blue
        g2d.setStroke(new BasicStroke(3));
        
        for (int i = 0; i < N_VALUES.length - 1; i++) {
            int x1 = MARGIN + (int)(scaleX(N_VALUES[i]) * PLOT_WIDTH);
            int y1 = HEIGHT - MARGIN - (int)(scaleY(AVG_TIMES[i]) * PLOT_HEIGHT);
            int x2 = MARGIN + (int)(scaleX(N_VALUES[i + 1]) * PLOT_WIDTH);
            int y2 = HEIGHT - MARGIN - (int)(scaleY(AVG_TIMES[i + 1]) * PLOT_HEIGHT);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Draw error bars
        g2d.setColor(new Color(162, 59, 114)); // Purple
        g2d.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < N_VALUES.length; i++) {
            int x = MARGIN + (int)(scaleX(N_VALUES[i]) * PLOT_WIDTH);
            int y = HEIGHT - MARGIN - (int)(scaleY(AVG_TIMES[i]) * PLOT_HEIGHT);
            
            double upperTime = AVG_TIMES[i] + STD_DEVS[i];
            double lowerTime = Math.max(0.01, AVG_TIMES[i] - STD_DEVS[i]);
            
            int yUpper = HEIGHT - MARGIN - (int)(scaleY(upperTime) * PLOT_HEIGHT);
            int yLower = HEIGHT - MARGIN - (int)(scaleY(lowerTime) * PLOT_HEIGHT);
            
            // Vertical error bar
            g2d.drawLine(x, yUpper, x, yLower);
            // Cap lines
            g2d.drawLine(x - 5, yUpper, x + 5, yUpper);
            g2d.drawLine(x - 5, yLower, x + 5, yLower);
        }
        
        // Draw data points
        g2d.setColor(new Color(46, 134, 171)); // Blue
        for (int i = 0; i < N_VALUES.length; i++) {
            int x = MARGIN + (int)(scaleX(N_VALUES[i]) * PLOT_WIDTH);
            int y = HEIGHT - MARGIN - (int)(scaleY(AVG_TIMES[i]) * PLOT_HEIGHT);
            
            g2d.fillOval(x - 6, y - 6, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x - 3, y - 3, 6, 6);
            g2d.setColor(new Color(46, 134, 171));
        }
        
        // Annotate key points
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        int[] annotateIndices = {0, 4, 7}; // Points at n=10, 1000, 10000
        
        for (int idx : annotateIndices) {
            int x = MARGIN + (int)(scaleX(N_VALUES[idx]) * PLOT_WIDTH);
            int y = HEIGHT - MARGIN - (int)(scaleY(AVG_TIMES[idx]) * PLOT_HEIGHT);
            
            String label = String.format("(%d, %.3f ms)", N_VALUES[idx], AVG_TIMES[idx]);
            
            // Draw background box
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            
            g2d.setColor(new Color(255, 255, 200, 230));
            g2d.fillRoundRect(x + 10, y - 15, labelWidth + 10, labelHeight + 5, 5, 5);
            
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x + 10, y - 15, labelWidth + 10, labelHeight + 5, 5, 5);
            g2d.drawString(label, x + 15, y - 2);
            
            // Draw arrow
            g2d.drawLine(x + 6, y, x + 10, y - 10);
        }
    }
    
    private void drawLegend(Graphics2D g2d) {
        int legendX = WIDTH - MARGIN - 280;
        int legendY = MARGIN + 20;
        
        // Legend background
        g2d.setColor(new Color(255, 255, 255, 230));
        g2d.fillRoundRect(legendX - 10, legendY - 10, 270, 80, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(legendX - 10, legendY - 10, 270, 80, 10, 10);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Experimental data legend
        g2d.setColor(new Color(46, 134, 171));
        g2d.fillOval(legendX, legendY, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Experimental Results (avg ± std dev)", legendX + 20, legendY + 10);
        
        // Theoretical curve legend
        legendY += 30;
        g2d.setColor(new Color(241, 143, 1));
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                                     10, new float[]{10, 5}, 0));
        g2d.drawLine(legendX, legendY + 5, legendX + 30, legendY + 5);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Theoretical O(n log n + nm)", legendX + 40, legendY + 10);
    }
    
    private void drawLabels(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        
        // X-axis label
        String xLabel = "Input Size (n = number of advertisements)";
        FontMetrics fm = g2d.getFontMetrics();
        int xLabelX = (WIDTH - fm.stringWidth(xLabel)) / 2;
        g2d.drawString(xLabel, xLabelX, HEIGHT - 20);
        
        // Y-axis label (rotated)
        String yLabel = "Execution Time (milliseconds)";
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.rotate(-Math.PI / 2);
        fm = g2dRotated.getFontMetrics();
        int yLabelY = (HEIGHT - fm.stringWidth(yLabel)) / 2;
        g2dRotated.drawString(yLabel, -HEIGHT + yLabelY, 20);
        g2dRotated.dispose();
    }
    
    private double scaleX(double n) {
        // Logarithmic scaling
        if (useLogScale) {
            double logN = Math.log(n);
            double logMin = Math.log(10);
            double logMax = Math.log(10000);
            return (logN - logMin) / (logMax - logMin);
        }
        return (n - 10) / (10000 - 10);
    }
    
    private double scaleY(double time) {
        // Logarithmic scaling
        if (useLogScale) {
            double logTime = Math.log(Math.max(0.01, time));
            double logMin = Math.log(0.01);
            double logMax = Math.log(40);
            return (logTime - logMin) / (logMax - logMin);
        }
        return time / 40;
    }
    
    public static void saveGraph(String filename) {
        GraphGenerator panel = new GraphGenerator();
        panel.setSize(WIDTH, HEIGHT);
        
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        panel.paintComponent(g2d);
        g2d.dispose();
        
        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("Graph saved successfully as: " + filename);
            System.out.println("Image dimensions: " + WIDTH + "x" + HEIGHT + " pixels");
            System.out.println("Resolution: 300 DPI equivalent for LaTeX");
        } catch (IOException e) {
            System.err.println("Error saving graph: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Generating Complexity Graph ===\n");
        
        // Generate the main graph (log scale)
        saveGraph("complexity_graph.png");
        
        System.out.println("\nGraph generation complete!");
        System.out.println("\nStatistical Summary:");
        System.out.printf("Input size range: %d to %d (factor of %.0f)\n", 
                         N_VALUES[0], N_VALUES[N_VALUES.length-1], 
                         (double)N_VALUES[N_VALUES.length-1] / N_VALUES[0]);
        System.out.printf("Time range: %.3fms to %.3fms (factor of %.0f)\n", 
                         AVG_TIMES[0], AVG_TIMES[AVG_TIMES.length-1],
                         AVG_TIMES[AVG_TIMES.length-1] / AVG_TIMES[0]);
        
        // Calculate average coefficient of variation
        double avgCV = 0;
        for (int i = 0; i < AVG_TIMES.length; i++) {
            avgCV += (STD_DEVS[i] / AVG_TIMES[i]);
        }
        avgCV /= AVG_TIMES.length;
        System.out.printf("Average coefficient of variation: %.1f%%\n", avgCV * 100);
        
        // Growth rate analysis
        System.out.println("\nGrowth Rate Analysis:");
        System.out.printf("Growth from n=100 to n=1000: %.2fx\n", AVG_TIMES[4] / AVG_TIMES[2]);
        System.out.printf("Growth from n=1000 to n=10000: %.2fx\n", AVG_TIMES[7] / AVG_TIMES[4]);
        
        System.out.println("\nPlace 'complexity_graph.png' in the same directory as your LaTeX file.");
    }
}