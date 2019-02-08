import javax.swing.*;
import java.awt.*;

public class Container {

    private static int image_width;
    private static int image_height;
    private static Pixel[][] pixel_buffer;
    private static JFrame frame;

    private static bmpPanel BMP_PANEL;

    // title bar
    public int title_bar;

    // block array
    public static Block[] r_blocks;
    public static Block[] g_blocks;
    public static Block[] b_blocks;

    // mode
    public int mode;

    Container(int image_width, int image_height, Pixel[][] pixel_buffer, int mode) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.pixel_buffer = pixel_buffer;

        title_bar = 23;

        // mode
        this.mode = mode;

        r_blocks = new Block[(image_width * image_height) / 64];
        g_blocks = new Block[(image_width * image_height) / 64];
        b_blocks = new Block[(image_width * image_height) / 64];

        // set up bmp panel
        BMP_PANEL = new bmpPanel(image_width, image_height, pixel_buffer);

        frame = new JFrame("THE BMP IMAGE");
        frame.setSize(image_width, image_height + title_bar);
        frame.add(BMP_PANEL, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // if DCT compress
        if(mode == 0) {
            // building block
            buildingBlocks();

            // apply DCT
            dctTransformation r_dct = new dctTransformation(r_blocks, image_width, image_height);
            dctTransformation g_dct = new dctTransformation(g_blocks, image_width, image_height);
            dctTransformation b_dct = new dctTransformation(b_blocks, image_width, image_height);
        }
    }

    // building blocks using pixel buffer
    public void buildingBlocks() {

        int block_index = 0;
        int offset = image_width / 8;
        int temp = 0 - offset;
        int current_index = -1;

        for(int i = 0; i < image_height; i ++) {
            if(i % 8 == 0) {
                temp += offset;
            }
            for(int j = 0; j < image_width; j ++) {
                if(j % 8 == 0 && i % 8 == 0) {

                    Block r_block = new Block();
                    r_blocks[block_index] = r_block;

                    Block g_block = new Block();
                    g_blocks[block_index] = g_block;

                    Block b_block = new Block();
                    b_blocks[block_index] = b_block;

                    block_index += 1;
                }
                if(j % 8 == 0) {
                    current_index += 1;
                }
                // fill up value
                r_blocks[current_index].setBlockValue(i % 8, j % 8, (double)pixel_buffer[i][j].getRed());
                g_blocks[current_index].setBlockValue(i % 8, j % 8, (double)pixel_buffer[i][j].getGreen());
                b_blocks[current_index].setBlockValue(i % 8, j % 8, (double)pixel_buffer[i][j].getBlue());
            }
            current_index = temp - 1;
        }
    }
}
