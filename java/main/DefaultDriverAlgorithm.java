package main;

import java.io.*;
import java.util.*;
import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm extends AbstractAlgorithm {

    int numDrivers = 1;

    private static final long serialVersionUID = 654963126362653L;

    DefaultDriverGenome[] drivers = new DefaultDriverGenome[numDrivers];
    int[] results = new int[numDrivers];

    public Class<? extends Driver> getDriverClass() {
        return DefaultDriver.class;
    }

    // Select which tracks
    Map<String, String[]> tracks = new HashMap<String, String[]>() {
        {
            this.put("dirt", new String[]{
                    "mixed-3",
                    //"corkscrew",
                    //"alpine-1",
                    //"brondehach",
                    //"wheel-1",
                    //"wheel-2"
            });
            /*this.put("oval", new String[] {
                    "a-speedway",
                    "b-speedway",
                    "e-speedway",
                    "michigan"
            }); */
            /*
            this.put("dirt", new String[] {
                    "dirt-1",
                    "dirt-2",
                    "dirt-3",
                    "mixed-1",
                    "mixed-2"
            }); */
        }
    };

    public void run(boolean continue_from_checkpoint) {

        for (int i=0; i<numDrivers; i++) {
            drivers[i] = new DefaultDriverGenome();
        }

        DefaultRace race = new DefaultRace();
        race.tracktype = "road";
        race.track = "aalborg";
        race.laps = 3;
        race.runRace(drivers, true);
    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File("code/resources/torcs.properties"));
        /*
		 *
		 * Start without arguments to run the algorithm
		 * Start with -continue to continue a previous run
		 * Start with -show to show the best found
		 * Start with -show-race to show a race with 10 copies of the best found
		 * Start with -human to race against the best found
		 *
		 */
        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
        DriversUtils.registerMemory(algorithm.getDriverClass());

        if (args.length > 0 && args[0].equals("-show")) {
            new DefaultRace().showBest();
        } else if (args.length > 0 && args[0].equals("-show-race")) {
            new DefaultRace().showBestRace();
        } else if (args.length > 0 && args[0].equals("-human")) {
            new DefaultRace().raceBest();
        } else if (args.length > 0 && args[0].equals("-continue")) {
            if (DriversUtils.hasCheckpoint()) {
                DriversUtils.loadCheckpoint().run(true);
            } else {
                algorithm.run();
            }
        } else {
            algorithm.run();
        }
    }

}