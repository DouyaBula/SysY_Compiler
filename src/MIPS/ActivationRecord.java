package MIPS;

import IR.SymbolTable;
import IR.SymbolType;
import IR.TableTree;
import IR.Template;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivationRecord {
    private final ActivationRecord lastAR;
    private final SymbolTable baseTable;
    private final ArrayList<Integer> tableIdList;    // 用于记录当前AR中所有的符号表
    private final HashMap<String, Integer> def;
    private final HashMap<String, Integer> stack;
    private final ArrayList<String> mipsCode;
    private final CodePool codePool = CodePool.getInstance();
    private int defSize;
    private final int reserveSize = 12;    // 4 for last AR addr, 4 for stackSize, 4 for defSize

    public ActivationRecord(SymbolTable baseTable, ActivationRecord lastAR,
                            ArrayList<String> mipsCode) {
        this.lastAR = lastAR;
        this.baseTable = baseTable;
        this.tableIdList = new ArrayList<>();
        def = new HashMap<>();
        defSize = reserveSize;
        stack = new HashMap<>();
        this.mipsCode = mipsCode;
        initializeDef();
    }

    private void addStackSize(int delta) {
        // $s1 用来暂存AR大小
        mipsCode.add("# update stack size");
        mipsCode.add(codePool.code("lw", "$s1", -4 + "($fp)"));
        mipsCode.add(codePool.code("addi", "$s1", "$s1", delta + ""));
        mipsCode.add(codePool.code("sw", "$s1", -4 + "($fp)"));
    }

    private void initializeDef() {
        if (baseTable.getParent() != null) {  // not root
            // 把符号表和子符号表中的def递归加入到当前AR的def中
            // 考虑到子符号表的def可能会覆盖父符号表的def，所以用id+name作为key
            ArrayList<SymbolTable> symbolTables = new ArrayList<>();
            symbolTables.add(baseTable);
            while (!symbolTables.isEmpty()) {
                SymbolTable symbolTable = symbolTables.remove(0);
                tableIdList.add(symbolTable.getId());
                int baseOffset = getDefSize();
                for (Template template : symbolTable.getContent().values()) {
                    if (!template.is(SymbolType.FUNC)) {
                        def.put(symbolTable.getId() + template.getName(),
                                baseOffset + template.getOffset());
                    }
                }
                defSize += symbolTable.getSize();
                symbolTables.addAll(symbolTable.getChildren());
            }
        } else {
            tableIdList.add(baseTable.getId());
        }
    }

    public void addTemp(String name) {
        if (!stack.containsKey(name)) {
            stack.put(name, stack.size() * 4);
            addStackSize(4);
        }
    }

    public int getOffset(String name, int tableId) {
        if (stack.containsKey(name)) {
            return stack.get(name);
        } else {
            // 递归查找def
            SymbolTable table = TableTree.getInstance().getTable(tableId);
            while (table != null && tableIdList.contains(table.getId())) {
                Template template = table.getContent().get(name);
                if (template != null) {
                    return def.get(table.getId() + name);
                }
                table = table.getParent();
            }
        }
        return -114514;
    }

    public int getReserveSize() {
        return reserveSize;
    }

    public int getDefSize() {
        return defSize;
    }

    public void pushSth() {
        stack.put("sthAt" + stack.size(), stack.size() * 4);
        addStackSize(4);
    }

    public void pushSth(int cnt) {
        for (int i = 0; i < cnt; i++) {
            stack.put("sthAt" + stack.size(), stack.size() * 4);
        }
        addStackSize(cnt * 4);
    }

    public void popSth() {
        stack.remove("sthAt" + (stack.size() - 1));
        addStackSize(-4);
    }

    public void popSth(int cnt) {
        for (int i = 0; i < cnt; i++) {
            stack.remove("sthAt" + (stack.size() - 1));
        }
        addStackSize(-cnt * 4);
    }

    // 返回当前AR中的def
    public Template getDef(String name, int tableId) {
        SymbolTable table = TableTree.getInstance().getTable(tableId);
        while (table != null && tableIdList.contains(table.getId())) {
            Template template = table.getContent().get(name);
            if (template != null) {
                return template;
            }
            table = table.getParent();
        }
        return null;
    }

    // 全局查找Def
    public Template getDefGlobally(String name, int tableId) {
        Template def = getDef(name, tableId);
        if (def != null) {
            return def;
        } else {
            // 必在全局符号表中
            return TableTree.getInstance().getTable(0).getTemplate(name);
        }
    }

}
