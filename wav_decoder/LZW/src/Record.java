public class Record {

    public String s, c, string;
    public int code, output;

    // initialization process
    Record(int code, String string) {
        this.code = code;
        this.string = string;
    }

    // encoding process
    Record(String s, String c, int output, int code, String string) {
        this.s = s;
        this.c = c;
        this.output = output;
        this.code = code;
        this.string = string;
    }
}
