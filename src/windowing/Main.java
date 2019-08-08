package windowing;

	// you can rewrite main.java and client.java and add more files.
	// Leave RenderArea.java and Window.java unchanged.
	// Change Drawable and PageTurner if you like, but know that renderarea
	// (which you cannot change) IS_A Drawable, and window361 depends on
	// PageTurner. If you need a different interface to Drawable, it will be
	// better to make a separate DrawableTwo (or whatever) interface.

	// do not instantiate Canvas361 yourself.

import client.A4Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
        launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		Window361 window = new Window361(primaryStage);
		Drawable drawable = window.getDrawable();
		A4Client client = new A4Client(drawable, getParameters().getRaw());
		window.setPageTurner(client);
		primaryStage.show();
	}

}
