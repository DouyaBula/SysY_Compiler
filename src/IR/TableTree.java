package IR;

import java.util.ArrayList;
import java.util.HashMap;

public class TableTree {
    private static TableTree tableTree;
    private SymbolTable rootTable;
    private SymbolTable currentTable;
    private ArrayList<String> stringPool;
    private int stringCnt = 0;
    private HashMap<Integer, SymbolTable> id2Table;
    private int size;

    public static TableTree getInstance() {
        if (tableTree == null) {
            tableTree = new TableTree();
            tableTree.rootTable = new SymbolTable(null);
            tableTree.currentTable = tableTree.rootTable;
            tableTree.stringPool = new ArrayList<>();
            tableTree.id2Table = new HashMap<>();
            tableTree.id2Table.put(tableTree.rootTable.getId(), tableTree.rootTable);
            tableTree.size = 0;
        }
        return tableTree;
    }

    public SymbolTable getTable(int id) {
        return id2Table.get(id);
    }

    public int getSize() {
        return size;
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

    public ArrayList<String> getStringPool() {
        return stringPool;
    }

    public void setCurrentTable(SymbolTable currentTable) {
        this.currentTable = currentTable;
    }

    public int addString(String str) {
        stringPool.add(str);
        return stringCnt++;
    }

    public void addSymbol(String name, Template template) {
        template.setBelongTable(currentTable);
        currentTable.addSymbol(name, template);
    }

    public void addConstDef(String name, Operand dim1, Operand dim2, ArrayList<Operand> initVal) {
        Template template = new Template(name, dim1, dim2, true, initVal);
        template.setOffset(currentTable.getSize());
        int delta = dim1.getConstVal() == 0 ? 4 :
                (dim2.getConstVal() == 0 ? 4 * dim1.getConstVal() :
                        4 * dim1.getConstVal() * dim2.getConstVal());
        if (currentTable.getParent() != null) {
            currentTable.addSize(delta);
            size += delta;
        }
        addSymbol(name, template);
    }

    public void addVarDef(String name, Operand dim1, Operand dim2, ArrayList<Operand> initVal) {
        Template template = new Template(name, dim1, dim2, false, initVal);
        template.setOffset(currentTable.getSize());
        int delta = dim1.getConstVal() == 0 ? 4 :
                (dim2.getConstVal() == 0 ? 4 * dim1.getConstVal() :
                        4 * dim1.getConstVal() * dim2.getConstVal());
        if (currentTable.getParent() != null) {
            currentTable.addSize(delta);
            size += delta;
        }
        addSymbol(name, template);
    }

    public void addFuncDef(String name, boolean hasRet, ArrayList<Operand> params) {
        Template template = new Template(name, hasRet, params);
        addSymbol(name, template);
    }

    public void addFuncDefToParent(String name, boolean hasRet, ArrayList<Operand> params) {
        Template template = new Template(name, hasRet, params);
        currentTable.getParent().addSymbol(name, template);
    }

    public void addFuncParam(String name, Operand dim1, Operand dim2) {
        Template template = new Template(name, dim1, dim2);
        template.setOffset(currentTable.getSize());
        int delta = dim1.getConstVal() == 0 ? 4 :
                (dim2.getConstVal() == 0 ? 4 * dim1.getConstVal() :
                        4 * dim1.getConstVal() * dim2.getConstVal());
        if (currentTable.getParent() != null) {
            currentTable.addSize(delta);
            size += delta;
        }
        addSymbol(name, template);
    }

    public void enterBlock() {
        SymbolTable newTable = new SymbolTable(currentTable);
        currentTable.addChild(newTable);
        currentTable = newTable;
        id2Table.put(currentTable.getId(), currentTable);
    }

    public void exitBlock() {
        currentTable = currentTable.getParent();
    }

    public void printTableTree() {
        System.out.println("Table Max Offset: " + size);
        System.out.println("String Pool:");
        for (int i = 0; i < stringCnt; i++) {
            System.out.println("#str" + i + ": " + stringPool.get(i));
        }
        System.out.println();
        rootTable.bfsPrint();
    }
}
