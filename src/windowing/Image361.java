package windowing;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Image361 extends ImageView implements Drawable {
	public static final int IMAGE_X = 750;
	public static final int IMAGE_Y = 750;
	
	private WritableImage image;
	private PixelReader pixelReader;
	private PixelWriter pixelWriter;
	

	public Image361() {
		image = new WritableImage(IMAGE_X, IMAGE_Y);
		setImage(image);
		
		pixelReader = image.getPixelReader();
		pixelWriter = image.getPixelWriter();
	}
	
	
	@Override
	public void setPixel(int x, int y, int color) {
		pixelWriter.setArgb(x, y, color);
	}

	@Override
	public int getPixel(int x, int y) {
		return pixelReader.getArgb(x, y);
	}

}
