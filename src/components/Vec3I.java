package components;

/**
 * Created by alex on 2/12/2017.
 */
public class Vec3I extends Vec2I{

    public final int z;

    public Vec3I(Vec2I v, int z){
        this(v.x, v.y, z);
    }


    public Vec3I(int x, int y, int z){
        super(x,y);
        this.z = z;
    }
}
