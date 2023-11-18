package MIPS;

import java.util.ArrayList;

public class CodePool {

    private CodePool() {
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

    public ArrayList<String> syscall(int id){
        ArrayList<String> codes = new ArrayList<>();
        codes.add(code("li", "$v0", "" + id));
        codes.add(code("syscall"));
        return codes;
    }

    public ArrayList<String> saveRegs(int stackSize) {
        ArrayList<String> codes = new ArrayList<>();
        // save $t1 - $t4
        codes.add(code("sw", "$t1", 4 - stackSize + "($sp)"));
        codes.add(code("sw", "$t2", 8 - stackSize + "($sp)"));
        codes.add(code("sw", "$t3", 12 - stackSize + "($sp)"));
        codes.add(code("sw", "$t4", 16 - stackSize + "($sp)"));
        // save $ra
        codes.add(code("sw", "$ra", 20 - stackSize + "($sp)"));
        // save $fp
        codes.add(code("sw", "$fp", 24 - stackSize + "($sp)"));
        // save $sp
        codes.add(code("sw", "$sp", 28 - stackSize + "($sp)"));
        // save $v0
        codes.add(code("sw", "$v0", 32 - stackSize + "($sp)"));
        return codes;
    }

    public ArrayList<String> restoreRegs(int stackSize) {
        ArrayList<String> codes = new ArrayList<>();
        // save $t1 - $t4
        codes.add(code("lw", "$t1", -4 - stackSize + "($sp)"));
        codes.add(code("lw", "$t2", -8 - stackSize + "($sp)"));
        codes.add(code("lw", "$t3", -12 - stackSize + "($sp)"));
        codes.add(code("lw", "$t4", -16 - stackSize + "($sp)"));
        // save $ra
        codes.add(code("lw", "$ra", -20 - stackSize + "($sp)"));
        // save $fp
        codes.add(code("lw", "$fp", -24 - stackSize + "($sp)"));
        // save $sp
        codes.add(code("lw", "$sp", -28 - stackSize + "($sp)"));
        // save $v0
        codes.add(code("lw", "$v0", -32 - stackSize + "($sp)"));
        return codes;
    }


}
