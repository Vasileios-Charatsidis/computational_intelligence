package main;

import java.io.*;
import java.util.ArrayList;

public class ParamLoader {

    public static double[][] parseMatrix(String line) {
        String[] meta = line.split("#");
        int dimx = Integer.parseInt(meta[0]);
        int dimy = Integer.parseInt(meta[1]);
        String[] vs = meta[2].split("\\|");
        double[][] m = new double[dimx][dimy];
        for (int i=0; i<vs.length; i++) {
            m[i] = parseVector(vs[i]);
        }
        return m;
    }

    public static double[] parseVector(String line) {
        String[] vals = line.split(",");
        double[] v = new double[vals.length];
        for (int i=0; i<vals.length; i++) {
            v[i] = Double.parseDouble(vals[i]);
        }
        return v;
    }

    public static MLP loadMLP (InputStream is) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Activation activation = Activation.valueOf(lines.get(0));
        int numLayers = Integer.parseInt(lines.get(1));
        MLPLayer[] layers = new MLPLayer[numLayers];
        for (int i=0; i<numLayers; i++) {
            int coefIdx = 2*i + 2;
            int biasIdx = 2*i + 1 + 2;
            double[][] coefs = parseMatrix(lines.get(coefIdx));
            double[] bias = parseVector(lines.get(biasIdx));
            if (i < numLayers -1) {
                layers[i] = new MLPLayer(activation, coefs, bias);
            } else {
                layers[i] = new MLPLayer(Activation.identity, coefs, bias);
            }
        }
        return new MLP(layers);
    }

    public static PCA loadPCA (InputStream is) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        double[][] coefs = parseMatrix(lines.get(0));
        double[] bias = parseVector(lines.get(1));
        return new PCA(coefs, bias);
    }

}
