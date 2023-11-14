import Error.Reporter;
import IR.TableTree;
import IR.Translator;
import IR.TupleList;
import Lexer.Lexer;
import Lexer.Token;
import MIPS.Generator;
import Parser.Node;
import Parser.Parser;

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
            // 打开输入文件
            FileReader inputFile = new FileReader(inputFilePath);
            BufferedReader input = new BufferedReader(inputFile);
            FileWriter outputFile = new FileWriter(outputFilePath);
            BufferedWriter output = new BufferedWriter(outputFile);

            // 词法分析
            Reporter reporter = new Reporter(output);
            Lexer lexer = new Lexer(input, reporter);
            ArrayList<Token> tokens = lexer.analyze();

            // 语法分析
            Parser parser = new Parser(tokens, reporter);
            Node root = parser.parseCompUnit();
            reporter.print();

            // 语义分析与中间代码生成
            if (!reporter.hasError()) {
                Translator translator = new Translator(root);
                translator.translate();
                TableTree.getInstance().printTableTree();
                TupleList.getInstance().print();
            }

            // MIPS目标代码生成
            if (!reporter.hasError()) {
                Generator generator = new Generator();
                generator.generate();
            }


            // 关闭文件
            input.close();
            inputFile.close();
            output.close();
            outputFile.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}