package components;

import java.util.List;
import java.util.Vector;

/**
 * Created by alex on 1/15/2017.
 */
public class Renderer {
    public static final double PIXEL_RADIUS = 0.5;
    public static final VertexShader VS_DEFAULT =
            (Vertex v, List<Vertex> face, Color ambient, List<Light> lights, Camera camera, double specCoeff, double specExp) -> {
                return v;
            };

    public static final PixelShader PS_DEFAULT = (Pixel p, Color ambient, List<Light> lights, Camera camera, double specCoeff, double specExp) -> {
        return p;
    };

    private DisplayArea display;
    Camera camera = new Camera(-1, -1, 1, 1, 0.01, 200, Mat4x4.identity(), new Vec4(0, 0, 0));
    Mat4x4 screenSpaceTransform = Mat4x4.identity();
    double[] zBuffer;

    Color surfaceColor = new Color(255, 255, 255);
    double specularReflection = 0.3;
    double specularExponent = 8;
    Vector<Pixel> pixQueue = new Vector<>();
    Color ambient = new Color(0, 0, 0);
    DepthEffect depthEffect = new DepthEffect(Double.MAX_VALUE, Double.MAX_VALUE, new Color(0, 0, 0));

    ShadingStyle shadingStyle = ShadingStyle.PHONG;
    Vector<Light> lights = new Vector<>();

    public Renderer() {
    }

    public void setDisplayArea(DisplayArea display) {
        this.display = display;
        zBuffer = new double[display.dim.x * display.dim.y];
    }

    public void setSurfaceColor(Color c) {
        surfaceColor = c;
    }

    public void setSpecularCoeff(double refl, double pow) {
        specularReflection = refl;
        specularExponent = pow;
    }

    public void setShadingStyle(ShadingStyle style) {
        shadingStyle = style;
    }

    public void setAmbientColor(Color c) {
        this.ambient = c;
    }

    public void setDepthEffect(DepthEffect d) {
        this.depthEffect = d;
    }


    public void setCamera(Camera camera) {
        this.camera = camera;
        Mat4x4 scale = Mat4x4.scale(
                display.dim.x / camera.clipSpace.size.x,
                display.dim.y / camera.clipSpace.size.y,
                1
        );
        double tX = display.dim.x / 2.0 - (camera.clipSpace.topLeft.x + camera.clipSpace.size.x / 2.0);
        double tY = display.dim.y / 2.0 - (camera.clipSpace.topLeft.y + camera.clipSpace.size.y / 2.0);
        Mat4x4 translate = Mat4x4.translate(tX, tY, 0);
        screenSpaceTransform = Mat4x4.multiply(translate, scale);
    }

    public void clearDisplayArea(Color color) {
        display.fill(color);
    }

    public void clearZBuffer(double val) {
        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = val;
        }
    }

    public void addLight(Light l) {
        this.lights.add(l);
    }

    public void render(List<Vertex> vertices, boolean wireframe, boolean antialiased) {
        Vector<Vertex> coloredVertices = new Vector<>();
        for (Vertex v : vertices) {
            Vertex newV = v;
            if (v.color == null) {
                newV = new Vertex(v, surfaceColor, v.normal);
            }
            coloredVertices.add(newV);
        }

        Vector<Vertex> worldVertices = new Vector<>();
        for (Vertex v : coloredVertices) {
            Vertex tV = new Vertex(
                    v,
                    new Color(
                            v.color.r * ambient.r / 255,
                            v.color.g * ambient.g / 255,
                            v.color.b * ambient.b / 255
                    ),
                    v.normal);

            if (coloredVertices.size() > 2 && !wireframe) {
                tV = shadingStyle.VS.run(v, coloredVertices, ambient, lights, camera, specularReflection, specularExponent);
            }
            worldVertices.add(tV);
        }

        boolean inClipSpace = false;
        for (Vertex v : worldVertices) {
            Vec3 t = Mat4x4.multiply(camera.csTransform, v).toEuclid();
            if (t.z >= camera.clipSpace.minDepth && t.z <= camera.clipSpace.maxDepth) {
                inClipSpace = true;
                break;
            }
        }
        if (!inClipSpace) {
            return;
        }

        Vector<Vertex> cameraVertices = new Vector<>();
        for (Vertex v : worldVertices) {
            Vec4 csV = Mat4x4.multiply(camera.csTransform, v).toEuclid();
            Vec4 psV = Mat4x4.multiply(Mat4x4.perspective(), csV).toEuclid();
            Vec4 finVPos = new Vec4(psV.x, psV.y, csV.z);

            cameraVertices.add(new Vertex(finVPos, v.color, v.normal, v));
        }

        inClipSpace = false;
        for (Vec3 v : cameraVertices) {
            if (v.x >= camera.clipSpace.topLeft.x && v.x <= (camera.clipSpace.topLeft.x + camera.clipSpace.size.x)
                    && v.y >= camera.clipSpace.topLeft.y && v.y <= (camera.clipSpace.topLeft.y + camera.clipSpace.size.y)) {
                inClipSpace = true;
                break;
            }
        }

        if (!inClipSpace) {
            return;
        }

        Vector<Vertex> outVertices = new Vector<>();
        for (int i = 0; i < cameraVertices.size(); i++) {
            Vertex v = cameraVertices.get(i);
            Vec4 vPos = Mat4x4.multiply(screenSpaceTransform, v);
            Vertex vFinal = new Vertex(vPos, v.color, v.normal, v.worldPosition);
            outVertices.add(vFinal);
        }

        if (outVertices.size() == 2) {
            Vertex v1 = outVertices.get(0);
            Vertex v2 = outVertices.get(1);
            drawLine(this, Vec3.toEuclid(v1), Vec3.toEuclid(v2), v1.color, v2.color, antialiased);
        } else if (outVertices.size() > 2) {
            drawPolygon(this, outVertices, wireframe, antialiased);
        }

        for (Pixel p : pixQueue) {
            Pixel fP = p;
            if (coloredVertices.size() > 2 && !wireframe) {
                fP = shadingStyle.PS.run(p, ambient, lights, camera, specularReflection, specularExponent);
            }

            Pixel fin = fP;
            if (fP.z >= depthEffect.far) {
                fin = new Pixel(new Vec3(fP.x, fP.y, fP.z), depthEffect.color);
            } else if (fP.z >= depthEffect.near && fP.z <= depthEffect.far) {
                double ratio = (fP.z - depthEffect.near) / (depthEffect.far - depthEffect.near);
                int r = (int) ((1 - ratio) * fP.color.r + ratio * depthEffect.color.r);
                int g = (int) ((1 - ratio) * fP.color.g + ratio * depthEffect.color.g);
                int b = (int) ((1 - ratio) * fP.color.b + ratio * depthEffect.color.b);
                fin = new Pixel(new Vec3(fP.x, fP.y, fP.z), new Color(r, g, b));
            }
            writePixel(fin);
        }
    }

    private void queuePixel(Pixel p) {
        pixQueue.add(p);
    }

    private void writePixel(Pixel p) {
        if (p.z < 0) return;

        int i = Math.round((int) Math.round(p.y) * display.dim.x + (int) Math.round(p.x));
        if (i < 0 || i >= zBuffer.length) {
            return;
        }
        double currZVal = zBuffer[i];
        if (currZVal > p.z) {
            zBuffer[i] = p.z;
            display.setPixel(new Vec2I((int) Math.round(p.x), (int) Math.round(p.y)), p.color);
        }
    }

    private Color readPixel(Vec2 p) {
        return display.getPixel(new Vec2I((int) Math.round(p.x), (int) Math.round(p.y)));
    }

    public static void drawLine(Renderer renderer,
                                Vec3 v1, Vec3 v2, Color c1, Color c2,
                                boolean antialiase) {
        double deltaX = v2.x - v1.x;
        double deltaY = v2.y - v1.y;

        if (deltaX > 0 && Math.abs(deltaX) >= Math.abs(deltaY)) { //octants 1 and 8
            drawLineDDA_xAdj(renderer, v1, v2, c1, c2, antialiase);
        } else if (deltaX < 0 && Math.abs(deltaX) >= Math.abs(deltaY)) { //octants 4 and 5
            drawLineDDA_xAdj(renderer, v2, v1, c2, c1, antialiase);
        } else if (deltaY > 0 && Math.abs(deltaX) < Math.abs(deltaY)) { //octants 2 and 3
            drawLineDDA_yAdj(renderer, v1, v2, c1, c2, antialiase);
        } else if (deltaY < 0 && Math.abs(deltaX) < Math.abs(deltaY)) { //octants 6 and 7
            drawLineDDA_yAdj(renderer, v2, v1, c2, c1, antialiase);
        } else if (deltaX == 0 && deltaY == 0) {
            renderer.queuePixel(new Pixel(v1, c1));
        }
    }

    private static void drawLineDDA_xAdj(Renderer renderer,
                                         Vec3 vertex1, Vec3 vertex2, Color c1, Color c2,
                                         boolean antialiase) {
        Vec3 v1 = new Vec3(
                (int) Math.round(vertex1.x),
                (int) Math.round(vertex1.y),
                vertex1.z);
        Vec3 v2 = new Vec3(
                (int) Math.round(vertex2.x),
                (int) Math.round(vertex2.y),
                vertex2.z);

        double m = (v2.y - v1.y) / (v2.x - v1.x);
        double b = v1.y - m * v1.x;
        for (double x = v1.x; x <= v2.x; x++) {
            double y = m * x + b;
            Vec2 pLine = new Vec2(x, y);
            double z = Utility.lerpZ_PC(pLine, v1, v2);
            Color color = Utility.lerpColor_PC(c1, c2, z, v1.z, v2.z);
            if (antialiase && (v2.y - v1.y) != 0) {
                antialias_xAdj(renderer, v1, v2, m, b, new Vec3(pLine, z), color);
            } else {
                renderer.queuePixel(new Pixel(new Vec3(pLine, z), color));
            }
        }
    }

    private static void antialias_xAdj(Renderer renderer, Vec3 lineStart, Vec3 lineEnd, double m, double b, Vec3 pL, Color color) {
        Vec3 pLine = new Vec3(
                Math.round(pL.x),
                Math.round(pL.y),
                pL.z
        );

        //makes a list of potentially shaded pixels surrounding the point on the line
        Vector<Vec3> pointsToTry = new Vector<>();
        if (pLine.equals(lineStart)) {
            Vec2 p = new Vec2(pLine.x - 1, pLine.y - 2);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y + 2);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

        } else if (pLine.equals(lineEnd)) {
            Vec2 p = new Vec2(pLine.x + 1, pLine.y - 2);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y + 2);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));
        }

        {
            Vec2 p = new Vec2(pLine.x, pLine.y - 3);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y - 2);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            pointsToTry.add(pLine);

            p = new Vec2(pLine.x, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y + 2);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y + 3);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));
        }

        for (Vec3 p : pointsToTry) {
            double xInt = (p.y + 1 / m * p.x - b) * (m / (Math.pow(m, 2) + 1));
            double yInt = m * xInt + b;

            //checks whether the intersect is on the line segment
            double dy = lineEnd.y - lineStart.y;
            if (xInt < lineStart.x || xInt > lineEnd.x)
                continue;

            if (dy >= 0) {
                if (yInt < lineStart.y || yInt > lineEnd.y)
                    continue;
            } else {
                if (yInt > lineStart.y || yInt < lineEnd.y)
                    continue;
            }

            double dxInt = xInt - p.x;
            double dyInt = yInt - p.y;
            double distance = Math.sqrt(Math.pow(dxInt, 2) + Math.pow(dyInt, 2));

            //checks whether the pixel is shaded
            if (distance >= 2 * PIXEL_RADIUS) {
                continue;
            }

            double intersectArea = getPixelLineIntersectionArea(PIXEL_RADIUS, distance);
            double opacity = intersectArea / (Math.PI * Math.pow(PIXEL_RADIUS, 2));
            Color cColor = renderer.readPixel(p);
            Color newColor = color.blend(cColor, (float) opacity);
            renderer.queuePixel(new Pixel(p, newColor));
        }
    }

    public static double getPixelLineIntersectionArea(double r, double distance) {
        if (distance == 0) {
            return Math.PI * Math.pow(r, 2);
        } else if (distance > r) {
            double d = distance - r;
            return getPixelLineIntersectionArea_internal(r, d);
        } else if (distance < r) {
            double d = r - distance;
            return Math.PI * Math.pow(r, 2) - getPixelLineIntersectionArea_internal(r, d);
        } else {
            return Math.PI * Math.pow(r, 2) / 2;
        }
    }

    public static double getPixelLineIntersectionArea_internal(double r, double d) {
        double r2 = Math.pow(r, 2);
        double pr2 = Math.PI * r2;
        double triArea = d * Math.sqrt(r2 - Math.pow(d, 2));
        double pieArea = (1 - Math.acos(d / r) / Math.PI) * pr2;
        return 1 - (triArea + pieArea) / pr2;
    }

    private static void drawLineDDA_yAdj(Renderer renderer,
                                         Vec3 vertex1, Vec3 vertex2, Color c1, Color c2,
                                         boolean antialiase) {

        Vec3 v1 = new Vec3(
                (int) Math.round(vertex1.x),
                (int) Math.round(vertex1.y),
                vertex1.z);
        Vec3 v2 = new Vec3(
                (int) Math.round(vertex2.x),
                (int) Math.round(vertex2.y),
                vertex2.z);

        if (v2.x - v1.x != 0) {
            double m = (v2.y - v1.y) / (v2.x - v1.x);
            double b = v1.y - m * v1.x;
            for (double y = v1.y; y <= v2.y; y++) {
                double x = (y - b) / m;
                Vec2 pLine = new Vec2(x, y);
                double z = Utility.lerpZ_PC(pLine, v1, v2);
                Color color = Utility.lerpColor_PC(c1, c2, z, v1.z, v2.z);
                if (antialiase) {
                    antialias_yAdj(renderer, v1, v2, m, b, new Vec3(pLine, z), color);
                } else {
                    renderer.queuePixel(new Pixel(new Vec3(pLine, z), color));
                }
            }
        } else {
            for (double y = v1.y; y <= v2.y; y++) {
                Vec2 pLine = new Vec2(v1.x, y);
                double z = Utility.lerpZ_PC(pLine, v1, v2);
                Color color = Utility.lerpColor_PC(c1, c2, z, v1.z, v2.z);
                renderer.queuePixel(new Pixel(new Vec3(pLine, z), color));
            }
        }
    }

    private static void antialias_yAdj(Renderer renderer, Vec3 lineStart, Vec3 lineEnd, double m, double b, Vec3 pL, Color color) {
        Vec3 pLine = new Vec3(
                Math.round(pL.x),
                Math.round(pL.y),
                pL.z
        );

        //makes a list of potentially shaded pixels surrounding the point on the line
        Vector<Vec3> pointsToTry = new Vector<>();
        if (pLine.equals(lineStart)) {
            Vec2 p = new Vec2(pLine.x - 2, pLine.y - 1);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 2, pLine.y - 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

        } else if (pLine.equals(lineEnd)) {
            Vec2 p = new Vec2(pLine.x - 2, pLine.y + 1);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 1, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 2, pLine.y + 1);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));
        }

        {
            Vec2 p = new Vec2(pLine.x - 3, pLine.y);
            double z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 2, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x - 1, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            pointsToTry.add(pLine);

            p = new Vec2(pLine.x + 1, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 2, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));

            p = new Vec2(pLine.x + 3, pLine.y);
            z = Utility.lerpZ_PC(p, lineStart, lineEnd);
            pointsToTry.add(new Vec3(p, z));
        }


        for (Vec3 p : pointsToTry) {
            double xInt = (p.y + 1 / m * p.x - b) * (m / (Math.pow(m, 2) + 1));
            double yInt = m * xInt + b;

            //checks whether the intersect is on the line segment
            double dx = lineEnd.x - lineStart.x;
            if (yInt < lineStart.y || yInt > lineEnd.y)
                continue;

            if (dx >= 0) {
                if (xInt < lineStart.x || xInt > lineEnd.x)
                    continue;
            } else {
                if (xInt > lineStart.x || xInt < lineEnd.x)
                    continue;
            }

            double dxInt = xInt - p.x;
            double dyInt = yInt - p.y;
            double distance = Math.sqrt(Math.pow(dxInt, 2) + Math.pow(dyInt, 2));

            //checks whether the pixel is shaded
            if (distance >= 2 * PIXEL_RADIUS) {
                continue;
            }

            double intersectArea = getPixelLineIntersectionArea(PIXEL_RADIUS, distance);
            double opacity = intersectArea / (Math.PI * Math.pow(PIXEL_RADIUS, 2));
            Color cColor = renderer.readPixel(p);
            Color newColor = color.blend(cColor, (float) opacity);
            renderer.queuePixel(new Pixel(p, newColor));
        }
    }

    public static void drawPolygon(Renderer renderer, List<Vertex> vertices, boolean wireframe, boolean antialiased) {
        if (wireframe) {
            drawPolygonWireframe(renderer, vertices, antialiased);
        } else {
            drawPolygonFilled(renderer, vertices);
        }
    }

    public static void drawPolygonWireframe(Renderer renderer, List<Vertex> vertices, boolean antialiased) {
        if (vertices.size() < 3) {
            return;
        }

        Vertex start = vertices.get(vertices.size() - 1);
        Vertex end = vertices.get(0);
        drawLine(renderer, Vec3.toEuclid(start), Vec3.toEuclid(end), start.color, end.color, antialiased);

        for (int i = 1; i < vertices.size(); i++) {
            start = vertices.get(i - 1);
            end = vertices.get(i);
            drawLine(renderer, Vec3.toEuclid(start), Vec3.toEuclid(end), start.color, end.color, antialiased);
        }
    }


    public static void drawPolygonFilled(Renderer renderer, List<Vertex> vs) {
        if (vs.size() < 3) {
            return;
        }

        Vector<Vertex> vertices = new Vector<>();
        for (Vertex v : vs) {
            Vec3 vec = Vec3.toEuclid(v);
            vertices.add(new Vertex(new Vec4(
                    (int) Math.round(vec.x),
                    (int) Math.round(vec.y),
                    vec.z
            ), v.color, v.normal, v.worldPosition));
        }

        //find top and bottom vertices
        int iTop = 0;
        int iBtm = 0;
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).y < vertices.get(iTop).y) {
                iTop = i;
            } else if (vertices.get(i).y >= vertices.get(iBtm).y) {
                iBtm = i;
            }
        }

        //construct left and right chains
        Vector<Vertex> leftChain = new Vector<>();
        Vector<Vertex> rightChain = new Vector<>();
        if (iTop < iBtm) {
            for (int i = iTop; i <= iBtm; i++) {
                leftChain.add(vertices.get(i));
            }
            for (int i = iTop; i >= 0; i--) {
                rightChain.add(vertices.get(i));
            }
            for (int i = vertices.size() - 1; i >= iBtm; i--) {
                rightChain.add(vertices.get(i));
            }
        } else if (iTop > iBtm) {
            for (int i = iTop; i < vertices.size(); i++) {
                leftChain.add(vertices.get(i));
            }
            for (int i = 0; i <= iBtm; i++) {
                leftChain.add(vertices.get(i));
            }
            for (int i = iTop; i >= iBtm; i--) {
                rightChain.add(vertices.get(i));
            }
        }

        //horizontal scan
        int iLeft = 1;
        int iRight = 1;

        for (double y = leftChain.get(0).y; y <= leftChain.get(leftChain.size() - 1).y; y++) {
            if (y > leftChain.get(iLeft).y) {
                iLeft++;
            }
            if (y > rightChain.get(iRight).y) {
                iRight++;
            }

            Vertex lStart = leftChain.get(iLeft - 1);
            Vertex lEnd = leftChain.get(iLeft);
            Vertex rStart = rightChain.get(iRight - 1);
            Vertex rEnd = rightChain.get(iRight);

            double lx;
            if (lEnd.x - lStart.x != 0 && lEnd.y - lStart.y != 0) {
                double lm = (lEnd.y - lStart.y) / (lEnd.x - lStart.x);
                double lb = lStart.y - lm * lStart.x;
                lx = (y - lb) / lm;
            } else {
                lx = lEnd.x;
            }

            double rx;
            if (rEnd.x - rStart.x != 0 && rEnd.y - rStart.y != 0) {
                double rm = (rEnd.y - rStart.y) / (rEnd.x - rStart.x);
                double rb = rStart.y - rm * rStart.x;
                rx = (y - rb) / rm;
            } else {
                rx = rEnd.x;
            }

            double lZ = Utility.lerpZ_PC(new Vec2(lx, y), lStart, lEnd);
            Vec3 lP = new Vec3(lx, y, lZ);
            Color lC = Utility.lerpColor_PC(lStart.color, lEnd.color, lZ, lStart.z, lEnd.z);
            Vec3 lN = Utility.lerpNormal_PC(lStart.normal, lEnd.normal, lZ, lStart.z, lEnd.z);
            Vec3 lWP = Utility.lerpPosition_PC(lStart.worldPosition, lEnd.worldPosition, lZ, lStart.z, lEnd.z);

            double rZ = Utility.lerpZ_PC(new Vec2(rx, y), rStart, rEnd);
            Vec3 rP = new Vec3(rx, y, rZ);
            Color rC = Utility.lerpColor_PC(rStart.color, rEnd.color, rZ, rStart.z, rEnd.z);
            Vec3 rN = Utility.lerpNormal_PC(rStart.normal, rEnd.normal, rZ, rStart.z, rEnd.z);
            Vec3 rWP = Utility.lerpPosition_PC(rStart.worldPosition, rEnd.worldPosition, rZ, rStart.z, rEnd.z);


            if (lx < rx) {
                for (int x = (int) Math.round(lx); x < (int) Math.round(rx); x++) {
                    double z = Utility.lerpZ_PC(new Vec2(x, y), lP, rP);
                    Vec3 p = new Vec3(x, y, z);
                    Color c = Utility.lerpColor_PC(lC, rC, z, lZ, rZ);
                    Vec3 n = Utility.lerpNormal_PC(lN, rN, z, lZ, rZ);
                    Vec3 wp = Utility.lerpPosition_PC(lWP, rWP, z, lZ, rZ);
                    Pixel pix = new Pixel(p, c, n, wp);
                    renderer.queuePixel(pix);
                }
            } else {
                for (int x = (int) Math.round(rx); x < (int) Math.round(lx); x++) {
                    double z = Utility.lerpZ_PC(new Vec2(x, y), rP, lP);
                    Vec3 p = new Vec3(x, y, z);
                    Color c = Utility.lerpColor_PC(rC, lC, z, rZ, lZ);
                    Vec3 n = Utility.lerpNormal_PC(rN, lN, z, rZ, lZ);
                    Vec3 wp = Utility.lerpPosition_PC(rWP, lWP, z, rZ, lZ);
                    Pixel pix = new Pixel(p, c, n, wp);
                    renderer.queuePixel(pix);
                }
            }
        }
    }
}
