import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.PriorityQueue;

public class HuffmanEncoder {

    public static String[] strings;
    private static String[] SYMBOLS;
    public static double NUM_SYMBOLS;

    public static String[] SYMBOLS_COUNTS;
    public static double[] counts;
    public static String[] COUNTS;
    public static List<Node> CODEWORDS;

    // nodes
    public static PriorityQueue<Node> nodes;

    // compression ratio
    public static double COMPRESSION_RATIO;

    HuffmanEncoder(String[] strings) {

        // start time
        final double start_time = System.currentTimeMillis();

        this.strings = strings;

        // put symbols into array
        putSymbols();

        // calculate frequency counts for each symbol
        System.out.println("Calculating symbol intervals ...\n");
        calculateCounts();

        // sort symbols
        Comparator<Node> comparator = new nodeComparator();
        nodes = new PriorityQueue<Node>(SYMBOLS_COUNTS.length, comparator);
        System.out.println("Sorting symbols ...\n");
        sortSymbols();

        // build Huffman encoding tree
        buildEncodingTree();

        // compression ratio
        calculateCompressionRatio();
        System.out.println("Compression ratio is: " + COMPRESSION_RATIO);

        // end time
        final double end_time = System.currentTimeMillis();

        // runtime in seconds
        double duration = (end_time - start_time) / 1000.0;
        System.out.println("Encoding time is: " + duration + " seconds");

//        int i = 0;
//        while(i < CODEWORDS.size()) {
//            System.out.println("symbol: " + CODEWORDS.get(i).symbol + " | " + "codeword: " + CODEWORDS.get(i).codeword);
//            i ++;
//        }
    }

    public static void putSymbols() {

        int index = 0;
        int count = 0;
        while(strings[index] != null) {

            count ++;
            index ++;
            // if EOF
            if(index >= strings.length) {
                break;
            }
        }
        //System.out.println("the size of symbols: " + count);
        NUM_SYMBOLS = count;

        SYMBOLS = new String[count];
        index = 0;
        while(strings[index] != null) {
            SYMBOLS[index] = strings[index];
            index ++;
            // if EOF
            if(index >= strings.length) {
                break;
            }
        }
    }

    public static void calculateCounts() {

        SYMBOLS_COUNTS = new String[SYMBOLS.length];
        counts = new double[SYMBOLS.length];
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
                count_list.add(Double.toString(counts[i]));
                i ++;
            }
            else {
                i ++;
            }
        }
        SYMBOLS_COUNTS = list.toArray(new String[list.size()]);
        COUNTS = count_list.toArray(new String[count_list.size()]);

    }

    public static void sortSymbols() {

        int i = 0;
        while(i < SYMBOLS_COUNTS.length) {
            // create node (symbol, count)
            // all leaf nodes
            Node node = new Node(SYMBOLS_COUNTS[i], Double.parseDouble(COUNTS[i]));
            nodes.add(node);
            i ++;
        }
    }

    public static void buildEncodingTree() {

        // initialize codewords
        CODEWORDS = new ArrayList<>();

        while(nodes.size() != 1) {
            Node parent = new Node(nodes.poll(), nodes.poll());
            nodes.add(parent);
        }
        // root left
        encodeSymbols(nodes.peek(), "");
    }

    // encode symbols
    public static void encodeSymbols(Node root, String codeword) {

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

    // compression ratio
    public static void calculateCompressionRatio() {
        int index = 0;
        // bit_before = number of sample * 16bits
        double bitsBefore = SYMBOLS_COUNTS.length * SYMBOLS_COUNTS[0].length() * 4;
        double bitsAfter = 0;
        while(index < SYMBOLS_COUNTS.length) {
            bitsAfter += CODEWORDS.get(index).codeword.length();
            index ++;
        }
        COMPRESSION_RATIO = bitsBefore / bitsAfter;
    }
}
