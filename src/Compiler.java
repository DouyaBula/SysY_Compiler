import Error.Reporter;
import IR.Translator;
import IR.TupleList;
import Lexer.Lexer;
import Lexer.Token;
import Parser.Node;
import Parser.Parser;
import Symbol.Table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class Compiler {
    public static final String inputFilePath = "testfile.txt";
    public static final String outputFilePath = "error.txt";

    public static void main(String[] args) {
        try {
            FileReader inputFile = new FileReader(inputFilePath);
            BufferedReader input = new BufferedReader(inputFile);
            FileWriter outputFile = new FileWriter(outputFilePath);
            BufferedWriter output = new BufferedWriter(outputFile);

            Reporter reporter = new Reporter(output);
            Lexer lexer = new Lexer(input, reporter);
            ArrayList<Token> tokens = lexer.analyze();
            Parser parser = new Parser(tokens, reporter);
            Node root = parser.parseCompUnit();
            reporter.print();
            Translator translator = new Translator(root);
            translator.translate();
            TupleList.getInstance().print();

            input.close();
            inputFile.close();
            output.close();
            outputFile.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}