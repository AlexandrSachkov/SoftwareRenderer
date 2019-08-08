package components;

import java.util.Arrays;

/**
 * Created by alex on 2/10/2017.
 */
public class Mat4x4 {

    public final double[][] data;

    public Mat4x4(double[][] data) {
        assert (data.length == 4 && data[0].length == 4);
        this.data = data;
    }

    public static Mat4x4 identity() {
        double[][] data = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 multiply(Mat4x4 left, Mat4x4 right) {
        double[][] data = new double[4][4];

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                data[r][c] = left.data[r][0] * right.data[0][c] +
                        left.data[r][1] * right.data[1][c] +
                        left.data[r][2] * right.data[2][c] +
                        left.data[r][3] * right.data[3][c];
            }
        }

        return new Mat4x4(data);
    }

    public static Mat4x4 rotateX(double a) {
        double aRad = -a / 360 * Math.PI * 2;
        double[][] data = {
                {1, 0, 0, 0},
                {0, Math.cos(aRad), -Math.sin(aRad), 0},
                {0, Math.sin(aRad), Math.cos(aRad), 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 rotateY(double a) {
        double aRad = -a / 360 * Math.PI * 2;
        double[][] data = {
                {Math.cos(aRad), 0, Math.sin(aRad), 0},
                {0, 1, 0, 0},
                {-Math.sin(aRad), 0, Math.cos(aRad), 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 rotateZ(double a) {
        double aRad = -a / 360 * Math.PI * 2;
        double[][] data = {
                {Math.cos(aRad), -Math.sin(aRad), 0, 0},
                {Math.sin(aRad), Math.cos(aRad), 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 scale(double x, double y, double z) {
        double[][] data = {
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 translate(double x, double y, double z) {
        double[][] data = {
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Vec4 multiply(Mat4x4 left, Vec4 right) {
        double[] data = new double[4];

        for (int c = 0; c < 4; c++) {
            data[c] = left.data[c][0] * right.x +
                    left.data[c][1] * right.y +
                    left.data[c][2] * right.z +
                    left.data[c][3] * right.w;
        }

        return new Vec4(data);
    }

    public static Mat4x4 reflectY() {
        double[][] data = {
                {1, 0, 0, 0},
                {0, -1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 perspective() {
        double[][] data = {
                {1, 0, 0, 0},
                {0, -1, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 1, 0}
        };
        return new Mat4x4(data);
    }

    public static Mat4x4 inverse(Mat4x4 m)
    {
        double[][] a = m.data;
        double s0 = a[0][0] * a[1][1] - a[1][0] * a[0][1];
        double s1 = a[0][0] * a[1][2] - a[1][0] * a[0][2];
        double s2 = a[0][0] * a[1][3] - a[1][0] * a[0][3];
        double s3 = a[0][1] * a[1][2] - a[1][1] * a[0][2];
        double s4 = a[0][1] * a[1][3] - a[1][1] * a[0][3];
        double s5 = a[0][2] * a[1][3] - a[1][2] * a[0][3];

        double c5 = a[2][2] * a[3][3] - a[3][2] * a[2][3];
        double c4 = a[2][1] * a[3][3] - a[3][1] * a[2][3];
        double c3 = a[2][1] * a[3][2] - a[3][1] * a[2][2];
        double c2 = a[2][0] * a[3][3] - a[3][0] * a[2][3];
        double c1 = a[2][0] * a[3][2] - a[3][0] * a[2][2];
        double c0 = a[2][0] * a[3][1] - a[3][0] * a[2][1];

        double det = s0 * c5 - s1 * c4 + s2 * c3 + s3 * c2 - s4 * c1 + s5 * c0;
        if(det == 0){
            return null;
        }

        double invdet = 1.0 / det;
        double[][] b = new double[4][4];

        b[0][0] = ( a[1][1] * c5 - a[1][2] * c4 + a[1][3] * c3) * invdet;
        b[0][1] = (-a[0][1] * c5 + a[0][2] * c4 - a[0][3] * c3) * invdet;
        b[0][2] = ( a[3][1] * s5 - a[3][2] * s4 + a[3][3] * s3) * invdet;
        b[0][3] = (-a[2][1] * s5 + a[2][2] * s4 - a[2][3] * s3) * invdet;

        b[1][0] = (-a[1][0] * c5 + a[1][2] * c2 - a[1][3] * c1) * invdet;
        b[1][1] = ( a[0][0] * c5 - a[0][2] * c2 + a[0][3] * c1) * invdet;
        b[1][2] = (-a[3][0] * s5 + a[3][2] * s2 - a[3][3] * s1) * invdet;
        b[1][3] = ( a[2][0] * s5 - a[2][2] * s2 + a[2][3] * s1) * invdet;

        b[2][0] = ( a[1][0] * c4 - a[1][1] * c2 + a[1][3] * c0) * invdet;
        b[2][1] = (-a[0][0] * c4 + a[0][1] * c2 - a[0][3] * c0) * invdet;
        b[2][2] = ( a[3][0] * s4 - a[3][1] * s2 + a[3][3] * s0) * invdet;
        b[2][3] = (-a[2][0] * s4 + a[2][1] * s2 - a[2][3] * s0) * invdet;

        b[3][0] = (-a[1][0] * c3 + a[1][1] * c1 - a[1][2] * c0) * invdet;
        b[3][1] = ( a[0][0] * c3 - a[0][1] * c1 + a[0][2] * c0) * invdet;
        b[3][2] = (-a[3][0] * s3 + a[3][1] * s1 - a[3][2] * s0) * invdet;
        b[3][3] = ( a[2][0] * s3 - a[2][1] * s1 + a[2][2] * s0) * invdet;

        return new Mat4x4(b);
    }

    public static Mat4x4 transpose(Mat4x4 m)
    {
        double[][] d = m.data;
        double[][] data = {
                {d[0][0], d[1][0], d[2][0], d[3][0]},
                {d[0][1], d[1][1], d[2][1], d[3][1]},
                {d[0][2], d[1][2], d[2][2], d[3][2]},
                {d[0][3], d[1][3], d[2][3], d[3][3]}
        };
        return new Mat4x4(data);
    }

    @Override
    public String toString(){
        return Arrays.deepToString(data);
    }
}
