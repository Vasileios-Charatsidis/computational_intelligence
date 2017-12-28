package main;

enum Activation {
    identity,
    logistic,
    tanh,
    relu;

    static double logistic(double x) {
        return 1. / (1. + Math.exp(-x));
    }

    static double relu(double x) {
        if (x >= 0) {
            return x;
        } else {
            return 0;
        }
    }
}


class MLPLayer {

    private final Activation activation;

    private final double[][] C;

    private final double[] b;

    public MLPLayer(Activation activation, double[][] c, double[] b) {
        C = c;
        this.b = b;
        this.activation = activation;
    }

    private double[] activate(double[] x) {
        double[] z = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            z[i] = activate(x[i]);
        }
        return z;
    }

    private double activate(double x) {
        switch (activation) {
            case identity:
                return x;
            case logistic:
                return Activation.logistic(x);
            case tanh:
                return Math.tanh(x);
            case relu:
                return Activation.relu(x);
            default:
                throw new IllegalArgumentException("Unknown activation " + activation.name());
        }
    }

    public double[] feedforward(double[] x) {
        return activate(Vector.add(Matrix.multiply(C, x), b));
    }

}

public class MLP {

    private final MLPLayer[] layers;

    public MLP(MLPLayer[] layers) {
        this.layers = layers;
    }

    public double[] feedforward(double[] x) {
        double[] z = x;
        for (MLPLayer layer : layers) {
            z = layer.feedforward(z);
        }
        return z;
    }

}
