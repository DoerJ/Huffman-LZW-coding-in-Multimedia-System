import java.util.ArrayList;
import java.util.List;

public class LZWEncoder {

    // dictionary
    public static List<Record> DICTIONARY;
    public static List<String> LOOKUP_TABLE;

    // symbols
    private static String[] SYMBOLS;
    public static List<String> CLEAN_SYMBOLS;
    public static int CODE;
    public static List<String> OUTPUT;

    //compression ratio
    public double COMPRESSION_RATIO;
    public double bits_before;
    public double bits_after;

    LZWEncoder(String[] SYMBOLS) {

        // start time
        final double start_time = System.currentTimeMillis();

        this.SYMBOLS = SYMBOLS;
        DICTIONARY = new ArrayList<>();
        CLEAN_SYMBOLS = new ArrayList<>();
        LOOKUP_TABLE = new ArrayList<>();
        OUTPUT = new ArrayList<>();

        // eliminate redundant symbols
        eliminateRedundantSymbols();

        // add dictionary
        System.out.println("Initializing dictionary ...\n");
        initializeDictionary();

        // build dictionary
        System.out.println("Building dictionary ...\n");
        buildDictionary();

        // compression ratio
        calculateCompressionRatio();

        System.out.println("Compression ratio is: " + COMPRESSION_RATIO);

        // end time
        final double end_time = System.currentTimeMillis();

        // runtime in seconds
        double duration = (end_time - start_time) / 1000.0;
        System.out.println("Encoding time is: " + duration + " seconds");
    }

    // eliminate redundant symbols
    public static void eliminateRedundantSymbols() {
        int index = 0;
        while(index < SYMBOLS.length) {
            // if list doesn't contain the symbol
            if(CLEAN_SYMBOLS.contains(SYMBOLS[index]) == false) {
                CLEAN_SYMBOLS.add(SYMBOLS[index]);
                index ++;
            }
            // if list contains the symbol
            else {
                index ++;
            }
        }
    }

    // initialize dictionary
    public static void initializeDictionary() {
        // empty dictionary
        int index = 0;
        int code = 1;
        while(index < CLEAN_SYMBOLS.size()) {
            DICTIONARY.add(new Record(code, CLEAN_SYMBOLS.get(index)));
            LOOKUP_TABLE.add(CLEAN_SYMBOLS.get(index));
            code ++;
            index ++;
        }
        CODE = code;
    }

    // build dictionary
    public static void buildDictionary() {
        String current = SYMBOLS[0];
        int output;
        int index = 0;
        while(index < SYMBOLS.length) {
            // if not EOF
            if(index != SYMBOLS.length - 1) {
                String next = SYMBOLS[index + 1];
                // if s + c is contained in dictionary
                if(LOOKUP_TABLE.contains(current + next)) {
                    Record record = new Record(current, next, 0, 0, null);
                    DICTIONARY.add(record);
                    current = current + next;
                    index ++;
                }
                // is s + c is not contained in dictionary
                else {
                    output = DICTIONARY.get(LOOKUP_TABLE.indexOf(current)).code;
                    OUTPUT.add(Integer.toString(output));
                    Record record = new Record(current, next, output, CODE, current + next);
                    DICTIONARY.add(record);
                    LOOKUP_TABLE.add(current + next);
                    current = next;
                    index ++;
                }
            }

            // if EOF
            else {
                output = DICTIONARY.get(LOOKUP_TABLE.indexOf(current)).code;
                Record record = new Record(current, "EOF", output, 0, null);
                OUTPUT.add(Integer.toString(output));
                DICTIONARY.add(record);
                index ++;
            }
        }
    }

    // compression ratio
    public void calculateCompressionRatio() {
        // 2 bytes / sample

        // bits before
        bits_before = (double)(SYMBOLS.length * 16);

        // bits after
        bits_after = (double)(OUTPUT.size() * 16);

        COMPRESSION_RATIO = bits_before / bits_after;
    }
}
