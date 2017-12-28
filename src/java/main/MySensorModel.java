package main;

import java.util.Arrays;
import scr.SensorModel;

public class MySensorModel {
    // basic information about your car and the track (you probably should take care of these somehow)
    private static double speed = 0, trackPosition = 0, angleToTrackAxis = 0, lateralSpeed = 0, currentLapTime = 0, damage = 0,
            distanceFromStartLine = 0, distanceRaced = 0, fuelLevel = 0, lastLapTime = 0, RPM = 0, ZSpeed = 0, Z = 0;
    private static double[] trackEdgeSensors;
    private static double[] focusSensors;
    private static double[] opponentSensors;
    private static int gear = 0, racePosition = 0;
    private static double[] wheelSpinVelocity;
    private static String message = "";
    private static int idx = 0;


    public MySensorModel() {}

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        MySensorModel.speed = speed;
    }

    public double getAngleToTrackAxis() {
        return angleToTrackAxis;
    }

    public void setAngleToTrackAxis(double angleToTrackAxis) {
        MySensorModel.angleToTrackAxis = angleToTrackAxis;
    }

    public double[] getTrackEdgeSensors() {
        return trackEdgeSensors;
    }

    public void setTrackEdgeSensors(double[] trackEdgeSensors) {
        MySensorModel.trackEdgeSensors = trackEdgeSensors;
    }

    public double[] getFocusSensors() {
        return focusSensors;
    }

    public void setFocusSensors(double[] focusSensors) {
        MySensorModel.focusSensors = focusSensors;
    }


    public double getTrackPosition() {
        return trackPosition;
    }


    public void setTrackPosition(double trackPosition) {
        MySensorModel.trackPosition = trackPosition;
    }


    public int getGear() {
        return gear;
    }


    public void setGear(int gear) {
        MySensorModel.gear = gear;
    }


    public double[] getOpponentSensors() {
        return opponentSensors;
    }


    public void setOpponentSensors(double[] opponentSensors) {
        MySensorModel.opponentSensors = opponentSensors;
    }


    public int getRacePosition() {
        return racePosition;
    }


    public void setRacePosition(int racePosition) {
        MySensorModel.racePosition = racePosition;
    }


    public double[] getWheelSpinVelocity() {
        return wheelSpinVelocity;
    }


    public void setWheelSpinVelocity(double[] wheelSpinVelocity) {
        MySensorModel.wheelSpinVelocity = wheelSpinVelocity;
    }

    public double getDistanceFromStartLine() {
        return distanceFromStartLine;
    }


    public void setDistanceFromStartLine(double distanceFromStartLine) {
        MySensorModel.distanceFromStartLine = distanceFromStartLine;
    }


    public double getDistanceRaced() {
        return distanceRaced;
    }


    public void setDistanceRaced(double distanceRaced) {
        MySensorModel.distanceRaced = distanceRaced;
    }


    public double getLateralSpeed() {
        return lateralSpeed;
    }


    public void setLateralSpeed(double lateralSpeed) {
        MySensorModel.lateralSpeed = lateralSpeed;
    }

    public double getCurrentLapTime() {
        return currentLapTime;
    }

    public void setCurrentLapTime(double currentLapTime) {
        MySensorModel.currentLapTime = currentLapTime;
    }


    public double getDamage() {
        return damage;
    }


    public void setDamage(double damage) {
        MySensorModel.damage = damage;
    }


    public double getFuelLevel() {
        return fuelLevel;
    }


    public void setFuelLevel(double fuelLevel) {
        MySensorModel.fuelLevel = fuelLevel;
    }


    public double getLastLapTime() {
        return lastLapTime;
    }


    public void setLastLapTime(double lastLapTime) {
        MySensorModel.lastLapTime = lastLapTime;
    }


    public double getRPM() {
        return RPM;
    }


    public void setRPM(double rPM) {
        RPM = rPM;
    }


    public double getZSpeed() {
        return ZSpeed;
    }


    public void setZSpeed(double zSpeed) {
        ZSpeed = zSpeed;
    }


    public double getZ() {
        return Z;
    }


    public void setZ(double z) {
        Z = z;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        MySensorModel.message = message;
    }

    private static void append(StringBuilder vector, String label, double val) {
        vector.append(" (" + label + " " + val + ")");
    }

    private static void append(StringBuilder vector, String label, double[] val) {
        vector.append(" (" + label + " " + Arrays.toString(val).replace("[", "").replace("[", "]") + ")");
    }

    public static String getVector(SensorModel s) {
        // (angle 0.00197632)
        // (curLapTime -0.982)
        // (damage 0)
        // (distFromStart 977.936)(
        // distRaced 0)
        // (fuel 2.57504)
        // (gear 0)
        // (lastLapTime 0)
        // (opponents 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 200 30.4477 10.5324 200 200 200 200 200 200 200 200 200 200 200 200 200 20.2925 30.018)
        // (racePos 6)
        // (rpm 942.478)
        // (speedX 0.0482544)
        // (speedY 0.00820909)
        // (speedZ 0.00287051)
        // (track 3.33319 3.38368 3.54037 3.82814 4.29936 5.05945 6.33299 8.65157 13.4031 23.7317 41.9296 66.2904 15.7873 11.0947 8.97687 7.80582 7.13132 6.77648 6.66683)
        // (trackPos 0.333363)
        // (wheelSpinVel 0 0 0 0)
        // (z 0.351084)
        // (focus -1 -1 -1 -1 -1)
        StringBuilder vector = new StringBuilder();
        append(vector, "angle", s.getAngleToTrackAxis());
        append(vector, "curLapTime", s.getCurrentLapTime());
        append(vector, "damage", s.getDamage());
        append(vector, "distFromStart", s.getDistanceFromStartLine());
        append(vector, "distRaced", s.getDistanceRaced());
        append(vector, "fuel", s.getFuelLevel());
        append(vector, "gear", s.getGear());
        append(vector, "lastLapTime", s.getLastLapTime());
        append(vector, "opponents", s.getOpponentSensors());
        append(vector, "racePos", s.getRacePosition());
        append(vector, "rpm", s.getRPM());
        append(vector, "speedX", s.getSpeed()); // speedX
        append(vector, "speedY", s.getLateralSpeed()); //speedY
        append(vector, "speedZ", s.getZSpeed()); // speedZ duh
        append(vector, "track", s.getTrackEdgeSensors());
        append(vector, "trackPos", s.getTrackPosition());
        append(vector, "wheelSpinVel", s.getWheelSpinVelocity());
        append(vector, "z", s.getZ());
        append(vector, "focus", s.getFocusSensors());

        MySensorModel.idx = 0;
        return vector.toString();
    }
}
