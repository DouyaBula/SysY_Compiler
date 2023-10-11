package Error;

import java.math.BigInteger;
import java.util.Comparator;

public class ListComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        BigInteger line1 = new BigInteger(((String)o1).split(" ")[0]);
        BigInteger line2 = new BigInteger(((String)o2).split(" ")[0]);
        return line1.compareTo(line2);
    }
}
