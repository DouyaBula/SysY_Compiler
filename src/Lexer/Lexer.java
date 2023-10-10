package Lexer;

import Error.Error;
import Error.Reporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class Lexer {
    private final BufferedReader input;
    private final ArrayList<Token> tokens;
    private BigInteger lineCnt;
    private int error;
    private final Reporter reporter;

    public Lexer(BufferedReader input, Reporter reporter) {
        this.input = input;
        this.tokens = new ArrayList<>();
        this.lineCnt = BigInteger.ZERO;
        this.error = 0;
        this.reporter = reporter;
    }

    public void error(Error error) throws IOException {
        reporter.report(error, lineCnt);
        this.error++;
    }

    public void error() {
        System.out.println("Error: " + lineCnt);
        error++;
    }

    public ArrayList<Token> analyze() throws IOException {
        String line;
        int isInNotation = 0;
        while ((line = input.readLine()) != null) {
            lineCnt = lineCnt.add(BigInteger.ONE);
            Stepper stepper = new Stepper(line);
            while (!stepper.atEnd()) {
                // 换行注释检测
                if (isInNotation == 1) {
                    isInNotation = 0;
                    break;
                } else if (isInNotation == 2) {
                    while (!stepper.atEnd()) {
                        if (stepper.peek() == '*' && stepper.peek(1) == '/') {
                            stepper.next(2);
                            isInNotation = 0;
                            break;
                        }
                        stepper.next();
                    }
                }
                // 跳过空白符
                stepper.skip();
                // 注释头检测
                if (stepper.peek() == '/') {
                    if (stepper.peek(1) == '/') {
                        stepper.next(2);
                        isInNotation = stepper.atEnd() ? 0 : 1;
                        continue;
                    } else if (stepper.peek(1) == '*') {
                        stepper.next(2);
                        isInNotation = 2;
                        continue;
                    } else {
                        tokens.add(new Token(Symbol.DIV, "/", lineCnt));
                    }
                }
                // 标识符与保留字检测
                else if (Character.isLetter(stepper.peek()) || stepper.peek() == '_') {
                    String name = stepper.getWord();
                    tokens.add(new Token(Symbol.lookupTable.getOrDefault(
                            name, Symbol.IDENFR), name, lineCnt));
                }
                // 格式化字符串检测
                else if (stepper.peek() == '\"') {
                    String name = stepper.getFormatStr();
                    if (name == null) {
                        error(Error.a);
                        Token fmtStr = new Token(Symbol.STRCON, "wrongFormat", lineCnt);
                        fmtStr.illegal();
                        tokens.add(fmtStr);
                        while (stepper.peek() != '\"') {
                            stepper.next();
                        }
                    } else {
                        Token fmtStr = new Token(Symbol.STRCON, name, lineCnt);
                        tokens.add(fmtStr);
                        int formatCharCnt = name.split("%").length - 1;
                        fmtStr.setFormatCharCnt(formatCharCnt);
                    }
                }
                // 常数检测
                else if (Character.isDigit(stepper.peek())) {
                    if (stepper.peek() == '0' && Character.isDigit(stepper.peek(1))) {
                        error();
                    } else {
                        tokens.add(new Token(Symbol.INTCON, stepper.getConst(), lineCnt));
                    }
                }
                // 简单符号检测
                else {
                    normal(stepper);
                }
                stepper.next();
            }
        }
        return tokens;
    }

    private void normal(Stepper stepper) {
        switch (stepper.peek()) {
            case '!':
                if (stepper.peek(1) == '=') {
                    tokens.add(new Token(Symbol.NEQ, "!=", lineCnt));
                    stepper.next();
                } else {
                    tokens.add(new Token(Symbol.NOT, "!", lineCnt));
                }
                break;
            case '&':
                if (stepper.peek(1) == '&') {
                    tokens.add(new Token(Symbol.AND, "&&", lineCnt));
                    stepper.next();
                } else {
                    error();
                }
                break;
            case '|':
                if (stepper.peek(1) == '|') {
                    tokens.add(new Token(Symbol.OR, "||", lineCnt));
                    stepper.next();
                } else {
                    error();
                }
                break;
            case '+':
                tokens.add(new Token(Symbol.PLUS, "+", lineCnt));
                break;
            case '-':
                tokens.add(new Token(Symbol.MINU, "-", lineCnt));
                break;
            case '*':
                tokens.add(new Token(Symbol.MULT, "*", lineCnt));
                break;
            case '%':
                tokens.add(new Token(Symbol.MOD, "%", lineCnt));
                break;
            case '<':
                if (stepper.peek(1) == '=') {
                    tokens.add(new Token(Symbol.LEQ, "<=", lineCnt));
                    stepper.next();
                } else {
                    tokens.add(new Token(Symbol.LSS, "<", lineCnt));
                }
                break;
            case '>':
                if (stepper.peek(1) == '=') {
                    tokens.add(new Token(Symbol.GEQ, ">=", lineCnt));
                    stepper.next();
                } else {
                    tokens.add(new Token(Symbol.GRE, ">", lineCnt));
                }
                break;
            case '=':
                if (stepper.peek(1) == '=') {
                    tokens.add(new Token(Symbol.EQL, "==", lineCnt));
                    stepper.next();
                } else {
                    tokens.add(new Token(Symbol.ASSIGN, "=", lineCnt));
                }
                break;
            case ';':
                tokens.add(new Token(Symbol.SEMICN, ";", lineCnt));
                break;
            case ',':
                tokens.add(new Token(Symbol.COMMA, ",", lineCnt));
                break;
            case '(':
                tokens.add(new Token(Symbol.LPARENT, "(", lineCnt));
                break;
            case ')':
                tokens.add(new Token(Symbol.RPARENT, ")", lineCnt));
                break;
            case '[':
                tokens.add(new Token(Symbol.LBRACK, "[", lineCnt));
                break;
            case ']':
                tokens.add(new Token(Symbol.RBRACK, "]", lineCnt));
                break;
            case '{':
                tokens.add(new Token(Symbol.LBRACE, "{", lineCnt));
                break;
            case '}':
                tokens.add(new Token(Symbol.RBRACE, "}", lineCnt));
                break;
            default:
                break;
        }
    }

}
