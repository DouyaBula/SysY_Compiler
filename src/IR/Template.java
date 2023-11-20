package IR;

import java.util.ArrayList;

public class Template {
    private final SymbolType type;
    private final String name;
    private Operand dim1;   // 必为常数
    private Operand dim2;   // 必为常数
    private final ArrayList<Operand> initVal;   // 初值表
    private boolean hasRet;    // 是否有返回值
    private final ArrayList<Operand> paramList;  // 参数表

    private SymbolTable belongTable; // 所属符号表
    // offset should only be used for var and param
    private int offset; // 相对于符号表基址的偏移量
    private int bodyId; // 函数体所在符号表的id
    private int line;   // 变量定义的行号

    public Template(String name, Operand dim1, Operand dim2, boolean isConst,
                    ArrayList<Operand> initVal, int line) {
        this.type = isConst ? SymbolType.CONST : SymbolType.VAR;
        this.name = name;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.initVal = initVal;
        this.paramList = new ArrayList<>();
        this.line = line;
    }

    public Template(String name, boolean hasRet, ArrayList<Operand> paramList, int line) {
        this.type = SymbolType.FUNC;
        this.name = name;
        this.hasRet = hasRet;
        this.initVal = new ArrayList<>();
        this.paramList = paramList;
        this.line = line;
    }

    public Template(String name, Operand dim1, Operand dim2, int line) {
        this.type = SymbolType.PARAM;
        this.name = name;
        this.dim1 = dim1;
        this.dim2 = dim2;
        this.initVal = new ArrayList<>();
        this.paramList = new ArrayList<>();
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public boolean isGlobal() {
        return belongTable.getParent() == null &&
                (type == SymbolType.VAR || type == SymbolType.CONST);
    }

    public int getDimCnt() {
        return dim1.getConstVal() == 0 ? 0 :
                (dim2.getConstVal() == 0 ? 1 : 2);
    }

    public void setBodyId(int bodyId) {
        this.bodyId = bodyId;
    }

    public int getBodyId() {
        return bodyId;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setBelongTable(SymbolTable belongTable) {
        this.belongTable = belongTable;
    }

    public SymbolTable getBelongTable() {
        return belongTable;
    }

    public boolean is(SymbolType type) {
        return this.type == type;
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

    public boolean hasRet() {
        return hasRet;
    }

    public void addParamDim(Operand dim) {
        paramList.add(dim);
    }

    public int getParamNum() {
        return paramList.size();
    }

    public ArrayList<Operand> getParamList() {
        return paramList;
    }

    public Operand getParamDim(int index) {
        return paramList.get(index);
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
            case VAR, CONST ->
                    String.format("%s %s [%s][%s] = %s", type, name, dim1, dim2, initVal);
            case FUNC -> String.format("%s %s (%s)", type, name, paramList);
            case PARAM -> String.format("%s %s [%s][%s]", type, name, dim1, dim2);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }


}
