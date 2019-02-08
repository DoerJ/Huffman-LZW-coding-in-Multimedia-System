import javax.swing.*;
import java.awt.*;

public class Container {

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;
    private static JFrame frame;

    private static bmpPanel BMP_PANEL;

    public int call;

    Container(int image_width, int image_height, Pixel[][] pixel_buffer, int call) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;
        this.call = call;

        // the height of title bar
        int title_bar = 23;

        // set up bmp panel
        BMP_PANEL = new bmpPanel(image_width, image_height, pixel_buffer, call);

        frame = new JFrame("THE BMP IMAGE");
        frame.setSize(image_width, image_height + title_bar);
        frame.add(BMP_PANEL, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
