import java.util.*;

public class HotspotDetector {
    private final double[][] grid;
    private final int n;
    private final double threshold;
    private long comparisonCount = 0;

    public HotspotDetector(double[][] grid, double threshold) {
        this.grid = grid;
        this.n = grid.length;
        this.threshold = threshold;
    }

    public List<PollutionData> detectHotspots(int k) {
        comparisonCount = 0;
        Region wholeRegion = new Region(0, 0, n - 1, n - 1);
        List<PollutionData> allHotspots = divideAndConquer(wholeRegion);
        Collections.sort(allHotspots);
        return allHotspots.subList(0, Math.min(k, allHotspots.size()));
    }

    private List<PollutionData> divideAndConquer(Region region) {
        if (region.getArea() <= 4) {
            return findHotspotsDirectly(region);
        }

        int midX = (region.getStartX() + region.getEndX()) / 2;
        int midY = (region.getStartY() + region.getEndY()) / 2;

        Region topLeft = new Region(region.getStartX(), region.getStartY(), midX, midY);
        Region topRight = new Region(midX + 1, region.getStartY(), region.getEndX(), midY);
        Region bottomLeft = new Region(region.getStartX(), midY + 1, midX, region.getEndY());
        Region bottomRight = new Region(midX + 1, midY + 1, region.getEndX(), region.getEndY());

        List<PollutionData> hotspotsNW = divideAndConquer(topLeft);
        List<PollutionData> hotspotsNE = divideAndConquer(topRight);
        List<PollutionData> hotspotsSW = divideAndConquer(bottomLeft);
        List<PollutionData> hotspotsSE = divideAndConquer(bottomRight);

        List<PollutionData> merged = new ArrayList<>();
        merged.addAll(hotspotsNW);
        merged.addAll(hotspotsNE);
        merged.addAll(hotspotsSW);
        merged.addAll(hotspotsSE);

        checkBoundary(region, midX, midY, merged);
        return filterAndDeduplicate(merged);
    }

    private List<PollutionData> findHotspotsDirectly(Region region) {
        List<PollutionData> hotspots = new ArrayList<>();
        
        for (int i = region.getStartX(); i <= region.getEndX(); i++) {
            for (int j = region.getStartY(); j <= region.getEndY(); j++) {
                comparisonCount++;
                if (grid[i][j] >= threshold) {
                    if (isLocalMaximum(i, j)) {
                        hotspots.add(new PollutionData(i, j, grid[i][j]));
                    }
                }
            }
        }
        
        return hotspots;
    }

    private boolean isLocalMaximum(int x, int y) {
        double value = grid[x][y];
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                int nx = x + dx;
                int ny = y + dy;
                
                if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                    comparisonCount++;
                    if (grid[nx][ny] > value) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }

    private void checkBoundary(Region region, int midX, int midY, List<PollutionData> merged) {
        for (int i = region.getStartX(); i <= region.getEndX(); i++) {
            if (midY >= region.getStartY() && midY <= region.getEndY()) {
                comparisonCount++;
                if (grid[i][midY] >= threshold && isLocalMaximum(i, midY)) {
                    merged.add(new PollutionData(i, midY, grid[i][midY]));
                }
            }
        }
        
        for (int j = region.getStartY(); j <= region.getEndY(); j++) {
            if (j != midY && midX >= region.getStartX() && midX <= region.getEndX()) {
                comparisonCount++;
                if (grid[midX][j] >= threshold && isLocalMaximum(midX, j)) {
                    merged.add(new PollutionData(midX, j, grid[midX][j]));
                }
            }
        }
    }

    private List<PollutionData> filterAndDeduplicate(List<PollutionData> hotspots) {
        Set<PollutionData> uniqueHotspots = new HashSet<>(hotspots);
        List<PollutionData> result = new ArrayList<>(uniqueHotspots);
        result.removeIf(h -> h.getIntensity() < threshold);
        return result;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }
}
