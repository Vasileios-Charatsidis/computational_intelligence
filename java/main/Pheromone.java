package main;

public class Pheromone {

    private long time;

    private final double distance;

    private final double intensity;

    public Pheromone(long time, double distance, double intensity) {
        this.time = time;
        this.distance = distance;
        this.intensity = intensity;
    }

    public double getDistance() {
        return distance;
    }

    public double getIntensity() {
        return intensity;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString () {
        return String.format("%d,%f,%f", time, distance, intensity);
    }

    static Pheromone fromString(String string) {
        String[] fragments = string.split("\\,");
        long time = Long.parseLong(fragments[0]);
        double distance = Double.parseDouble(fragments[1]);
        double intensity = Double.parseDouble(fragments[2]);
        return new Pheromone(time, distance, intensity);
    }
}
