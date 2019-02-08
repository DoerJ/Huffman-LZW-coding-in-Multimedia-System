import java.util.ArrayList;
import java.util.List;

public class Block {

    // 8 x 8 image block
    public double block_value[][] = new double[8][8];
    // dct coefficient
    public double dct_coefficients[][] = new double[8][8];
    // quantization coefficients
    public int quantization_coefficients[][] = new int[8][8];
    // coefficient string
    public List<String> coefficient_string = new ArrayList<>();

    Block() {

    }

    public void setBlockValue(int i, int j, double value) {
        block_value[i][j] = value;
    }
}
