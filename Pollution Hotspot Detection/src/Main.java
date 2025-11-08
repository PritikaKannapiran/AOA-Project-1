import java.util.*;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== Pollution Hotspot Detection using Divide and Conquer ===\n");
        
        demonstrateAlgorithm();
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        realisticScenario();
    }
    
    private static void demonstrateAlgorithm() {
        System.out.println("DEMONSTRATION: 8x8 Satellite Grid\n");
        
        double[][] grid = {
            {15, 18, 22, 25, 20, 18, 16, 15},
            {18, 25, 35, 42, 38, 25, 20, 18},
            {22, 35, 88, 95, 85, 35, 25, 22},
            {25, 42, 95, 120, 92, 40, 28, 25},
            {20, 38, 85, 92, 80, 35, 25, 20},
            {18, 25, 35, 40, 35, 90, 95, 88},
            {16, 20, 25, 28, 32, 95, 110, 92},
            {15, 18, 22, 25, 28, 88, 92, 85}
        };
        
        System.out.println("Pollution Grid (Intensity values):");
        printGrid(grid);
        
        double threshold = 80.0;
        int k = 5;
        
        HotspotDetector detector = new HotspotDetector(grid, threshold);
        List<PollutionData> hotspots = detector.detectHotspots(k);
        
        System.out.println("\nDetected Hotspots (threshold = " + threshold + "):");
        System.out.println("-".repeat(50));
        for (int i = 0; i < hotspots.size(); i++) {
            PollutionData h = hotspots.get(i);
            System.out.printf("%d. %s%n", i + 1, h);
        }
        
        System.out.println("\nAlgorithm Statistics:");
        System.out.println("  - Grid size: 8x8");
        System.out.println("  - Comparisons made: " + detector.getComparisonCount());
        System.out.println("  - Hotspots found: " + hotspots.size());
    }
    
    private static void realisticScenario() {
        System.out.println("REALISTIC SCENARIO: 64x64 Satellite Grid\n");
        
        int n = 64;
        Random rand = new Random(12345);
        
        double[][] grid = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = 15 + rand.nextDouble() * 25;
            }
        }
        
        addHotspot(grid, 15, 20, 6, 120, rand);
        addHotspot(grid, 45, 10, 5, 110, rand);
        addHotspot(grid, 30, 50, 4, 95, rand);
        addHotspot(grid, 55, 55, 3, 85, rand);
        
        double threshold = 75.0;
        int k = 10;
        
        HotspotDetector detector = new HotspotDetector(grid, threshold);
        
        long startTime = System.nanoTime();
        List<PollutionData> hotspots = detector.detectHotspots(k);
        long endTime = System.nanoTime();
        
        double executionTime = (endTime - startTime) / 1_000_000.0;
        
        System.out.println("Top " + k + " Pollution Hotspots:");
        System.out.println("-".repeat(50));
        for (int i = 0; i < hotspots.size(); i++) {
            PollutionData h = hotspots.get(i);
            System.out.printf("%2d. %s%n", i + 1, h);
        }
        
        System.out.println("\nPerformance Metrics:");
        System.out.println("  - Grid size: " + n + "x" + n);
        System.out.println("  - Execution time: " + String.format("%.3f", executionTime) + " ms");
        System.out.println("  - Comparisons: " + detector.getComparisonCount());
        System.out.println("  - Hotspots detected: " + hotspots.size());
    }
    
    private static void addHotspot(double[][] grid, int centerX, int centerY, 
                                   int radius, double peakIntensity, Random rand) {
        int n = grid.length;
        for (int i = Math.max(0, centerX - radius); i < Math.min(n, centerX + radius + 1); i++) {
            for (int j = Math.max(0, centerY - radius); j < Math.min(n, centerY + radius + 1); j++) {
                double distance = Math.sqrt((i - centerX) * (i - centerX) + 
                                           (j - centerY) * (j - centerY));
                if (distance <= radius) {
                    double intensity = peakIntensity * (1 - distance / radius) * (0.8 + 0.4 * rand.nextDouble());
                    grid[i][j] = Math.max(grid[i][j], intensity);
                }
            }
        }
    }
    
    private static void printGrid(double[][] grid) {
        System.out.print("    ");
        for (int j = 0; j < grid[0].length; j++) {
            System.out.printf("%5d ", j);
        }
        System.out.println();
        System.out.println("    " + "-".repeat(grid[0].length * 6));
        
        for (int i = 0; i < grid.length; i++) {
            System.out.printf("%2d |", i);
            for (int j = 0; j < grid[i].length; j++) {
                System.out.printf("%5.0f ", grid[i][j]);
            }
            System.out.println();
        }
    }
}
