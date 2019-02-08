import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class histogramPanel extends JPanel{
    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;
    // the largest number of pixels at certain color level among all channels
    private static int largest_color = 0;
    // the transparency of histogram
    private static float alpha = 0.4f;

    // histogram array
    private static int[] histogram_red = new int[256];
    private static int[] histogram_green = new int[256];
    private static int[] histogram_blue = new int[256];

    histogramPanel(int image_width, int image_height, Pixel[][] pixel_buffer) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;

        // fill histogram arrays with all 0
        Arrays.fill(histogram_red, 0);
        Arrays.fill(histogram_green, 0);
        Arrays.fill(histogram_blue, 0);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.white);

        fillHistogramArray();

        // for purpose of normalization, get the largest item in each histogram array
        searchTheLargest();

        // normalize histogram values
        normHistogram();

        // if image_width < 256, compress the histogram horizontally
        if(image_width < 256) {
            drawCompressedHistogram(g);
        }
        // draw histogram for each of RGB channels
        else {
            drawColorHistogram(g);
        }
    }
    // get the largest number of pixels for each RGB channels
    private void searchTheLargest() {
        int red_num = 0;
        int green_num = 0;
        int blue_num = 0;
        for(int i = 0; i < histogram_red.length; i ++) {
            if(histogram_red[i] > red_num) {
                red_num = histogram_red[i];
            }
            if(histogram_green[i] > green_num) {
                green_num = histogram_green[i];
            }
            if(histogram_blue[i] > blue_num) {
                blue_num = histogram_blue[i];
            }
        }
        int[] color_num = {red_num, green_num, blue_num};
        for(int j = 0; j < color_num.length; j ++) {
            if(color_num[j] > largest_color) {
                largest_color = color_num[j];
            }
        }
    }

    public void fillHistogramArray() {
        int red, green, blue;
        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {

                red = pixel_buffer[i][j].getRed();
                histogram_red[red] += 1;

                green = pixel_buffer[i][j].getGreen();
                histogram_green[green] += 1;

                blue = pixel_buffer[i][j].getBlue();
                histogram_blue[blue] += 1;
            }
        }
    }
    private void normHistogram() {
        // normalize: (# of pixels at color value / total # of pixels) * image_height
        int temp_red, temp_green, temp_blue;
        for(int i = 0; i < histogram_red.length; i ++) {
            temp_red = histogram_red[i];
            temp_green = histogram_green[i];
            temp_blue = histogram_blue[i];

            //System.out.println("The temp_red is: " + temp_red);

            histogram_red[i] = (int)(((double)temp_red / (double)largest_color) * image_height);
            histogram_green[i] = (int)(((double)temp_green / (double)largest_color) * image_height);
            histogram_blue[i] = (int)(((double)temp_blue / (double)largest_color) * image_height);

            //System.out.println("The histogram red is: " + histogram_red[i]);
        }
    }
    private void drawColorHistogram(Graphics pen) {

        pen.setColor(new Color(1, 0, 0, alpha));
        for(int i = 0; i < histogram_red.length; i ++) {
            pen.drawLine(i, image_height, i, image_height - histogram_red[i]);
        }
        pen.setColor(new Color(0, 1, 0, alpha));
        for(int i = 0; i < histogram_green.length; i ++) {
            pen.drawLine(i, image_height, i, image_height - histogram_green[i]);
        }
        pen.setColor(new Color(0, 0, 1, alpha));
        for(int i = 0; i < histogram_blue.length; i ++) {
            pen.drawLine(i, image_height, i, image_height - histogram_blue[i]);
        }
    }
    private void drawCompressedHistogram(Graphics pen) {

        double width_per_colorValue = (double)image_width / (double)256;
        double x_pos = 0.0;
        pen.setColor(new Color(1, 0, 0, alpha));
        int i = 0;
        while(i < histogram_red.length) {
            pen.drawLine((int)Math.round(x_pos), image_height, (int)Math.round(x_pos), image_height - histogram_red[i]);
            x_pos += width_per_colorValue;
            i += 1;
        }
        pen.setColor(new Color(0, 1, 0, alpha));
        i = 0;
        x_pos = 0.0;
        while(i < histogram_green.length) {
            pen.drawLine((int)Math.round(x_pos), image_height, (int)Math.round(x_pos), image_height - histogram_green[i]);
            x_pos += width_per_colorValue;
            i += 1;
        }
        pen.setColor(new Color(0, 0, 1, alpha));
        i = 0;
        x_pos = 0.0;
        while(i < histogram_blue.length) {
            pen.drawLine((int)Math.round(x_pos), image_height, (int)Math.round(x_pos), image_height - histogram_blue[i]);
            x_pos += width_per_colorValue;
            i += 1;
        }
    }
}
