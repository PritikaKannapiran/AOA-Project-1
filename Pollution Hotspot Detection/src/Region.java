public class Region {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;

    public Region(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public int getWidth() {
        return endX - startX + 1;
    }

    public int getHeight() {
        return endY - startY + 1;
    }

    public int getArea() {
        return getWidth() * getHeight();
    }

    @Override
    public String toString() {
        return String.format("Region[(%d,%d) to (%d,%d)]", startX, startY, endX, endY);
    }
}
