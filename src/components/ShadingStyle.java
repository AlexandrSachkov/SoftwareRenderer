package components;

import java.util.List;

/**
 * Created by alex on 4/3/2017.
 */

public enum ShadingStyle {

    PHONG(Renderer.VS_DEFAULT, (Pixel p, Color ambient, List<Light> lights, Camera camera, double specRefl, double specExp) -> {
        Vertex v = calculateLighting(new Vec4(p, 1), p.color, p.worldPosition, p.normal, ambient, lights, camera, specRefl, specExp);
        return new Pixel(
                v,
                v.color,
                v.normal
        );
    }),

    GOURAUD((Vertex v, List<Vertex> face, Color ambient, List<Light> lights, Camera camera, double specRefl, double specExp)-> {
        return calculateLighting(v, v.color, v, v.normal, ambient, lights, camera, specRefl, specExp);
    }, Renderer.PS_DEFAULT),


    FLAT((Vertex v, List<Vertex> face, Color ambient, List<Light> lights, Camera camera, double specRefl, double specExp)-> {
        Vec3 vertexSum = face.get(0);
        for(int i = 1; i < face.size(); i++){
            vertexSum = Vec3.add(vertexSum, face.get(i));
        }

        Vec3 faceCenter = Vec3.divide(vertexSum, face.size());
        Vec3 N = Utility.averageFaceNormals(face);

        return calculateLighting(v, v.color, faceCenter, N, ambient, lights, camera, specRefl, specExp);
    }, Renderer.PS_DEFAULT);

    public final VertexShader VS;
    public final PixelShader PS;

    ShadingStyle(VertexShader vs, PixelShader ps){
        this.VS = vs;
        this.PS = ps;
    }

    private static Vertex calculateLighting(Vec4 elementPosition, Color elementColor, Vec3 normalPosition, Vec3 normal, Color ambient,
                                     List<Light> lights, Camera camera, double specRefl, double specExp) {
        Vec3 N = normal;

        //vertex
        double vR = (double)elementColor.r / 255;
        double vG = (double)elementColor.g / 255;
        double vB = (double)elementColor.b / 255;

        //ambient
        double aR = vR * ambient.r / 255;
        double aG = vG * ambient.g / 255;
        double aB = vB * ambient.b / 255;

        //final color
        double fR = aR;
        double fG = aG;
        double fB = aB;

        for(Light l : lights){
            Vec3 L = Vec3.normalize(Vec3.subtract(l.position, normalPosition));
            double aNL = Vec3.dot(N, L);

            if(aNL <= 0) continue;

            Vec3 R = Vec3.normalize(Vec3.subtract(Vec3.multiply(N, 2 * aNL), L));
            Vec3 V = Vec3.normalize(Vec3.subtract(camera.position, normalPosition));
            double aVR = Vec3.dot(V, R);

            double spec = specRefl * Math.pow(aVR, specExp);

            double lDist = Vec3.distance(Vec3.subtract(l.position, normalPosition));
            double att = 1 / (l.A + l.B * lDist);

            double dR = l.color.r / 255 * att * (vR * aNL + spec);
            double dG = l.color.g / 255 * att * (vG * aNL + spec);
            double dB = l.color.b / 255 * att * (vB * aNL + spec);

            fR += dR;
            fG += dG;
            fB += dB;
        }

        if(fR > 1) fR = 1;
        if(fG > 1) fG = 1;
        if(fB > 1) fB = 1;

        Color fC = new Color(
                (int)(fR * 255),
                (int)(fG * 255),
                (int)(fB * 255)
        );

        return new Vertex(elementPosition, fC, new Vec4(N, 0));
    }
}
