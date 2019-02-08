import javax.swing.*;
import java.awt.*;

public class bmpPanel extends JPanel{

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;
    private static int mode = 0;
    private static double[][] ORDERED_DITHERING = new double[2][2];

    bmpPanel(int image_width, int image_height, Pixel[][] pixel_buffer, int mode) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;
        this.mode = mode;

        // fill 2x2 ordered dithering matrix
        ORDERED_DITHERING[0][0] = 0.19;
        ORDERED_DITHERING[0][1] = 0.43;
        ORDERED_DITHERING[1][0] = 0.69;
        ORDERED_DITHERING[1][1] = 0.23;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.white);

        drawBMP(g);
    }
    public void drawBMP(Graphics bmpPen) {
        int red, green, blue;
        double y, norm_y;
        int index_x, index_y;
        double dithering;
        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {
                // original pixels
                red = pixel_buffer[i][j].getRed();
                green = pixel_buffer[i][j].getGreen();
                blue = pixel_buffer[i][j].getBlue();
                // page 3: 1.5 times brighter
                if(mode % 5 == 2) {
                    red = (int)(pixel_buffer[i][j].getRed() * 1.5);
                    green = (int)(pixel_buffer[i][j].getGreen() * 1.5);
                    blue = (int)(pixel_buffer[i][j].getBlue() * 1.5);

                    if(red > 255) {
                        red = 255;
                    }

                    if(green > 255) {
                        green = 255;
                    }

                    if(blue > 255) {
                        blue = 255;
                    }
                }
                // page 4: gray-scale of original image
                else if(mode % 5 == 3) {
                    y = 0.299 * red + 0.587 * green + 0.114 * blue;
                    red = (int)y;
                    green = (int)y;
                    blue = (int)y;
                }
                // page 5: ordered dithering image
                else if(mode % 5 == 4) {
                    // intensity level used to compare with dithering matrix item
                    y = 0.299 * red + 0.587 * green + 0.114 * blue;
                    norm_y = y / 255;
                    // pixel location: (j, image_height - i)
                    index_x = j % 2;
                    index_y = (image_height - i) % 2;
                    dithering = ORDERED_DITHERING[index_x][index_y];
                    // print a dot
                    if(norm_y > dithering) {
                        red = 0;
                        green = 0;
                        blue = 0;
                    }
                    else {
                        red = 255;
                        green = 255;
                        blue = 255;
                    }
                }
                Color color = new Color(red, green, blue);
                bmpPen.setColor(color);
                // render pixel
                bmpPen.fillRect(j, image_height - i, 1, 1);
            }
        }
    }
}
