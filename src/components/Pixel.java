package components;

/**
 * Created by alex on 3/5/2017.
 */
public class Pixel extends Vec3 {

    public final Color color;
    public final Vec3 normal;
    public final Vec3 worldPosition;

    public Pixel(Vec3 v, Color color){
        this(v, color, null, null);
    }

    public Pixel(Vec3 v, Color color, Vec3 n){
        this(v, color, n, null);
    }

    public Pixel(Vec3 v, Color c, Vec3 n, Vec3 wPos){
        this(v.x, v.y, v.z, c, n, wPos);
    }

    public Pixel(double x, double y, double z, Color color, Vec3 normal, Vec3 wPos){
        super(x, y, z);
        this.color = color;
        this.normal = normal;
        this.worldPosition = wPos;
    }
}