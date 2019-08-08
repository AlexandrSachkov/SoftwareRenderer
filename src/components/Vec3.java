package components;

/**
 * Created by alex on 2/11/2017.
 */
public class Vec3 extends Vec2{

    public final double z;

    public Vec3(Vec2 v, double z){
        this(v.x, v.y, z);
    }

    public Vec3(Vec3 v){
        this(v.x, v.y, v.z);
    }

    public Vec3(double x, double y, double z){
        super(x,y);
        this.z = z;
    }

    public static Vec3 toEuclid(Vec4 v4){
        return new Vec3(
                v4.x/v4.w,
                v4.y/v4.w,
                v4.z/v4.w
        );
    }

    public static double distance(Vec3 v){
        return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2) + Math.pow(v.z, 2));
    }


    public static Vec3 normalize(Vec3 v){
        double dist = distance(v);
        return new Vec3(
                v.x / dist,
                v.y / dist,
                v.z / dist
        );
    }

    public static Vec3 add(Vec3 v1, Vec3 v2){
        return new Vec3(
                v1.x + v2.x,
                v1.y + v2.y,
                v1.z + v2.z
        );
    }

    public static Vec3 subtract(Vec3 v1, Vec3 v2){
        return new Vec3(
                v1.x - v2.x,
                v1.y - v2.y,
                v1.z - v2.z
        );
    }

    public static Vec3 multiply(Vec3 v, double val){
        return new Vec3(
                v.x * val,
                v.y * val,
                v.z * val
        );
    }

    public static Vec3 divide(Vec3 v, double val){
        return new Vec3(
                v.x / val,
                v.y / val,
                v.z / val
        );
    }

    public static double dot(Vec3 v1, Vec3 v2){
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static Vec3 cross(Vec3 v1, Vec3 v2){
        return new Vec3(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x
        );
    }

    public boolean equals(Vec3 other) {
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

}
