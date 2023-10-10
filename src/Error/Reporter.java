package Error;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;

public class Reporter {
    BufferedWriter output;

    public Reporter(BufferedWriter output) {
        this.output = output;
    }

    // TODO: 改为按行号顺序输出
    public void report(Error error, BigInteger line){
        System.out.println("Error at line " + line + ": " + error);
        try {
            output.write(line + " " + error + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
