package components;

/**
 * Created by alex on 1/15/2017.
 */
public class Color {

    public final int r;
    public final int g;
    public final int b;
    public final float a;

    public Color(int r, int g, int b){
        this(r, g, b, 1);
    }

    public Color(int r, int g, int b, float a){
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int toInt() {
        return (0xff << 24)
                + ((this.r & 0xff) << 16)
                + ((this.g & 0xff) << 8)
                + (this.b & 0xff);
    }

    public Color blend(Color existing){
        return blend(existing, this.a);
    }

    public Color blend(Color existing, float opacity){
        int r = Math.round(opacity * this.r  + (1 - opacity)* existing.r);
        int g = Math.round(opacity * this.g  + (1 - opacity)* existing.g);
        int b = Math.round(opacity * this.b  + (1 - opacity)* existing.b);
        return new Color(r, g, b);
    }

    public static Color fromInt(int color){
        String hex = Integer.toHexString(color);
        int r = (color & 0x00ff0000) >> 16;
        int g = (color & 0x0000ff00) >> 8;
        int b = color & 0x000000ff;
        return new Color(r,g,b);
    }
}
