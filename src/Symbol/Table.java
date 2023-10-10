package Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {
    private Table parent;
    private ArrayList<Table> children;
    private final HashMap<String, Attribute> content;

    public Table(Table parent) {
        this.parent = parent;
        this.children = new ArrayList<>();
        this.content = new HashMap<>();
    }

    public void addChild(Table child) {
        children.add(child);
    }

    public void addSymbol(String name, Attribute attr) {
        content.put(name, attr);
    }

    public Table getParent() {
        return parent;
    }

    public Attribute getSymbol(String name) {
        return content.get(name);
    }

    public boolean isRoot() {
        return parent == null;
    }
}
