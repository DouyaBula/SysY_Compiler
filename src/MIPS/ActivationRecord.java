package MIPS;

import IR.OperandType;
import IR.Operator;
import IR.SymbolTable;
import IR.SymbolType;
import IR.TableTree;
import IR.Template;
import IR.Tuple;
import IR.TupleList;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivationRecord {
    private final SymbolTable baseTable;
    private final ArrayList<Integer> tableIdList;    // 用于记录当前AR中所有的符号表
    private final HashMap<String, Integer> def;
    private final HashMap<String, Integer> temp;
    private int defSize;
    private int tempSize;
    private int saveSize = CodePool.getInstance().getFrameCnt() * 4;
    private final int reserveSize = 4;    // 4 for last AR addr
    private final HashMap<SymbolTable, ActivationRecord> arMap;

    public ActivationRecord(SymbolTable baseTable,
                            HashMap<SymbolTable, ActivationRecord> arMap, int tupleId) {
        this.baseTable = baseTable;
        this.tableIdList = new ArrayList<>();
        def = new HashMap<>();
        temp = new HashMap<>();
        defSize = reserveSize;
        this.arMap = arMap;
        initialize(tupleId);
    }

    private void initialize(int tupleId) {
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
                arMap.put(symbolTable, this);
            }

            // 接下来处理temp
            Tuple tuple = TupleList.getInstance().getTuple(tupleId - 1);
            String funcName = tuple.getOperand1().getName();    // 得到函数名
            funcName = funcName.substring(0, funcName.length() - 6); // 去掉funcName的"_BEGIN"后缀
            // 遍历tuple直到遇见函数结尾
            int i = tupleId;
            while (i < TupleList.getInstance().getTuples().size()) {
                tuple = TupleList.getInstance().getTuple(i);
                if (tuple.getOperator() == Operator.LABEL) {
                    if (tuple.getOperand1().getName().equals(funcName + "_END")) {
                        break;
                    }
                }
                // 找到tuple中的temp Operand
                if (tuple.getOperand1() != null &&
                        tuple.getOperand1().getType() == OperandType.TEMP &&
                        !temp.containsKey(tuple.getOperand1().getName())) {
                    temp.put(tuple.getOperand1().getName(), tempSize);
                    tempSize += 4;
                }
                if (tuple.getOperand2() != null &&
                        tuple.getOperand2().getType() == OperandType.TEMP &&
                        !temp.containsKey(tuple.getOperand2().getName())) {
                    temp.put(tuple.getOperand2().getName(), tempSize);
                    tempSize += 4;
                }
                if (tuple.getResult() != null &&
                        tuple.getResult().getType() == OperandType.TEMP &&
                        !temp.containsKey(tuple.getResult().getName())) {
                    temp.put(tuple.getResult().getName(), tempSize);
                    tempSize += 4;
                }
                i++;
            }
        } else {
            tableIdList.add(baseTable.getId());
            arMap.put(baseTable, this);
        }
    }

    public int getOffset(String name, int tableId) {
        if (temp.containsKey(name)) {
            return defSize + temp.get(name);
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

    public int getTempSize() {
        return tempSize;
    }

    public int getSaveSize() {
        return saveSize;
    }

    public int getSize() {
        return defSize + tempSize + saveSize;
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
