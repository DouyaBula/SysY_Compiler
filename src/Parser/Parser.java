package Parser;

import Error.Error;
import Error.Reporter;
import Lexer.Symbol;
import Lexer.Token;
import Symbol.Attribute;
import Symbol.Table;
import Symbol.Type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class Parser {
    private final Stepper stepper;
    private final Reporter reporter;
    private final HashSet<Table> tables;
    private Table curTable;
    private final Attribute attTem = new Attribute(
            BigInteger.ZERO, null, "", Type.VOID);
    private boolean isInFuncBlock;
    private boolean needRet;
    private boolean isInLoopBlock;

    public Parser(ArrayList<Token> tokens, Reporter reporter) {
        this.stepper = new Stepper(tokens);
        this.reporter = reporter;
        this.tables = new HashSet<>();
        curTable = new Table(null);
        this.isInFuncBlock = false;
        this.needRet = false;
        this.isInLoopBlock = false;
    }

    private Attribute getSymbolAll(String name) {
        Table table = curTable;
        while (table != null) {
            Attribute attr = table.getSymbol(name);
            if (attr != null) {
                return attr;
            }
            table = table.getParent();
        }
        return null;
    }

    private void addSymbol(String name, Attribute attr) {
        if (hasSymbol(name)) {
            reporter.report(Error.b, attr.getLine());
        } else {
            curTable.addSymbol(name, attr);
        }
    }

    private boolean hasSymbol(String name) {
        return curTable.getSymbol(name) != null;
    }

    private boolean hasSymbolAll(String name) {
        Table table = curTable;
        while (table != null) {
            if (table.getSymbol(name) != null) {
                return true;
            }
            table = table.getParent();
        }
        return false;
    }

    private void enterField() {
        Table newTable = new Table(curTable);
        tables.add(newTable);
        curTable.addChild(newTable);
        curTable = newTable;
    }

    private void quitField() {
        curTable = curTable.getParent();
    }

    public void error() {
        System.out.printf("error: current token %s line %d\n",
                stepper.peek().getRaw(), stepper.peek().getLine());
    }

    public Node parseCompUnit() {
        Node compUnit = new Node(Term.CompUnit);
        while (stepper.is(Symbol.CONSTTK) ||
                (stepper.is(Symbol.INTTK, Symbol.IDENFR)
                        && !stepper.peek(2).is(Symbol.LPARENT))) {
            compUnit.addChild(parseDecl());
        }
        while (!stepper.peek(1).is(Symbol.MAINTK) &&
                (stepper.is(Symbol.VOIDTK, Symbol.IDENFR, Symbol.LPARENT)
                        || stepper.is(Symbol.INTTK, Symbol.IDENFR, Symbol.LPARENT))) {
            compUnit.addChild(parseFuncDef());
        }
        if (stepper.is(Symbol.INTTK, Symbol.MAINTK)) {
            compUnit.addChild(parseMainFuncDef());
        } else {
            error();
        }
        return compUnit;
    }

    public Node parseDecl() {
        Node decl = new Node(Term.Decl);
        if (stepper.peek().is(Symbol.CONSTTK)) {
            decl.addChild(parseConstDecl());
        } else if (stepper.peek().is(Symbol.INTTK)) {
            decl.addChild(parseVarDecl());
        } else {
            error();
        }
        return decl;
    }

    public Node parseConstDecl() {
        Node constDecl = new Node(Term.ConstDecl);
        if (stepper.is(Symbol.CONSTTK)) {
            constDecl.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.INTTK)) {
            constDecl.addChild(parseBtype());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            constDecl.addChild(parseConstDef());
        } else {
            error();
        }
        while (stepper.is(Symbol.COMMA)) {
            constDecl.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.IDENFR)) {
                constDecl.addChild(parseConstDef());
            } else {
                error();
            }
        }
        checkSemicn(constDecl);
        return constDecl;
    }

    public Node parseBtype() {
        Node btype = new Node(Term.BType);
        if (stepper.is(Symbol.INTTK)) {
            btype.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        return btype;
    }

    public Node parseConstDef() {
        Node constDef = new Node(Term.ConstDef);
        Attribute att = attTem;
        if (stepper.is(Symbol.IDENFR)) {
            constDef.addChild(new Node(stepper.peek()));
            Token temp = stepper.peek();
            att = new Attribute(temp.getLine(), curTable, temp.getRaw(), Type.CONST);
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.LBRACK)) {
            constDef.addChild(new Node(stepper.peek()));
            stepper.next();
            att.addDim();
            if (stepper.isUnaryExp()) {
                constDef.addChild(parseConstExp());
            } else {
                error();
            }
            checkRbrack(constDef);
        }
        if (stepper.is(Symbol.ASSIGN)) {
            constDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
            constDef.addChild(parseConstInitVal());
        } else {
            error();
        }
        addSymbol(att.getName(), att);
        return constDef;
    }

    public Node parseConstInitVal() {
        Node constInitVal = new Node(Term.ConstInitVal);
        if (stepper.isUnaryExp()) {
            constInitVal.addChild(parseConstExp());
        } else if (stepper.is(Symbol.LBRACE)) {
            constInitVal.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
                constInitVal.addChild(parseConstInitVal());
                while (stepper.is(Symbol.COMMA)) {
                    constInitVal.addChild(new Node(stepper.peek()));
                    stepper.next();
                    if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
                        constInitVal.addChild(parseConstInitVal());
                    } else {
                        error();
                    }
                }
            }
            if (stepper.is(Symbol.RBRACE)) {
                constInitVal.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
        } else {
            error();
        }
        return constInitVal;
    }

    public Node parseVarDecl() {
        Node varDecl = new Node(Term.VarDecl);
        if (stepper.is(Symbol.INTTK)) {
            varDecl.addChild(parseBtype());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            varDecl.addChild(parseVarDef());
        } else {
            error();
        }
        while (stepper.is(Symbol.COMMA)) {
            varDecl.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.IDENFR)) {
                varDecl.addChild(parseVarDef());
            } else {
                error();
            }
        }
        checkSemicn(varDecl);
        return varDecl;
    }

    public Node parseVarDef() {
        Node varDef = new Node(Term.VarDef);
        Attribute att = attTem;
        if (stepper.is(Symbol.IDENFR)) {
            varDef.addChild(new Node(stepper.peek()));
            Token temp = stepper.peek();
            att = new Attribute(temp.getLine(), curTable, temp.getRaw(), Type.VAR);
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.LBRACK)) {
            varDef.addChild(new Node(stepper.peek()));
            att.addDim();
            stepper.next();
            if (stepper.isUnaryExp()) {
                varDef.addChild(parseConstExp());
            }
            checkRbrack(varDef);
        }
        if (stepper.is(Symbol.ASSIGN)) {
            varDef.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
                varDef.addChild(parseInitVal());
            } else {
                error();
            }
        }
        addSymbol(att.getName(), att);
        return varDef;
    }

    public Node parseInitVal() {
        Node initVal = new Node(Term.InitVal);
        if (stepper.isUnaryExp()) {
            initVal.addChild(parseExp());
        } else if (stepper.is(Symbol.LBRACE)) {
            initVal.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
                initVal.addChild(parseInitVal());
                while (stepper.is(Symbol.COMMA)) {
                    initVal.addChild(new Node(stepper.peek()));
                    stepper.next();
                    if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
                        initVal.addChild(parseInitVal());
                    } else {
                        error();
                    }
                }
            }
            if (stepper.is(Symbol.RBRACE)) {
                initVal.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
        } else {
            error();
        }
        return initVal;
    }

    public Node parseFuncDef() {
        Node funcDef = new Node(Term.FuncDef);
        Attribute att = attTem;
        Type type = Type.VOID;
        if (stepper.is(Symbol.VOIDTK) || stepper.is(Symbol.INTTK)) {
            type = stepper.peek().is(Symbol.VOIDTK) ? Type.VOID : Type.INT;
            funcDef.addChild(parseFuncType());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            funcDef.addChild(new Node(stepper.peek()));
            Token temp = stepper.peek();
            att = new Attribute(temp.getLine(), curTable, temp.getRaw(), Type.FUNCTION);
            stepper.next();
        } else {
            error();
        }
        enterField();
        if (stepper.is(Symbol.LPARENT)) {
            funcDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.INTTK)) {
            funcDef.addChild(parseFuncFParams(att));
        }
        checkRparent(funcDef);
        if (stepper.is(Symbol.LBRACE)) {
            needRet = type == Type.INT;
            isInFuncBlock = true;
            funcDef.addChild(parseBlock());
        } else {
            error();
        }
        quitField();
        att.setReType(type);
        att.setDim(type == Type.INT ? 0 : -114514);
        addSymbol(att.getName(), att);
        needRet = false;
        isInFuncBlock = false;
        return funcDef;
    }

    public Node parseMainFuncDef() {
        Node mainFuncDef = new Node(Term.MainFuncDef);
        if (stepper.is(Symbol.INTTK)) {
            mainFuncDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.MAINTK)) {
            mainFuncDef.addChild(new Node(stepper.peek()));
            Attribute main = new Attribute(
                    stepper.peek().getLine(), curTable, "main", Type.FUNCTION);
            main.setDim(0);
            main.setReType(Type.INT);
            addSymbol("main", main);
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LPARENT)) {
            mainFuncDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        checkRparent(mainFuncDef);
        if (stepper.is(Symbol.LBRACE)) {
            needRet = true;
            isInFuncBlock = true;
            mainFuncDef.addChild(parseBlock());
        } else {
            error();
        }
        needRet = false;
        isInFuncBlock = false;
        return mainFuncDef;
    }

    public Node parseFuncType() {
        Node funcType = new Node(Term.FuncType);
        if (stepper.is(Symbol.VOIDTK) || stepper.is(Symbol.INTTK)) {
            funcType.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        return funcType;
    }

    public Node parseFuncFParams(Attribute funcAtt) {
        Node funcFParams = new Node(Term.FuncFParams);
        if (stepper.is(Symbol.INTTK)) {
            funcFParams.addChild(parseFuncFParam(funcAtt));
        } else {
            error();
        }
        while (stepper.is(Symbol.COMMA)) {
            funcFParams.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.INTTK)) {
                funcFParams.addChild(parseFuncFParam(funcAtt));
            } else {
                error();
            }
        }
        return funcFParams;
    }

    public Node parseFuncFParam(Attribute funcAtt) {
        Node funcFParam = new Node(Term.FuncFParam);
        Attribute paramAtt = attTem;
        int dimCnt = 0;
        if (stepper.is(Symbol.INTTK)) {
            funcFParam.addChild(parseBtype());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            funcFParam.addChild(new Node(stepper.peek()));
            Token temp = stepper.peek();
            paramAtt = new Attribute(temp.getLine(), curTable, temp.getRaw(), Type.VAR);
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LBRACK)) {
            funcFParam.addChild(new Node(stepper.peek()));
            dimCnt++;
            paramAtt.addDim();
            stepper.next();
            checkRbrack(funcFParam);
            while (stepper.is(Symbol.LBRACK)) {
                funcFParam.addChild(new Node(stepper.peek()));
                stepper.next();
                dimCnt++;
                paramAtt.addDim();
                if (stepper.isUnaryExp()) {
                    funcFParam.addChild(parseConstExp());
                } else {
                    error();
                }
                checkRbrack(funcFParam);
            }
        }
        addSymbol(paramAtt.getName(), paramAtt);
        funcAtt.addParamType(dimCnt);
        return funcFParam;
    }

    public Node parseBlock() {
        Node block = new Node(Term.Block);
        if (stepper.is(Symbol.LBRACE)) {
            block.addChild(new Node(stepper.peek()));
            if (!isInFuncBlock) {
                enterField();
            }
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.CONSTTK)
                || stepper.is(Symbol.INTTK)
                || stepper.isStmt()) {
            block.addChild(parseBlockItem());
        }

        // Error g: check the last stmt is return
        checkNeedRet(isInFuncBlock, needRet, block);

        if (stepper.is(Symbol.RBRACE)) {
            block.addChild(new Node(stepper.peek()));
            if (!isInFuncBlock) {
                quitField();
            }
            stepper.next();
        } else {
            error();
        }
        return block;
    }

    public Node parseBlockItem() {
        Node blockItem = new Node(Term.BlockItem);
        if (stepper.is(Symbol.CONSTTK)
                || stepper.is(Symbol.INTTK)) {
            blockItem.addChild(parseDecl());
        } else if (stepper.isStmt()) {
            blockItem.addChild(parseStmt());
        } else {
            error();
        }
        return blockItem;
    }

    public Node parseStmt() {
        Node stmt = new Node(Term.Stmt);
        if (stepper.is(Symbol.IDENFR)) {
            if (stepper.isAssignStmt()) {
                checkConst();
                stmt.addChild(parseLVal());
                if (stepper.is(Symbol.ASSIGN)) {
                    stmt.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
                if (stepper.isUnaryExp()) {
                    stmt.addChild(parseExp());
                } else {
                    error();
                }
            } else if (stepper.isGetintStmt()) {
                checkConst();
                stmt.addChild(parseLVal());
                if (stepper.is(Symbol.ASSIGN)) {
                    stmt.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
                if (stepper.is(Symbol.GETINTTK)) {
                    stmt.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
                if (stepper.is(Symbol.LPARENT)) {
                    stmt.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
                checkRparent(stmt);
            } else if (stepper.isUnaryExp()) {
                stmt.addChild(parseExp());
            } else {
                error();
            }
            checkSemicn(stmt);
        } else if (stepper.isUnaryExp()) {
            stmt.addChild(parseExp());
            checkSemicn(stmt);
        } else if (stepper.is(Symbol.LBRACE)) {
            stmt.addChild(parseBlock());
        } else if (stepper.is(Symbol.IFTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.LPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.isUnaryExp()) {
                stmt.addChild(parseCond());
            } else {
                error();
            }
            checkRparent(stmt);
            if (stepper.isStmt()) {
                stmt.addChild(parseStmt());
            } else {
                error();
            }
            if (stepper.is(Symbol.ELSETK)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isStmt()) {
                    stmt.addChild(parseStmt());
                } else {
                    error();
                }
            }
        } else if (stepper.is(Symbol.FORTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.LPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.is(Symbol.IDENFR)) {
                stmt.addChild(parseForstmt());
            }
            checkSemicn(stmt);
            if (stepper.isUnaryExp()) {
                stmt.addChild(parseCond());
            }
            checkSemicn(stmt);
            if (stepper.is(Symbol.IDENFR)) {
                stmt.addChild(parseForstmt());
            }
            checkRparent(stmt);
            isInLoopBlock = true;
            if (stepper.isStmt()) {
                stmt.addChild(parseStmt());
            } else {
                error();
            }
            isInLoopBlock = false;
        } else if (stepper.is(Symbol.BREAKTK)) {
            stmt.addChild(new Node(stepper.peek()));
            checkLoop();
            stepper.next();
            checkSemicn(stmt);
        } else if (stepper.is(Symbol.CONTINUETK)) {
            stmt.addChild(new Node(stepper.peek()));
            checkLoop();
            stepper.next();
            checkSemicn(stmt);
        } else if (stepper.is(Symbol.RETURNTK)) {
            stmt.addChild(new Node(stepper.peek()));
            boolean hasRet = false;
            BigInteger line = stepper.peek().getLine();

            stepper.next();
            if (stepper.isUnaryExp()) {
                hasRet = true;
                stmt.addChild(parseExp());
            }

            if (isInFuncBlock && !needRet && hasRet) {
                reporter.report(Error.f, line);
            }

            checkSemicn(stmt);
        } else if (stepper.is(Symbol.PRINTFTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.LPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }

            int expectedFmtCharCnt = 0;
            int actualFmtCharCnt = 0;
            boolean needCheckFmtCharCnt = false;
            if (stepper.is(Symbol.STRCON)) {
                stmt.addChild(new Node(stepper.peek()));
                expectedFmtCharCnt = stepper.peek().getFormatCharCnt();
                needCheckFmtCharCnt = stepper.peek().isLegal();
                stepper.next();
            } else {
                error();
            }
            while (stepper.is(Symbol.COMMA)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isUnaryExp()) {
                    stmt.addChild(parseExp());
                    actualFmtCharCnt++;
                } else {
                    error();
                }
            }
            if (needCheckFmtCharCnt &&
                    expectedFmtCharCnt != actualFmtCharCnt) {
                reporter.report(Error.l, stepper.peek().getLine());
            }
            checkRparent(stmt);
            checkSemicn(stmt);
        } else checkSemicn(stmt);
        return stmt;
    }

    public Node parseForstmt() {
        Node forstmt = new Node(Term.ForStmt);
        if (stepper.is(Symbol.IDENFR)) {
            forstmt.addChild(parseLVal());
        } else {
            error();
        }
        if (stepper.is(Symbol.ASSIGN)) {
            forstmt.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.isUnaryExp()) {
            forstmt.addChild(parseExp());
        } else {
            error();
        }
        return forstmt;
    }

    public Node parseExp() {
        Node exp = new Node(Term.Exp);
        if (stepper.isUnaryExp()) {
            exp.addChild(parseAddExp());
        } else {
            error();
        }
        return exp;
    }

    public Node parseCond() {
        Node cond = new Node(Term.Cond);
        if (stepper.isUnaryExp()) {
            cond.addChild(parseLOrExp());
        } else {
            error();
        }
        return cond;
    }

    public Node parseLVal() {
        Node lVal = new Node(Term.LVal);
        if (stepper.is(Symbol.IDENFR)) {
            checkIdenfr(stepper.peek());
            lVal.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.LBRACK)) {
            lVal.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                lVal.addChild(parseExp());
            } else {
                error();
            }
            checkRbrack(lVal);
        }
        return lVal;
    }

    public Node parsePrimaryExp() {
        Node primaryExp = new Node(Term.PrimaryExp);
        if (stepper.is(Symbol.LPARENT)) {
            primaryExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                primaryExp.addChild(parseExp());
            } else {
                error();
            }
            checkRparent(primaryExp);
        } else if (stepper.is(Symbol.IDENFR)) {
            primaryExp.addChild(parseLVal());
        } else if (stepper.is(Symbol.INTCON)) {
            primaryExp.addChild(parseNumber());
        } else {
            error();
        }
        return primaryExp;
    }

    public Node parseNumber() {
        Node number = new Node(Term.Number);
        if (stepper.is(Symbol.INTCON)) {
            number.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        return number;
    }

    public Node parseUnaryExp() {
        Node unaryExp = new Node(Term.UnaryExp);
        if (stepper.is(Symbol.LPARENT) ||
                (stepper.is(Symbol.IDENFR) &&
                        !stepper.peek(1).is(Symbol.LPARENT)) ||
                stepper.is(Symbol.INTCON)) {
            unaryExp.addChild(parsePrimaryExp());
        } else if (stepper.is(Symbol.IDENFR) &&
                stepper.peek(1).is(Symbol.LPARENT)) {
            checkIdenfr(stepper.peek());
            Token funcName = stepper.peek();
            BigInteger line = funcName.getLine();
            Attribute func = getSymbolAll(funcName.getRaw());
            unaryExp.addChild(new Node(stepper.peek()));
            stepper.next();
            unaryExp.addChild(new Node(stepper.peek()));
            stepper.next();
            boolean hasParam = false;
            if (stepper.isUnaryExp()) {
                hasParam = true;
                unaryExp.addChild(parseFuncRParams(func, line));
            }
            if (func != null && !hasParam && func.getParamNum() > 0) {
                reporter.report(Error.d, line);
            }
            checkRparent(unaryExp);
        } else if (stepper.is(Symbol.PLUS) ||
                stepper.is(Symbol.MINU) ||
                stepper.is(Symbol.NOT)) {
            unaryExp.addChild(parseUnaryOp());
            if (stepper.isUnaryExp()) {
                unaryExp.addChild(parseUnaryExp());
            } else {
                error();
            }
        }
        return unaryExp;
    }


    public Node parseUnaryOp() {
        Node unaryOp = new Node(Term.UnaryOp);
        if (stepper.is(Symbol.PLUS)
                || stepper.is(Symbol.MINU)
                || stepper.is(Symbol.NOT)) {
            unaryOp.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        return unaryOp;
    }

    public Node parseFuncRParams(Attribute func, BigInteger line) {
        Node funcRParams = new Node(Term.FuncRParams);
        int paramNum = 0;
        if (stepper.isUnaryExp()) {
            checkRParamDim(func, paramNum, line);
            funcRParams.addChild(parseExp());
            paramNum++;
            while (stepper.is(Symbol.COMMA)) {
                funcRParams.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isUnaryExp()) {
                    checkRParamDim(func, paramNum, line);
                    funcRParams.addChild(parseExp());
                    paramNum++;
                } else {
                    error();
                }
            }
        } else {
            error();
        }
        if (paramNum != func.getParamNum()) {
            reporter.report(Error.d, line);
        }
        return funcRParams;
    }


    public Node parseMulExp() {
        Node mulExp = new Node(Term.MulExp);
        if (stepper.isUnaryExp()) {
            mulExp.addChild(parseUnaryExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.MULT) ||
                stepper.is(Symbol.DIV) ||
                stepper.is(Symbol.MOD)) {
            mulExp.mergeChildrenTo(new Node(Term.MulExp));
            mulExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                mulExp.addChild(parseUnaryExp());
            } else {
                error();
            }
        }
        return mulExp;
    }

    public Node parseAddExp() {
        Node addExp = new Node(Term.AddExp);
        if (stepper.isUnaryExp()) {
            addExp.addChild(parseMulExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.PLUS) ||
                stepper.is(Symbol.MINU)) {
            addExp.mergeChildrenTo(new Node(Term.AddExp));
            addExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                addExp.addChild(parseMulExp());
            } else {
                error();
            }
        }
        return addExp;
    }

    public Node parseRelExp() {
        Node relExp = new Node(Term.RelExp);
        if (stepper.isUnaryExp()) {
            relExp.addChild(parseAddExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.LSS) ||
                stepper.is(Symbol.LEQ) ||
                stepper.is(Symbol.GRE) ||
                stepper.is(Symbol.GEQ)) {
            relExp.mergeChildrenTo(new Node(Term.RelExp));
            relExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                relExp.addChild(parseAddExp());
            } else {
                error();
            }
        }
        return relExp;
    }

    public Node parseEqExp() {
        Node eqExp = new Node(Term.EqExp);
        if (stepper.isUnaryExp()) {
            eqExp.addChild(parseRelExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.EQL) ||
                stepper.is(Symbol.NEQ)) {
            eqExp.mergeChildrenTo(new Node(Term.EqExp));
            eqExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                eqExp.addChild(parseRelExp());
            } else {
                error();
            }
        }
        return eqExp;
    }

    public Node parseLAndExp() {
        Node lAndExp = new Node(Term.LAndExp);
        if (stepper.isUnaryExp()) {
            lAndExp.addChild(parseEqExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.AND)) {
            lAndExp.mergeChildrenTo(new Node(Term.LAndExp));
            lAndExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                lAndExp.addChild(parseEqExp());
            } else {
                error();
            }
        }
        return lAndExp;
    }

    public Node parseLOrExp() {
        Node lOrExp = new Node(Term.LOrExp);
        if (stepper.isUnaryExp()) {
            lOrExp.addChild(parseLAndExp());
        } else {
            error();
        }
        while (stepper.is(Symbol.OR)) {
            lOrExp.mergeChildrenTo(new Node(Term.LOrExp));
            lOrExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                lOrExp.addChild(parseLAndExp());
            } else {
                error();
            }
        }
        return lOrExp;
    }

    public Node parseConstExp() {
        Node constExp = new Node(Term.ConstExp);
        if (stepper.isUnaryExp()) {
            constExp.addChild(parseAddExp());
        } else {
            error();
        }
        return constExp;
    }

    private boolean checkIdenfr(Token token) {
        if (!hasSymbolAll(token.getRaw())) {
            reporter.report(Error.c, token.getLine());
            return false;
        }
        return true;
    }

    private void checkRParamDim(Attribute func, int paramNum, BigInteger line) {
        if (func == null) {
            return;
        }
        int dim = 0;
        if (stepper.is(Symbol.IDENFR)) {
            Attribute attr = getSymbolAll(stepper.peek().getRaw());
            if (attr == null) {
                return;
            }
            if (attr.getType() != Type.FUNCTION) {
                int i = 1;
                while (stepper.peek(i).is(Symbol.LBRACK)) {
                    dim++;
                    while (!stepper.peek(i).is(Symbol.RBRACK)) {
                        i++;
                    }
                    i++;
                }
            }
            dim = attr.getDimCnt() - dim;
        }
        if (func.getParamDim(paramNum) == -1) {
            return;
        }
        if (dim != func.getParamDim(paramNum)) {
            reporter.report(Error.e, line);
        }
    }

    private void checkNeedRet(boolean isFunc, boolean needRet, Node block) {
        if (isFunc && needRet) {
            boolean hasLastRet = false;
            Node lastStmt = block.getLastChild(); // blockItem
            lastStmt = lastStmt.getLastChild(); // should be stmt rather than decl or null
            if (lastStmt == null) {
                reporter.report(Error.g, stepper.peek().getLine());
                return;
            }
            if (lastStmt.getType() == Term.Stmt) {
                Node firstChild = lastStmt.getFirstChild(); // should be return
                if (firstChild.isLeaf() && firstChild.getToken().is(Symbol.RETURNTK)) {
                    hasLastRet = true;
                }
            }
            if (!hasLastRet) {
                reporter.report(Error.g, stepper.peek().getLine());
            }
        }
    }

    private void checkConst() {
        Attribute lval = getSymbolAll(stepper.peek().getRaw());
        if (lval != null && lval.getType() == Type.CONST) {
            reporter.report(Error.h, stepper.peek().getLine());
        }
    }

    private void checkSemicn(Node node) {
        if (stepper.is(Symbol.SEMICN)) {
            node.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            reporter.report(Error.i, stepper.peek(-1).getLine());
        }
    }

    private void checkRparent(Node node) {
        if (stepper.is(Symbol.RPARENT)) {
            node.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            node.addChild(new Node(
                    new Token(Symbol.RPARENT, ")", BigInteger.ZERO)));
            reporter.report(Error.j, stepper.peek(-1).getLine());
        }
    }

    private void checkRbrack(Node node) {
        if (stepper.is(Symbol.RBRACK)) {
            node.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            reporter.report(Error.k, stepper.peek().getLine());
        }
    }

    private void checkLoop() {
        if (!isInLoopBlock) {
            reporter.report(Error.m, stepper.peek().getLine());
        }
    }
}
