package components;

/**
 * Created by alex on 3/6/2017.
 */
public class Camera {

    public final ClipSpace clipSpace;
    public final Mat4x4 csTransform;
    public final Vec4 position;

    public Camera(double xL, double yL, double xH, double yH, double near, double far, Mat4x4 transform, Vec4 position){
        this.clipSpace = new ClipSpace(
                new Vec2(xL, yL),
                new Vec2(xH - xL, yH - yL),
                near,
                far);
        this.csTransform = transform;
        this.position = position;
    }
}
