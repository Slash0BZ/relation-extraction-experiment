package edu.illinois.cs.cogcomp.wsd.math;

/**
 * Created by haowu4 on 1/13/17.
 */
public class Distance {
    public static double cosine(DenseVector v1, DenseVector v2, double v1n,
                              double v2n) {
        float[] v1_ = v1.getData();
        float[] v2_ = v2.getData();
        double r = 0;
        for (int i = 0; i < v1_.length; i++) {
            r += (v1_[i] * v2_[i]);
        }
        return r / (v1n * v2n);
    }

    public static double norm(DenseVector v1) {
        float[] v1_ = v1.getData();
        double r = 0;
        for (int i = 0; i < v1_.length; i++) {
            r += (v1_[i] * v1_[i]);
        }
        return Math.sqrt(r);
    }
}
