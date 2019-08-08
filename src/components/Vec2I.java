package components;

/**
 * Created by alex on 1/15/2017.
 */
public class Vec2I {

    public final int x;
    public final int y;

    public Vec2I(int x, int y){
        this.x = x;
        this.y = y;
    }

    public boolean equals(Vec2I other) {
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public String toString(){
        return "Vec2I{ x=" + this.x +
                ", y=" + this.y + "}";
    }
}
