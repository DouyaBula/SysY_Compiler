package Parser;

import Lexer.Symbol;
import Lexer.Token;

import java.util.ArrayList;

public class Parser {
    private final Stepper stepper;

    public Parser(ArrayList<Token> tokens) {
        this.stepper = new Stepper(tokens);
    }

    public void error() {
        System.out.println("error");
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
        if (stepper.is(Symbol.SEMICN)) {
            constDecl.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
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
        if (stepper.is(Symbol.IDENFR)) {
            constDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.LBRACK)) {
            constDef.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                constDef.addChild(parseConstExp());
            }
            if (stepper.is(Symbol.RBRACK)) {
                constDef.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
        }
        if (stepper.is(Symbol.ASSIGN)) {
            constDef.addChild(new Node(stepper.peek()));
            stepper.next();
            constDef.addChild(parseConstInitVal());
        } else {
            error();
        }
        if (stepper.isUnaryExp() || stepper.is(Symbol.LBRACE)) {
            constDef.addChild(parseConstInitVal());
        } else {
            error();
        }
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
        if (stepper.is(Symbol.SEMICN)) {
            varDecl.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        return varDecl;
    }

    public Node parseVarDef() {
        Node varDef = new Node(Term.VarDef);
        if (stepper.is(Symbol.IDENFR)) {
            varDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.LBRACK)) {
            varDef.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                varDef.addChild(parseConstExp());
            }
            if (stepper.is(Symbol.RBRACK)) {
                varDef.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
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
        return varDef;
    }

    public Node parseInitVal() {
        Node initVal = new Node(Term.InitVal);
        if (stepper.isUnaryExp()) {
            initVal.addChild(parseConstExp());
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
        if (stepper.is(Symbol.VOIDTK) || stepper.is(Symbol.MAINTK)) {
            funcDef.addChild(parseFuncType());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            funcDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LPARENT)) {
            funcDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.INTTK)) {
            funcDef.addChild(parseFuncFParams());
        }
        if (stepper.is(Symbol.RPARENT)) {
            funcDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LBRACE)) {
            funcDef.addChild(parseBlock());
        } else {
            error();
        }
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
        if (stepper.is(Symbol.RPARENT)) {
            mainFuncDef.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LBRACE)) {
            mainFuncDef.addChild(parseBlock());
        } else {
            error();
        }
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

    public Node parseFuncFParams() {
        Node funcFParams = new Node(Term.FuncFParams);
        if (stepper.is(Symbol.INTTK)) {
            funcFParams.addChild(parseFuncFParam());
            while (stepper.is(Symbol.COMMA)) {
                funcFParams.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.is(Symbol.INTTK)) {
                    funcFParams.addChild(parseFuncFParam());
                } else {
                    error();
                }
            }
        }
        return funcFParams;
    }

    public Node parseFuncFParam() {
        Node funcFParam = new Node(Term.FuncFParam);
        if (stepper.is(Symbol.INTTK)) {
            funcFParam.addChild(parseBtype());
        } else {
            error();
        }
        if (stepper.is(Symbol.IDENFR)) {
            funcFParam.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        if (stepper.is(Symbol.LBRACK)) {
            funcFParam.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.RBRACK)) {
                funcFParam.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            while (stepper.is(Symbol.LBRACK)) {
                funcFParam.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isUnaryExp()) {
                    funcFParam.addChild(parseConstExp());
                } else {
                    error();
                }
                if (stepper.is(Symbol.RBRACK)) {
                    funcFParam.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
            }
        }
        return funcFParam;
    }

    public Node parseBlock() {
        Node block = new Node(Term.Block);
        if (stepper.is(Symbol.LBRACE)) {
            block.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
        while (stepper.is(Symbol.CONSTTK)
                || stepper.is(Symbol.INTTK)
                || stepper.isStmt()) {
            block.addChild(parseBlockItem());
        }
        if (stepper.is(Symbol.RBRACE)) {
            block.addChild(new Node(stepper.peek()));
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
                if (stepper.is(Symbol.RPARENT)) {
                    stmt.addChild(new Node(stepper.peek()));
                    stepper.next();
                } else {
                    error();
                }
            } else if (stepper.isUnaryExp()) {
                stmt.addChild(parseExp());
            } else {
                error();
            }
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
            if (stepper.is(Symbol.RPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
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
            if (stepper.is(Symbol.SEMICN)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.isUnaryExp()) {
                stmt.addChild(parseCond());
            }
            if (stepper.is(Symbol.SEMICN)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.is(Symbol.IDENFR)) {
                stmt.addChild(parseForstmt());
            }
            if (stepper.is(Symbol.RPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.isStmt()) {
                stmt.addChild(parseStmt());
            } else {
                error();
            }
        } else if (stepper.is(Symbol.BREAKTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
        } else if (stepper.is(Symbol.CONTINUETK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
        } else if (stepper.is(Symbol.RETURNTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                stmt.addChild(parseExp());
            }
        } else if (stepper.is(Symbol.PRINTFTK)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.is(Symbol.LPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            if (stepper.is(Symbol.STRCON)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
            while (stepper.is(Symbol.COMMA)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isUnaryExp()) {
                    stmt.addChild(parseExp());
                } else {
                    error();
                }
            }
            if (stepper.is(Symbol.RPARENT)) {
                stmt.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
        } else {
            error();
        }
        if (stepper.is(Symbol.SEMICN)) {
            stmt.addChild(new Node(stepper.peek()));
            stepper.next();
        } else {
            error();
        }
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
            if (stepper.is(Symbol.RBRACK)) {
                lVal.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
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
            if (stepper.is(Symbol.RPARENT)) {
                primaryExp.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
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
                stepper.is(Symbol.IDENFR) ||
                (stepper.is(Symbol.INTCON)
                        && !stepper.peek(1).is(Symbol.LPARENT))) {
            unaryExp.addChild(parsePrimaryExp());
        } else if (stepper.is(Symbol.IDENFR) &&
                stepper.peek(1).is(Symbol.LPARENT)) {
            unaryExp.addChild(new Node(stepper.peek()));
            stepper.next();
            unaryExp.addChild(new Node(stepper.peek()));
            stepper.next();
            if (stepper.isUnaryExp()) {
                unaryExp.addChild(parseFuncRParams());
            } else {
                error();
            }
            if (stepper.is(Symbol.RPARENT)) {
                unaryExp.addChild(new Node(stepper.peek()));
                stepper.next();
            } else {
                error();
            }
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

    public Node parseFuncRParams() {
        Node funcRParams = new Node(Term.FuncRParams);
        if (stepper.isUnaryExp()) {
            funcRParams.addChild(parseExp());
            while (stepper.is(Symbol.COMMA)) {
                funcRParams.addChild(new Node(stepper.peek()));
                stepper.next();
                if (stepper.isUnaryExp()) {
                    funcRParams.addChild(parseExp());
                } else {
                    error();
                }
            }
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
            constExp.addChild(parseLOrExp());
        } else {
            error();
        }
        return constExp;
    }
}
