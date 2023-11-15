package IR;

import java.util.HashMap;

public class Operand {
    private OperandType type;   // const, var, temp, label
    private int constVal; // const
    private String name;
    private boolean isOffset;

    private Operand(OperandType type, int constVal, String name) {
        this.type = type;
        this.constVal = constVal;
        this.name = name;
        this.isOffset = false;
    }

    public void setOffset(boolean isOffset) {
        this.isOffset = isOffset;
    }

    public boolean isOffset() {
        return isOffset;
    }

    public String getName() {
        return name;
    }

    public int getConstVal() {
        return constVal;
    }

    public boolean isConst() {
        return type == OperandType.CONSTVAL;
    }

    public OperandType getType() {
        return type;
    }

    // factory pattern
    public static int tempCnt = 0;
    public static HashMap<String, Integer> labelCntMap = new HashMap<>();

    public static Operand getConstOperand(int constVal) {
        return new Operand(OperandType.CONSTVAL, constVal, null);
    }

    public static Operand getDefOperand(String def) {
        return new Operand(OperandType.DEF, -1, def);
    }

    public static Operand getTempOperand() {
        return new Operand(OperandType.TEMP, -1, "t" + tempCnt++);
    }

    public static Operand getLabelOperand(String label) {
        return new Operand(OperandType.LABEL, -1, label);
    }

    public static Operand getAutoLabelOperand(String label) {
        int labelCnt;
        if (labelCntMap.containsKey(label)) {
            labelCnt = labelCntMap.get(label);
        } else {
            labelCnt = 0;
            labelCntMap.put(label, 0);
        }
        labelCntMap.put(label, labelCnt + 1);
        return new Operand(OperandType.LABEL, -1, label + "_" + labelCnt);
    }

    public static Operand getStrOperand(String str) {
        return new Operand(OperandType.STR, -1, str);
    }


    @Override
    public String toString() {
        return type == OperandType.CONSTVAL ? String.valueOf(constVal) : name;
    }
}
