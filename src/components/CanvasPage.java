package components;

import windowing.Drawable;

import java.util.Vector;

/**
 * Created by alex on 2/9/2017.
 */
public class CanvasPage implements Page {

    DisplayArea bgSector;
    Vector<DisplayArea> sectors = new Vector<>();
    DrawingStrategy strategy;

    public CanvasPage(Drawable drawable, DrawingStrategy strategy){
        bgSector = new DisplayArea(drawable, new Vec2I(0,0), new Vec2I(750,750));
        this.sectors.add( new DisplayArea(drawable, new Vec2I(50,50), new Vec2I(650,650)));
        this.strategy = strategy;
    }

    @Override
    public void draw() {
        bgSector.fill(new Color(255, 255, 255));
        for(int i = 0; i < sectors.size(); ++i){
            sectors.get(i).fill(new Color(0,0,0));
        }
        this.strategy.draw(sectors);
    }
}
