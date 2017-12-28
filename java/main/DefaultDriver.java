package main;

import cicontest.algorithm.abstracts.AbstractDriver;
import cicontest.torcs.controller.extras.ABS;
import cicontest.torcs.controller.extras.AutomatedClutch;
import cicontest.torcs.controller.extras.AutomatedGearbox;
import cicontest.torcs.controller.extras.AutomatedRecovering;
import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;


public class DefaultDriver extends AbstractDriver {

    private static String PATH = "/Users/ci_course/torcs/shared_folder/WJ7wh033pQZY";

    ////////////////////////////////////////

    private MLP cobosteer_mlp;

    private MLP inferno_ab_mlp;
    private PCA inferno_ab_pca;

    private static int COUNTER = 0;


    ////////////////////////////////////////

    private final double STEER_LOCK = .785398;

    private final PheromoneTracker pheromoneTracker;

    private double lastSpeed;
    private long pheromoneTickCounter;

    private String identifier;

    // NOT PART OF PARAMS
    private int[] gearUp = new int[]{9000, 9000, 9000, 8500, 8500, 8300};
    private int[] gearDown = new int[]{0, 2000, 3000, 3500, 4000, 3500};
    final float[] wheelRadius = new float[]{0.3179F, 0.3179F, 0.3276F, 0.3276F};

    private double prev_steer = 0;

    private int recoveryCounter = 0; // time spend in recovery mode
    private double scrapeCounter = 0;  // time spent scraping against wall
    private int reverseCounter = 0; // time spent reversing during recovery
    private double stuckAngle = 0.;

    private final static int MODE_NORMAL = 1;
    private final static int MODE_REVERSE = -1;
    private final static int MODE_BRAKE = 0;

    private int mode = MODE_NORMAL;

    private long currentTicks;

    private SensorModel lastSensorReadings;

    private int accumulatedLapPosition = 0;
    private Parameters parameters;

    private DefaultDriverGenome genome;

    public DefaultDriver() {

        this.setStage(Stage.RACE);

        this.parameters = loadParameters();

        this.pheromoneTracker = new PheromoneTracker(parameters);


        COUNTER += 1;

        identifier = String.valueOf(COUNTER);

        initialize();

        this.pheromoneTickCounter = 0;

        this.lastSpeed = 0;

        this.currentTicks = 0;


        ////////////////////////////
        // NEURAL NETWORKS
        ////////////////////////////

        ClassLoader classLoader = this.getClass().getClassLoader();

        this.cobosteer_mlp = ParamLoader.loadMLP(classLoader.getResourceAsStream("cobosteer_mlp.params"));

        this.inferno_ab_pca = ParamLoader.loadPCA(classLoader.getResourceAsStream("inferno_ab_pca.params"));
        this.inferno_ab_mlp = ParamLoader.loadMLP(classLoader.getResourceAsStream("inferno_ab_mlp.params"));

    }

    @Override
    public float[] initAngles() {
        float[] angles = new float[19];
        for (int i = 0; i < 19; ++i) {
            angles[i] = (float) (-90 + i * 10);
        }
        return angles;
    }

    private void initialize() {
        Path file = Paths.get(PATH + "/" + this.identifier);
        try {
            Files.write(file, Collections.singletonList(""), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.enableExtras(new ABS());
    }

    public int getGear(SensorModel sensors) {
        // recovery gear system
        if (mode == MODE_REVERSE) {
            return MODE_REVERSE;
        } else if (mode == MODE_BRAKE) {
            return MODE_BRAKE;
        }

        // normal gear system
        int gear = sensors.getGear();
        double rpm = sensors.getRPM();
        int nextgear = gear;
        if (gear < 1) {
            nextgear = 1;
        } else if (gear < 6 && rpm >= (double) this.gearUp[gear - 1]) {
            nextgear = gear + 1;
        } else {
            if (gear > 1 && rpm <= (double) this.gearDown[gear - 1]) {
                nextgear = gear - 1;
            }
        }
        return nextgear;
    }

    public double getClutch(SensorModel sensors) {
        double clutch = 0.0D;
        float maxClutch = 0.5F;
        if (sensors.getDistanceRaced() < 10.0D) {
            clutch = (double) maxClutch;
        }

        if (clutch > 0.0D) {
            double delta = 0.05000000074505806D;
            if (sensors.getGear() < 2) {
                delta /= 2.0D;
                maxClutch *= 1.3F;
                if (sensors.getCurrentLapTime() < 1.5D) {
                    clutch = (double) maxClutch;
                }
            }

            clutch = Math.min((double) maxClutch, clutch);
            if (clutch != (double) maxClutch) {
                clutch -= delta;
                clutch = Math.max(0.0D, clutch);
            } else {
                clutch -= 0.009999999776482582D;
            }
        }
        return clutch;
    }

    @Override
    public void loadGenome(IGenome genome) {
        if (genome instanceof DefaultDriverGenome) {
            this.genome = (DefaultDriverGenome) genome;
        } else {
            System.err.println("Invalid Genome assigned");
        }
    }


    @Override
    public double getAcceleration(SensorModel sensors) {
        return 0;
    }

    public double getBrake(SensorModel sensors) {
        return 0;
    }

    @Override
    public double getSteering(SensorModel sensors) {
        return 0;
    }

    @Override
    public String getDriverName() {
        return "sociopathfinders " + System.nanoTime();
    }

    @Override
    public Action controlWarmUp(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlQualification(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    @Override
    public Action controlRace(SensorModel sensors) {
        Action action = new Action();
        return defaultControl(action, sensors);
    }

    // Target Velocity Eq. 2 COBOSTAR paper
    private double getTargetVelocity(double maxDistance, double alpha) {
        double theta1 = parameters.get(ParamType.COBOSTAR_THETA_1);
        double theta2 = parameters.get(ParamType.COBOSTAR_THETA_2);
        double p1 = parameters.get(ParamType.COBOSTAR_P_1);
        double p2 = parameters.get(ParamType.COBOSTAR_P_2);
        double p3 = parameters.get(ParamType.COBOSTAR_P_3);
        double p4 = parameters.get(ParamType.COBOSTAR_P_4);
        double p5 = parameters.get(ParamType.COBOSTAR_P_5);
        double p6 = parameters.get(ParamType.COBOSTAR_P_6);
        double p7 = parameters.get(ParamType.COBOSTAR_P_7);

        double vt;
        if (maxDistance < theta2) {
            vt = p1 + (p2 * maxDistance) + p3 * Math.pow(Math.max(0, (maxDistance - theta1) / (theta2 - theta1)), p4) - p5 * Math.pow(Math.abs((alpha - 9d) / 9d), p6);
        } else {
            vt = 1000d;
        }
        return Math.max(vt, p7);

    }

    // Braking Scheme from Cobostar
    private double[] getAccelAndBrake(double targetVelocity, SensorModel sensorModel) {

        double p8 = parameters.get(ParamType.COBOSTAR_P_8);
        double p9 = parameters.get(ParamType.COBOSTAR_P_9);

        double accel, brake;
        if (sensorModel.getSpeed() < targetVelocity) {  // if less, accelerate
            accel = 1.;
            brake = 0.;
        } else if (targetVelocity < sensorModel.getSpeed() && sensorModel.getSpeed() < p8 * targetVelocity) { // if within a range of target velocity
            accel = 0.;
            brake = 0.;
        } else {  // going to fast, brake a bit
            accel = 0.;
            brake = Math.min(1., p9 * (sensorModel.getSpeed() - p8 * targetVelocity));
        }
        double[] results = {accel, brake};
        return results;
    }

    private void log(String message) {
//        System.out.println(message);
    }

    // If going slow / not moving, increment recovery counter
    private void f0(SensorModel sensorModel) {
        if (Math.abs(sensorModel.getSpeed()) > parameters.get(ParamType.RECOVERY_Q_9)) {
            recoveryCounter = 0;
        } else {
            recoveryCounter++;
        }
        f1(sensorModel);
    }

    // if at the beginning of track, not going too fast, headed forward, then prepare to reverse
    private void f1(SensorModel sensorModel) {

        if (mode == MODE_NORMAL && recoveryCounter > parameters.get(ParamType.RECOVERY_MAX_RECOVERY_COUNTER)
                && sensorModel.getDistanceRaced() > 50
                && sensorModel.getRPM() < 1000) {

            stuckAngle = sensorModel.getAngleToTrackAxis();
            recoveryCounter = 0;
            mode = MODE_REVERSE;
            scrapeCounter = parameters.get(ParamType.RECOVERY_MAX_SCRAPE_TIMEOUT);
            reverseCounter = 0;
        }
        f4(sensorModel);
    }

    private void ftemp(SensorModel sensorModel) {
        if (mode == MODE_REVERSE) {
            reverseCounter++;
            if (reverseCounter > parameters.get(ParamType.RECOVERY_MAX_REVERSE_COUNTER)) { // but if reversing for too long, go forward
                reverseCounter = 0;
                mode = MODE_NORMAL;
            }
        }

        f3(sensorModel);
    }

    // if at the beginning of track, not going too fast, headed forward, then prepare to reverse


    // Scraping logic
    private void f3(SensorModel sensorModel) {

        if (sensorModel.getSpeed() > 50) {
            scrapeCounter = 0;
        } else if (scrapeCounter >= 0) {

            // if velocity is not very low, and angle to track is within an acceptable range of 22.5 degrees and facing towards the wall
            // i.e. if still scraping, add to scrape counter
            if (sensorModel.getSpeed() > 5 &&
                    Math.abs(sensorModel.getAngleToTrackAxis()) > Math.PI / 8
                    && Math.abs(sensorModel.getTrackPosition()) > 1 && sensorModel.getAngleToTrackAxis() * sensorModel.getTrackPosition() < 0) {
                scrapeCounter += 5 - Math.floor(Math.abs(sensorModel.getSpeed() / 10));
            } else { // no longer scraping
                scrapeCounter = 0;
            }
        } else { // not in SCRAPE_TIMEOUT
            scrapeCounter++;
        }

        f7(sensorModel);
    }

    // If reversing, and car is in right angle, then brake (in preparation for going forward later)
    private void f4(SensorModel sensorModel) {
        if (mode == MODE_REVERSE && Math.abs(sensorModel.getAngleToTrackAxis() / stuckAngle) < 0.5) {
            mode = MODE_BRAKE;
        }

        f5(sensorModel);
    }

    // Reverse Recovery timeout: start going forward (may be reversing/stuck into wall)
    private void f5(SensorModel sensorModel) {

        if (mode == MODE_REVERSE
                && recoveryCounter > parameters.get(ParamType.RECOVERY_MAX_RECOVERY_COUNTER)) {
            recoveryCounter = 0;
            mode = MODE_NORMAL;
        }

        f6(sensorModel);
    }

    // if braking, and slow enough already, start going forward
    private void f6(SensorModel sensorModel) {

        if (mode == MODE_BRAKE
                && (Math.abs(sensorModel.getSpeed()) < parameters.get(ParamType.RECOVERY_Q_9)
                || sensorModel.getSpeed() < 0)) {
            mode = MODE_NORMAL;
        }

        ftemp(sensorModel);
    }

    // If driving forward but scraping wall for a long time, then reverse
    private void f7(SensorModel sensorModel) {

        if (mode == MODE_NORMAL
                && scrapeCounter > parameters.get(ParamType.RECOVERY_MAX_SCRAPE_COUNTER)
                && sensorModel.getDistanceRaced() > 150) {
            stuckAngle = sensorModel.getAngleToTrackAxis();
            mode = MODE_REVERSE;
            scrapeCounter = parameters.get(ParamType.RECOVERY_MAX_SCRAPE_TIMEOUT);
        }

        f8(sensorModel);
    }

    // if reversing and back on track, great, then brake (preparation for going forward)
    private void f8(SensorModel sensorModel) {

        if (mode == MODE_REVERSE && Math.abs(sensorModel.getTrackPosition()) < 0.5) {
            mode = MODE_BRAKE;
        }
    }

    // If recovery mode is needed, return controls. Else, return null
    private double[] recoveryControls(SensorModel sensorModel) {
        f0(sensorModel); // waterfalls through all recovery logics

        double q1 = parameters.get(ParamType.RECOVERY_Q_1);
        double q2 = parameters.get(ParamType.RECOVERY_Q_2);
        double q3 = parameters.get(ParamType.RECOVERY_Q_3);
        double q4 = parameters.get(ParamType.RECOVERY_Q_4);
        double q5 = parameters.get(ParamType.RECOVERY_Q_5);

        // calculate recovery params if needed
        if (Math.abs(sensorModel.getTrackPosition()) > 1) { // off track
            double beta = Math.signum(sensorModel.getTrackPosition()) * (Math.abs(sensorModel.getTrackPosition()) - q1) * q2; //Eq.5 COBOSTAR paper
            double targetVelocity = q3 + q4 * Math.max(0, 1 - q5 * Math.abs(sensorModel.getAngleToTrackAxis() - beta)); // Eq 6. COBOSTAR paper

            double[] accelbrake = getAccelAndBrake(targetVelocity, sensorModel);

            double steer = sensorModel.getAngleToTrackAxis() - beta;
            double[] results = {steer, accelbrake[0], accelbrake[1]};
            return results;
        }
        return null;
    }

    // Params for curvature and steering
    public double[] getAlpha(SensorModel sensors) {

        double maxDistance = 0.1;
        double angle = sensors.getAngleToTrackAxis();

        // Find sensors pointing in direction of track
        int minSensor = (int) Math.max(0, Math.ceil(-angle / Math.PI * 18));
        int maxSensor = (int) Math.min(18, Math.ceil((1 - angle / Math.PI) * 18));

        int maxIndex = Math.max(minSensor, 0);

        // Find longest track sensor
        for (int i = minSensor; i <= maxSensor; i++) {
            double distance = sensors.getTrackEdgeSensors()[i];
            if (maxDistance < distance) {
                maxIndex = i;
                maxDistance = distance;
            }
        }

        if (sensors.getOpponentSensors()[maxIndex] < parameters.get(ParamType.OVERTAKING_DISTANCE_THRESHOLD)) {
            if (sensors.getOpponentSensors()[maxIndex + 1] > sensors.getOpponentSensors()[maxIndex - 1]) {
                maxIndex++;
            } else {
                maxIndex--;
            }
        }

        maxIndex = Math.min(17, Math.max(1, maxIndex));

//        Find the sensor on right and left of max
        double distanceLeft, distanceRight;
        distanceLeft = sensors.getTrackEdgeSensors()[maxIndex - 1];
        distanceRight = sensors.getTrackEdgeSensors()[maxIndex + 1];
        maxDistance = sensors.getTrackEdgeSensors()[maxIndex];

        double diffLeft = (maxDistance - distanceLeft);
        double diffRight = (maxDistance - distanceRight);

        // Eq.1 COBOSTAR paper
        double alpha = maxIndex - 0.5 + diffLeft / (diffLeft + diffRight + 1e-6);
        double[] results = {alpha, maxIndex, diffLeft / (diffLeft + diffRight + 1e-6), maxDistance};
        return results;
    }


    @Override
    public Action defaultControl(Action action, SensorModel sensors) {

        lastSensorReadings = sensors;

        try {
            if (action == null) {
                action = new Action();
            }
            double[] recoveryControls = recoveryControls(sensors); // steer, accel, brake
            if (recoveryControls != null) {
                // log("Recovery Mode");
                action.steering = recoveryControls[0];
                action.accelerate = recoveryControls[1];
                action.brake = recoveryControls[2];
            } else {

                //log("Normal Mode");
                loadPheromones().forEach(pheromoneTracker::addToEnd);

                double[] alpha_data = getAlpha(sensors);
                double alpha = alpha_data[0];
                double maxIndex = alpha_data[1];
                double curvature = alpha_data[2];
                double maxDistance = alpha_data[3];

                double distance = sensors.getDistanceFromStartLine();

                currentTicks++;

                double[] v = getState(sensors);

                int[] ab_in = {0, 45, 46, 47, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43};
                double[] ab_data = new double[ab_in.length];
                for (int i = 0; i < ab_in.length; i++) {
                    ab_data[i] = v[ab_in[i]];
                }

                double[] ab_pred = this.inferno_ab_mlp.feedforward(this.inferno_ab_pca.transform(ab_data));

                double[] state = {maxIndex, sensors.getSpeed(), sensors.getLateralSpeed(), curvature};
                double[] cobosteer_pred = this.cobosteer_mlp.feedforward(state);

                double cobostarSteer = parameters.get(ParamType.COBOSTAR_P_10) * (9d - alpha);
                double mlpSteer = cobosteer_pred[0];

                action.steering = cobostarSteer * parameters.get(ParamType.STEER_MIXTURE_COBOSTAR)
                        + mlpSteer * parameters.get(ParamType.STEER_MIXTURE_MLP);

                sensors.getSpeed();

                double pheromoneLevel = pheromoneTracker.getIntensity(sensors.getDistanceFromStartLine(), currentTicks);

                double maxSpeed1 = sensors.getTrackEdgeSensors()[8] * parameters.get(ParamType.DISTANCE_AHEAD_TARGET_SPEED_TRACK_8_INFLUENCE)
                        + sensors.getTrackEdgeSensors()[9] * parameters.get(ParamType.DISTANCE_AHEAD_TARGET_SPEED_TRACK_9_INFLUENCE)
                        + sensors.getTrackEdgeSensors()[10] * parameters.get(ParamType.DISTANCE_AHEAD_TARGET_SPEED_TRACK_10_INFLUENCE);

                double maxSpeed2 = getTargetVelocity(maxDistance, alpha);

                double maxSpeed = Math.max(
                        60,
                        parameters.get(ParamType.PHEROMONE_SPEED_OFFSET)
                                + (1 - (Math.abs(pheromoneLevel))) * parameters.get(ParamType.PHEROMONE_SPEED_MULTIPLIER));


                if (sensors.getLaps() > 0) {
                    double a1 = parameters.get(ParamType.SPEED_MIXTURE_PHEROMONE);
                    double a2 = parameters.get(ParamType.SPEED_MIXTURE_DISTANCE_AHEAD);
                    double a3 = parameters.get(ParamType.SPEED_MIXTURE_COBOSTAR);
                    double inv1 = a1 / maxSpeed;
                    double inv2 = a2 / maxSpeed1;
                    double inv3 = a3 / maxSpeed2;
                    maxSpeed = (a1 + a2 + a3) / (inv1 + inv2 + inv3);
                } else {
                    double a1 = parameters.get(ParamType.SPEED_MIXTURE_DISTANCE_AHEAD);
                    double a2 = parameters.get(ParamType.SPEED_MIXTURE_COBOSTAR);
                    double inv1 = a1 / maxSpeed1;
                    double inv2 = a2 / maxSpeed2;
                    maxSpeed = (a1 + a2) / (inv1 + inv2);
                }


                double[] accelbrake = getAccelAndBrake(maxSpeed, sensors);

                if (sensors.getSpeed() < maxSpeed) {
                    double a1 = 1 * parameters.get(ParamType.ACCELERATION_DEFAULT_INFLUENCE);
                    double a2 = ab_pred[0] * parameters.get(ParamType.ACCELERATION_MLP_INFLUENCE);
                    double a3 = accelbrake[0] * parameters.get(ParamType.ACCELERATION_COBOSTAR_INFLUENCE);
                    action.accelerate = a1 + a2 + a3;
                    action.brake = 0;
                } else if (sensors.getSpeed() > maxSpeed + 20) {
                    double b1 = 0.4 * parameters.get(ParamType.BRAKE_DEFAULT_INFLUENCE);
                    double b2 = ab_pred[1] * parameters.get(ParamType.BRAKE_MLP_INFLUENCE);
                    double b3 = accelbrake[1] * parameters.get(ParamType.BRAKE_COBOSTAR_INFLUENCE);
                    action.brake = b1 + b2 + b3;
                    action.accelerate = 0;
                }

                double deltaSpeed = lastSpeed - sensors.getSpeed();
                lastSpeed = sensors.getSpeed();

                if (deltaSpeed > parameters.get(ParamType.PHEROMONE_DELTA_SPEED_THRESHOLD)) {
                    writePheromone(
                            new Pheromone(
                                    currentTicks,
                                    distance,
                                    deltaSpeed * parameters.get(ParamType.PHEROMONE_DELTA_SPEED_INTENSITY)));

                }

                if (sensors.getTrackEdgeSensors()[9] < parameters.get(ParamType.PHEROMONE_MIN_FRONT_SENSOR_THRESHOLD)) {
                    double f = (200 - sensors.getTrackEdgeSensors()[9]) / 200;
                    writePheromone(
                            new Pheromone(
                                    currentTicks,
                                    distance,
                                    f * parameters.get(ParamType.PHEROMONE_MIN_FRONT_SENSOR_INTENSITY)));

                }

                pheromoneTickCounter += 1;
                if (pheromoneTickCounter % 20 == 0) {
                    writePheromone(new Pheromone(currentTicks, distance,
                            parameters.get(ParamType.PHEROMONE_STEERING_INTENSITY) * action.steering * action.steering));
                }

                boolean isOutside = sensors.getTrackEdgeSensors()[9] <= 2;
                if (isOutside) {
                    action.accelerate = 0.5;
                    action.brake = 0;
                    action.steering = (sensors.getAngleToTrackAxis() - 0.5 * sensors.getTrackPosition()) / STEER_LOCK;
                }
            }


            if (mode == MODE_REVERSE) {
                action.steering = -action.steering;
                action.accelerate = 0.7;
                action.brake = 0.;
            } else if (mode == MODE_BRAKE) {
                action.steering = -action.steering;
            }

            if (mode != MODE_REVERSE && prev_steer * action.steering < 0 && sensors.getSpeed() < 0 && Math.abs(prev_steer - action.steering) > 4) {
                action.steering = Math.signum(prev_steer) * Math.abs(action.steering);
            }

            prev_steer = action.steering;


            action.gear = getGear(sensors);
            action.clutch = getClutch(sensors);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return action;

    }

    public void writePheromone(Pheromone pheromone) {

        pheromoneTracker.addToEnd(pheromone);

        File folder = new File(PATH);
        File[] listOfFiles = folder.listFiles();

        Path file = Paths.get(PATH + "/" + this.identifier);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (!file.toString().equals(listOfFiles[i])) {
                    PrintWriter output = null;
                    try {
                        output = new PrintWriter(new FileWriter(listOfFiles[i], true));
                        output.println(pheromone.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (output != null) {
                            output.close();
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Pheromone> loadPheromones() {
        Path file = Paths.get(PATH + "/" + this.identifier);

        ArrayList<Pheromone> result = new ArrayList<>();
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(file.toFile()));
            bf.lines().forEach(line -> {
                if (line.length() > 0) {
                    result.add(Pheromone.fromString(line));
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter(file.toFile()));
            output.println("");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.close();
            }
        }

        return result;
    }

    public Parameters loadParameters() {
        ClassLoader classLoader = getClass().getClassLoader();

        InputStream is = classLoader.getResourceAsStream("parameters.txt");
        try {
            is = new FileInputStream("/home/dana/Desktop/computational_intelligence/sociopathsubmission/torcs/python/parameters.txt");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        ArrayList<Parameters> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(Parameters.fromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.get(0);
    }

    @Override
    public void shutdown() {

        System.out.println("LapPosition " + accumulatedLapPosition);

        Path file = Paths.get(PATH + "/" + this.identifier);
        try {
            Files.delete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        super.shutdown();
    }

    private double[] getState(SensorModel sensors) {
        double[] v = new double[79];

        v[0] = sensors.getAngleToTrackAxis();
        v[1] = sensors.getCurrentLapTime();
        v[2] = sensors.getDamage();
        v[3] = sensors.getDistanceFromStartLine();
        v[4] = sensors.getDistanceRaced();
        v[5] = sensors.getFuelLevel();
        v[6] = sensors.getGear();
        v[7] = sensors.getLastLapTime();

        //opponents
        for (int i = 8; i < sensors.getOpponentSensors().length; i++) {
            v[i] = sensors.getOpponentSensors()[i - 8];
        }

        v[44] = sensors.getRacePosition();
        v[45] = sensors.getRPM();
        v[46] = sensors.getSpeed();
        v[47] = sensors.getLateralSpeed();
        v[48] = sensors.getZSpeed();

        //track
        for (int i = 49; i < sensors.getTrackEdgeSensors().length; i++) {
            v[i] = sensors.getTrackEdgeSensors()[i - 49];
        }

        v[68] = sensors.getTrackPosition();

        //wheels

        for (int i = 69; i < sensors.getWheelSpinVelocity().length; i++) {
            v[i] = sensors.getWheelSpinVelocity()[i - 69];
        }

        v[73] = sensors.getZ();

        // focus
        for (int i = 74; i < sensors.getFocusSensors().length; i++) {
            v[i] = sensors.getFocusSensors()[i - 74];
        }

        return v;
    }

    @Override
    public void reset() {
        this.parameters = loadParameters();
        accumulatedLapPosition += lastSensorReadings.getRacePosition();

    }


}