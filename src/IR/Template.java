package IR;

import java.util.ArrayList;

public class Template {
    private final SymbolType type;
    private final String name;
    private Operand dim1;
    private Operand dim2;
    private final ArrayList<Operand> initVal;   // 初值表
    private boolean hasRet;    // 是否有返回值
    private final ArrayList<Operand> paramDimList;  // 参数维度表

    public Template(String name, Operand dim1, Operand dim2, boolean isConst,
                    ArrayList<Operand> initVal) {
        this.type = isConst ? SymbolType.CONST : SymbolType.VAR;
        this.name = name;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.initVal = initVal;
        this.paramDimList = new ArrayList<>();
    }

    public Template(String name, boolean hasRet, ArrayList<Operand> paramDimList) {
        this.type = SymbolType.FUNC;
        this.name = name;
        this.hasRet = hasRet;
        this.initVal = new ArrayList<>();
        this.paramDimList = paramDimList;
    }

    public Template(String name, Operand dim1, Operand dim2) {
        this.type = SymbolType.PARAM;
        this.name = name;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.initVal = new ArrayList<>();
        this.paramDimList = new ArrayList<>();
    }



    public SymbolType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Operand getDim1() {
        return dim1;
    }

    public Operand getDim2() {
        return dim2;
    }

    public boolean getHasRet() {
        return hasRet;
    }

    public void addParamDim(Operand dim) {
        paramDimList.add(dim);
    }

    public int getParamNum() {
        return paramDimList.size();
    }

    public Operand getParamDim(int index) {
        return paramDimList.get(index);
    }

    public ArrayList<Operand> getInitVal() {
        return initVal;
    }

    public Operand getInitVal(int index) {
        return initVal.get(index);
    }

    @Override
    public String toString() {
        return switch (type) {
            case VAR, CONST -> String.format("%s %s [%s][%s] = %s", type, name, dim1, dim2, initVal);
            case FUNC -> String.format("%s %s (%s)", type, name, paramDimList);
            case PARAM -> String.format("%s %s [%s][%s]", type, name, dim1, dim2);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }


}
