package components;

import java.util.Arrays;

/**
 * Created by alex on 2/10/2017.
 */
public class Vec4 extends Vec3{

    public final double w;

    public Vec4(double x, double y, double z){
        this(x,y,z,1);
    }

    public Vec4(Vec4 v){
        this(v.x, v.y, v.z, v.w);
    }

    public Vec4(Vec3 v){
        this(v, 1);
    }

    public Vec4(Vec3 v, double w){
        this(v.x,v.y,v.z,w);
    }

    public Vec4(double[] data){
        this(data[0],data[1],data[2], data[3]);
    }

    public Vec4(double x, double y, double z, double w) {
        super(x, y, z);
        this.w = w;
    }

    public Vec4 toEuclid(){
        return new Vec4(x/w, y/w, z/w, 1);
    }
}
