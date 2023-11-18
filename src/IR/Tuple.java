package IR;

public class Tuple {
    // 4-tuple
    private Operator operator;
    private Operand operand1;
    private Operand operand2;
    private Operand result;
    private SymbolTable belongTable;

    public Tuple(Operator operator, Operand operand1, Operand operand2, Operand result) {
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
        this.belongTable = TableTree.getInstance().getCurrentTable();
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

    public SymbolTable getBelongTable() {
        return belongTable;
    }

    @Override
    public String toString() {
        String str = "";
        switch (operator) {
            case LABEL:
                str = operand1 + ": ";
                break;
            case ASSIGN:
                str = result + " = " + operand1;
                break;
            case JUMPTRUE:
                str = "ifTrue " + operand1 + " goto " + operand2;
                break;
            case JUMPFALSE:
                str = "ifFalse " + operand1 + " goto " + operand2;
                break;
            case DEF, GOTO, RETURN, READ, PRINT:
                str = operator + " " + operand1;
                break;
            case PUSH:
                str = operator + " " + operand1 + " to table#" + result.getConstVal();
                break;
            case CALL:
                if (result != null) {
                    str = result + " = " + operand1 + "()";
                } else {
                    str = operand1 + "()";
                }
                break;
            case EQ, NEQ, LT, GT, LEQ, GEQ, ADD, SUB, MUL, DIV, MOD:
                str = result + " = " + operand1 + " " + operator + " " + operand2;
                break;
            case NEG, NOT, POS:
                str = result + " = " + operator + " " + operand1;
                break;
            case LOAD:
                str = result + " = " + operand1 + "[" + operand2 + "]";
                break;
            case LOADADDR:
                str = result + " = &" + operand1 +
                        (operand2 == null ? "" : "[" + operand2 + "]");
                break;
            case STORE:
                str = operand1 + "[" + operand2 + "]" + " = " + result;
                break;
            case EXIT:
                str = "exit program";
                break;
            case PUSHAR:
                str = "push AR";
                break;
            case POPAR:
                str = "pop AR";
                break;
            default:
                break;
        }
        return str;
    }
}
