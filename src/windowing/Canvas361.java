package windowing;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Canvas361 extends Canvas implements Drawable {
	public static final int CANVAS_X = 750;
	public static final int CANVAS_Y = 750;

	public Canvas361() {
		super(CANVAS_X, CANVAS_Y);
		paintCanvas();
		requestFocus();
	}

	public void setPixel(int x, int y, int color) {
		GraphicsContext gc = getGraphicsContext2D();
		PixelWriter writer = gc.getPixelWriter();
		writer.setArgb(x, y, color);
	}
	public int getPixel(int x, int y) {
		WritableImage image = snapshot(null, null);			// slow! makes an image just to get a pixel. :(
		PixelReader reader = image.getPixelReader();
		return reader.getArgb(x, y);
	}

	public void paintCanvas() {
		drawSquare(0, 0, CANVAS_X, CANVAS_Y, 0xffff0080);
	}
	public void drawSquare(int x1, int y1, int x2, int y2, int color) {
		for(int x=x1; x<x2; x++) {
			for(int y=y1; y<y2; y++) {
				setPixel(x, y, color);
			}
		}
	}
}

