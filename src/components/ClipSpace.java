package components;

/**
 * Created by alex on 2/11/2017.
 */
public class ClipSpace {

    public final Vec2 topLeft;
    public final Vec2 size;
    public final double minDepth;
    public final double maxDepth;

    public ClipSpace(
            Vec2 topLeft,
            Vec2 size,
            double minDepth,
            double maxDepth){

        this.topLeft = topLeft;
        this.size = size;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }
}
