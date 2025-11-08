public class PollutionData implements Comparable<PollutionData> {
    private final int x;
    private final int y;
    private final double intensity;

    public PollutionData(int x, int y, double intensity) {
        this.x = x;
        this.y = y;
        this.intensity = intensity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public int compareTo(PollutionData other) {
        return Double.compare(other.intensity, this.intensity);
    }

    @Override
    public String toString() {
        return String.format("Position(%d, %d): %.2f", x, y, intensity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PollutionData other = (PollutionData) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
