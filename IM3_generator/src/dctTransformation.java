import java.util.ArrayList;
import java.util.List;

public class dctTransformation {

    public Block[] r_blocks;
    // r string used for Huffman encoding
    public List<String> SYMBOLS;
    public String[] symbols;

    // DCT matrix
    public double[][] DCT_MATRIX;
    // transpose of DCT matrix
    public double[][] DCT_MATRIX_T;
    // quantization table
    public int[][] QUANT_TABLE;

    // image dimension
    public static int image_width;
    public static int image_height;

    // call
    public static int call = 0;

    dctTransformation(Block[] r_blocks, int image_width, int image_height) {

        this.r_blocks = r_blocks;
        this.image_width = image_width;
        this.image_height = image_height;

        DCT_MATRIX = new double[8][8];
        DCT_MATRIX_T = new double[8][8];
        QUANT_TABLE = new int[8][8];

        SYMBOLS = new ArrayList<>();

        // call
        call += 1;

        // define dct matrix
        dctMatrix();

        // transpose of dct matrix
        dctMatrixTranspose();

        // apply dct to block
        blockToDCT();

        // define quantization table
        quantizationTable();

        // apply quantization
        dctToQuantizedDCT();

        // build block coefficient string
        buildingBlockString();

        // building symbols string
        buildingSymbols();
        symbols = SYMBOLS.toArray(new String[SYMBOLS.size()]);

        if(call == 1) {
            System.out.println("--------------------------------------------------------------");
            System.out.println("Sending R channel for Huffman encoding ...");
        }
        else if(call == 2) {
            System.out.println("Sending G channel for Huffman encoding ...");
        }
        else if(call == 3) {
            System.out.println("Sending B channel for Huffman encoding ...");
        }

        HuffmanEncoder encoder = new HuffmanEncoder(symbols, image_width, image_height);
    }

    dctTransformation() {

        QUANT_TABLE = new int[8][8];
        DCT_MATRIX = new double[8][8];
        DCT_MATRIX_T = new double[8][8];


        quantizationTable();
        dctMatrix();
        dctMatrixTranspose();
    }

    // define the DCT matrix
    public void dctMatrix() {

        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {

                if(i == 0) {
                    DCT_MATRIX[i][j] = 1.0 / (2.0 * Math.sqrt(2.0));
                }
                else {
                    DCT_MATRIX[i][j] = Math.sqrt(2.0 / 8.0) * Math.cos((double)(2 * j + 1) * (double)i * Math.PI / 16.0);
                }
            }
        }
    }

    public void dctMatrixTranspose() {

        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                // dialog
                if(i == j) {
                    DCT_MATRIX_T[i][j] = DCT_MATRIX[i][j];
                }
                else {
                    DCT_MATRIX_T[i][j] = DCT_MATRIX[j][i];
                }
            }
        }
    }

    // multiplication of two matrix
    public double[][] matrixMultiplication(double[][] m1, double[][] m2) {

        double[][] result = new double[8][8];

        // initialize result
        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                result[i][j] = 0.0;
            }
        }

        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                for(int k = 0; k < 8; k ++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return result;
    }

    public void blockToDCT() {

        int index = 0;
        while(index < r_blocks.length) {
            double[][] temp = matrixMultiplication(DCT_MATRIX, r_blocks[index].block_value);
            r_blocks[index].dct_coefficients = matrixMultiplication(temp, DCT_MATRIX_T);
            index += 1;
        }
    }

    // quantization table
    public void quantizationTable() {

        QUANT_TABLE[0][0] = 1;
        double power = 0.0;
        // fill up the first row
        for(int j = 1; j < 8; j ++) {
            QUANT_TABLE[0][j] = (int)Math.pow(2, power);
            power += 2.5;
        }
        power = 0.0;

        // fill up the upper-right part
        int index = 1;
        while(index < 8) {
            double temp_power = power;
            QUANT_TABLE[index][index] = (int)Math.pow(2, power);
            for(int j = index; j < 8; j ++) {
                QUANT_TABLE[index][j] = (int)Math.pow(2, power);
                power += 2.5;
            }
            power = temp_power;
            power += 2.5;
            index += 1;
        }

        // fill up the upper-left part
        for(int i = 1; i < 8; i ++) {
            for(int j = 0; j < i; j ++) {
                QUANT_TABLE[i][j] = QUANT_TABLE[j][i];
            }
        }
    }

    // apply quantization
    public void dctToQuantizedDCT() {

        int index = 0;
        while(index < r_blocks.length) {
            r_blocks[index].quantization_coefficients = matrixDivision(r_blocks[index].dct_coefficients, QUANT_TABLE);
            index += 1;
        }
    }

    // matrix division for quantization
    public int[][] matrixDivision(double[][] m1, int[][] m2) {

        int[][] result = new int[8][8];

        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                result[i][j] = (int)m1[i][j] / m2[i][j];
            }
        }
        return result;
    }

    // build block string
    public void buildingBlockString() {

        int index = 0;
        while(index < r_blocks.length) {
            r_blocks[index].coefficient_string = getBlockString(r_blocks[index].quantization_coefficients);
            index += 1;
        }
    }

    // get block arraylist
    public List<String> getBlockString(int[][] m1) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                list.add(Integer.toString(m1[i][j]));
            }
        }
        return list;
    }

    // building symbol string for Huffman coding
    public void buildingSymbols() {
        int index = 0;
        while(index < r_blocks.length) {
            SYMBOLS.addAll(r_blocks[index].coefficient_string);
            index += 1;
        }
    }
}
