// AUTHOR: YUAHO HE
// DATE: 2018.6.12
// DESCRIPTION: THE PROGRAM READS IN AND PROCESS WAV FILE,
// AND DISPLAYS THE WAVEFORM (VOLTAGE VALUE VS TIME)

import javax.swing.JFileChooser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// For now, only handle single-channel (left channel)
public class readWav {

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
    public static int DATACHUNK_SIZE = 0;
    public static int SAMPLE_RATE = 0;
    public static int NUM_SAMPLE = 0;
    // the starting point of sample data chunk
    public static int POS_SAMPLE = 0;
    // [channel][sample_bytes]
    public static String[][] CHANNEL_SAMPLE;
    // default: 16 bits/sample
    public static double MAX_LIMIT = 32767;
    public static double MIN_LIMIT = -32768;
    // normalized value for right and left channels
    // for displaying waveform
    public static double[] LEFT_CHANNEL_SAMPLE;
    public static double[] RIGHT_CHANNEL_SAMPLE;
    public static int[] LEFT_SIGNED_SAMPLE;

    public static String[] HEADER_INFO = {"CHUNK_ID", "CHUNK_SIZE", "FORMAT", "SUBCHUNK1_ID", "SUBCHUNK_SIZE",
    "AUDIO_FORMAT", "NUM_CHANNELS", "SAMPLE_RATE", "BYTE_RATE", "BLOCK_ALIGN", "BITS_PER_SAMPLE",
    "SUBCHUNK2_ID", "SUBCHUNK2_SIZE"};
    public static int[] HEADER_OFFSET = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
    public static String[] HEX_INFO = new String[HEADER_OFFSET.length];

    // panel info
    public static int PANEL_WIDTH = 1200;
    public static int PANEL_HEIGHT = 500;

    // main function
    public static void main(String args[]) {
        //the absolute path of .wav file
        file_path = openWav();

        // find the .wav file (using absolute path) and store the file data into inputStream
        storeDataStream();

        // draw waveform panel
        drawPanel();
    }
// open dialog for loading wav file
    private static String openWav() {
        JFileChooser dialog = new JFileChooser();
        String wav_path = "";
        // set the properties for the file chooser
        // set the default directory as project workspace
        dialog.setCurrentDirectory(new java.io.File("."));
        dialog.setDialogTitle("Please choose the .wav file you want to open");
        if(dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

        }
        try {
            // print out the absolute path of .wav file and return it
            wav_path = dialog.getSelectedFile().getAbsolutePath();
            System.out.println("The path of .wav file is: " + wav_path);
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
            //System.out.println("The first byte of .wav file is: " + wavBytes.read());
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
        System.out.println("The total number of bytes is: " + wavSize);
        //System.out.println("The value of the second byte is: " + byte_buffer[1]);

        // extract wave file info from buffer
        extractInfo();
    }
    // extract header info, bit/sample, sampling rate, and so on
    private static void extractInfo() {
        //System.out.println("The number of channel is: " + byte_buffer[21] + " + " + byte_buffer[22]);
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
        //System.out.println("The first hex is: " + HEX_INFO[2]);

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
                //System.out.println("MIN_LIMIT is: " + MIN_LIMIT);
            }
            // record number of channels
            else if(HEADER_INFO[i].equals("NUM_CHANNELS") == true) {
                NUM_CHANNEL = hex_decode;
            }
            // record the sampling rate (i.e., # of samples/sec)
            else if(HEADER_INFO[i].equals("SAMPLE_RATE") == true) {
                SAMPLE_RATE = hex_decode;
            }
            System.out.println(HEADER_INFO[i] + ":      " + hex_decode);
        }
//        System.out.println("The sample size is: " + SAMPLE_BYTE + " bytes per sample");
//        System.out.println("The number of channels is: " + NUM_CHANNEL);

        // record the starting point of sample chunk
        POS_SAMPLE = pos + 1;
        System.out.println("The pos_sample is: " + POS_SAMPLE);
        // store the sample data into buffer
        storeSample();
    }
    // extract samples
    private static void storeSample() {
        // count the size of data chunk
        DATACHUNK_SIZE = wavSize - POS_SAMPLE;
        // calculate the number of frames
        NUM_FRAME = DATACHUNK_SIZE / (SAMPLE_BYTE * NUM_CHANNEL);

        // # of samples = # of frames
        NUM_SAMPLE = NUM_FRAME;

        //System.out.println("The size of data chunk is: " + NUM_FRAME);
        CHANNEL_SAMPLE = new String[NUM_CHANNEL][NUM_FRAME];

        // initialize left and right channels sample array
        LEFT_CHANNEL_SAMPLE = new double[NUM_FRAME];
        RIGHT_CHANNEL_SAMPLE = new double[NUM_FRAME];
        LEFT_SIGNED_SAMPLE = new int[NUM_FRAME];

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
        //System.out.println("The hex of left channel: " + CHANNEL_SAMPLE[0][0]);
        // channel_sample store the hex value of samples
        // we need to convert each hex into SIGNED integer value for normalization
        int[] left_signedSample;
        int[] right_signedSample;

        // normalize the left channel samples
        left_signedSample = convertSignedInt(CHANNEL_SAMPLE[0]);
        LEFT_SIGNED_SAMPLE = left_signedSample;
        LEFT_CHANNEL_SAMPLE = Norm(left_signedSample);

        // normalize the right channel samples
        if(NUM_CHANNEL == 2) {
            right_signedSample = convertSignedInt(CHANNEL_SAMPLE[1]);
            RIGHT_CHANNEL_SAMPLE = Norm(right_signedSample);
        }
    }

    // convert 2-bytes hex to signed integer
    private static int[] convertSignedInt(String[] hexArray) {
        int[] temp_signed = new int[hexArray.length];
        for(int i = 0; i < hexArray.length; i ++) {

            short temp = (short)Integer.parseInt(hexArray[i], 16);
            temp_signed[i] = (int)temp;

        }
        return temp_signed;
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
    // normalize the sample value to 0-1 space
    private static double[] Norm(int[] normalize) {

        //double deno = MAX_LIMIT + Math.abs(MIN_LIMIT);

        double[] norm = new double[normalize.length];
        for(int i = 0; i < norm.length; i ++) {
            if(normalize[i] >= 0) {
                norm[i] = normalize[i] / MAX_LIMIT;
            }
            else {
                norm[i] = normalize[i] / Math.abs(MIN_LIMIT);
            }
        }
        return norm;
    }

    // draw waveform panel
    public static void drawPanel() {

        Container container = new Container(PANEL_WIDTH, PANEL_HEIGHT, NUM_FRAME, LEFT_CHANNEL_SAMPLE, LEFT_SIGNED_SAMPLE);
    }
}
