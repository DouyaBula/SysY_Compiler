package Symbol;

import java.math.BigInteger;
import java.util.ArrayList;

public class Attribute {
    private final BigInteger line;
    private final Table table;
    private final String name;
    private final Type type;
    private int dimCnt;
    private Type reType;
    private final ArrayList<Integer> paramDimList;

    public Attribute(BigInteger line, Table table, String name, Type type) {
        this.line = line;
        this.table = table;
        this.name = name;
        this.type = type;
        this.paramDimList = new ArrayList<>();
        this.dimCnt = 0;
    }

    public void addDim() {
        this.dimCnt++;
    }
    public void setDim(int dim) {
        this.dimCnt = dim;
    }

    public void setReType(Type reType) {
        this.reType = reType;
    }


    public void addParamType(int paramType) {
        paramDimList.add(paramType);
    }

    public BigInteger getLine() {
        return line;
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Type getReType() {
        return reType;
    }

    public int getParamNum() {
        return paramDimList.size();
    }

    public int getParamDim(int paramIndex) {
        if (paramIndex < paramDimList.size()) {
            return paramDimList.get(paramIndex);
        } else {
            return -1;
        }
    }

    public int getDimCnt() {
        return dimCnt;
    }
}
