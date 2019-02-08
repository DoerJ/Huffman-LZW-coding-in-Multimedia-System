import java.util.BitSet;
import java.util.PriorityQueue;
import java.util.Comparator;

public class HuffmanDecoder {

    public int image_width, image_height;
    public int[] r_symbols, g_symbols, b_symbols, r_frequency, g_frequency, b_frequency;
    public byte[] r_getByte, g_getByte, b_getByte;

    // codeword stream strings
    public String r_string;
    public String g_string;
    public String b_string;

    // rgb nodes
    public PriorityQueue<Node> r_nodes;
    public PriorityQueue<Node> g_nodes;
    public PriorityQueue<Node> b_nodes;

    // rgb pixels
    public int[] r_pixels;
    public int[] g_pixels;
    public int[] b_pixels;
    public Pixel[][] pixel_buffer;

    HuffmanDecoder(int image_width, int image_height, int[] r_symbols, int[] g_symbols,
                   int[] b_symbols, int[] r_frequency, int[] g_frequency, int[] b_frequency,
                   byte[] r_getByte, byte[] g_getByte, byte[] b_getByte) {

        System.out.println("\nBuilding Huffman decoding tree ...");

        this.image_width = image_width;
        this.image_height = image_height;
        this.r_symbols = r_symbols;
        this.g_symbols = g_symbols;
        this.b_symbols = b_symbols;
        this.r_frequency = r_frequency;
        this.g_frequency = g_frequency;
        this.b_frequency = b_frequency;
        this.r_getByte = r_getByte;
        this.g_getByte = g_getByte;
        this.b_getByte = b_getByte;

        pixel_buffer = new Pixel[image_height][image_width];

        r_string = "";
        g_string = "";
        b_string = "";

        // sorting
        Comparator<Node> comparator = new nodeComparator();
        // initialize node priority queues
        r_nodes = new PriorityQueue<Node>(r_symbols.length, comparator);
        g_nodes = new PriorityQueue<Node>(g_symbols.length, comparator);
        b_nodes = new PriorityQueue<Node>(b_symbols.length, comparator);

        r_pixels = new int[image_width * image_height];
        g_pixels = new int[image_width * image_height];
        b_pixels = new int[image_width * image_height];

        // convert byte[] to String
        convertByteArrayToString();

        // building Huffman decoding tree
        buildingDecodingTree();

        // fill up pixel arrays
        fillUpPixelArray();

        // make pixel buffer
        makePixelBuffer();

        // display the decompressed image
        System.out.println("\nDisplaying the image ...");
        bmpHeaderReader reader = new bmpHeaderReader();
        double end_time = System.currentTimeMillis();
        double duration = (end_time - reader.start_time_decode) / 1000.0;
        System.out.println("Decoding time: " + duration);

        Container container = new Container(image_width, image_height, pixel_buffer, 3);
    }

    public void convertByteArrayToString() {

        // do for r channel
        BitSet r_bits = BitSet.valueOf(r_getByte);
        for(int i = 0; i < r_bits.length(); i ++) {
            // if 1
            if(r_bits.get(i)) {
                r_string += "1";
            }
            // if 0
            else {
                r_string += "0";
            }
        }

        // do for g channel
        BitSet g_bits = BitSet.valueOf(g_getByte);
        for(int i = 0; i < g_bits.length(); i ++) {
            // if 1
            if(g_bits.get(i)) {
                g_string += "1";
            }
            // if 0
            else {
                g_string += "0";
            }
        }

        // do for b channel
        BitSet b_bits = BitSet.valueOf(b_getByte);
        for(int i = 0; i < b_bits.length(); i ++) {
            // if 1
            if(b_bits.get(i)) {
                b_string += "1";
            }
            // if 0
            else {
                b_string += "0";
            }
        }
    }

    // build decoding tree
    public void buildingDecodingTree() {
        // sort symbols
        // build r_nodes
        for(int i = 0; i < r_symbols.length; i ++) {
            Node node = new Node(Integer.toString(r_symbols[i]), (double)r_frequency[i]);
            r_nodes.add(node);
        }
        while(r_nodes.size() != 1) {
            Node node = new Node(r_nodes.poll(), r_nodes.poll());
            r_nodes.add(node);
        }

        // build g_nodes
        for(int i = 0; i < g_symbols.length; i ++) {
            Node node = new Node(Integer.toString(g_symbols[i]), (double)g_frequency[i]);
            g_nodes.add(node);
        }
        while(g_nodes.size() != 1) {
            Node node = new Node(g_nodes.poll(), g_nodes.poll());
            g_nodes.add(node);
        }

        // build b_nodes
        for(int i = 0; i < b_symbols.length; i ++) {
            Node node = new Node(Integer. toString(b_symbols[i]), (double)b_frequency[i]);
            b_nodes.add(node);
        }
        while(b_nodes.size() != 1) {
            Node node = new Node(b_nodes.poll(), b_nodes.poll());
            b_nodes.add(node);
        }
    }

    public void fillUpPixelArray() {

        // do for r channel
        int index = 0;
        int i = 0;
        while(index < r_pixels.length) {
            Node root = r_nodes.peek();
            // while not leaf node
            while(root.left != null && root.right != null && i < r_string.length()) {
                if(r_string.charAt(i) == '0') {
                    root = root.left;
                }
                else {
                    root = root.right;
                }
                i += 1;
            }
            // root: leaf node that contains symbols
            r_pixels[index] = Integer.parseInt(root.symbol);
            index += 1;
        }

        // do for g channel
        index = 0;
        i = 0;
        while(index < g_pixels.length) {
            Node root = g_nodes.peek();
            while(root.left != null && root.right != null && i < g_string.length()) {
                if(g_string.charAt(i) == '0') {
                    root = root.left;
                }
                else {
                    root = root.right;
                }
                i += 1;
            }
            g_pixels[index] = Integer.parseInt(root.symbol);
            index += 1;
        }

        // do for b channel
        index = 0;
        i = 0;
        while(index < b_pixels.length) {
            Node root = b_nodes.peek();
            while(root.left != null && root.right != null && i < b_string.length()) {
                if(b_string.charAt(i) == '0') {
                    root = root.left;
                }
                else {
                    root = root.right;
                }
                i += 1;
            }
            b_pixels[index] = Integer.parseInt(root.symbol);
            index += 1;
        }
    }

    // pixel buffer
    public void makePixelBuffer() {

        System.out.println("\nFilling up the pixel buffer ...");

        int index = 0;
        for(int i = 0; i < image_height; i ++) {
            for(int j = 0; j < image_width; j ++) {
                int r = r_pixels[index];
                int g = g_pixels[index];
                int b = b_pixels[index];
                Pixel pixel = new Pixel();
                pixel.setRed(r);
                pixel.setGreen(g);
                pixel.setBlue(b);
                pixel_buffer[i][j] = pixel;
                index += 1;
            }
        }
    }
}
