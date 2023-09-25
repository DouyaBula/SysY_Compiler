package Lexer;

import java.math.BigInteger;

public class Token {
    private final Symbol type;
    private final String raw;
    private final BigInteger line;

    public Token(Symbol type, String raw, BigInteger line) {
        this.type = type;
        this.raw = raw;
        this.line = line;
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
}
