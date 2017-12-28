package main;

/**
 * Created by jonas on 07.12.16.
 */
public class Vector {

    public static double[] add(double[] x, double y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] + y;
        }
        return z;
    }

    public static double[] mul(double[] x, double y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] * y;
        }
        return z;
    }

    public static double[] sub(double[] x, double y) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] - y;
        }
        return z;
    }

    public static double[] add(double[] x, double[] y) {
        assert x.length == y.length;
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] + y[i];
        }
        return z;
    }

    public static double[] mul(double[] x, double[] y) {
        assert x.length == y.length;
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] * y[i];
        }
        return z;
    }

    public static double[] sub(double[] x, double[] y) {
        assert x.length == y.length;
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = x[i] - y[i];
        }
        return z;
    }

    public static double dot(double[] x, double[] y) {
        assert x.length == y.length;
        double z = 0;
        for (int i = 0; i < x.length; i++) {
            z = x[i] * y[i];
        }
        return z;
    }
}
