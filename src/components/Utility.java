package components;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by alex on 1/21/2017.
 */
public class Utility {

    private static final Random rand = new Random();

    public static Vec2 getEndPoint(Vec2 p, double length, double angle) {
        double radAngle = Math.toRadians(angle);
        double dx = Math.round(length * Math.cos(radAngle));
        double dy = Math.round(length * Math.sin(radAngle));
        return new Vec2(p.x + dx, p.y + dy);
    }

    public static Color genColor() {
        return new Color(rand.nextInt(255) + 1, rand.nextInt(255) + 1, rand.nextInt(255) + 1);
    }

    public static Color lerpColor(Vec2 p, Vec2 iP, Vec2 fP, Color iC, Color fC) {
        double dTot = Math.sqrt(Math.pow(fP.x - iP.x, 2) + Math.pow(fP.y - iP.y, 2));
        if (dTot == 0) {
            return fC;
        }

        double d = Math.sqrt(Math.pow(p.x - iP.x, 2) + Math.pow(p.y - iP.y, 2));

        double rTot = fC.r - iC.r;
        double gTot = fC.g - iC.g;
        double bTot = fC.b - iC.b;
        double aTot = fC.a - iC.a;

        double r = iC.r + rTot / dTot * d;
        double g = iC.g + gTot / dTot * d;
        double b = iC.b + bTot / dTot * d;
        double a = iC.a + aTot / dTot * d;

        return new Color(
                (int) Math.round(r),
                (int) Math.round(g),
                (int) Math.round(b),
                (int) Math.round(a));
    }

    public static Color lerpColor_PC(Color iC, Color fC, double z, double iZ, double fZ){
        if(iZ == fZ){
            return fC;
        }

        double r = lerpV_PC(iC.r, fC.r, z, iZ, fZ);
        if (r > 255) r = 255;
        else if(r < 0) r = 0;

        double g = lerpV_PC(iC.g, fC.g, z, iZ, fZ);
        if (g > 255) g = 255;
        else if(g < 0) g = 0;

        double b = lerpV_PC(iC.b, fC.b, z, iZ, fZ);
        if (b > 255) b = 255;
        else if(b < 0) b = 0;

        double a = lerpV_PC(iC.a, fC.a, z, iZ, fZ);
        if (a > 1) a = 1;
        else if(a < 0) a = 0;

        return new Color(
                (int) Math.round(r),
                (int) Math.round(g),
                (int) Math.round(b),
                (int) Math.round(a));
    }

    public static Vec3 lerpNormal_PC(Vec3 iN, Vec3 fN, double cZ, double iZ, double fZ){
        double x = lerpV_PC(iN.x, fN.x, cZ, iZ, fZ);
        double y = lerpV_PC(iN.y, fN.y, cZ, iZ, fZ);
        double z = lerpV_PC(iN.z, fN.z, cZ, iZ, fZ);

        return Vec3.normalize(new Vec3(x, y, z));
    }

    public static Vec3 lerpPosition_PC(Vec3 iP, Vec3 fP, double cZ, double iZ, double fZ){
        double x = lerpV_PC(iP.x, fP.x, cZ, iZ, fZ);
        double y = lerpV_PC(iP.y, fP.y, cZ, iZ, fZ);
        double z = lerpV_PC(iP.z, fP.z, cZ, iZ, fZ);
        return new Vec3(x, y, z);
    }

    public static double lerpZ_PC(Vec2 p, Vec3 iP, Vec3 fP) {
        double dTot = Math.sqrt(Math.pow(fP.x - iP.x, 2) + Math.pow(fP.y - iP.y, 2));
        if (dTot == 0) {
            return fP.z;
        }

        double d = Math.sqrt(Math.pow(p.x - iP.x, 2) + Math.pow(p.y - iP.y, 2));

        double q = d / dTot;
        double z = 1 / (1/iP.z * (1 - q) + 1/fP.z * q);
        return z;
    }

    public static double lerpV_PC(double vI, double vF, double z, double zI, double zF){
        double q = (z - zI)/(zF - zI);
        double iRat = vI/zI;
        double fRat = vF/zF;
        double v = z * (iRat * (1 - q) + fRat * q);
        return v;
    }

    public static Vector<String[]> loadCommandsFromFile(String name) {
        Vector<String[]> commands = new Vector<>();

        String fileName = name + ".simp";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#' || line.trim().length() == 0) {
                    continue;
                }

                line = line.trim().replace(',', ' ');
                String[] tokens = line.split("[ \t]+");
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i];
                    if (token.charAt(0) == '(' || token.charAt(0) == '"') {
                        token = token.substring(1);
                    }
                    if (token.charAt(token.length() - 1) == ')'
                            || token.charAt(token.length() - 1) == ','
                            || token.charAt(token.length() - 1) == '"') {
                        token = token.substring(0, token.length() - 1);
                    }
                    tokens[i] = token;
                }

                if (tokens[0].equals("file")) {
                    Vector<String[]> cmds = loadCommandsFromFile(tokens[1]);
                    if (cmds != null) {
                        commands.addAll(cmds);
                    }
                } else {
                    commands.add(tokens);
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read file: " + fileName);
            return null;
        }
        return commands;
    }

    public static ObjDesc loadObjFile(String name){
        ObjDesc obj = new ObjDesc();

        String fileName = name + ".obj";
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(!line.startsWith("v ") && !line.startsWith("f ") && !line.startsWith("vn ")){
                    continue;
                }

                line = line.trim();

                if(line.startsWith("v ")){
                    line = line.substring(1).trim();
                    String[] coords = line.split(" ");
                    Vertex v = new Vertex(new Vec4(
                            Double.parseDouble(coords[0]),
                            Double.parseDouble(coords[1]),
                            Double.parseDouble(coords[2]),
                            coords.length == 4 ? Double.parseDouble(coords[3]) : 1
                    ), null);
                    obj.vertices.add(v);

                } else if (line.startsWith("f ")){
                    line = line.substring(2);
                    String[] coords = line.split(" ");
                    Vector<VertexDesc> desc = new Vector<>();

                    for(int i = 0; i < coords.length; i++){
                        String[] indices = coords[i].split("/");
                        int vI = Integer.parseInt(indices[0]);
                        if(vI < 0){
                            vI = obj.vertices.size() - vI;
                        }

                        int nI = 0;
                        if(indices.length == 3){
                            nI = Integer.parseInt(indices[2]);
                            if(nI < 0){
                                nI = obj.normals.size() - nI;
                            }
                        }else{
                            nI = -1;
                        }


                        desc.add(new VertexDesc(vI, nI));
                    }

                    for(int i = 2; i < desc.size(); i++){
                        Vector<VertexDesc> vertices = new Vector<>();
                        vertices.add(desc.get(0));
                        vertices.add(desc.get(i - 1));
                        vertices.add(desc.get(i));
                        obj.faces.add(vertices);
                    }

                } else { //normal
                    line = line.substring(3);
                    String[] coords = line.split(" ");
                    Vec4 n = new Vec4(
                            Double.parseDouble(coords[0]),
                            Double.parseDouble(coords[1]),
                            Double.parseDouble(coords[2]),
                            0
                    );
                    obj.normals.add(n);
                }
            }
        } catch (IOException e) {
            System.out.println("Unable to read file: " + fileName);
            return null;
        }
        return obj;
    }

    public static void renderCommandList(Renderer renderer, List<String[]> commands) {
        if(commands == null || commands.size() == 0){
            return;
        }
        boolean wireframe = false;
        Mat4x4 currTransform = Mat4x4.identity();
        Stack<Mat4x4> transforms = new Stack<>();

        for (String[] command : commands) {
            switch (command[0]) {
                case "{":
                    transforms.push(currTransform);
                    break;

                case "}":
                    currTransform = transforms.pop();
                    break;

                case "scale": {
                    Mat4x4 transform = Mat4x4.scale(
                            Double.parseDouble(command[1]),
                            Double.parseDouble(command[2]),
                            Double.parseDouble(command[3])
                    );
                    currTransform = Mat4x4.multiply(currTransform, transform);
                }
                break;

                case "rotate": {
                    double a = Double.parseDouble(command[2]);
                    Mat4x4 transform;
                    switch (command[1]) {
                        case "X":
                            transform = Mat4x4.rotateX(a);
                            break;
                        case "Y":
                            transform = Mat4x4.rotateY(a);
                            break;
                        case "Z":
                            transform = Mat4x4.rotateZ(a);
                            break;
                        default:
                            System.out.println("Unknown rotation axis: " + command[1]);
                            continue;
                    }
                    currTransform = Mat4x4.multiply(currTransform, transform);
                }
                break;

                case "translate": {
                    Mat4x4 transform = Mat4x4.translate(
                            Double.parseDouble(command[1]),
                            Double.parseDouble(command[2]),
                            Double.parseDouble(command[3])
                    );
                    currTransform = Mat4x4.multiply(currTransform, transform);
                }
                break;

                case "line":
                case "polygon":
                case "triangle":{
                    int numVertices = 3;
                    if(command[0].equals("line")){
                        numVertices = 2;
                    }

                    Vector<Vertex> vertices = new Vector<>();
                    int numCoords = (command.length - 1) / numVertices;

                    boolean hasW = false;
                    boolean hasColor = false;

                    if (numCoords == 4){
                        hasW = true;
                    } else if (numCoords == 6){
                        hasColor = true;
                    } else if (numCoords == 7){
                        hasW = true;
                        hasColor = true;
                    }

                    for (int i = 1; i < command.length; i += numCoords) {
                        Vec4 coord = new Vec4(
                                Double.parseDouble(command[i]),
                                Double.parseDouble(command[i + 1]),
                                Double.parseDouble(command[i + 2]),
                                hasW ? Double.parseDouble(command[i + 3]) : 1
                        );

                        Color color = null;
                        int colorOffset = 3;
                        if(hasW){
                            colorOffset = 4;
                        }

                        if(hasColor){
                            color = new Color(
                                    (int)(255 * Double.parseDouble(command[i + colorOffset])),
                                    (int)(255 * Double.parseDouble(command[i + colorOffset + 1])),
                                    (int)(255 * Double.parseDouble(command[i + colorOffset + 2]))
                            );
                        }

                        Vec4 transformedCoord = Mat4x4.multiply(currTransform, coord);
                        vertices.add(new Vertex(transformedCoord, color));
                    }

                    Vector<Vertex> finVertices = vertices;
                    if(numVertices > 2){
                        finVertices = computeFaceNormal(vertices);
                    }
                    renderer.render(finVertices, wireframe, false);
                }
                break;

                case "wire":
                    wireframe = true;
                    break;

                case "filled":
                    wireframe = false;
                    break;

                case "surface":{
                    Color color = new Color(
                            (int)(255 * Double.parseDouble(command[1])),
                            (int)(255 * Double.parseDouble(command[2])),
                            (int)(255 * Double.parseDouble(command[3]))
                    );
                    renderer.setSurfaceColor(color);

                    if(command.length > 4){
                        double refl = Double.parseDouble(command[4]);
                        double pow = Double.parseDouble(command[5]);
                        renderer.setSpecularCoeff(refl, pow);
                    }
                }
                    break;

                case "obj": {
                    ObjDesc desc = loadObjFile(command[1]);

                    Vector<Vertex> vertices = new Vector<>();
                    for(int i = 0; i < desc.vertices.size(); i++){
                        Vertex v = desc.vertices.get(i);
                        Vec4 tC = Mat4x4.multiply(currTransform, v);
                        vertices.add(new Vertex(tC, null));
                    }

                    Mat4x4 normalTransform = Mat4x4.transpose(Mat4x4.inverse(currTransform));
                    Vector<Vec4> normals = new Vector<>();
                    for(int i = 0; i < desc.normals.size(); i++){
                        Vec4 n = desc.normals.get(i);
                        Vec3 tN = Vec3.normalize(Mat4x4.multiply(normalTransform, n));
                        normals.add(new Vec4(tN, 0));
                    }

                    for(int i = 0; i < desc.faces.size(); i++){
                        Vector<Vertex> poly = new Vector<>();
                        Vector<VertexDesc> face = desc.faces.get(i);
                        for(int j = 0; j < face.size(); j++){
                            Vertex v = vertices.get(face.get(j).vI - 1);

                            Vec4 n = null;
                            if(face.get(j).nI != -1){
                                n = normals.get(face.get(j).nI - 1);
                            }

                            Vertex newV = new Vertex(v, v.color, n);
                            poly.add(newV);
                        }

                        Vector<Vertex> polyWithNormals = poly;
                        if(poly.size() > 2 && poly.get(0).normal == null){
                            polyWithNormals = computeFaceNormal(poly);
                        }
                        renderer.render(polyWithNormals, wireframe, false);
                    }
                }
                break;

                case "camera":{
                    Mat4x4 t = Mat4x4.inverse(currTransform);
                    if(t == null){
                        assert(false);
                    }
                    Vec4 position = Mat4x4.multiply(currTransform, new Vec4(0,0,0));

                    Camera cam = new Camera(
                            Double.parseDouble(command[1]),
                            Double.parseDouble(command[2]),
                            Double.parseDouble(command[3]),
                            Double.parseDouble(command[4]),
                            Double.parseDouble(command[5]),
                            Double.parseDouble(command[6]),
                            t,
                            position
                    );
                    renderer.setCamera(cam);
                    renderer.clearZBuffer(cam.clipSpace.maxDepth);
                }
                break;

                case "ambient": {
                    Color color = new Color(
                            (int)(255 * Double.parseDouble(command[1])),
                            (int)(255 * Double.parseDouble(command[2])),
                            (int)(255 * Double.parseDouble(command[3]))
                    );
                    renderer.setAmbientColor(color);
                }
                break;

                case "depth": {
                    double near = Double.parseDouble(command[1]);
                    double far = Double.parseDouble(command[2]);
                    Color color = new Color(
                            (int)(255 * Double.parseDouble(command[3])),
                            (int)(255 * Double.parseDouble(command[4])),
                            (int)(255 * Double.parseDouble(command[5]))
                    );
                    renderer.setDepthEffect(new DepthEffect(near, far, color));
                }
                break;

                case "light": {
                    Color color = new Color(
                            (int)(255 * Double.parseDouble(command[1])),
                            (int)(255 * Double.parseDouble(command[2])),
                            (int)(255 * Double.parseDouble(command[3]))
                    );
                    double a = Double.parseDouble(command[4]);
                    double b = Double.parseDouble(command[5]);

                    Vec4 position = Mat4x4.multiply(currTransform, new Vec4(0,0,0));
                    renderer.addLight(new Light(color, a, b, position));
                }
                break;

                case "phong": {
                    renderer.setShadingStyle(ShadingStyle.PHONG);
                }
                break;

                case "gouraud": {
                    renderer.setShadingStyle(ShadingStyle.GOURAUD);
                }
                break;

                case "flat": {
                    renderer.setShadingStyle(ShadingStyle.FLAT);
                }
                break;

                default:
                    System.out.println("Unsupported operation: " + command[0] + ". Skipping...");
                    continue;
            }
        }
    }

    public static Vector<Vertex> computeFaceNormal(List<Vertex> vertices){
        Vec3 v12 = Vec3.subtract(vertices.get(1), vertices.get(0));
        Vec3 v13 = Vec3.subtract(vertices.get(2), vertices.get(0));
        Vec3 normal = Vec3.normalize(Vec3.multiply(Vec3.cross(v12, v13), -1));

        Vector<Vertex> newVertices = new Vector<>();
        for(Vertex v : vertices){
            Vertex newV = new Vertex(v, v.color, new Vec4(normal, 0));
            newVertices.add(newV);
        }

        return newVertices;
    }

    public static Vec3 averageFaceNormals(List<Vertex> vertices){
        Vec3 normalSum = vertices.get(0).normal;
        for(int i = 1; i < vertices.size(); i++){
            normalSum = Vec3.add(normalSum, vertices.get(i).normal);
        }
        return Vec3.normalize(Vec3.divide(normalSum, vertices.size()));
    }
}
