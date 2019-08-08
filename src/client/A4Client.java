package client;

import components.*;
import windowing.Drawable;
import windowing.PageTurner;

import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by alex on 1/15/2017.
 */
public class A4Client implements PageTurner {

    Vector<Page> pages = new Vector<>();
    int pageNumber = 0;
    Renderer renderer = new Renderer();

    public A4Client(Drawable drawable, List<String> args) {
        this.pages.add(new CanvasPage(drawable, (List<DisplayArea> sectors) -> {
            Vector<String[]> commands = Utility.loadCommandsFromFile(args.get(0));

            renderer.setDisplayArea(sectors.get(0));
            renderer.clearDisplayArea(new Color(0, 0,0));

            Utility.renderCommandList(renderer, commands);
        }));
    }

    @Override
    public void nextPage() {
        System.out.println("Showing page: " + (pageNumber + 1));

        pages.get(pageNumber).draw();

        pageNumber++;
        pageNumber %= pages.size();
    }
}
