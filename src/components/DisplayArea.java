package components;

import windowing.Drawable;

/**
 * Created by alex on 1/15/2017.
 */
public class DisplayArea {

    final Drawable drawable;
    final Vec2I start;
    final Vec2I dim;

    public DisplayArea(Drawable drawable, Vec2I start, Vec2I dim) {
        this.drawable = drawable;
        this.start = start;
        this.dim = dim;
    }

    public Vec2I getStartPoint(){
        return this.start;
    }

    public Vec2I getDimensions(){
        return this.dim;
    }

    public void setPixel(Vec2I point, Color color) {
        if (point.x < 0 || point.y < 0 || point.x >= this.dim.x || point.y >= this.dim.y) {
            return;
        }

        Vec2I translatedPt = new Vec2I(point.x + start.x, point.y + start.y);
        Color finalColor = color;
        if(color.a != 1.0f){
            finalColor = color.blend(getPixel(point));
        }
        drawable.setPixel(translatedPt.x, translatedPt.y, finalColor.toInt());
    }

    public Color getPixel(Vec2I p){
        Vec2I translatedPt = new Vec2I(p.x + start.x, p.y + start.y);
        int color;
        try{
            color = drawable.getPixel(translatedPt.x, translatedPt.y);
        }catch(IndexOutOfBoundsException e){
            color = 0;
        }
        return Color.fromInt(color);
    }


    public void fill(Color color) {
        for (int x = 0; x < this.dim.x; ++x) {
            for (int y = 0; y < this.dim.y; ++y) {
                setPixel(new Vec2I(x, y), color);
            }
        }
    }
}
