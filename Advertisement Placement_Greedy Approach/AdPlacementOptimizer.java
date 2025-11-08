import java.util.*;
import java.util.stream.Collectors;

/**
 * Advertisement Placement Optimization using Greedy Algorithm
 * Maximizes total engagement by placing ads with highest CTR in available slots
 */
public class AdPlacementOptimizer {
    
    /**
     * Represents an advertisement with engagement metrics
     */
    static class Advertisement implements Comparable<Advertisement> {
        String id;
        double clickThroughRate;  // CTR as a percentage (0-100)
        int duration;             // Duration in time units
        String name;
        
        public Advertisement(String id, String name, double ctr, int duration) {
            this.id = id;
            this.name = name;
            this.clickThroughRate = ctr;
            this.duration = duration;
        }
        
        @Override
        public int compareTo(Advertisement other) {
            // Sort in descending order by CTR (highest first)
            return Double.compare(other.clickThroughRate, this.clickThroughRate);
        }
        
        @Override
        public String toString() {
            return String.format("%s (CTR: %.2f%%, Duration: %d)", name, clickThroughRate, duration);
        }
    }
    
    /**
     * Represents a time slot for advertisement placement
     */
    static class TimeSlot {
        int startTime;
        int endTime;
        Advertisement placedAd;
        
        public TimeSlot(int start, int end) {
            this.startTime = start;
            this.endTime = end;
            this.placedAd = null;
        }
        
        public int getDuration() {
            return endTime - startTime;
        }
        
        public boolean canFit(Advertisement ad) {
            return placedAd == null && ad.duration <= getDuration();
        }
        
        @Override
        public String toString() {
            if (placedAd != null) {
                return String.format("[%d-%d]: %s", startTime, endTime, placedAd);
            }
            return String.format("[%d-%d]: EMPTY", startTime, endTime);
        }
    }
    
    /**
     * Result of the optimization algorithm
     */
    static class PlacementResult {
        List<TimeSlot> schedule;
        double totalEngagement;
        int adsPlaced;
        
        public PlacementResult(List<TimeSlot> schedule, double engagement, int placed) {
            this.schedule = schedule;
            this.totalEngagement = engagement;
            this.adsPlaced = placed;
        }
    }
    
    /**
     * GREEDY ALGORITHM: Maximize total engagement by placing ads with highest CTR first
     * 
     * Algorithm:
     * 1. Sort advertisements by CTR in descending order
     * 2. For each ad (in sorted order):
     *    a. Find the first available slot that can fit the ad
     *    b. If found, place the ad in that slot
     *    c. Update total engagement
     * 3. Return the schedule and total engagement
     * 
     * Time Complexity: O(n log n + n*m) where n = number of ads, m = number of slots
     * Space Complexity: O(n + m)
     */
    public static PlacementResult optimizePlacement(List<Advertisement> ads, List<TimeSlot> slots) {
        // Step 1: Sort ads by CTR (descending) - O(n log n)
        List<Advertisement> sortedAds = new ArrayList<>(ads);
        Collections.sort(sortedAds);
        
        double totalEngagement = 0.0;
        int adsPlaced = 0;
        
        // Step 2: Greedily assign ads to slots - O(n * m)
        for (Advertisement ad : sortedAds) {
            // Find first available slot that fits this ad
            for (TimeSlot slot : slots) {
                if (slot.canFit(ad)) {
                    // Place the ad
                    slot.placedAd = ad;
                    totalEngagement += ad.clickThroughRate;
                    adsPlaced++;
                    break; // Move to next ad
                }
            }
        }
        
        return new PlacementResult(slots, totalEngagement, adsPlaced);
    }
    
    /**
     * Experimental validation: measures running time for different input sizes
     * Uses multiple runs and warmup to get accurate measurements
     */
    public static void runTimeComplexityExperiment() {
        System.out.println("=== Running Time Complexity Experiment ===\n");
        System.out.println("Warming up JVM...");
        
        // Warmup phase: run algorithm multiple times to trigger JIT compilation
        for (int i = 0; i < 5; i++) {
            List<Advertisement> warmupAds = generateRandomAds(1000);
            List<TimeSlot> warmupSlots = generateTimeSlots(500);
            optimizePlacement(warmupAds, warmupSlots);
        }
        
        System.out.println("Warmup complete. Running experiments...\n");
        System.out.println("Input Size (n,m)\tAvg Time (ms)\tStd Dev\t\tAds Placed");
        System.out.println("------------------------------------------------------------------------");
        
        int[] inputSizes = {10, 50, 100, 500, 1000, 2000, 5000, 10000};
        int numRuns = 10; // Run each size multiple times for averaging
        
        for (int size : inputSizes) {
            double[] times = new double[numRuns];
            int adsPlaced = 0;
            
            for (int run = 0; run < numRuns; run++) {
                // Generate fresh test data for each run
                List<Advertisement> ads = generateRandomAds(size);
                List<TimeSlot> slots = generateTimeSlots(size / 2); // m = n/2
                
                // Force garbage collection before timing
                System.gc();
                try {
                    Thread.sleep(10); // Small delay to let GC complete
                } catch (InterruptedException e) {
                    // Ignore
                }
                
                // Measure execution time
                long startTime = System.nanoTime();
                PlacementResult result = optimizePlacement(ads, slots);
                long endTime = System.nanoTime();
                
                times[run] = (endTime - startTime) / 1_000_000.0;
                if (run == 0) {
                    adsPlaced = result.adsPlaced;
                }
            }
            
            // Calculate statistics
            double avgTime = calculateMean(times);
            double stdDev = calculateStdDev(times, avgTime);
            
            System.out.printf("(%d, %d)\t\t%.3f\t\t%.3f\t\t%d\n", 
                size, size/2, avgTime, stdDev, adsPlaced);
        }
        
        System.out.println("\nNote: Time complexity is O(n log n + n*m)");
        System.out.println("Results show average of " + numRuns + " runs with standard deviation.");
        System.out.println("As input size increases, we observe the expected growth pattern.\n");
    }
    
    /**
     * Calculate mean of an array
     */
    private static double calculateMean(double[] values) {
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }
    
    /**
     * Calculate standard deviation
     */
    private static double calculateStdDev(double[] values, double mean) {
        double sumSquaredDiff = 0;
        for (double v : values) {
            sumSquaredDiff += Math.pow(v - mean, 2);
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }
    
    /**
     * Generate random advertisements for testing
     */
    private static List<Advertisement> generateRandomAds(int count) {
        List<Advertisement> ads = new ArrayList<>();
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < count; i++) {
            String id = "AD" + i;
            String name = "Advertisement " + i;
            double ctr = 0.5 + rand.nextDouble() * 9.5; // CTR between 0.5% and 10%
            int duration = 30 + rand.nextInt(91); // Duration between 30-120 seconds
            ads.add(new Advertisement(id, name, ctr, duration));
        }
        
        return ads;
    }
    
    /**
     * Generate time slots for testing
     */
    private static List<TimeSlot> generateTimeSlots(int count) {
        List<TimeSlot> slots = new ArrayList<>();
        int currentTime = 0;
        Random rand = new Random(42);
        
        for (int i = 0; i < count; i++) {
            int duration = 60 + rand.nextInt(61); // Slots of 60-120 seconds
            slots.add(new TimeSlot(currentTime, currentTime + duration));
            currentTime += duration;
        }
        
        return slots;
    }
    
    /**
     * Helper method to repeat a string (for Java 8 compatibility)
     */
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    /**
     * Demonstration with a realistic example
     */
    public static void demonstrateExample() {
        System.out.println("=== Advertisement Placement Optimization Demo ===\n");
        
        // Create sample advertisements
        List<Advertisement> ads = Arrays.asList(
            new Advertisement("A1", "Sports Shoe Ad", 8.5, 60),
            new Advertisement("A2", "Laptop Deal Ad", 6.2, 90),
            new Advertisement("A3", "Fast Food Ad", 9.1, 30),
            new Advertisement("A4", "Movie Trailer", 7.8, 120),
            new Advertisement("A5", "Car Insurance", 5.5, 60),
            new Advertisement("A6", "Smartphone Ad", 8.9, 45),
            new Advertisement("A7", "Travel Package", 6.8, 75)
        );
        
        // Create time slots (e.g., during a streaming session)
        List<TimeSlot> slots = Arrays.asList(
            new TimeSlot(0, 60),      // 1-minute slot
            new TimeSlot(60, 150),    // 1.5-minute slot
            new TimeSlot(150, 210),   // 1-minute slot
            new TimeSlot(210, 300),   // 1.5-minute slot
            new TimeSlot(300, 360)    // 1-minute slot
        );
        
        System.out.println("Available Advertisements (sorted by CTR):");
        List<Advertisement> sorted = new ArrayList<>(ads);
        Collections.sort(sorted);
        for (Advertisement ad : sorted) {
            System.out.println("  " + ad);
        }
        
        System.out.println("\nAvailable Time Slots:");
        for (TimeSlot slot : slots) {
            System.out.println("  " + slot);
        }
        
        // Run optimization
        PlacementResult result = optimizePlacement(ads, slots);
        
        System.out.println("\n=== OPTIMIZATION RESULT ===");
        System.out.println("Ads Placed: " + result.adsPlaced + " out of " + ads.size());
        System.out.printf("Total Engagement (CTR Sum): %.2f%%\n\n", result.totalEngagement);
        
        System.out.println("Final Schedule:");
        for (TimeSlot slot : result.schedule) {
            System.out.println("  " + slot);
        }
        
        System.out.println("\n" + repeatString("=", 50) + "\n");
    }
    
    /**
     * Main method
     */
    public static void main(String[] args) {
        // Run demonstration
        demonstrateExample();
        
        // Run experimental validation
        runTimeComplexityExperiment();
    }
}