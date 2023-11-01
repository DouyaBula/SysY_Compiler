package IR;

public class Tuple {
    // 4-tuple
    private Operator operator;
    private Operand operand1;
    private Operand operand2;
    private Operand result;

    public Tuple(Operator operator, Operand operand1, Operand operand2, Operand result) {
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }

    public Operator getOperator() {
        return operator;
    }

    public Operand getOperand1() {
        return operand1;
    }

    public Operand getOperand2() {
        return operand2;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public String toString() {
        if (operator == Operator.LABEL){
            return operand1.toString()+": ";
        }
        return String.format("%-10s", operator) +
                (operand1 == null ? String.format("%-15s", "null") : String.format("%-15s", operand1)) +
                (operand2 == null ? String.format("%-15s", "null") : String.format("%-15s", operand2)) +
                (result == null ? String.format("%-15s", "null") : String.format("%-15s", result));
    }
}
