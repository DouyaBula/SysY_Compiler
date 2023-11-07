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

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }

    public void print() {
        for (Tuple tuple : tuples) {
            System.out.println(tuple);
        }
    }

    private void addTuple(Operator operator, Operand operand1, Operand operand2, Operand result) {
        tuples.add(new Tuple(operator, operand1, operand2, result));
    }

    public void addLabel(String label) {
        Operand labelOperand = Operand.getLabelOperand(label);
        addTuple(Operator.LABEL, labelOperand, null, null);
    }

    public void addLabel(Operand labelOperand) {
        addTuple(Operator.LABEL, labelOperand, null, null);
    }

    public void addDef(String name) {
        addTuple(Operator.DEF, Operand.getDefOperand(name), null, null);
    }

    public void addAssign(Operand operand1, Operand operand2) {
        addTuple(Operator.ASSIGN, operand1, operand2, null);
    }

    public void addGoto(Operand label) {
        addTuple(Operator.GOTO, label, null, null);
    }

    public void addJumpTrue(Operand operand, Operand label) {
        addTuple(Operator.JUMPTRUE, operand, label, null);
    }

    public void addJumpFalse(Operand operand, Operand label) {
        addTuple(Operator.JUMPFALSE, operand, label, null);
    }

    public void addReturn(Operand operand) {
        addTuple(Operator.RETURN, operand, null, null);
    }

    public void addRead(Operand operand) {
        addTuple(Operator.READ, operand, null, null);
    }

    public void addPrint(String str) {
        Operand strOperand = Operand.getStrOperand(str);
        addTuple(Operator.PRINT, strOperand, null, null);
    }

    public void addPrint(Operand operand) {
        addTuple(Operator.PRINT, operand, null, null);
    }

    public void addEq(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.EQ, operand1, operand2, result);
    }

    public void addNeq(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.NEQ, operand1, operand2, result);
    }

    public void addLt(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.LT, operand1, operand2, result);
    }

    public void addGt(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.GT, operand1, operand2, result);
    }

    public void addLeq(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.LEQ, operand1, operand2, result);
    }

    public void addGeq(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.GEQ, operand1, operand2, result);
    }

    public void addAdd(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.ADD, operand1, operand2, result);
    }

    public void addSub(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.SUB, operand1, operand2, result);
    }

    public void addMul(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.MUL, operand1, operand2, result);
    }

    public void addDiv(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.DIV, operand1, operand2, result);
    }

    public void addMod(Operand operand1, Operand operand2, Operand result) {
        addTuple(Operator.MOD, operand1, operand2, result);
    }

    public void addPush(Operand operand){
        addTuple(Operator.PUSH, operand, null, null);
    }

    public void addCall(Operand func, Operand result) {
        addTuple(Operator.CALL, func, null, result);
    }

    public void addNeg(Operand operand1, Operand result) {
        addTuple(Operator.NEG, operand1, null, result);
    }

    public void addNot(Operand operand1, Operand result) {
        addTuple(Operator.NOT, operand1, null, result);
    }

    public void addLoad(Operand base, Operand offset, Operand result) {
        addTuple(Operator.LOAD, base, offset, result);
    }

    public void addStore(Operand base, Operand offset, Operand result) {
        addTuple(Operator.STORE, base, offset, result);
    }

    public void addEnterBlock() {
        addTuple(Operator.ENTERBLOCK, null, null, null);
    }

    public void addExitBlock() {
        addTuple(Operator.EXITBLOCK, null, null, null);
    }

}
