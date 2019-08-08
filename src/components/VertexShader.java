package components;

import java.util.List;

/**
 * Created by alex on 2/12/2017.
 */
public interface VertexShader {

    Vertex run(Vertex v, List<Vertex> face, Color ambient, List<Light> lights, Camera camera, double specCoeff, double specExp);
}
