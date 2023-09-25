package Lexer;

public class Stepper {
    private int pos;
    private final String line;

    public Stepper(String line) {
        this.line = line;
        this.pos = 0;
    }

    public char peek() {
        return peek(0);
    }

    public char peek(int offset) {
        if (pos + offset >= line.length()) {
            return '\0';
        }
        return line.charAt(pos + offset);
    }

    public char next() {
        pos++;
        if (pos >= line.length()) {
            pos = line.length();
            return '\0';
        }
        return line.charAt(pos - 1);
    }

    public char next(int offset) {
        pos += offset;
        if (pos >= line.length()) {
            pos = line.length();
            return '\0';
        }
        return line.charAt(pos - offset);
    }


    public void skip() {
        // skip white spaces
        while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
    }

    public String getWord() {
        StringBuilder word = new StringBuilder();
        while (pos < line.length() &&
                (Character.isLetter(line.charAt(pos))
                        || Character.isDigit(line.charAt(pos))
                        || line.charAt(pos) == '_')) {
            word.append(line.charAt(pos++));
        }
        pos--;
        return word.toString();
    }

    public String getFormatStr() {
        StringBuilder word = new StringBuilder();
        word.append(line.charAt(pos++)); // "
        while (pos < line.length()) {
            char c = line.charAt(pos);
            if (c == '\"') {
                word.append(line.charAt(pos++));
                break;
            } else if (c == 32 || c == 33 || (c >= 40 && c <= 126 && c != '\\')) {
                word.append(line.charAt(pos++));
            } else if (c == '\\') {
                if (line.charAt(pos + 1) != 'n') {
                    word = null;
                    break;
                } else {
                    word.append(line.charAt(pos++)); // \
                    word.append(line.charAt(pos++)); // n
                }
            } else if (c == '%') {
                if (line.charAt(pos + 1) != 'd') {
                    word = null;
                    break;
                } else {
                    word.append(line.charAt(pos++)); // %
                    word.append(line.charAt(pos++)); // d
                }
            } else {
                word = null;
                break;
            }
        }
        pos--;
        return word == null ? null : word.toString();
    }

    public String getConst() {
        StringBuilder word = new StringBuilder();
        while (pos < line.length() && Character.isDigit(line.charAt(pos))) {
            word.append(line.charAt(pos++));
        }
        pos--;
        return word.toString();
    }

    public boolean atEnd() {
        return pos >= line.length();
    }
}
