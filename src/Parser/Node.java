package Parser;

import Lexer.Token;

import java.util.ArrayList;

public class Node {
    private final Term type;

    private final ArrayList<Node> children;
    private final Token token;
    private boolean isLeaf;

    public Node(Token token) {
        this.type = null;
        this.children = new ArrayList<>();
        this.isLeaf = true;
        this.token = token;
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

    public Token getToken() {
        return token;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Node getChildren(int index) {
        return children.get(index);
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
        if (isLeaf) {
            return token.getType() + " " + token.getRaw();
        } else {
            return "<" + type.toString() + ">";
        }
    }
}
