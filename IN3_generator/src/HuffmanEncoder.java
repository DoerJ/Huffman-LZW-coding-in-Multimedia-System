import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.PriorityQueue;


public class HuffmanEncoder {

    private String[] SYMBOLS;
    public String[] SYMBOLS_COUNTS;
    public int[] counts;
    public String[] COUNTS;
    public List<Node> CODEWORDS  = new ArrayList<>();

    // image size
    public static int image_width = 0;
    public static int image_height = 0;

    // nodes
    public PriorityQueue<Node> nodes;

    // codeword stream
    public static String r_codeStream = "";
    public static String g_codeStream = "";
    public static String b_codeStream = "";
    public static int call = 0;

    // codeword decoding table offset
    public static int r_offset = 0;
    public static int g_offset = 0;
    public static int b_offset = 0;

    // codeword decoding table
    // to be embedded into IN3 file header, used for reconstructing Huffman tree
    public static int[] r_symbols;
    public static int[] g_symbols;
    public static int[] b_symbols;

    public static int[] r_frequency;
    public static int[] g_frequency;
    public static int[] b_frequency;

    public static int r_codewordSize = 0;
    public static int g_codewordSize = 0;
    public static int b_codewordSize = 0;

    HuffmanEncoder(String[] SYMBOLS, int image_width, int image_height) {

        this.SYMBOLS = SYMBOLS;
        this.image_width = image_width;
        this.image_height = image_height;

        call += 1;

        // calculate frequency counts for each symbol
        calculateCounts();

        // sort symbols
        Comparator<Node> comparator = new nodeComparator();
        nodes = new PriorityQueue<Node>(SYMBOLS_COUNTS.length, comparator);
        sortSymbols();

        // build Huffman encoding tree
        buildEncodingTree();

        if(call == 1) {
            System.out.println("Codeword table for R channel: \n");
        }
        else if(call == 2) {
            System.out.println("Codeword table for G channel: \n");
        }
        else if(call == 3) {
            System.out.println("Codeword table for B channel: \n");
        }

        int i = 0;
        while(i < CODEWORDS.size()) {
            System.out.println("symbol: " + CODEWORDS.get(i).symbol + " | " + "codeword: " + CODEWORDS.get(i).codeword);
            i ++;

        }

        // progress
        if(call == 1) {
            System.out.println("\nEncoding R channel ...");
        }
        else if(call == 2) {
            System.out.println("\nEncoding G channel ...");
        }
        else if(call == 3) {
            System.out.println("\nEncoding B channel ...");
        }

        buildCodewordStream();
    }

    public void calculateCounts() {

        SYMBOLS_COUNTS = new String[SYMBOLS.length];
        counts = new int[SYMBOLS.length];
        int index = 0;
        while(index < SYMBOLS.length) {
            // if list doesn't contain the symbol
            if(Arrays.asList(SYMBOLS_COUNTS).contains(SYMBOLS[index]) == false) {
                SYMBOLS_COUNTS[index] = SYMBOLS[index];
                counts[index] = 1;
                index ++;
            }
            // if list contains the symbol, increment the count
            else {
                counts[Arrays.asList(SYMBOLS).indexOf(SYMBOLS[index])] ++;
                index ++;
            }
        }
        // get rid of null values in arrays
        List<String> list = new ArrayList<>();
        List<String> count_list = new ArrayList<>();
        int i = 0;
        while(i < SYMBOLS_COUNTS.length) {
            if(SYMBOLS_COUNTS[i] != null) {
                list.add(SYMBOLS_COUNTS[i]);
                count_list.add(Integer.toString(counts[i]));
                i ++;
            }
            else {
                i ++;
            }
        }
        SYMBOLS_COUNTS = list.toArray(new String[list.size()]);
        COUNTS = count_list.toArray(new String[count_list.size()]);

        // convert String[] to int[]
        // if R channel
        if(call == 1) {
            r_symbols = new int[SYMBOLS_COUNTS.length];
            r_frequency = new int[COUNTS.length];
            for(i = 0; i < SYMBOLS_COUNTS.length; i ++) {
                r_symbols[i] = Integer.parseInt(SYMBOLS_COUNTS[i]);
                r_frequency[i] = Integer.parseInt(COUNTS[i]);
            }
            r_offset = SYMBOLS_COUNTS.length;
        }

        // if G channel
        else if(call == 2) {
            g_symbols = new int[SYMBOLS_COUNTS.length];
            g_frequency = new int[COUNTS.length];
            for(i = 0; i < SYMBOLS_COUNTS.length; i ++) {
                g_symbols[i] = Integer.parseInt(SYMBOLS_COUNTS[i]);
                g_frequency[i] = Integer.parseInt(COUNTS[i]);
            }
            g_offset = SYMBOLS_COUNTS.length;
        }

        // if B channel
        else if(call == 3) {
            b_symbols = new int[SYMBOLS_COUNTS.length];
            b_frequency = new int[COUNTS.length];
            for(i = 0; i < SYMBOLS_COUNTS.length; i ++) {
                b_symbols[i] = Integer.parseInt(SYMBOLS_COUNTS[i]);
                b_frequency[i] = Integer.parseInt(COUNTS[i]);
            }
            b_offset = SYMBOLS_COUNTS.length;
        }

    }

    public void sortSymbols() {

        int i = 0;
        while(i < SYMBOLS_COUNTS.length) {
            // create node (symbol, count)
            // all leaf nodes
            Node node = new Node(SYMBOLS_COUNTS[i], Double.parseDouble(COUNTS[i]));
            nodes.add(node);
            i ++;
        }
    }

    public void buildEncodingTree() {

        while(nodes.size() != 1) {
            Node parent = new Node(nodes.poll(), nodes.poll());
            nodes.add(parent);
        }
        // root left
        encodeSymbols(nodes.peek(), "");
    }

    // encode symbols
    public void encodeSymbols(Node root, String codeword) {

        if(root.left != null) {
            encodeSymbols(root.left, codeword + "0");
        }
        if(root.right != null) {
            encodeSymbols(root.right, codeword + "1");
        }
        // if leaf node (symbol)
        if(root.left == null && root.right == null) {
            CODEWORDS.add(new Node(root.symbol, codeword));
        }
    }

    // build codeword steam
    public void buildCodewordStream() {

        for(int i = 0; i < SYMBOLS.length; i ++) {
            // define inner loop, once found corresponding codeword, jump out of innerloop
            for(int j = 0; j < CODEWORDS.size(); j ++) {
                // IMPROVE!!!
                if(SYMBOLS[i].equals(CODEWORDS.get(j).symbol)) {
                    // encode r_channel
                   if(call == 1) {
                       r_codeStream += CODEWORDS.get(j).codeword;
                       r_codewordSize += CODEWORDS.get(j).codeword.length();
                       j = CODEWORDS.size();
                   }
                   // encode g_channel
                   else if(call == 2) {
                       g_codeStream += CODEWORDS.get(j).codeword;
                       g_codewordSize += CODEWORDS.get(j).codeword.length();
                       j = CODEWORDS.size();
                   }
                   // encode b_channel
                   else if(call == 3) {
                       b_codeStream += CODEWORDS.get(j).codeword;
                       b_codewordSize += CODEWORDS.get(j).codeword.length();
                       j = CODEWORDS.size();
                   }
                }
            }
        }

        // showing encoding progress
        if(call == 1) {
            System.out.println("The length of R channel codeword stream: " + r_codewordSize + " bits");
            System.out.println("--------------------------------------------------------------");
        }
        else if(call == 2) {
            System.out.println("The length of G channel codeword stream: " + g_codewordSize + " bits");
            System.out.println("--------------------------------------------------------------");
        }
        else if(call == 3) {
            System.out.println("The length of B channel codeword stream: " + b_codewordSize + " bits");
            System.out.println("--------------------------------------------------------------");
            System.out.println("Encoding completed! Now writing IN3 file ...");

            // writing IN3 file
            IN3Writer in3_writer = new IN3Writer(image_width, image_height, r_offset, g_offset,
                    b_offset, r_symbols, g_symbols, b_symbols, r_frequency, g_frequency, b_frequency,
                    r_codeStream, g_codeStream, b_codeStream, r_codewordSize, g_codewordSize, b_codewordSize);
        }
    }
}
