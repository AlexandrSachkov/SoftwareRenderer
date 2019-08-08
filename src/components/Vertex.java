package components;

/**
 * Created by alex on 2/9/2017.
 */
public class Vertex extends Vec4 {

    public final Color color;
    public final Vec4 normal;
    public final Vec4 worldPosition;

    public Vertex(Vec4 v, Color color){
        this(v, color, null, null);
    }

    public Vertex(Vec4 v, Color color, Vec4 normal){
        this(v, color, normal, null);
    }

    public Vertex(Vec4 v, Color color, Vec4 normal, Vec4 wPos){
        super(v.x, v.y, v.z, v.w);
        this.color = color;
        this.normal = normal;
        this.worldPosition = wPos;
    }
}
