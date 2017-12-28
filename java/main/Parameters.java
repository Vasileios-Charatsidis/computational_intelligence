package main;

import java.io.IOException;

enum ParamType {

    ///////////////////////////////////////////////////////
    // PHEROMONE
    ///////////////////////////////////////////////////////

    PHEROMONE_LOOK_AHEAD_DISTANCE,                         // 0     50.0
    PHEROMONE_GAUSSIAN_SIGMA,                              // 1     2.0
    PHEROMONE_SPEED_OFFSET,                                // 2     108.0
    PHEROMONE_SPEED_MULTIPLIER,                            // 3     240.0
    PHEROMONE_DELTA_SPEED_THRESHOLD,                       // 4     5.0
    PHEROMONE_DELTA_SPEED_INTENSITY,                       // 5     10.0
    PHEROMONE_MIN_FRONT_SENSOR_THRESHOLD,                  // 6     5.0
    PHEROMONE_MIN_FRONT_SENSOR_INTENSITY,                  // 7     5.0
    PHEROMONE_STEERING_INTENSITY,                          // 8     4.0

    ///////////////////////////////////////////////////////
    // DISTANCE AHEAD
    ///////////////////////////////////////////////////////

    DISTANCE_AHEAD_TARGET_SPEED_TRACK_8_INFLUENCE,         // 9     0.0
    DISTANCE_AHEAD_TARGET_SPEED_TRACK_9_INFLUENCE,         // 10    3.287
    DISTANCE_AHEAD_TARGET_SPEED_TRACK_10_INFLUENCE,        // 11    0.0

    ///////////////////////////////////////////////////////
    // SPEED MIXTURE
    ///////////////////////////////////////////////////////

    SPEED_MIXTURE_DISTANCE_AHEAD,                          // 12    1.0
    SPEED_MIXTURE_COBOSTAR,                                // 13    1.0
    SPEED_MIXTURE_PHEROMONE,                               // 14    1.0

    ///////////////////////////////////////////////////////
    // STEERING
    ///////////////////////////////////////////////////////

    STEER_MIXTURE_COBOSTAR,                               // 15     0.7
    STEER_MIXTURE_MLP,                                    // 16     0.3

    ///////////////////////////////////////////////////////
    // OVERTAKING
    ///////////////////////////////////////////////////////

    OVERTAKING_DISTANCE_THRESHOLD,                        // 17     5.0

    ///////////////////////////////////////////////////////
    // RECOVERY
    ///////////////////////////////////////////////////////

    RECOVERY_Q_1,                                         // 18     0.392
    RECOVERY_Q_2,                                         // 19     0.150
    RECOVERY_Q_3,                                         // 20     117.5
    RECOVERY_Q_4,                                         // 21     123.6
    RECOVERY_Q_5,                                         // 22     34.56
    RECOVERY_Q_9,                                         // 23     2.03

    RECOVERY_MAX_RECOVERY_COUNTER,                        // 24     53.3
    RECOVERY_MAX_SCRAPE_COUNTER,                          // 25     150.0
    RECOVERY_MAX_SCRAPE_TIMEOUT,                          // 26     -500.0
    RECOVERY_MAX_REVERSE_COUNTER,                         // 27     200.0

    ///////////////////////////////////////////////////////
    // COBOSTAR
    ///////////////////////////////////////////////////////

    COBOSTAR_P_1,                                         // 28     43.23
    COBOSTAR_P_2,                                         // 29     1.99
    COBOSTAR_P_3,                                         // 30     104.76
    COBOSTAR_P_4,                                         // 31     9.38
    COBOSTAR_P_5,                                         // 32     907.6
    COBOSTAR_P_6,                                         // 33     1.92
    COBOSTAR_P_7,                                         // 34     11.89
    COBOSTAR_P_8,                                         // 35     1.13
    COBOSTAR_P_9,                                         // 36     0.70
    COBOSTAR_P_10,                                        // 37     0.39

    COBOSTAR_THETA_1,                                     // 38     36.50
    COBOSTAR_THETA_2,                                     // 39     97.33

    ///////////////////////////////////////////////////////
    // ACCELERATION BRAKE
    ///////////////////////////////////////////////////////

    ACCELERATION_MLP_INFLUENCE,                           // 40     0.1
    BRAKE_MLP_INFLUENCE,                                  // 41     0.1
    ACCELERATION_COBOSTAR_INFLUENCE,                      // 42     0.1
    BRAKE_COBOSTAR_INFLUENCE,                             // 43     0.1
    ACCELERATION_DEFAULT_INFLUENCE,                       // 44     0.8
    BRAKE_DEFAULT_INFLUENCE,                              // 45     0.8

}

public class Parameters {

    private final double[] params;

    public Parameters(double[] params) {
        if(params.length != ParamType.values().length) {
            throw new IllegalArgumentException("wrong number of parameters");
        }
        this.params = params;
    }

    public double get(ParamType type) {
        return params[type.ordinal()];
    }

    public static Parameters fromString(String line) {
        String[] data = line.split(",");
        double[] params = new double[ParamType.values().length];
        if (data.length != ParamType.values().length) {
            throw new IllegalArgumentException("wrong number of parameters");
        }
        for (int i=0; i<data.length; i++) {
            try {
                params[i] = Double.parseDouble(data[i]);
            } catch (Exception e) {
                throw new IllegalArgumentException("could not parse data", e);
            }
        }
        return new Parameters(params);
    }
}