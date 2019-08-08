package components;

/**
 * Created by alex on 3/7/2017.
 */
public class DepthEffect {

    public final double near;
    public final double far;
    public final Color color;

    public DepthEffect(double near, double far, Color c){
        this.near = near;
        this.far = far;
        this.color = c;
    }
}
