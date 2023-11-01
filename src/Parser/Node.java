package Parser;

import Lexer.Symbol;
import Lexer.Token;

import java.util.ArrayList;

public class Node {
    private final Term type;
    private Node parent;
    private final ArrayList<Node> children;
    private final Token token;
    private final boolean isLeaf;

    public Node(Token token) {
        this.type = null;
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.token = token;
        this.parent = null;
    }

    public Node(Term term) {
        this.type = term;
        this.children = new ArrayList<>();
        this.isLeaf = false;
        this.token = null;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public Term getType() {
        return type;
    }

    public boolean is(Term term) {
        return type != null && type == term;
    }

    public boolean is(Symbol symbol) {
        if (!isLeaf) return false;
        assert token != null;
        return token.is(symbol);
    }

    public boolean contains(Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (getChild(i) == null) {
                return false;
            }
            if (objects[i] instanceof Term) {
                if (!getChild(i).is((Term) objects[i])) {
                    return false;
                }
            } else if (objects[i] instanceof Symbol) {
                if (!getChild(i).is((Symbol) objects[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public Token getToken() {
        return token;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public void addChild(Node node) {
        children.add(node);
        node.setParent(this);
    }

    public void mergeChildrenTo(Node insert) {
        for (Node child : children) {
            insert.addChild(child);
        }
        children.clear();
        addChild(insert);
    }

    public Node getLastChild() {
        return getChild(children.size() - 1);
    }

    public Node getFirstChild() {
        return getChild(0);
    }

    public Node getChild(int index) {
        if (index < 0 || index >= children.size()) {
            return null;
        }
        return children.get(index);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void traversalLRN() {
        for (Node child :
                children) {
            child.traversalLRN();
        }
        if (type != Term.BlockItem && type != Term.Decl && type != Term.BType) {
            System.out.println(this);
        }
    }

    @Override
    public String toString() {
        if (isLeaf()) {
            assert token != null;
            return token.getType() + " " + token.getRaw();
        } else {
            assert type != null;
            return "<" + type + ">";
        }
    }
}
