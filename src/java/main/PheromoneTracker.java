package main;

public class PheromoneTracker {

    private static final int MAX_PHEROMONES = 100000;
    private static final long MAX_DECAY_TIME =  300 * 100L;
    private static final double MAX_DISTANCE = 1000;

    private double maxLapDistance = 0;

    private final Pheromone[] pheromones;

    private int begin, size;

    private Parameters parameters;


    public PheromoneTracker(Parameters parameters) {
        this.parameters = parameters;
        pheromones = new Pheromone[MAX_PHEROMONES];
        begin = 0;
        size = 0;
    }

    boolean addToEnd (Pheromone pheromone) {
        if (size < MAX_PHEROMONES) {
            int end = (begin + size) % MAX_PHEROMONES;
            pheromones[end] = pheromone;
            maxLapDistance = Math.max(pheromone.getDistance(), maxLapDistance);
            size++;
            return true;
        } else {
            return false;
        }
    }

    boolean popFirst() {
        if (size > 0) {
            pheromones[begin] = null;
            begin++;
            size--;
            return true;
        } else {
            return false;
        }
    }

    double gaussian(double x) {
        return Math.exp(-x*x);
    }

    double computeWeight(double deltaDistance) {
        double d = parameters.get(ParamType.PHEROMONE_LOOK_AHEAD_DISTANCE);
        double f = parameters.get(ParamType.PHEROMONE_GAUSSIAN_SIGMA);
        return gaussian(  f*(deltaDistance - d) / d );
        //return gaussian(  1.3*(deltaDistance - d) / d );
    }

    public int getSize() {
        return size;
    }

    double getIntensity(double distanceRaced, long time) {
        double aggregated = 0;
        double weights = 0;
        int numEffective = 0;
        for (int i = begin; i < begin + size; i++) {
            int p = i % MAX_PHEROMONES;

            double deltaDistance = Math.min(
                    maxLapDistance + distanceRaced - pheromones[p].getDistance(),
                    pheromones[p].getDistance() - distanceRaced);


            long deltaTime = time - pheromones[p].getTime();
            double intensity = pheromones[p].getIntensity();
            double weight = computeWeight(deltaDistance);
            weights += weight;
            if (deltaTime > MAX_DECAY_TIME) {
                popFirst();
            } else {
                aggregated += weight * intensity;
            }
        }
        if (size > 0) {
            return aggregated / weights;
        } else {
            return 0;
        }
    }

    public static void main (String[] args) {

    }


}
