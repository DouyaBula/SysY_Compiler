package Lexer;

import java.util.HashMap;

public enum Symbol {
    IDENFR,
    INTCON,
    STRCON,
    MAINTK,
    CONSTTK,
    INTTK,
    BREAKTK,
    CONTINUETK,
    IFTK,
    ELSETK,
    NOT,
    AND,
    OR,
    FORTK,
    GETINTTK,
    PRINTFTK,
    RETURNTK,
    PLUS,
    MINU,
    VOIDTK,
    MULT,
    DIV,
    MOD,
    LSS,
    LEQ,
    GRE,
    GEQ,
    EQL,
    NEQ,
    ASSIGN,
    SEMICN,
    COMMA,
    LPARENT,
    RPARENT,
    LBRACK,
    RBRACK,
    LBRACE,
    RBRACE,
    NULL,
    ;
    public final static HashMap<String, Symbol> lookupTable = new HashMap<>() {{
        put("main", MAINTK);
        put("const", CONSTTK);
        put("int", INTTK);
        put("break", BREAKTK);
        put("continue", CONTINUETK);
        put("if", IFTK);
        put("else", ELSETK);
        put("for", FORTK);
        put("getint", GETINTTK);
        put("printf", PRINTFTK);
        put("return", RETURNTK);
        put("void", VOIDTK);
    }};
}

