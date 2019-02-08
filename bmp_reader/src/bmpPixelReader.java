
public class bmpPixelReader {

    private static int image_width;
    private static int image_height;
    private static int offset;
    private static int bits_per_pixel;
    // buffer that stores all the bytes of bmp file
    private static int[] byte_buffer;
    private static Pixel[][] PIXEL_BUFFER;

    bmpPixelReader(int offset, int image_width, int image_height, int bits_per_pixel, int[] byte_buffer) {
        this.offset = offset;
        this.image_width = image_width;
        this.image_height = image_height;
        this.bits_per_pixel = bits_per_pixel;
        this.byte_buffer = byte_buffer;
    }

    public void storePixels(int color_byte) {
        System.out.println("The image size is: " + image_width + " x " + image_height);
        System.out.println("The RGB byte is: " + color_byte);
        bmpHeaderReader headerReader = new bmpHeaderReader();
        PIXEL_BUFFER = new Pixel[image_height][image_width];

        // the row size in bytes
        int rowSize = (int)((bits_per_pixel * image_width + 31.0) / 32.0);
        rowSize = rowSize * 4;
        int bytes_per_pixel = bits_per_pixel / 8;

        // end point
        int pos = offset - 1;
        int row_start = offset - 1;
        // start point
        int bound = offset;
        String temp_hex;
        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {
                pos += bytes_per_pixel;
                // hex string RGB (x x x)
                temp_hex = headerReader.hexReverse(bound, pos);
                // cut off "0x"
                temp_hex = temp_hex.substring(2);
                generatePixel(temp_hex, i, j);
                bound = pos + 1;
            }
            row_start += rowSize;
            pos = row_start;
            bound = pos + 1;
        }
        // draw bmp image
        drawPanel();
    }
    private static void generatePixel(String colorHex, int row, int col) {
        //split into R, G, B hex code
        // x(R) x(G) x(B)
        int red = Integer.parseInt(colorHex.substring(0, 2), 16);
        int green = Integer.parseInt(colorHex.substring(2, 4), 16);
        int blue = Integer.parseInt(colorHex.substring(4), 16);

        Pixel pixel = new Pixel();
        pixel.setRed(red);
        pixel.setGreen(green);
        pixel.setBlue(blue);
        PIXEL_BUFFER[row][col] = pixel;
    }
    // draw bmp image
    public static void drawPanel() {

        Container panel = new Container(image_width, image_height, PIXEL_BUFFER);
    }
}
