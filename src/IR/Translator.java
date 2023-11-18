package IR;

import Lexer.Symbol;
import Parser.Node;
import Parser.Term;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class Translator {
    private final BufferedWriter irFile;
    // parser has already ensured the correctness of the grammar tree
    // so just translate it as the grammar rules without any error handling
    private final Node root;
    private final Stack<Operand> loopTailStack = new Stack<>();
    private final Stack<Operand> loopEndStack = new Stack<>();

    // 唉, 全局变量, 我还是来了
    private boolean inMain = false;

    public Translator(Node root, BufferedWriter irFile) {
        this.root = root;
        this.irFile = irFile;
    }

    public ArrayList<Tuple> translate() {
        if (root == null) {
            return null;
        }
        translateCompUnit(root);
        return TupleList.getInstance().getTuples();
    }

    private boolean checkConst(Operand operand) {
        return operand.getType() == OperandType.CONSTVAL
                || TableTree.getInstance().getCurrentTable().getId() == 0;
    }

    private void translateCompUnit(Node node) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.Decl)) {
                translateDecl(child);
            } else if (child.is(Term.FuncDef)) {
                translateFuncDef(child);
            } else if (child.is(Term.MainFuncDef)) {
                translateMainFuncDef(child);
            }
        }
    }

    private void translateDecl(Node node) {
        Node decl = node.getChild(0);
        if (decl.is(Term.VarDecl)) {
            translateVarDecl(decl);
        } else if (decl.is(Term.ConstDecl)) {
            translateConstDecl(decl);
        }
    }

    private void translateConstDecl(Node node) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.ConstDef)) {
                translateConstDef(child);
            }
        }
    }

    private void translateConstDef(Node node) {
        String name = "firetruck";
        int dimCnt = 0;
        Operand[] dims = {Operand.getConstOperand(0), Operand.getConstOperand(0)};
        ArrayList<Operand> initVal = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Symbol.IDENFR)) {
                name = child.getToken().getRaw();
            } else if (child.is(Term.ConstExp)) {
                dims[dimCnt++] = translateConstExp(child);
            } else if (child.is(Term.ConstInitVal)) {
                initVal = translateConstInitVal(child);
            }
        }
        TableTree.getInstance().addConstDef(name, dims[0], dims[1], initVal);
        TupleList.getInstance().addDef(name);
    }

    private ArrayList<Operand> translateConstInitVal(Node node) {
        ArrayList<Operand> initVal = new ArrayList<>();
        if (node.getFirstChild().is(Term.ConstExp)) {
            initVal.add(translateConstExp(node.getFirstChild()));
        } else {
            for (Node child : node.getChildren()) {
                if (child.is(Term.ConstInitVal)) {
                    initVal.addAll(translateConstInitVal(child));
                }
            }
        }
        return initVal;
    }

    private void translateVarDecl(Node node) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.VarDef)) {
                translateVarDef(child);
            }
        }
    }

    private void translateVarDef(Node node) {
        String name = "firetruck";
        int dimCnt = 0;
        Operand[] dims = {Operand.getConstOperand(0), Operand.getConstOperand(0)};
        ArrayList<Operand> initVal = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Symbol.IDENFR)) {
                name = child.getToken().getRaw();
            } else if (child.is(Term.ConstExp)) {
                dims[dimCnt++] = translateConstExp(child);
            } else if (child.is(Term.InitVal)) {
                initVal = translateInitVal(child);
            }
        }
        TableTree.getInstance().addVarDef(name, dims[0], dims[1], initVal);
        TupleList.getInstance().addDef(name);
    }

    private ArrayList<Operand> translateInitVal(Node node) {
        ArrayList<Operand> initVal = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Term.Exp)) {
                initVal.add(translateExp(child));
            } else if (child.is(Term.InitVal)) {
                initVal.addAll(translateInitVal(child));
            }
        }
        return initVal;
    }

    private void translateFuncDef(Node node) {
        String name = "firetruck";
        boolean hasRet = false;
        ArrayList<Operand> paramList = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Term.FuncType)) {
                hasRet = child.getChild(0).is(Symbol.INTTK);
            } else if (child.is(Symbol.IDENFR)) {
                name = child.getToken().getRaw();
                TableTree.getInstance().enterBlock();
                TupleList.getInstance().addLabel(name + "_BEGIN");
                TupleList.getInstance().addPushAR();
            } else if (child.is(Term.FuncFParams)) {
                paramList = translateFuncFParams(child);
            } else if (child.is(Term.Block)) {
                TableTree.getInstance().addFuncDefToParent(name, hasRet, paramList);
                translateBlock(child);
                TupleList.getInstance().addLabel(name + "_END");
                if (!hasRet) {
                    TupleList.getInstance().addPopAR();
                }
                TableTree.getInstance().exitBlock();
            }
        }
    }

    private void translateMainFuncDef(Node node) {
        String name = "main";
        for (Node child : node.getChildren()) {
            if (child.is(Term.Block)) {
                TableTree.getInstance().addFuncDef(name, false, new ArrayList<>());
                TableTree.getInstance().enterBlock();
                TupleList.getInstance().addLabel(name + "_BEGIN");
                TupleList.getInstance().addPushAR();
                inMain = true;
                translateBlock(child);
                TupleList.getInstance().addLabel(name + "_END");
                inMain = false;
                TableTree.getInstance().exitBlock();
            }
        }
    }

    private ArrayList<Operand> translateFuncFParams(Node node) {
        ArrayList<Operand> paramList = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Term.FuncFParam)) {
                paramList.add(translateFuncFParam(child));
            }
        }
        return paramList;
    }

    private Operand translateFuncFParam(Node node) {
        String name = "firetruck";
        int dimCnt = 0;
        Operand[] dims = {Operand.getConstOperand(0), Operand.getConstOperand(0)};
        for (Node child : node.getChildren()) {
            if (child.is(Symbol.IDENFR)) {
                name = child.getToken().getRaw();
            } else if (child.is(Symbol.LBRACK)) {
                dims[dimCnt++] = Operand.getConstOperand(0);
            } else if (child.is(Term.ConstExp)) {
                dims[1] = translateConstExp(child);
            }
        }
        // 数组形参的第一个维度总是为空, 这里用114514来标识
        if (dimCnt > 0) {
            dims[0] = Operand.getConstOperand(114514);
        }
        TableTree.getInstance().addFuncParam(name, dims[0], dims[1]);
        return Operand.getDefOperand(name);
    }

    private void translateBlock(Node node) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.BlockItem)) {
                translateBlockItem(child);
            }
        }
    }

    private void translateBlockItem(Node node) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.Decl)) {
                translateDecl(child);
            } else if (child.is(Term.Stmt)) {
                translateStmt(child);
            }
        }
    }

    private void translateStmt(Node node) {
        if (node.contains(Term.LVal, Symbol.ASSIGN, Term.Exp)) {
            translateStmt_Assign(node);
        } else if (node.contains(Term.Exp)) {
            translateExp(node.getChild(0));
        } else if (node.contains(Term.Block)) {
            TableTree.getInstance().enterBlock();
            translateBlock(node.getChild(0));
            TableTree.getInstance().exitBlock();
        } else if (node.contains(Symbol.IFTK)) {
            translateStmt_If(node);
        } else if (node.contains(Symbol.FORTK)) {
            translateStmt_For(node);
        } else if (node.contains(Symbol.BREAKTK)) {
            TupleList.getInstance().addGoto(loopEndStack.peek());
        } else if (node.contains(Symbol.CONTINUETK)) {
            TupleList.getInstance().addGoto(loopTailStack.peek());
        } else if (node.contains(Symbol.RETURNTK)) {
            if (inMain) {
                TupleList.getInstance().addExit();
            } else {
                Operand ret = node.getChild(1).is(Term.Exp) ? translateExp(node.getChild(1)) : null;
                TupleList.getInstance().addReturn(ret);
            }
        } else if (node.contains(Term.LVal, Symbol.ASSIGN, Symbol.GETINTTK)) {
            String name = node.getChild(0).getChild(0).getToken().getRaw();
            Operand lval = Operand.getDefOperand(name);
            Operand offset = translateLVal(node.getChild(0), true);
            if (!offset.isOffset()) {
                TupleList.getInstance().addRead(lval);
            } else {
                Operand temp = Operand.getTempOperand();
                TupleList.getInstance().addRead(temp);
                TupleList.getInstance().addStore(lval, offset, temp);
            }
        } else if (node.contains(Symbol.PRINTFTK)) {
            translateStmt_Print(node);
        }
    }

    private void translateStmt_Assign(Node node) {
        String name = null;
        Operand offset = null;
        Operand lVal = null;
        Operand exp = null;
        for (Node child : node.getChildren()) {
            if (child.is(Term.LVal)) {
                name = child.getChild(0).getToken().getRaw();
                lVal = Operand.getDefOperand(name);
                offset = translateLVal(child, true);
            } else if (child.is(Term.Exp)) {
                exp = translateExp(child);
            }
        }
        if (!offset.isOffset()) {
            TupleList.getInstance().addAssign(lVal, exp);
        } else {
            TupleList.getInstance().addStore(lVal, offset, exp);
        }
    }

    private void translateStmt_If(Node node) {
        Operand bodyLabel = Operand.getAutoLabelOperand("IfBody");
        Operand endLabel = Operand.getAutoLabelOperand("IfEnd");
        Operand elseLabel = Operand.getAutoLabelOperand("IfElse");
        boolean hasElse = false;
        for (Node child : node.getChildren()) {
            if (child.is(Symbol.ELSETK)) {
                hasElse = true;
                break;
            }
        }
        for (Node child : node.getChildren()) {
            if (child.is(Term.Cond)) {
                translateCond(child, bodyLabel, hasElse ? elseLabel : endLabel);
            } else if (child.is(Symbol.RPARENT)) {
                TupleList.getInstance().addLabel(bodyLabel);
            } else if (child.is(Symbol.ELSETK)) {
                TupleList.getInstance().addGoto(endLabel);
                TupleList.getInstance().addLabel(elseLabel);
            } else if (child.is(Term.Stmt)) {
                translateStmt(child);
            }
        }
        TupleList.getInstance().addLabel(endLabel);
    }

    private void translateStmt_For(Node node) {
        Operand beginLabel = Operand.getAutoLabelOperand("ForBegin");
        Operand bodyLabel = Operand.getAutoLabelOperand("ForBody");
        Operand tailLabel = Operand.getAutoLabelOperand("ForTail");
        Operand endLabel = Operand.getAutoLabelOperand("ForEnd");
        loopTailStack.push(tailLabel);
        loopEndStack.push(endLabel);
        int i;
        for (i = 0; i < node.getChildren().size(); i++) {
            if (node.getChild(i).is(Term.ForStmt)) {
                translateForStmt(node.getChild(i));
            }
            if (node.getChild(i).is(Symbol.SEMICN)) {
                i++;
                break;
            }
        }
        TupleList.getInstance().addLabel(beginLabel);
        for (; i < node.getChildren().size(); i++) {
            if (node.getChild(i).is(Term.Cond)) {
                translateCond(node.getChild(i), bodyLabel, endLabel);
            }
            if (node.getChild(i).is(Symbol.SEMICN)) {
                i++;
                break;
            }
        }
        int j = i;
        TupleList.getInstance().addLabel(bodyLabel);
        for (; i < node.getChildren().size(); i++) {
            if (node.getChild(i).is(Term.Stmt)) {
                translateStmt(node.getChild(i));
            }
        }
        TupleList.getInstance().addLabel(tailLabel);
        for (; j < node.getChildren().size(); j++) {
            if (node.getChild(j).is(Term.ForStmt)) {
                translateForStmt(node.getChild(j));
            }
        }
        TupleList.getInstance().addGoto(beginLabel);
        TupleList.getInstance().addLabel(endLabel);
        loopTailStack.pop();
        loopEndStack.pop();
    }

    private void translateStmt_Print(Node node) {
        String str;
        String[] splitStr = new String[0];
        ArrayList<Operand> expList = new ArrayList<>();
        for (Node child : node.getChildren()) {
            if (child.is(Symbol.STRCON)) {
                str = child.getToken().getRaw();
                // remove \"
                str = str.substring(1, str.length() - 1);
                splitStr = str.split("%d", -1);
            } else if (child.is(Term.Exp)) {
                expList.add(translateExp(child));
            }
        }
        // traverse the string and add the string and the exp alternatively
        int i = 0;
        int j = 0;
        for (; i < splitStr.length; i++) {
            if (!splitStr[i].isEmpty()) {
                int strId = TableTree.getInstance().addString(splitStr[i]);
                TupleList.getInstance().addPrint("#str" + strId);
            }
            if (j < expList.size()) {
                TupleList.getInstance().addPrint(expList.get(j++));
            }
        }
    }


    private void translateForStmt(Node node) {
        translateStmt_Assign(node);
    }

    private Operand translateExp(Node node) {
        return translateAddExp(node.getChild(0));
    }

    // need short-circuit evaluation
    private void translateCond(Node node, Operand trueLabel, Operand falseLabel) {
        Node lOrExp = node.getChild(0);
        translateLOrExp(lOrExp, trueLabel, falseLabel);
        TupleList.getInstance().addGoto(falseLabel);
    }

    private Operand translateLVal(Node node, boolean isLeft) {
        Operand result = null;
        String name = null;
        int realDimCnt = 0;
        int dimCnt = 0;
        Operand[] dims = {Operand.getConstOperand(0), Operand.getConstOperand(0)};
        for (Node child : node.getChildren()) {
            if (child.is(Term.Exp)) {
                dims[dimCnt++] = translateExp(child);
            } else if (child.is(Symbol.IDENFR)) {
                name = child.getToken().getRaw();
                Template template = TableTree.getInstance().getTemplate(name);
                if (template != null) {
                    realDimCnt = template.getDimCnt();
                }
            }
        }
        // 标记返回的operand是否作为offset使用
        dims[0].setOffset(false);
        if (dimCnt == 0) {
            // 检测是否与数组维数相同
            if (dimCnt == realDimCnt) {
                // 加载值
                result = Operand.getDefOperand(name);
            } else {
                // 加载地址
                result = Operand.getTempOperand();
                TupleList.getInstance().addLoadAddr(Operand.getDefOperand(name), null, result);
            }
        } else if (dimCnt == 1) {
            result = Operand.getTempOperand();
            Operand def = Operand.getDefOperand(name);
            if (!isLeft) {
                // 检测是否与数组维数相同
                if (dimCnt == realDimCnt) {
                    // 加载值
                    TupleList.getInstance().addLoad(def, dims[0], result);
                } else {
                    // 加载地址
                    TupleList.getInstance().addLoadAddr(def, dims[0], result);
                }
            } else {
                dims[0].setOffset(true);
                return dims[0];
            }
        } else if (dimCnt == 2) {
            result = Operand.getTempOperand();
            Operand def = Operand.getDefOperand(name);
            Operand offset = Operand.getTempOperand();
            Operand dimSize =
                    TableTree.getInstance().getTemplate(name).getDim2();
            TupleList.getInstance().addMul(dims[0], dimSize, offset);
            TupleList.getInstance().addAdd(offset, dims[1], offset);
            if (!isLeft) {
                TupleList.getInstance().addLoad(def, offset, result);
            } else {
                offset.setOffset(true);
                return offset;
            }
        }
        // 不可能出现dimCnt > 2的情况, 该句无法到达
        return result;
    }

    private Operand translatePrimaryExp(Node node) {
        Operand result = null;
        if (node.contains(Symbol.LPARENT)) {
            result = translateExp(node.getChild(1));
        } else if (node.contains(Term.LVal)) {
            result = translateLVal(node.getChild(0), false);
        } else if (node.contains(Term.Number)) {
            result = translateNumber(node.getChild(0));
        }
        return result;
    }

    private Operand translateNumber(Node node) {
        return Operand.getConstOperand(Integer.parseInt(
                node.getChild(0).getToken().getRaw()));
    }

    private Operand translateUnaryExp(Node node) {
        Operand result = null;
        if (node.contains(Term.PrimaryExp)) {
            result = translatePrimaryExp(node.getChild(0));
        } else if (node.contains(Symbol.IDENFR)) {
            Template func = TableTree.getInstance().getTemplate(
                    node.getChild(0).getToken().getRaw());
            boolean hasRet = func != null && func.hasRet();
            if (hasRet) {
                result = Operand.getTempOperand();
            }
            String name = node.getChild(0).getToken().getRaw();
            if (node.getChild(2).is(Term.FuncRParams)) {
                translateFuncRParams(node.getChild(2), func);
            }
            Operand def = Operand.getDefOperand(name);
            if (hasRet) {
                TupleList.getInstance().addCall(def, result);
            } else {
                TupleList.getInstance().addCall(def);
            }
        } else if (node.contains(Term.UnaryOp)) {
            Node unaryOp = node.getChild(0).getChild(0);
            Node unaryExp = node.getChild(1);
            if (unaryOp.is(Symbol.PLUS)) {
                result = translateUnaryExp(unaryExp);
            } else if (unaryOp.is(Symbol.MINU)) {
                Operand op = translateUnaryExp(unaryExp);
                if (checkConst(op)) {
                    result = Operand.getConstOperand(-op.getConstVal());
                } else {
                    result = Operand.getTempOperand();
                    TupleList.getInstance().addNeg(op, result);
                }
            } else if (unaryOp.is(Symbol.NOT)) {
                result = Operand.getTempOperand();
                Operand op = translateUnaryExp(unaryExp);
                TupleList.getInstance().addNot(op, result);
            }
        }
        return result;
    }

    private void translateFuncRParams(Node node, Template func) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.Exp)) {
                Operand param = translateExp(child);
                TupleList.getInstance().addPush(param,
                        Operand.getConstOperand(func.getBodyId()));
            }
        }
    }

    private Operand translateMulExp(Node node) {
        Operand result = null;
        if (node.getChildren().size() == 1) {
            result = translateUnaryExp(node.getChild(0));
        } else {
            Operand op1 = translateMulExp(node.getChild(0));
            Operand op2 = translateUnaryExp(node.getChild(2));
            boolean isConst = checkConst(op1) && checkConst(op2);
            if (!isConst) {
                result = Operand.getTempOperand();
                if (node.getChild(1).is(Symbol.MULT)) {
                    TupleList.getInstance().addMul(op1, op2, result);
                } else if (node.getChild(1).is(Symbol.DIV)) {
                    TupleList.getInstance().addDiv(op1, op2, result);
                } else if (node.getChild(1).is(Symbol.MOD)) {
                    TupleList.getInstance().addMod(op1, op2, result);
                }
            } else {
                int val1 = op1.getConstVal();
                int val2 = op2.getConstVal();
                if (node.getChild(1).is(Symbol.MULT)) {
                    result = Operand.getConstOperand(val1 * val2);
                } else if (node.getChild(1).is(Symbol.DIV)) {
                    result = Operand.getConstOperand(val1 / val2);
                } else if (node.getChild(1).is(Symbol.MOD)) {
                    result = Operand.getConstOperand(val1 % val2);
                }
            }
        }
        return result;
    }

    private Operand translateAddExp(Node node) {
        Operand result = null;
        if (node.getChildren().size() == 1) {
            result = translateMulExp(node.getChild(0));
        } else {
            Operand op1 = translateAddExp(node.getChild(0));
            Operand op2 = translateMulExp(node.getChild(2));
            boolean isConst = checkConst(op1) && checkConst(op2);
            if (!isConst) {
                result = Operand.getTempOperand();
                if (node.getChild(1).is(Symbol.PLUS)) {
                    TupleList.getInstance().addAdd(op1, op2, result);
                } else if (node.getChild(1).is(Symbol.MINU)) {
                    TupleList.getInstance().addSub(op1, op2, result);
                }
            } else {
                int val1 = op1.getConstVal();
                int val2 = op2.getConstVal();
                if (node.getChild(1).is(Symbol.PLUS)) {
                    result = Operand.getConstOperand(val1 + val2);
                } else if (node.getChild(1).is(Symbol.MINU)) {
                    result = Operand.getConstOperand(val1 - val2);
                }
            }
        }
        return result;
    }

    private Operand translateRelExp(Node node) {
        Operand result;
        if (node.getChildren().size() == 1) {
            result = translateAddExp(node.getChild(0));
        } else {
            result = Operand.getTempOperand();
            Operand op1 = translateRelExp(node.getChild(0));
            Operand op2 = translateAddExp(node.getChild(2));
            if (node.getChild(1).is(Symbol.LSS)) {
                TupleList.getInstance().addLt(op1, op2, result);
            } else if (node.getChild(1).is(Symbol.LEQ)) {
                TupleList.getInstance().addLeq(op1, op2, result);
            } else if (node.getChild(1).is(Symbol.GRE)) {
                TupleList.getInstance().addGt(op1, op2, result);
            } else if (node.getChild(1).is(Symbol.GEQ)) {
                TupleList.getInstance().addGeq(op1, op2, result);
            }
        }
        return result;
    }

    private Operand translateEqExp(Node node) {
        Operand result;
        if (node.getChildren().size() == 1) {
            result = translateRelExp(node.getChild(0));
        } else {
            result = Operand.getTempOperand();
            Operand op1 = translateEqExp(node.getChild(0));
            Operand op2 = translateRelExp(node.getChild(2));
            if (node.getChild(1).is(Symbol.EQL)) {
                TupleList.getInstance().addEq(op1, op2, result);
            } else if (node.getChild(1).is(Symbol.NEQ)) {
                TupleList.getInstance().addNeq(op1, op2, result);
            }
        }
        return result;
    }

    private void translateLAndExp(Node node, Operand subFalseLabel) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.EqExp)) {
                Operand eqExp = translateEqExp(child);
                TupleList.getInstance().addJumpFalse(eqExp, subFalseLabel);
            } else if (child.is(Term.LAndExp)) {
                translateLAndExp(child, subFalseLabel);
            }
        }
    }

    private void translateLOrExp(Node node, Operand trueLabel, Operand falseLabel) {
        for (Node child : node.getChildren()) {
            if (child.is(Term.LAndExp)) {
                Operand subFalseLabel = Operand.getAutoLabelOperand("LAndExpEnd");
                translateLAndExp(child, subFalseLabel);
                TupleList.getInstance().addGoto(trueLabel);
                TupleList.getInstance().addLabel(subFalseLabel);
            } else if (child.is(Term.LOrExp)) {
                translateLOrExp(child, trueLabel, falseLabel);
            }
        }
    }

    private Operand translateConstExp(Node node) {
        return translateAddExp(node.getChild(0));
    }

    public void write() throws IOException {
        TupleList tupleList = TupleList.getInstance();
        for (Tuple tuple : tupleList.getTuples()) {
            irFile.write(tuple.toString() + "\n");
        }
    }

}
