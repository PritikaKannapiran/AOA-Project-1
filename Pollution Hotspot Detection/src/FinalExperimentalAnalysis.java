import java.io.*;
import java.util.*;

public class FinalExperimentalAnalysis {
    
    public static double[][] generatePollutionGrid(int n, int numHotspots, Random rand) {
        double[][] grid = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = rand.nextDouble() * 30 + 10;
            }
        }
        
        for (int h = 0; h < numHotspots; h++) {
            int centerX = rand.nextInt(n);
            int centerY = rand.nextInt(n);
            int radius = 2 + rand.nextInt(3);
            
            for (int i = Math.max(0, centerX - radius); i < Math.min(n, centerX + radius); i++) {
                for (int j = Math.max(0, centerY - radius); j < Math.min(n, centerY + radius); j++) {
                    double distance = Math.sqrt((i - centerX) * (i - centerX) + (j - centerY) * (j - centerY));
                    if (distance <= radius) {
                        grid[i][j] += (100 - 20 * distance) * rand.nextDouble();
                    }
                }
            }
        }
        
        return grid;
    }
    
    public static void runExperiments() {
        int[] sizes = {16, 32, 64, 128, 256, 512, 1024};
        int numTrials = 5;
        double threshold = 80.0;
        int k = 10;
        
        System.out.println("=== Divide and Conquer Algorithm Analysis ===");
        System.out.println("Grid Size,Actual Time (ms),Std Dev (ms),Actual Comparisons,Theoretical O(n^2 log n)");
        
        try (PrintWriter csvWriter = new PrintWriter(new FileWriter("dc_analysis_results.csv"))) {
            csvWriter.println("GridSize,ActualTime,StdDev,ActualComparisons,TheoreticalComplexity,ActualComplexityNormalized");
            
            for (int size : sizes) {
                double[] times = new double[numTrials];
                long totalComparisons = 0;
                
                for (int trial = 0; trial < numTrials; trial++) {
                    Random rand = new Random(42 + trial);
                    double[][] grid = generatePollutionGrid(size, size / 10, rand);
                    HotspotDetector detector = new HotspotDetector(grid, threshold);
                    
                    long startTime = System.nanoTime();
                    List<PollutionData> hotspots = detector.detectHotspots(k);
                    long endTime = System.nanoTime();
                    
                    times[trial] = (endTime - startTime) / 1_000_000.0;
                    totalComparisons += detector.getComparisonCount();
                }
                
                double avgTime = Arrays.stream(times).average().orElse(0);
                double stdDev = calculateStdDev(times, avgTime);
                long avgComparisons = totalComparisons / numTrials;
                
                double theoreticalComplexity = size * size * (Math.log(size) / Math.log(2));
                double actualComplexityNormalized = avgComparisons;
                
                System.out.printf("%d,%.4f,%.4f,%d,%.0f%n", 
                    size, avgTime, stdDev, avgComparisons, theoreticalComplexity);
                
                csvWriter.printf("%d,%.4f,%.4f,%d,%.0f,%.0f%n", 
                    size, avgTime, stdDev, avgComparisons, theoreticalComplexity, actualComplexityNormalized);
            }
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
        
        System.out.println("\nResults saved to dc_analysis_results.csv");
    }
    
    private static double calculateStdDev(double[] values, double mean) {
        double sumSquaredDiffs = 0;
        for (double value : values) {
            sumSquaredDiffs += (value - mean) * (value - mean);
        }
        return Math.sqrt(sumSquaredDiffs / values.length);
    }
    
    public static void generatePlotScript() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("plot_dc_analysis.py"))) {
            writer.println("import pandas as pd");
            writer.println("import matplotlib.pyplot as plt");
            writer.println("import numpy as np");
            writer.println();
            writer.println("df = pd.read_csv('dc_analysis_results.csv')");
            writer.println();
            writer.println("fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(15, 6))");
            writer.println();
            writer.println("ax1.errorbar(df['GridSize'], df['ActualTime'], yerr=df['StdDev'], ");
            writer.println("             marker='o', capsize=5, label='Actual Performance', linewidth=2, color='blue')");
            writer.println();
            writer.println("theoretical_normalized = df['TheoreticalComplexity'] * (df['ActualTime'].iloc[0] / df['TheoreticalComplexity'].iloc[0])");
            writer.println("ax1.plot(df['GridSize'], theoretical_normalized, ");
            writer.println("         linestyle='--', label='Theoretical O(n² log n)', linewidth=2, color='red')");
            writer.println();
            writer.println("ax1.set_xlabel('Grid Size (n)', fontsize=12)");
            writer.println("ax1.set_ylabel('Running Time (ms)', fontsize=12)");
            writer.println("ax1.set_title('Divide & Conquer: Actual vs Theoretical Performance', fontsize=14, fontweight='bold')");
            writer.println("ax1.grid(True, alpha=0.3)");
            writer.println("ax1.legend()");
            writer.println();
            writer.println("ax2.loglog(df['GridSize'], df['ActualComparisons'], ");
            writer.println("           marker='o', label='Actual Comparisons', linewidth=2, color='blue')");
            writer.println("ax2.loglog(df['GridSize'], df['TheoreticalComplexity'], ");
            writer.println("           linestyle='--', label='Theoretical O(n² log n)', linewidth=2, color='red')");
            writer.println();
            writer.println("ax2.set_xlabel('Grid Size (n)', fontsize=12)");
            writer.println("ax2.set_ylabel('Number of Operations (log scale)', fontsize=12)");
            writer.println("ax2.set_title('Complexity Analysis: Actual vs Theoretical', fontsize=14, fontweight='bold')");
            writer.println("ax2.grid(True, alpha=0.3, which='both')");
            writer.println("ax2.legend()");
            writer.println();
            writer.println("efficiency = (df['ActualComparisons'] / df['TheoreticalComplexity'] * 100).mean()");
            writer.println("plt.figtext(0.5, 0.02, ");
            writer.println("    f'Average Algorithm Efficiency: {efficiency:.1f}% of theoretical maximum\\n' +");
            writer.println("    'Divide & Conquer shows consistent O(n² log n) scaling behavior', ");
            writer.println("    ha='center', fontsize=10, bbox=dict(facecolor='lightgray', alpha=0.8))");
            writer.println();
            writer.println("plt.tight_layout()");
            writer.println("plt.subplots_adjust(bottom=0.15)");
            writer.println("plt.savefig('dc_complexity_analysis.png', dpi=300, bbox_inches='tight')");
            writer.println("print('Divide & Conquer complexity analysis plot saved as dc_complexity_analysis.png')");
            writer.println("plt.show()");
            
            System.out.println("Python plotting script saved as plot_dc_analysis.py");
        } catch (IOException e) {
            System.err.println("Error writing plot script: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Pollution Hotspot Detection - Divide & Conquer Analysis ===\n");
        runExperiments();
        generatePlotScript();
        System.out.println("\nTo generate complexity comparison plots, run: python plot_dc_analysis.py");
    }
}
