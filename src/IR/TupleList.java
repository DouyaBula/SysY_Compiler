package IR;

// single instance of tuple list

import java.util.ArrayList;

public class TupleList {
    private static TupleList tupleList;
    private ArrayList<Tuple> tuples;

    public static TupleList getInstance() {
        if (tupleList == null) {
            tupleList = new TupleList();
            tupleList.tuples = new ArrayList<>();
        }
        return tupleList;
    }

    public Tuple getTuple(int index) {
        return tuples.get(index);
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }

    public void printTuples() {
        for (Tuple tuple : tuples) {
            System.out.println(tuple);
        }
    }

    private void addTuple(Operator operator, Operand operand1, Operand operand2,
                          Operand result, int line) {
        Tuple tuple = new Tuple(operator, operand1, operand2, result, line);
//        System.out.println(tuple);
        tuples.add(tuple);
    }

    public void addLabel(String label, int line) {
        Operand labelOperand = Operand.getLabelOperand(label);
        addTuple(Operator.LABEL, labelOperand, null, null, line);
    }

    public void addLabel(Operand labelOperand, int line) {
        addTuple(Operator.LABEL, labelOperand, null, null, line);
    }

    public void addDef(String name, int line) {
        addTuple(Operator.DEF, Operand.getDefOperand(name), null, null, line);
    }

    public void addAssign(Operand result, Operand exp, int line) {
        addTuple(Operator.ASSIGN, exp, null, result, line);
    }

    public void addGoto(Operand label, int line) {
        addTuple(Operator.GOTO, label, null, null, line);
    }

    public void addJumpTrue(Operand operand, Operand label, int line) {
        addTuple(Operator.JUMPTRUE, operand, label, null, line);
    }

    public void addJumpFalse(Operand operand, Operand label, int line) {
        addTuple(Operator.JUMPFALSE, operand, label, null, line);
    }

    public void addReturn(Operand operand, int line) {
        addTuple(Operator.RETURN, operand, null, null, line);
    }

    public void addPushAR(int line) {
        addTuple(Operator.PUSHAR, null, null, null, line);
    }

    public void addPopAR(int line) {
        addTuple(Operator.POPAR, null, null, null, line);
    }

    public void addExit(int line) {
        addTuple(Operator.EXIT, null, null, null, line);
    }

    public void addRead(Operand operand, int line) {
        addTuple(Operator.READ, operand, null, null, line);
    }

    public void addPrint(String str, int line) {
        Operand strOperand = Operand.getStrOperand(str);
        addTuple(Operator.PRINT, strOperand, null, null, line);
    }

    public void addPrint(Operand operand, int line) {
        addTuple(Operator.PRINT, operand, null, null, line);
    }

    public void addEq(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.EQ, operand1, operand2, result, line);
    }

    public void addNeq(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.NEQ, operand1, operand2, result, line);
    }

    public void addLt(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.LT, operand1, operand2, result, line);
    }

    public void addGt(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.GT, operand1, operand2, result, line);
    }

    public void addLeq(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.LEQ, operand1, operand2, result, line);
    }

    public void addGeq(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.GEQ, operand1, operand2, result, line);
    }

    public void addAdd(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.ADD, operand1, operand2, result, line);
    }

    public void addSub(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.SUB, operand1, operand2, result, line);
    }

    public void addMul(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.MUL, operand1, operand2, result, line);
    }

    public void addDiv(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.DIV, operand1, operand2, result, line);
    }

    public void addMod(Operand operand1, Operand operand2, Operand result, int line) {
        addTuple(Operator.MOD, operand1, operand2, result, line);
    }

    public void addPush(Operand param, Operand targetTable, int line) {
        addTuple(Operator.PUSH, param, null, targetTable, line);
    }

    // has return value
    public void addCall(Operand func, Operand result, int line) {
        addTuple(Operator.CALL, func, null, result, line);
    }

    // no return value
    public void addCall(Operand func, int line) {
        addTuple(Operator.CALL, func, null, null, line);
    }

    public void addNeg(Operand operand1, Operand result, int line) {
        addTuple(Operator.NEG, operand1, null, result, line);
    }

    public void addNot(Operand operand1, Operand result, int line) {
        addTuple(Operator.NOT, operand1, null, result, line);
    }

    public void addLoad(Operand base, Operand offset, Operand dest, int line) {
        addTuple(Operator.LOAD, base, offset, dest, line);
    }

    public void addLoadAddr(Operand base, Operand offset, Operand dest, int line) {
        addTuple(Operator.LOADADDR, base, offset, dest, line);
    }

    public void addStore(Operand base, Operand offset, Operand source, int line) {
        addTuple(Operator.STORE, base, offset, source, line);
    }

}
