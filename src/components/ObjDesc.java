package components;

import java.util.Vector;

/**
 * Created by alex on 3/6/2017.
 */
public class ObjDesc {

    public final Vector<Vertex> vertices = new Vector<>();
    public final Vector<Vec4> normals = new Vector<>();
    public final Vector<Vector<VertexDesc>> faces = new Vector<>();

    public ObjDesc() {
    }
}
