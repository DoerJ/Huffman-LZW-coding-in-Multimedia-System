// AUTHOR: YUHAO HE
// DATE: 2018.8.2
// DESCRIPTION: The program utilizes LZW encoding techniques to
// compress WAV file, and displays the compression ratio in console

import javax.swing.JFileChooser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class wavReader {
    public static String file_path = "";
    public static InputStream wavBytes;
    public static int[] byte_buffer = new int[10000*10000];
    // in term of number of bytes contained in wave file
    public static int wavSize = 0;

    // data samples
    public static int SAMPLE_BYTE = 0;
    public static int NUM_CHANNEL = 0;
    public static int NUM_FRAME = 0;

    // the size of sample data chunk in term of number of bytes
    public static int CHUNK_SIZE = 0;
    public static int SAMPLE_RATE = 0;
    public static int NUM_SAMPLE = 0;

    // the starting point of sample data chunk
    public static int POS_SAMPLE = 0;

    // [channel][sample_bytes]
    public static String[][] CHANNEL_SAMPLE;

    // default: 16 bits/sample
    public static double MAX_LIMIT = 32767;
    public static double MIN_LIMIT = -32768;

    public static String[] HEADER_INFO = {"CHUNK_ID", "CHUNK_SIZE", "FORMAT", "SUBCHUNK1_ID", "SUBCHUNK_SIZE",
            "AUDIO_FORMAT", "NUM_CHANNELS", "SAMPLE_RATE", "BYTE_RATE", "BLOCK_ALIGN", "BITS_PER_SAMPLE",
            "SUBCHUNK2_ID", "SUBCHUNK2_SIZE"};
    public static int[] HEADER_OFFSET = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
    public static String[] HEX_INFO = new String[HEADER_OFFSET.length];

    // symbols
    public static String[] SYMBOLS;

    // main function
    public static void main(String args[]) {
        //the absolute path of .wav file
        file_path = openWav();
        System.out.println("Reading WAV file ...\n");

        // find the .wav file (using absolute path) and store the file data into inputStream
        storeDataStream();

        // Huffman encoding
        System.out.println("Applying LZW encoding ...\n");
        LZWEncoder encoder = new LZWEncoder(SYMBOLS);
    }

    // open dialog for loading wav file
    private static String openWav() {
        JFileChooser dialog = new JFileChooser();
        String wav_path = "";
        // set the properties for the file chooser
        // set the default directory as project workspace
        dialog.setCurrentDirectory(new java.io.File("."));
        dialog.setDialogTitle("Please choose the WAV file you want to open");
        if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

        }
        try {
            // print out the absolute path of .wav file and return it
            wav_path = dialog.getSelectedFile().getAbsolutePath();
        } catch(NullPointerException e) {
            System.exit(0);
        }
        return wav_path;
    }

    // a .wav file is basically an array of bytes
    private static void storeDataStream() {
        // store the byte array
        // .read() reads the value of the byte, NOT in hex!!
        try(InputStream wavStream = new FileInputStream(file_path)) {
            wavBytes = wavStream;
            storeBytes();
        } catch(IOException exception) {
            System.out.println("The .wav file is empty!");
        }
    }

    // store each byte into byte buffer
    private static void storeBytes() {
        int numBytes = 0;
        int buffer_index = 0;
        try {
            int read_byte = wavBytes.read();

            while(read_byte != -1) {
                numBytes += 1;
                byte_buffer[buffer_index] = read_byte;
                buffer_index += 1;
                read_byte = wavBytes.read();
            }
        }
        catch(IOException e) {
            System.out.println("The byteStream is empty!");
        }
        wavSize = numBytes;

        // extract wave file info from buffer
        extractInfo();
    }

    // extract header info, bit/sample, sampling rate, and so on
    private static void extractInfo() {
        // reverse the bytes and convert into hex
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
        int hex_decode = 0;
        for(int i = 0; i < HEADER_OFFSET.length; i ++) {
            hex_decode = Integer.decode(HEX_INFO[i]);
            // record bytes per sample
            if(HEADER_INFO[i].equals("BITS_PER_SAMPLE") == true) {
                SAMPLE_BYTE = hex_decode / 8;
                // the upper and lower bound of sample value
                // n-bit signed integer ranges from: (-2^n) to (2^n - 1)
                MAX_LIMIT = Math.pow(2, SAMPLE_BYTE * 8 - 1) - 1;
                MIN_LIMIT = -MAX_LIMIT - 1;
            }
            // record number of channels
            else if(HEADER_INFO[i].equals("NUM_CHANNELS") == true) {
                NUM_CHANNEL = hex_decode;
            }
            // record the sampling rate (i.e., # of samples/sec)
            else if(HEADER_INFO[i].equals("SAMPLE_RATE") == true) {
                SAMPLE_RATE = hex_decode;
            }
            //System.out.println(HEADER_INFO[i] + ":      " + hex_decode);
        }
        // record the starting point of sample chunk
        POS_SAMPLE = pos + 1;

        // store the sample data into buffer
        storeSample();
    }

    // extract samples
    private static void storeSample() {
        // count the size of data chunk
        CHUNK_SIZE = wavSize - POS_SAMPLE;
        // calculate the number of frames
        NUM_FRAME = CHUNK_SIZE / (SAMPLE_BYTE * NUM_CHANNEL);
        // # of samples = # of frames
        NUM_SAMPLE = NUM_FRAME;
        //System.out.println("The size of data chunk is: " + NUM_FRAME);
        CHANNEL_SAMPLE = new String[NUM_CHANNEL][NUM_FRAME];

        // initialization of sample position
        int start_pos = POS_SAMPLE;
        int end_pos = POS_SAMPLE + 1;

        String sample_hex;
        for(int i = 0; i < NUM_FRAME; i ++) {
            for(int j = 0; j < NUM_CHANNEL; j ++) {

                sample_hex = hexReverse(start_pos, end_pos);

                // cut substring "0x" for parseInt()
                sample_hex = sample_hex.substring(2);
                CHANNEL_SAMPLE[j][i] = sample_hex;

                start_pos += SAMPLE_BYTE;
                end_pos += SAMPLE_BYTE;
            }
        }
        SYMBOLS = new String[CHANNEL_SAMPLE[0].length];
        SYMBOLS = CHANNEL_SAMPLE[0];
    }

    // reverse and concatenate hex string given the start and end position
    private static String hexReverse(int start, int end) {
        String temp_hex = "0x";
        // reverse the bytes
        for(int j = end; j >= start; j --) {
            // convert to 2-digit hex
            temp_hex += String.format("%02x", byte_buffer[j]);
        }
        return temp_hex;
    }
}
