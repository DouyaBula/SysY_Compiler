package IR;

public class Operand {
    private OperandType type;   // const, var, temp, label
    private int constVal; // const
    private String name;

    private Operand(OperandType type, int constVal, String name) {
        this.type = type;
        this.constVal = constVal;
        this.name = name;
    }

    public int getConstVal() {
        return constVal;
    }

    public OperandType getType() {
        return type;
    }

    // factory pattern
    public static int tempCnt = 0;
    public static int labelCnt = 0;

    public static Operand getConstOperand(int constVal) {
        return new Operand(OperandType.CONSTVAL, constVal, null);
    }

    public static Operand getDefOperand(String def) {
        return new Operand(OperandType.DEF, -1, def);
    }

    public static Operand getTempOperand() {
        return new Operand(OperandType.TEMP, -1, "$t" + tempCnt++);
    }

    public static Operand getLabelOperand(String label) {
        return new Operand(OperandType.LABEL, -1, label);
    }

    public static Operand getLabelOperand() {
        return new Operand(OperandType.LABEL, -1, "autoLabel" + labelCnt++);
    }

    public static Operand getStrOperand(String str) {
        return new Operand(OperandType.STR, -1, str);
    }


    @Override
    public String toString() {
        return type == OperandType.CONSTVAL ? String.valueOf(constVal) : name;
    }
}
