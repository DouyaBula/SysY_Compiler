package MIPS;

import IR.SymbolTable;
import IR.SymbolType;
import IR.Template;

import java.util.HashMap;

public class ActivationRecord {
    private final ActivationRecord lastAR;
    private final SymbolTable symbolTable;
    private final HashMap<String, Integer> def;
    private final HashMap<String, Integer> stack;

    public ActivationRecord(SymbolTable symbolTable, ActivationRecord lastAR) {
        this.lastAR = lastAR;
        this.symbolTable = symbolTable;
        def = new HashMap<>();
        initializeDef();
        stack = new HashMap<>();
    }

    private void initializeDef() {
        if (symbolTable.getParent() != null) {  // not root
            for (Template template : symbolTable.getContent().values()) {
                if (template.is(SymbolType.VAR) ||
                        template.is(SymbolType.CONST) ||
                        template.is(SymbolType.PARAM)) {
                    def.put(template.getName(), template.getOffset() + 4); // 4 for last AR addr
                }
            }
        }
    }

    public void addTemp(String name) {
        if (!stack.containsKey(name)) {
            stack.put(name, stack.size() * 4);
        }
    }

    public int getOffset(String name) {
        if (def.containsKey(name)) {
            return def.get(name);
        } else if (stack.containsKey(name)) {
            return stack.get(name);
        } else {
            return -114514;
        }
    }

    public int getDefSize() {
        return def.size() * 4 + 4;   // 4 for last AR addr
    }

    public ActivationRecord getLastAR() {
        return lastAR;
    }

    public int getStackSize() {
        return stack.size() * 4;
    }

    public void pushSth() {
        stack.put("sthAt" + stack.size(), stack.size() * 4);
    }

    public void pushSth(int size) {
        for (int i = 0; i < size; i++) {
            stack.put("sthAt" + stack.size(), stack.size() * 4);
        }
    }

    // 返回当前AR中的def
    public Template getDef(String name) {
        return symbolTable.getContent().get(name);
    }

    // 递归查找def
    public Template getDefRecursively(String name) {
        Template template = symbolTable.getContent().get(name);
        if (template != null) {
            return template;
        } else if (lastAR != null) {
            return lastAR.getDefRecursively(name);
        } else {
            return null;
        }
    }

    public int getSize() {
        return getDefSize() + getStackSize();
    }
}
