public class idctTransformation {

    public static int image_width;
    public static int image_height;

    // quantization coefficients
    public int[] r_coefficients;
    public int[] g_coefficients;
    public int[] b_coefficients;

    // reconstructed blocks
    public Block[] r_blocks;
    public Block[] g_blocks;
    public Block[] b_blocks;

    public dctTransformation idct;

    // quantization table
    public int[][] quantization_table;
    public double[][] dct_matrix;
    public double[][] tranpose_dct_matrix;

    // pixel buffer
    public Pixel[][] pixel_buffer;


    idctTransformation(int image_width, int image_height, int[] r_coefficients, int[] g_coefficients,
                       int[] b_coefficients) {

        this.image_width = image_width;
        this.image_height = image_height;
        this.r_coefficients = r_coefficients;
        this.g_coefficients = g_coefficients;
        this.b_coefficients = b_coefficients;

        quantization_table = new int[8][8];
        dct_matrix = new double[8][8];
        tranpose_dct_matrix = new double[8][8];

        idct = new dctTransformation();

        // define matrix
        quantization_table = idct.QUANT_TABLE;
        dct_matrix = idct.DCT_MATRIX;
        tranpose_dct_matrix = idct.DCT_MATRIX_T;

        // pixel buffer
        pixel_buffer = new Pixel[image_height][image_width];

        // blocks
        r_blocks = new Block[(image_width * image_height) / 64];
        g_blocks = new Block[(image_width * image_height) / 64];
        b_blocks = new Block[(image_width * image_height) / 64];

        // reconstruct the block
        reconstructBlocks();

        // de-quantization
        deQuantization();

        // inverse dct transformation
        inverseDctTransformation();

        // build pixel buffer for drawing
        buildingPixelBuffer();

        double end_time = System.currentTimeMillis();
        bmpHeaderReader reader = new bmpHeaderReader();
        double duration = (end_time - reader.start_time_decode) / 1000.0;
        System.out.println("Decoding time: " + duration);

        // display the image
        Container container = new Container(image_width, image_height, pixel_buffer, 1);
    }

    // reconstruct the blocks
    public void reconstructBlocks() {

        // symbols length
        int length = image_width * image_height;
        int index = 0;
        int block_index = 0;

        int[] r_array = new int[64];
        int[] g_array = new int[64];
        int[] b_array = new int[64];

        while(index < length) {

            // create a new block
            if(index % 64 == 0 && index != 0) {
                r_blocks[block_index] = new Block();
                r_blocks[block_index].quantization_coefficients = mapBlockCoefficient(r_array);

                g_blocks[block_index] = new Block();
                g_blocks[block_index].quantization_coefficients = mapBlockCoefficient(g_array);

                b_blocks[block_index] = new Block();
                b_blocks[block_index].quantization_coefficients = mapBlockCoefficient(b_array);

                block_index += 1;
            }
            r_array[index % 64] = r_coefficients[index];
            g_array[index % 64] = g_coefficients[index];
            b_array[index % 64] = b_coefficients[index];

            // the last block
            if(index == length - 1) {
                r_blocks[block_index] = new Block();
                r_blocks[block_index].quantization_coefficients = mapBlockCoefficient(r_array);

                g_blocks[block_index] = new Block();
                g_blocks[block_index].quantization_coefficients = mapBlockCoefficient(g_array);

                b_blocks[block_index] = new Block();
                b_blocks[block_index].quantization_coefficients = mapBlockCoefficient(b_array);
            }
            index += 1;
        }
    }

    public int[][] mapBlockCoefficient(int[] a1) {

        int[][] result = new int[8][8];
        int index = 0;
        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                result[i][j] = a1[index];
                index += 1;
            }
        }
        return result;
    }

    // de-quantization --> get dct coefficients
    public void deQuantization() {

        int index = 0;

        while(index < r_blocks.length) {
            r_blocks[index].dct_coefficients = computeDequantizedValue(r_blocks[index].quantization_coefficients);
            g_blocks[index].dct_coefficients = computeDequantizedValue(g_blocks[index].quantization_coefficients);
            b_blocks[index].dct_coefficients = computeDequantizedValue(b_blocks[index].quantization_coefficients);

            index += 1;
        }
    }

    // compute de-quantized value
    public double[][] computeDequantizedValue(int[][] m1) {
        double[][] result = new double[8][8];
        for(int i = 0; i < 8; i ++) {
            for(int j = 0; j < 8; j ++) {
                result[i][j] = (double)(m1[i][j] * quantization_table[i][j]);
            }
        }
        return result;
    }

    // apply inverse dct transformation
    public void inverseDctTransformation() {

        int index = 0;
        while(index < r_blocks.length) {
            // do for r channel
            double[][] temp = new double[8][8];
            temp = idct.matrixMultiplication(tranpose_dct_matrix, r_blocks[index].dct_coefficients);
            r_blocks[index].block_value = idct.matrixMultiplication(temp, dct_matrix);

            // do for g channel
            temp = idct.matrixMultiplication(tranpose_dct_matrix, g_blocks[index].dct_coefficients);
            g_blocks[index].block_value = idct.matrixMultiplication(temp, dct_matrix);

            // do for b channel
            temp = idct.matrixMultiplication(tranpose_dct_matrix, b_blocks[index].dct_coefficients);
            b_blocks[index].block_value = idct.matrixMultiplication(temp, dct_matrix);

            index += 1;
        }
    }

    // build pixel buffer for displaying
    public void buildingPixelBuffer() {

        int block_index = -1;
        int offset = image_width / 8;
        int temp = 0 - offset;

        for(int i = 0; i < image_height; i ++) {
            if(i % 8 == 0) {
                temp += offset;
            }
            for(int j = 0; j < image_width; j ++) {
                if(j % 8 == 0) {
                    block_index += 1;
                }
                Pixel pixel = new Pixel();
                int red = (int)(r_blocks[block_index].block_value[i % 8][j % 8]);
                int green = (int)(g_blocks[block_index].block_value[i % 8][j % 8]);
                int blue = (int)(b_blocks[block_index].block_value[i % 8][j % 8]);

                // test the range of color parameters
                if(red < 0) {
                    red = 0;
                }
                else if(red > 255) {
                    red = 255;
                }

                if(green < 0) {
                    green = 0;
                }
                else if(green > 255) {
                    green = 255;
                }

                if(blue < 0) {
                    blue = 0;
                }
                else if(blue > 255) {
                    blue = 255;
                }
                pixel.setRed(red);
                pixel.setGreen(green);
                pixel.setBlue(blue);
                pixel_buffer[i][j] = pixel;
            }
            block_index = temp - 1;
        }
    }
}
