package MIPS;

import IR.Operand;
import IR.OperandType;
import IR.SymbolTable;
import IR.SymbolType;
import IR.TableTree;
import IR.Template;
import IR.Tuple;
import IR.TupleList;

import java.util.ArrayList;
import java.util.HashMap;

// 不考虑代码优化, 不考虑寄存器分配, 只使用内存
public class Generator {
    private final ArrayList<String> mipsCode;
    private final HashMap<SymbolTable, Integer> table2Offset;
    private final HashMap<String, Integer> temp2Offset;
    private int tableMaxOffset;
    private final HashMap<Operand, String> operand2Reg;
    private int loop;
    private static final int TEMPSIZE = 4096;


    public Generator() {
        mipsCode = new ArrayList<>();
        table2Offset = new HashMap<>();
        temp2Offset = new HashMap<>();
        operand2Reg = new HashMap<>();
        loop = 1;
        tableMaxOffset = 0;
    }

    private String getReg(Operand operand, boolean isRVal) {
        operand2Reg.put(operand, "$t" + loop);
        loop = loop + 1 > 4 ? 1 : loop + 1;
        if (operand.getType() == OperandType.CONSTVAL) {
            mipsCode.add(generalCode(
                    "li", operand2Reg.get(operand), String.valueOf(operand.getConstVal())));
        } else if (operand.getType() == OperandType.DEF) {
            Template template = TableTree.getInstance().getTemplate(operand.getName());
            if (template.getType() == SymbolType.VAR) {
                if (template.isGlobal()) {
                    mipsCode.add(generalCode(
                            "lw", operand2Reg.get(operand), operand.getName()));
                } else {
                    int offset = table2Offset.get(template.getBelongTable())
                            + template.getOffset();
                    mipsCode.add(generalCode(
                            "lw", operand2Reg.get(operand), -offset + "($k0)"));
                }
            }
        } else if (operand.getType() == OperandType.TEMP) {
            if (!temp2Offset.containsKey(operand.toString())) {
                temp2Offset.put(operand.toString(), temp2Offset.size() * 4);
            }
            if (isRVal) {
                mipsCode.add(generalCode(
                        "lw", operand2Reg.get(operand),
                        temp2Offset.get(operand.toString()) + "($k1)"));
            }
        }
        return operand2Reg.get(operand);
    }

    private void saveRegs() {
        // save $t1 ~ $t4
        mipsCode.add(generalCode(
                "sw", "$t1", "-4($sp)"));
        mipsCode.add(generalCode(
                "sw", "$t2", "-8($sp)"));
        mipsCode.add(generalCode(
                "sw", "$t3", "-12($sp)"));
        mipsCode.add(generalCode(
                "sw", "$t4", "-16($sp)"));
        // save $ra
        mipsCode.add(generalCode(
                "sw", "$ra", "-20($sp)"));
        // save $fp
        mipsCode.add(generalCode(
                "sw", "$fp", "-24($sp)"));
        // save $gp
        mipsCode.add(generalCode(
                "sw", "$gp", "-28($sp)"));
        // save $v0
        mipsCode.add(generalCode(
                "sw", "$v0", "-32($sp)"));
        //over
        mipsCode.add(generalCode(
                "subiu", "$sp", "$sp", "32"));
    }

    private void restoreRegs() {
        // restore $sp
        mipsCode.add(generalCode(
                "addiu", "$sp", "$sp", "32"));
        // restore $v0
        mipsCode.add(generalCode(
                "lw", "$v0", "-32($sp)"));
        // restore $gp
        mipsCode.add(generalCode(
                "lw", "$gp", "-28($sp)"));
        // restore $fp
        mipsCode.add(generalCode(
                "lw", "$fp", "-24($sp)"));
        // restore $ra
        mipsCode.add(generalCode(
                "lw", "$ra", "-20($sp)"));
        // restore $t4
        mipsCode.add(generalCode(
                "lw", "$t4", "-16($sp)"));
        // restore $t3
        mipsCode.add(generalCode(
                "lw", "$t3", "-12($sp)"));
        // restore $t2
        mipsCode.add(generalCode(
                "lw", "$t2", "-8($sp)"));
        // restore $t1
        mipsCode.add(generalCode(
                "lw", "$t1", "-4($sp)"));
    }

    public void generate() {
        generateDataPart();
        generateTextPart();
        // debug, print
        for (String code : mipsCode) {
            System.out.println(code);
        }
    }

    // 分配空间
    private void allocateSpace() {
        SymbolTable rootTable = TableTree.getInstance().getTable(0);
        // 不用分配全局变量空间, 因为全局变量直接存储在数据段
        ArrayList<SymbolTable> queue = new ArrayList<>(rootTable.getChildren());
        while (!queue.isEmpty()) {
            SymbolTable table = queue.remove(0);
            table2Offset.put(table, tableMaxOffset);
            tableMaxOffset += table.getSize();
            queue.addAll(table.getChildren());
        }
        // k0用来保存符号表变量地址, 增长方向由高到低
        mipsCode.add(generalCode(
                "move", "$k0", "$sp"));
        mipsCode.add(generalCode(
                "subiu", "$sp", "$sp", String.valueOf(tableMaxOffset)));

        // 再腾出足够大的空间保存临时变量
        // k1用来保存临时变量基地址, 增长方向由低到高
        mipsCode.add(generalCode(
                "subiu", "$sp", "$sp", String.valueOf(4 * TEMPSIZE)));
        mipsCode.add(generalCode(
                "move", "$k1", "$sp"));
        mipsCode.add("");
    }

    // 生成全局数据段
    private void generateDataPart() {
        mipsCode.add(".data");
        SymbolTable rootTable = TableTree.getInstance().getTable(0);
        for (Template template : rootTable.getContent().values()) {
            if (template.is(SymbolType.VAR)) {
                String name = template.getName();
                StringBuilder code = new StringBuilder(name + ": .word ");
                ArrayList<Operand> initVal = template.getInitVal();
                for (int i = 0; i < initVal.size(); i++) {
                    code.append(initVal.get(i).getConstVal());
                    if (i != initVal.size() - 1) {
                        code.append(", ");
                    }
                }
                mipsCode.add(code.toString());
            }
        }
        for (int i = 0; i < TableTree.getInstance().getStringPool().size(); i++) {
            String str = TableTree.getInstance().getStringPool().get(i);
            mipsCode.add("str" + i + ": .asciiz \"" + str + "\"");
        }
    }

    // 生成全局代码段
    private void generateTextPart() {
        mipsCode.add(".text");
        allocateSpace();
        for (Tuple tuple : TupleList.getInstance().getTuples()) {
            TableTree.getInstance().setCurrentTable(tuple.getBelongTable());
            // 加入原始四元式以便阅读
            mipsCode.add(tuple.toString());
            convert(tuple);
            // 加入空行以便阅读
            mipsCode.add("");
        }
    }

    private String generalCode(String op, String str1, String str2, String str3) {
        return op + " " +
                (str1 == null ? "" : str1) +
                (str2 == null ? "" : (", " + str2)) +
                (str3 == null ? "" : (", " + str3));
    }

    private String generalCode(String op, String str1, String str2) {
        return generalCode(op, str1, str2, null);
    }

    private String generalCode(String op, String str1) {
        return generalCode(op, str1, null, null);
    }

    private String generalCode(String op) {
        return generalCode(op, null, null, null);
    }

    private void generalCode1(String op, Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        Operand operand2 = tuple.getOperand2();
        Operand result = tuple.getResult();
        String str2 = getReg(operand1, true);
        String str3 = getReg(operand2, true);
        String str1 = getReg(result, false);
        mipsCode.add(generalCode(op, str1, str2, str3));
        mipsCode.add(generalCode(
                "sw", str1,
                temp2Offset.get(result.toString()) + "($k1)"));
    }


    private void convert(Tuple tuple) {
        switch (tuple.getOperator()) {
            case DEF:
                convertDEF(tuple);
                break;
            case ASSIGN:
                convertASSIGN(tuple);
                break;
            case NOT:
                convertNOT(tuple);
                break;
            case NEG:
                convertNEG(tuple);
                break;
            case POS:
                convertPOS(tuple);
                break;
            case ADD:
                convertADD(tuple);
                break;
            case SUB:
                convertSUB(tuple);
                break;
            case MUL:
                convertMUL(tuple);
                break;
            case DIV:
                convertDIV(tuple);
                break;
            case MOD:
                convertMOD(tuple);
                break;
            case AND:
                convertAND(tuple);
                break;
            case OR:
                convertOR(tuple);
                break;
            case EQ:
                convertEQ(tuple);
                break;
            case NEQ:
                convertNEQ(tuple);
                break;
            case LT:
                convertLT(tuple);
                break;
            case GT:
                convertGT(tuple);
                break;
            case LEQ:
                convertLEQ(tuple);
                break;
            case GEQ:
                convertGEQ(tuple);
                break;
            case CALL:
                convertCALL(tuple);
                break;
            case RETURN:
                convertRETURN(tuple);
                break;
            case LABEL:
                convertLABEL(tuple);
                break;
            case GOTO:
                convertGOTO(tuple);
                break;
            case JUMPTRUE:
                convertJUMPTRUE(tuple);
                break;
            case JUMPFALSE:
                convertJUMPFALSE(tuple);
                break;
            case PUSH:
                convertPUSH(tuple);
                break;
            case LOAD:
                convertLOAD(tuple);
                break;
            case STORE:
                convertSTORE(tuple);
                break;
            case READ:
                convertREAD(tuple);
                break;
            case PRINT:
                convertPRINT(tuple);
                break;
            case EXIT:
                convertEXIT(tuple);
                break;
            default:
                break;
        }
    }

    private void convertDEF(Tuple tuple) {
        Operand var = tuple.getOperand1();
        Template template = TableTree.getInstance().getTemplate(var.getName());
        if (template.getType() == SymbolType.VAR && !template.isGlobal()) {
            ArrayList<Operand> initVal = template.getInitVal();
            for (Operand operand : initVal) {
                if (operand.getType() == OperandType.CONSTVAL) {
                    mipsCode.add(generalCode(
                            "li", "$t0", String.valueOf(operand.getConstVal())));
                } else {
                    mipsCode.add(generalCode(
                            "lw", "$t0", temp2Offset.get(operand.toString()) + "($k1)"));
                }
                mipsCode.add(generalCode(
                        "sw", "$t0", -template.getOffset() + "($k0)"));
            }
        }
    }

    private void convertASSIGN(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        String result = tuple.getResult().toString();
        Template template = TableTree.getInstance().getTemplate(result);
        if (template.isGlobal()) {
            mipsCode.add(generalCode(
                    "sw", getReg(operand1, true), result + "($zero)"));
        } else {
            int offset = table2Offset.get(template.getBelongTable())
                    + template.getOffset();
            mipsCode.add(generalCode(
                    "sw", getReg(operand1, true), -offset + "($k0)"));
        }
    }

    private void convertNOT(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        Operand result = tuple.getResult();
        mipsCode.add(generalCode(
                "not", getReg(result, false), getReg(operand1, true)));
        mipsCode.add(generalCode(
                "sw", getReg(result, true),
                temp2Offset.get(result.toString()) + "($k1)"));
    }

    private void convertNEG(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        Operand result = tuple.getResult();
        mipsCode.add(generalCode(
                "negu", getReg(result, false), getReg(operand1, true)));
        mipsCode.add(generalCode(
                "sw", getReg(result, true),
                temp2Offset.get(result.toString()) + "($k1)"));
    }

    // WARNING: this should be never used
    private void convertPOS(Tuple tuple) {
        System.out.println("WARNING: POS should be never used");
    }

    private void convertADD(Tuple tuple) {
        generalCode1("addu", tuple);
    }

    private void convertSUB(Tuple tuple) {
        generalCode1("subu", tuple);
    }

    private void convertMUL(Tuple tuple) {
        generalCode1("mul", tuple);
    }

    private void convertDIV(Tuple tuple) {
        generalCode1("div", tuple);
    }

    private void convertMOD(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        Operand operand2 = tuple.getOperand2();
        Operand result = tuple.getResult();
        mipsCode.add(generalCode(
                "div", getReg(operand1, true), getReg(operand2, true)));
        mipsCode.add(generalCode(
                "mfhi", getReg(result, false)));
    }

    private void convertAND(Tuple tuple) {
        generalCode1("and", tuple);
    }

    private void convertOR(Tuple tuple) {
        generalCode1("or", tuple);
    }

    private void convertEQ(Tuple tuple) {
        generalCode1("seq", tuple);
    }

    private void convertNEQ(Tuple tuple) {
        generalCode1("sne", tuple);
    }

    private void convertLT(Tuple tuple) {
        generalCode1("slt", tuple);
    }

    private void convertGT(Tuple tuple) {
        generalCode1("sgt", tuple);
    }

    private void convertLEQ(Tuple tuple) {
        generalCode1("sle", tuple);
    }

    private void convertGEQ(Tuple tuple) {
        generalCode1("sge", tuple);
    }

    // function call
    private void convertCALL(Tuple tuple) {
        // TODO: 栈中参数转移到内存对应的位置

        saveRegs();
        // call
        mipsCode.add(generalCode(
                "jal", tuple.getOperand1().toString()));
        if (tuple.getResult() != null) {
            // return value
            mipsCode.add(generalCode(
                    "move", getReg(tuple.getResult(), false), "$v0"));
        }
    }

    private void convertRETURN(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        mipsCode.add(generalCode(
                "move", "$v0", getReg(operand1, true)));
        restoreRegs();
        // return
        mipsCode.add(generalCode(
                "jr", "$ra"));
    }

    private void convertLABEL(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        mipsCode.add(generalCode(
                operand1.toString() + ":"));
    }

    private void convertGOTO(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        mipsCode.add(generalCode(
                "j", operand1.toString()));
    }

    // WARNING: this should be never used
    private void convertJUMPTRUE(Tuple tuple) {
        System.out.println("WARNING: JUMPTRUE should be never used");
    }

    private void convertJUMPFALSE(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        Operand operand2 = tuple.getOperand2();
        mipsCode.add(generalCode(
                "beq", getReg(operand1, true), "$zero", operand2.toString()));
    }

    private void convertPUSH(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        mipsCode.add(generalCode(
                "sw", getReg(operand1, true), "-4($sp)"));
        mipsCode.add(generalCode(
                "subiu", "$sp", "$sp", "4"));
    }

    // TODO: 有问题
    private void convertLOAD(Tuple tuple) {
        Operand base = tuple.getOperand1();
        Template template = TableTree.getInstance().getTemplate(base.getName());
        Operand offset = tuple.getOperand2();
        Operand dest = tuple.getResult();
        String destReg = getReg(dest, false);
        if (template.isGlobal()) {
            mipsCode.add(generalCode(
                    "lw", destReg,
                    base.getName() + "+" + offset.getConstVal() * 4));
        } else {
            int offset1 = table2Offset.get(template.getBelongTable())
                    + template.getOffset();
            mipsCode.add(generalCode(
                    "lw", destReg,
                    (-offset1 - offset.getConstVal() * 4) + "($k0)"));
        }
        mipsCode.add(generalCode(
                "sw", destReg,
                temp2Offset.get(dest.toString()) + "($k1)"));
    }

    private void convertSTORE(Tuple tuple) {
        Operand base = tuple.getOperand1();
        Template template = TableTree.getInstance().getTemplate(base.getName());
        Operand offset = tuple.getOperand2();
        Operand src = tuple.getResult();
        String srcReg = getReg(src, true);
        if (template.isGlobal()) {
            mipsCode.add(generalCode(
                    "sw", srcReg,
                    base.getName() + "+" + offset.getConstVal() * 4));
        } else {
            int offset1 = table2Offset.get(template.getBelongTable())
                    + template.getOffset();
            mipsCode.add(generalCode(
                    "sw", srcReg,
                    (-offset1 - offset.getConstVal() * 4) + "($k0)"));
        }
    }

    private void convertREAD(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        mipsCode.add(generalCode(
                "li", "$v0", "5"));
        mipsCode.add(generalCode(
                "syscall"));
        mipsCode.add(generalCode(
                "move", getReg(operand1, false), "$v0"));
    }

    private void convertPRINT(Tuple tuple) {
        Operand operand1 = tuple.getOperand1();
        if (operand1.getType() == OperandType.CONSTVAL) {
            mipsCode.add(generalCode(
                    "li", "$v0", "1"));
            mipsCode.add(generalCode(
                    "li", "$a0", String.valueOf(operand1.getConstVal())));
        } else if (operand1.getType() == OperandType.STR) {
            mipsCode.add(generalCode(
                    "li", "$v0", "4"));
            mipsCode.add(generalCode(
                    "la", "$a0", operand1.toString().substring(1)));
        } else {
            mipsCode.add(generalCode(
                    "li", "$v0", "1"));
            mipsCode.add(generalCode(
                    "move", "$a0", getReg(operand1, true)));
        }
        mipsCode.add(generalCode(
                "syscall"));
    }

    private void convertEXIT(Tuple tuple) {
        mipsCode.add(generalCode(
                "li", "$v0", "10"));
        mipsCode.add(generalCode(
                "syscall"));
    }
}
