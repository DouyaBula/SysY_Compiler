package IR;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final ArrayList<SymbolTable> children;
    private final HashMap<String, Template> content;
    private int depth;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.content = new HashMap<>();
        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.getDepth() + 1;
        }
    }

    public SymbolTable getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }

    public HashMap<String, Template> getContent() {
        return content;
    }

    public void addChild(SymbolTable child) {
        children.add(child);
    }

    public void addSymbol(String name, Template template) {
        content.put(name, template);
    }

    public Template getTemplate(String name) {
        return content.get(name);
    }

    public void bfsPrint() {
        ArrayList<SymbolTable> queue = new ArrayList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            SymbolTable table = queue.remove(0);
            System.out.println(table);
            queue.addAll(table.getChildren());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Depth").append(getDepth()).append("-SymbolTable: \n");
        for (String key : content.keySet()) {
            sb.append(key).append(" : ").append(content.get(key)).append("\n");
        }
        return sb.toString();
    }

}
