import javax.swing.*;
import java.awt.*;

public class bmpPanel extends JPanel{

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;

    bmpPanel(int image_width, int image_height, Pixel[][] pixel_buffer) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.white);

        drawBMP(g);
    }
    public void drawBMP(Graphics bmpPen) {
        int red, green, blue;
        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {
                // original pixels
                red = pixel_buffer[i][j].getRed();

                // do for r channel

                green = pixel_buffer[i][j].getGreen();

                blue = pixel_buffer[i][j].getBlue();

                Color color = new Color(red, green, blue);
                bmpPen.setColor(color);
                // render pixel
                bmpPen.fillRect(j, image_height - i, 1, 1);
            }
        }
    }
}
