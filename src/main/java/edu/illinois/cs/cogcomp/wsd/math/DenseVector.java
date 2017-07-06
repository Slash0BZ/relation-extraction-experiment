package edu.illinois.cs.cogcomp.wsd.math;

/**
 * Created by haowu4 on 1/13/17.
 */
public class DenseVector {
    float[] data;

    public DenseVector(float[] data) {
        this.data = data;
    }

    public DenseVector(DenseVector data) {
        float[] k = data.getData();
        this.data = new float[k.length];
        for (int i = 0; i < k.length; i++) {
            this.data[i] = k[i];
        }
    }

    public float[] getData() {
        return data;
    }

    public void iadd(DenseVector v) {
        float[] dv = v.getData();
        for (int i = 0; i < dv.length; i++) {
            this.data[i] += dv[i];
        }
    }

    public void idivide(float v) {
        for (int i = 0; i < data.length; i++) {
            this.data[i] /= v;
        }
    }

    public void isub(DenseVector v) {
        float[] dv = v.getData();
        for (int i = 0; i < dv.length; i++) {
            this.data[i] -= dv[i];
        }
    }
}
