package Error;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class Reporter {
    private final BufferedWriter output;
    private final ArrayList<String> errorList = new ArrayList<>();

    public Reporter(BufferedWriter output) {
        this.output = output;
    }

    public void report(Error error, BigInteger line){
        System.out.println(line + " " + error);
        errorList.add(line + " " + error);
    }

    public void print() throws IOException {
        errorList.sort(new ListComparator());
        for (String error : errorList) {
            output.write(error+"\n");
        }
    }

    public boolean hasError(){
        return !errorList.isEmpty();
    }
}
