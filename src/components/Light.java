package components;

/**
 * Created by alex on 4/3/2017.
 */
public class Light {
    public final Color color;
    public final double A;
    public final double B;
    public final Vec4 position;

    public Light(Color c, double a, double b, Vec4 pos){
        this.color = c;
        this.A = a;
        this.B = b;
        this.position = pos;
    }
}
