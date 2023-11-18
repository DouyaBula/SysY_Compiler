package MIPS;

import java.util.ArrayList;

public class CodePool {
    private final int frameCnt;
    private static final ArrayList<String> savedRegs =
            new ArrayList<String>() {{
                add("$t1");
                add("$t2");
                add("$t3");
                add("$t4");
                add("$ra");
                add("$s5");
                add("$s6");
                add("$v0");
            }};

    private CodePool() {
        frameCnt = savedRegs.size();
    }

    private static CodePool instance;

    public static CodePool getInstance() {
        if (instance == null) {
            instance = new CodePool();
        }
        return instance;
    }

    public String code(String op, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(op).append(" ");
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public ArrayList<String> syscall(int id) {
        ArrayList<String> codes = new ArrayList<>();
        codes.add(code("li", "$v0", "" + id));
        codes.add(code("syscall"));
        return codes;
    }

    public int getFrameCnt() {
        return frameCnt;
    }

    public ArrayList<String> saveRegs(String stackSizeReg) {
        ArrayList<String> codes = new ArrayList<>();
        codes.add(code("subu", stackSizeReg, "$sp", stackSizeReg));
        for (int i = 0; i < savedRegs.size(); i++) {
            codes.add(code("sw", savedRegs.get(i), (i + 1) * 4 + "(" + stackSizeReg + ")"));
        }
        return codes;
    }

    public ArrayList<String> restoreRegs(String stackSizeReg) {
        ArrayList<String> codes = new ArrayList<>();
        codes.add(code("subu", stackSizeReg, "$sp", stackSizeReg));
        for (int i = 0; i < savedRegs.size(); i++) {
            codes.add(code("lw", savedRegs.get(i), (i + 1) * 4 + "(" + stackSizeReg + ")"));
        }
        return codes;
    }


}
