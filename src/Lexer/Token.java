package Lexer;

import java.math.BigInteger;

public class Token {
    private final Symbol type;
    private final String raw;
    private final BigInteger line;
    private boolean legal;
    private int formatCharCnt;

    public final static Token nullToken = new Token(Symbol.NULL, "", BigInteger.ZERO);

    public Token(Symbol type, String raw, BigInteger line) {
        this.type = type;
        this.raw = raw;
        this.line = line;
        this.legal = true;
        this.formatCharCnt = 0;
    }

    public String getType() {
        return type.toString();
    }

    public String getRaw() {
        return raw;
    }

    public BigInteger getLine() {
        return line;
    }

    public boolean is(Symbol type) {
        return this.type == type;
    }

    public void illegal() {
        this.legal = false;
    }

    public void setFormatCharCnt(int cnt) {
        this.formatCharCnt = cnt;
    }

    public boolean isLegal() {
        return legal;
    }

    public int getFormatCharCnt() {
        return formatCharCnt;
    }
}
