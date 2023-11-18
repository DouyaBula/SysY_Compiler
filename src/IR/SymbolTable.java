package IR;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final SymbolTable parent;
    private final ArrayList<SymbolTable> children;
    private final HashMap<String, Template> content;
    private int depth;
    private int id;
    private int size;
    public static int cnt = 0;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.content = new HashMap<>();
        if (parent == null) {
            this.depth = 0;
        } else {
            this.depth = parent.getDepth() + 1;
        }
        this.size = 0;
        this.id = cnt++;
    }

    public int getSize() {
        return size;
    }

    public void addSize(int size) {
        this.size += size;
    }

    public int getId() {
        return id;
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

    public void bfsPrint(BufferedWriter tableFile) throws IOException {
        ArrayList<SymbolTable> queue = new ArrayList<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            SymbolTable table = queue.remove(0);
            tableFile.write(table.toString()+"\n");
            queue.addAll(table.getChildren());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SymbolTable ").append(id).append(" size ").append(size).append(":\n");
        for (String key : content.keySet()) {
            sb.append(content.get(key)).append(" offset ")
                    .append(content.get(key).getOffset()+4).append("\n");
        }
        return sb.toString();
    }

}
