import java.io.DataInputStream;
import java.io.IOException;
import java.io.FileInputStream;

public class IN3Reader {

    public static String file_path;
    public static DataInputStream in3_stream;

    // header info
    public static int image_width;
    public static int image_height;
    public static int r_offset;
    public static int g_offset;
    public static int b_offset;
    public static int r_codewordSize;
    public static int g_codewordSize;
    public static int b_codewordSize;

    // symbols & frequency (used for reconstructing Huffman tree)
    public static int[] r_symbols;
    public static int[] g_symbols;
    public static int[] b_symbols;
    public static int[] r_frequency;
    public static int[] g_frequency;
    public static int[] b_frequency;

    // r, g, b codeword stream bytes
    public static int r_bytes, g_bytes, b_bytes;

    IN3Reader(String file_path) {
        this.file_path = file_path;

        // read the header of IN3
        readingHeader();
    }

    // read header
    public void readingHeader() {
        try {
            in3_stream = new DataInputStream(new FileInputStream(file_path));
            image_width = in3_stream.readInt();
            image_height = in3_stream.readInt();
            System.out.println("image size: " + image_width + " * " + image_height);

            r_offset = in3_stream.readInt();
            //System.out.println("r_offset is: " + r_offset + "\n");

            // store R symbols and frequency
            r_symbols = new int[r_offset];
            r_frequency = new int[r_offset];
            System.out.println("Decoding table of R channel: ");
            for(int i = 0; i < r_offset; i ++) {
                r_symbols[i] = in3_stream.readInt();
                r_frequency[i] = in3_stream.readInt();
                System.out.println("symbol: " + r_symbols[i] + " | " + "frequency: " + r_frequency[i]);
            }

            g_offset = in3_stream.readInt();

            // store G symbols and frequency
            g_symbols = new int[g_offset];
            g_frequency = new int[g_offset];
            System.out.println("\nDecoding table of G channel: ");
            for(int i = 0; i < g_offset; i ++) {
                g_symbols[i] = in3_stream.readInt();
                g_frequency[i] = in3_stream.readInt();
                System.out.println("symbol: " + g_symbols[i] + " | " + "frequency: " + g_frequency[i]);
            }

            b_offset = in3_stream.readInt();

            // store B symbols and frequency
            b_symbols = new int[b_offset];
            b_frequency = new int[b_offset];
            System.out.println("\nDecoding table of B channel: ");
            for(int i = 0; i < b_offset; i ++) {
                b_symbols[i] = in3_stream.readInt();
                b_frequency[i] = in3_stream.readInt();
                System.out.println("symbol: " + b_symbols[i] + " | " + "frequency: " + b_frequency[i]);
            }

            // reading codeword stream size
            r_codewordSize = in3_stream.readInt();
            g_codewordSize = in3_stream.readInt();
            b_codewordSize = in3_stream.readInt();

            // reading codeword streams
            readingCodewordStream();

        } catch (IOException e) {
            System.out.println("The IN3 file is empty.");
        }
    }

    // reading IN3 codeword streams
    public void readingCodewordStream() {

        System.out.println("\nReading RGB codeword stream ...");

        // define the number of bytes for each codeword stream
        if(r_codewordSize % 8 != 0) {
            r_bytes = (r_codewordSize / 8) + 1;
        }
        else {
            r_bytes = r_codewordSize / 8;
        }

        if(g_codewordSize % 8 != 0) {
            g_bytes = (g_codewordSize / 8) + 1;
        }
        else {
            g_bytes = g_codewordSize / 8;
        }

        if(b_codewordSize % 8 != 0) {
            b_bytes = (b_codewordSize / 8) + 1;
        }
        else {
            b_bytes = b_codewordSize / 8;
        }

        try {
            // do for R channel
            byte[] r_getByte = new byte[r_bytes];
            in3_stream.read(r_getByte, 0, r_bytes);

            // do for G channel
            byte[] g_getByte = new byte[g_bytes];
            in3_stream.read(g_getByte, 0, g_bytes);

            // do for B channel
            byte[] b_getByte = new byte[b_bytes];
            in3_stream.read(b_getByte, 0, b_bytes);

            // decoding
            HuffmanDecoder in3_decoder = new HuffmanDecoder(image_width, image_height, r_symbols, g_symbols,
                    b_symbols, r_frequency, g_frequency, b_frequency, r_getByte, g_getByte, b_getByte);

        } catch (IOException e) {

        }
    }
}
