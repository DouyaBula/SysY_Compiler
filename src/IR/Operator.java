package IR;

import java.util.HashMap;

public enum Operator {
    // Symbol
    DEF,    // def

    // Assign
    ASSIGN, // =

    // Unary
    NOT,    // !
    NEG,    // -
    POS,    // +

    // Binary
    ADD,    // +
    SUB,    // -
    MUL,    // *
    DIV,    // /
    MOD,    // %

    // Logic
    AND,    // &&
    OR,     // ||
    EQ,     // ==
    NEQ,    // !=
    LT,     // <
    GT,     // >
    LEQ,    // <=
    GEQ,    // >=

    // Address
    CALL,   // ()
    RETURN,    // return
    LABEL,  // "label"
    GOTO,   // goto
    JUMPTRUE,   // jumptrue
    JUMPFALSE,  // jumpfalse
    PUSH,   // push
    LOAD,   // load
    STORE,  // store


    // IO
    READ,   // read
    PRINT,  // print

    // EXIT
    EXIT,  // exit
    ;

    public final static HashMap<Operator, String> tupleName = new HashMap<>() {{
        put(DEF, "DEF");
        put(ASSIGN, "=");
        put(NOT, "!");
        put(NEG, "-");
        put(POS, "+");
        put(ADD, "+");
        put(SUB, "-");
        put(MUL, "*");
        put(DIV, "/");
        put(MOD, "%");
        put(AND, "&&");
        put(OR, "||");
        put(EQ, "==");
        put(NEQ, "!=");
        put(LT, "<");
        put(GT, ">");
        put(LEQ, "<=");
        put(GEQ, ">=");
        put(CALL, "CALL");
        put(RETURN, "RETURN");
        put(LABEL, "LABEL");
        put(GOTO, "GOTO");
        put(JUMPTRUE, "JUMPTRUE");
        put(JUMPFALSE, "JUMPFALSE");
        put(PUSH, "PUSH");
        put(LOAD, "LOAD");
        put(READ, "READ");
        put(PRINT, "PRINT");
        put(EXIT, "EXIT");
    }};

    @Override
    public String toString() {
        return tupleName.get(this);
    }
}
