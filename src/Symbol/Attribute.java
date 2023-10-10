package Symbol;

import java.math.BigInteger;
import java.util.ArrayList;

public class Attribute {
    private BigInteger line;
    private Table table;
    private String name;
    private Type type;
    private int dimCnt;
    private BigInteger dim1;
    private BigInteger dim2;
    private Type reType;
    private ArrayList<Integer> paramDimList;

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

    public int getParamDim(int index){
        return paramDimList.get(index);
    }

    public int getDimCnt() {
        return dimCnt;
    }
}