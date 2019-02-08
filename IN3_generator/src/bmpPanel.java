import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class bmpPanel extends JPanel{

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;

    // list for RGB symbols
    public List<String> R_SYMBOLS;
    public List<String> G_SYMBOLS;
    public List<String> B_SYMBOLS;

    // call
    public static int call;


    bmpPanel(int image_width, int image_height, Pixel[][] pixel_buffer, int call) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;
        this.call = call;

        R_SYMBOLS = new ArrayList<>();
        G_SYMBOLS = new ArrayList<>();
        B_SYMBOLS = new ArrayList<>();

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setBackground(Color.white);

        // drawBMP() is called twice
        drawBMP(g);
    }

    public void drawBMP(Graphics bmpPen) {
        int red, green, blue;

        call += 1;

        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {
                // original pixels
                red = pixel_buffer[i][j].getRed();

                green = pixel_buffer[i][j].getGreen();

                blue = pixel_buffer[i][j].getBlue();

                Color color = new Color(red, green, blue);
                bmpPen.setColor(color);
                // render pixel
                bmpPen.fillRect(j, image_height - i, 1, 1);

                // BUG FIXED!!
                if(call == 2) {
                    R_SYMBOLS.add(Integer.toString(red));
                    G_SYMBOLS.add(Integer.toString(green));
                    B_SYMBOLS.add(Integer.toString(blue));
                }
            }
        }
        // access the encoder at the second call
        if(call == 2) {
            // convert list to string array
            String[] r_symbols = R_SYMBOLS.toArray(new String[R_SYMBOLS.size()]);
            HuffmanEncoder r_encoder = new HuffmanEncoder(r_symbols, image_width, image_height);

            String[] g_symbols = G_SYMBOLS.toArray(new String[G_SYMBOLS.size()]);
            HuffmanEncoder g_encoder = new HuffmanEncoder(g_symbols, image_width, image_height);

            String[] b_symbols = B_SYMBOLS.toArray(new String[B_SYMBOLS.size()]);
            HuffmanEncoder b_encoder = new HuffmanEncoder(b_symbols, image_width, image_height);
        }
    }
}
