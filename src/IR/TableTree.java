package IR;

import java.util.ArrayList;

public class TableTree {
    private static TableTree tableTree;
    private SymbolTable rootTable;
    private SymbolTable currentTable;
    private ArrayList<String> stringPool;
    private int stringCnt = 0;

    public static TableTree getInstance() {
        if (tableTree == null) {
            tableTree = new TableTree();
            tableTree.rootTable = new SymbolTable(null);
            tableTree.currentTable = tableTree.rootTable;
            tableTree.stringPool = new ArrayList<>();
        }
        return tableTree;
    }

    public Template getTemplate(String name) {
        SymbolTable table = currentTable;
        while (table != null) {
            Template template = table.getTemplate(name);
            if (template != null) {
                return template;
            }
            table = table.getParent();
        }
        return null;
    }

    public SymbolTable getCurrentTable() {
        return currentTable;
    }

    public int addString(String str) {
        stringPool.add(str);
        return stringCnt++;
    }

    public void addSymbol(String name, Template template) {
        currentTable.addSymbol(name, template);
    }

    public void addConstDef(String name, Operand dim1, Operand dim2, ArrayList<Operand> initVal) {
        Template template = new Template(name, dim1, dim2, true, initVal);
        addSymbol(name, template);
    }

    public void addVarDef(String name, Operand dim1, Operand dim2, ArrayList<Operand> initVal) {
        Template template = new Template(name, dim1, dim2, false, initVal);
        addSymbol(name, template);
    }

    public void addFuncDef(String name,boolean hasRet ,ArrayList<Operand> params) {
        Template template = new Template(name, hasRet, params);
        addSymbol(name, template);
    }

    public void addFuncDefToParent(String name,boolean hasRet ,ArrayList<Operand> params) {
        Template template = new Template(name, hasRet, params);
        currentTable.getParent().addSymbol(name, template);
    }

    public void addFuncParam(String name, Operand dim1, Operand dim2) {
        Template template = new Template(name, dim1, dim2);
        addSymbol(name, template);
    }

    public void enterBlock() {
        SymbolTable newTable = new SymbolTable(currentTable);
        currentTable.addChild(newTable);
        currentTable = newTable;
    }

    public void exitBlock() {
        currentTable = currentTable.getParent();
    }

    public void printTableTree() {
        System.out.println("String Pool:");
        for (int i = 0; i < stringCnt; i++) {
            System.out.println("#str" + i + ": " + stringPool.get(i));
        }
        System.out.println();
        rootTable.bfsPrint();
    }
}
