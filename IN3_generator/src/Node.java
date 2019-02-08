public class Node {

    String symbol;
    String codeword;
    double counts;
    Node left, right;

    // create child node
    Node(String symbol, double counts) {

        this.symbol = symbol;
        this.counts = counts;
        this.left = null;
        this.right = null;
    }

    // create parent node
    Node(Node left, Node right) {

        this.left = left;
        this.right = right;
        this.symbol = left.symbol + "|" + right.symbol;
        this.counts = left.counts + right.counts;
    }

    // codeword of node
    Node(String symbol, String codeword) {

        this.symbol = symbol;
        this.codeword = codeword;
    }
}
