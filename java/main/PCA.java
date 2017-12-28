package main;

public class PCA {

    private final double[][] A;

    private final double []m;

    public PCA(double[][] a, double[] m) {
        A = a;
        this.m = m;
    }

    public double[] transform (double[] x)
    {
        return Matrix.multiply(Vector.sub(x, m), A);
    }


}
