// AUTHOR: YUHAO HE
// DATE: 2018.8.2
// DESCRIPTION: The program applies LOSSY compression to BMP file.
// When read BMP file, the program compresses and generate IM3 file
// WHEN read IM3 file, the program decompresses the file and displays the image

import javax.swing.JFileChooser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class bmpHeaderReader {

    public static String file_path;
    public static InputStream bmpBytes;
    public static int[] BYTE_BUFFER = new int[10000*10000];
    // the number of bytes contained in bmp file
    public static int BMP_SIZE = 0;

    // bmp header
    public static String[] HEADER_INFO = {"SIGNATURE", "FILE_SIZE", "RESERVE_1", "RESERVE_2", "OFFSET_TO_PIXELS",
    "DIB_HEADER_SIZE", "IMAGE_WIDTH", "IMAGE_HEIGHT", "NUM_PLANES", "NUM_BITS_PER_PIXEL"};
    // header offset
    public static int[] HEADER_OFFSET = {2, 4, 2, 2, 4, 4, 4, 4, 2, 2};
    // the buffer stores the hex code of each section
    public static String[] HEX_INFO = new String[HEADER_OFFSET.length];

    public static int OFFSET_TO_PIXELS = 0;
    public static int IMAGE_WIDTH = 0;
    public static int IMAGE_HEIGHT = 0;
    public static int NUM_BITS_PER_PIXEL = 0;
    public static int RGB_BYTE = 0;

    // runtime
    public static double start_time = 0;
    public static double start_time_decode = 0;

    public static void main(String[] args) {

        file_path = readBmp();

        // check if the input file is bmp or IN3
        String extension = file_path.substring(file_path.lastIndexOf(".") + 1);
        // System.out.println("Extension is: " + extension);

        // if bmp file
        if(extension.equals("bmp")) {

            start_time = System.currentTimeMillis();

            System.out.println("Reading BMP file ...\n");
            // store the file data into inputStream
            storeDataStream();

            // read the pixel array
            readPixels();
        }
        // if IN3 file
        else {
            System.out.println("Reading IM3 file ...\n");

            start_time_decode = System.currentTimeMillis();
            // reading IN3 file
            IM3Reader read = new IM3Reader(file_path);
        }
    }
    // read .bmp file and return the file_path for processing
    private static String readBmp() {
        JFileChooser dialog = new JFileChooser();
        String bmp_path = "";
        // set the properties for the file chooser
        // set the default directory as project workspace
        dialog.setCurrentDirectory(new java.io.File("."));
        dialog.setDialogTitle("Please choose the .wav file you want to open");

        if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

        }
        try {
            // print out the absolute path of .wav file and return it
            bmp_path = dialog.getSelectedFile().getAbsolutePath();

        } catch(NullPointerException e) {
            System.exit(0);
        }
        System.out.println("The path of bmp file is: " + bmp_path);
        return bmp_path;
    }

    // store data bytes
    private static void storeDataStream() {
        // store the byte array
        // .read() reads the value of the byte, NOT in hex!!
        try(InputStream wavStream = new FileInputStream(file_path)) {
            bmpBytes = wavStream;
            //System.out.println("The first byte of bmp file is: " + bmpBytes.read());
            storeBytes();

        } catch(IOException exception) {
            System.out.println("The bmp file is empty!");
        }
    }
    // put bytes into buffer
    private static void storeBytes() {
        int numBytes = 0;
        int buffer_index = 0;
        try {
            int read_byte = bmpBytes.read();

            while(read_byte != -1) {
                numBytes += 1;
                BYTE_BUFFER[buffer_index] = read_byte;
                buffer_index += 1;
                read_byte = bmpBytes.read();
            }
        }
        catch(IOException e) {
            System.out.println("The byteStream is empty!");
        }
        BMP_SIZE = numBytes;
        System.out.println("The total number of bytes is: " + BMP_SIZE);

        // extract the header info out of buffer
        extractInfo();
    }

    private static void extractInfo() {
        int pos = -1;
        int bound = 0;
        String temp_hex;
        for(int i = 0; i < HEADER_OFFSET.length; i ++) {
            pos += HEADER_OFFSET[i];
            // reverse and concatenate hex string
            temp_hex = hexReverse(bound, pos);
            bound = pos + 1;
            HEX_INFO[i] = temp_hex;
        }
        // print header info and decode hex
        int hex_decode;
        for(int i = 0; i < HEADER_OFFSET.length; i ++) {
            hex_decode = Integer.decode(HEX_INFO[i]);

            if(HEADER_INFO[i].equals("OFFSET_TO_PIXELS") == true) {
                OFFSET_TO_PIXELS = hex_decode;
            }
            else if(HEADER_INFO[i].equals("IMAGE_WIDTH") == true) {
                IMAGE_WIDTH = hex_decode;
            }
            else if(HEADER_INFO[i].equals("IMAGE_HEIGHT") == true) {
                IMAGE_HEIGHT = hex_decode;
            }
            else if(HEADER_INFO[i].equals("NUM_BITS_PER_PIXEL") == true) {
                NUM_BITS_PER_PIXEL = hex_decode;
            }
            System.out.println(HEADER_INFO[i] + ":      " + hex_decode);
        }
        //System.out.println("The offset to pixel array is: " + OFFSET_TO_PIXELS);
    }
    // reverse and concatenate hex string given the start and end position
    public static String hexReverse(int start, int end) {
        String temp_hex = "0x";
        // reverse the bytes
        for(int j = end; j >= start; j --) {
            // convert to 2-digit hex
            temp_hex += String.format("%02x", BYTE_BUFFER[j]);
        }
        return temp_hex;
    }
    // read pixel array
    private static void readPixels() {
        bmpPixelReader pixelReader = new bmpPixelReader(OFFSET_TO_PIXELS, IMAGE_WIDTH, IMAGE_HEIGHT, NUM_BITS_PER_PIXEL
        , BYTE_BUFFER);
        // if 24-color image
        if(NUM_BITS_PER_PIXEL == 24) {
            // 1 byte for each color of RGB
            RGB_BYTE = (NUM_BITS_PER_PIXEL / 3) / 8;
        }
        pixelReader.storePixels(RGB_BYTE);
    }
}
