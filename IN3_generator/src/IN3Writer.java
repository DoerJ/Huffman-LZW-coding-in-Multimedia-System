import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;
import java.io.DataOutputStream;

public class IN3Writer {

    public int image_width, image_height;
    public int r_offset, g_offset, b_offset, r_codewordSize, g_codewordSize, b_codewordSize;
    public int[] r_symbols, g_symbols, b_symbols, r_frequency, g_frequency, b_frequency;
    public String r_codeStream, g_codeStream, b_codeStream;

    // file name and path
    public String file_path;
    public String file_name;

    // compression ratio
    public double original_size;
    public double compression_size = 0;
    public double compression_ratio;

    public String padding_bits = "";

    IN3Writer(int image_width, int image_height, int r_offset, int g_offset, int b_offset,
              int[] r_symbols, int[] g_symbols, int[] b_symbols, int[] r_frequency, int[] g_frequency,
              int[] b_frequency, String r_codeStream, String g_codeStream, String b_codeStream,
              int r_codewordSize, int g_codewordSize, int b_codewordSize) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.r_offset = r_offset;
        this.g_offset = g_offset;
        this.b_offset = b_offset;
        this.r_codewordSize = r_codewordSize;
        this.g_codewordSize = g_codewordSize;
        this.b_codewordSize = b_codewordSize;
        this.r_symbols = r_symbols;
        this.g_symbols = g_symbols;
        this.b_symbols = b_symbols;
        this.r_frequency = r_frequency;
        this.g_frequency = g_frequency;
        this.b_frequency = b_frequency;
        this.r_codeStream = r_codeStream;
        this.g_codeStream = g_codeStream;
        this.b_codeStream = b_codeStream;

        // initialize outputStream
        bmpHeaderReader header = new bmpHeaderReader();

        // set file path and name
        file_path = header.file_path.substring(0, header.file_path.lastIndexOf("/") + 1);
        file_name = header.file_path.substring(header.file_path.lastIndexOf("/") + 1, header.file_path.lastIndexOf("."));
        file_path += file_name + ".IN3";

        original_size = (double)header.BMP_SIZE;

        // mode 8 bits
        modBits();

        // writing IN3 header
        writingHeader();

    }

    // length(codeword stream) % 8
    // complete r, g, b codeword streams into byte array
    public void modBits() {

        // doing for r channel
        int mod_bits = r_codewordSize % 8;
        if(mod_bits != 0) {
            mod_bits = 8 - mod_bits;
            for(int i = 0; i < mod_bits; i ++) {
                padding_bits += "1";
            }
        }
        r_codeStream += padding_bits;

        // doing for g channel
        padding_bits = "";
        mod_bits = g_codewordSize % 8;
        if(mod_bits != 0) {
            mod_bits = 8 - mod_bits;
            for(int i = 0; i < mod_bits; i ++) {
                padding_bits += "1";
            }
        }
        g_codeStream += padding_bits;

        // doing for b channel
        padding_bits = "";
        mod_bits = b_codewordSize % 8;
        if(mod_bits != 0) {
            mod_bits = 8 - mod_bits;
            for(int i = 0; i < mod_bits; i ++) {
                padding_bits += "1";
            }
        }
        b_codeStream += padding_bits;
    }

    // writing IN3 header
    public void writingHeader() {
        System.out.println("Writing IN3 header ...");

        DataOutputStream file = null;

        // writing image width and height
        try {

            file = new DataOutputStream(new FileOutputStream(file_path));

            // 8 bytes
            file.writeInt(image_width);
            file.writeInt(image_height);
            compression_size += 8.0;

            // write r_channel decoding info
            file.writeInt(r_offset);
            for(int i = 0; i < r_symbols.length; i ++) {
                // 8 bytes
                file.writeInt(r_symbols[i]);
                file.writeInt(r_frequency[i]);
                compression_size += 8.0;
            }

            // write g_channel decoding info
            file.writeInt(g_offset);
            for(int i = 0; i < g_symbols.length; i ++) {
                // 8 bytes
                file.writeInt(g_symbols[i]);
                file.writeInt(g_frequency[i]);
                compression_size += 8.0;
            }

            // write b_channel decoding info
            file.writeInt(b_offset);
            for(int i = 0; i < b_symbols.length; i ++) {
                // 8 bytes
                file.writeInt(b_symbols[i]);
                file.writeInt(b_frequency[i]);
                compression_size += 8.0;
            }

            // write codeword stream size
            file.writeInt(r_codewordSize);
            file.writeInt(g_codewordSize);
            file.writeInt(b_codewordSize);

            // writing codeword stream ...

            // do for r channel stream
            BitSet r_bitStream = new BitSet(r_codeStream.length());
            int bit_counter = 0;
            for(int i = 0; i < r_codeStream.length(); i ++) {
                if(r_codeStream.charAt(i) == '1') {
                    r_bitStream.set(bit_counter);
                }
                bit_counter += 1;
            }
            compression_size += (double)(r_codeStream.length() / 8);
            byte[] r_byteArray = r_bitStream.toByteArray();

            // do for g channel stream
            BitSet g_bitStream = new BitSet(g_codeStream.length());
            bit_counter = 0;
            for(int i = 0; i < g_codeStream.length(); i ++) {
                if(g_codeStream.charAt(i) == '1') {
                    g_bitStream.set(bit_counter);
                }
                bit_counter += 1;
            }
            compression_size += (double)(g_codeStream.length() / 8);
            byte[] g_byteArray = g_bitStream.toByteArray();

            // do for b channel stream
            BitSet b_bitStream = new BitSet(b_codeStream.length());
            bit_counter = 0;
            for(int i = 0; i < b_codeStream.length(); i ++) {
                if(b_codeStream.charAt(i) == '1') {
                    b_bitStream.set(bit_counter);
                }
                bit_counter += 1;
            }
            compression_size += (double)(b_codeStream.length() / 8);
            byte[] b_byteArray = b_bitStream.toByteArray();

            System.out.println("Writing IN3 codeword streams ...");

            file.write(r_byteArray);
            file.write(g_byteArray);
            file.write(b_byteArray);

            double end_time = System.currentTimeMillis();
            bmpHeaderReader reader = new bmpHeaderReader();
            double duration = (end_time - reader.start_time) / 1000.0;
            System.out.println("Encoding time: " + duration);

        } catch (IOException e) {

            // close DataOutputStream writer
        } finally {
            try {
                if(file != null) {
                    file.close();
                    System.out.println("Compression completed!");
                    System.out.println("--------------------------------------------------------------");

                    // calculate the compression ratio
                    calculateCompressionRatio();
                }
            } catch (IOException e) {

            }
        }
    }

    // compression ratio
    public void calculateCompressionRatio() {
        compression_ratio = original_size / compression_size;
        System.out.println("Compression ratio: " + compression_ratio);
    }

}
