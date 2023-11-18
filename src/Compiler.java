import Error.Reporter;
import IR.TableTree;
import IR.Translator;
import Lexer.Lexer;
import Lexer.Token;
import MIPS.RobustGenerator;
import Parser.Node;
import Parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class Compiler {
    public static final String inputFilePath = "testfile.txt";
    public static final String errorFilePath = "error.txt";
    public static final String tableFilePath = "table.txt";
    public static final String irFilePath = "ir.txt";
    public static final String mipsFilePath = "mips.txt";

    public static void main(String[] args) {
        try {
            // 打开IO
            FileReader inputFile = new FileReader(inputFilePath);
            BufferedReader input = new BufferedReader(inputFile);
            FileWriter errorFile = new FileWriter(errorFilePath);
            BufferedWriter error = new BufferedWriter(errorFile);
            FileWriter tableFile = new FileWriter(tableFilePath);
            BufferedWriter table = new BufferedWriter(tableFile);
            FileWriter irFile = new FileWriter(irFilePath);
            BufferedWriter ir = new BufferedWriter(irFile);
            FileWriter mipsFile = new FileWriter(mipsFilePath);
            BufferedWriter mips = new BufferedWriter(mipsFile);

            // 词法分析
            Reporter reporter = new Reporter(error);
            Lexer lexer = new Lexer(input, reporter);
            ArrayList<Token> tokens = lexer.analyze();

            // 语法分析
            Parser parser = new Parser(tokens, reporter);
            Node root = parser.parseCompUnit();
            reporter.write();

            // 语义分析与中间代码生成
            if (!reporter.hasError()) {
                Translator translator = new Translator(root, ir);
                translator.translate();
                TableTree.getInstance().printTableTree(table);
                translator.write();
            }

            // MIPS目标代码生成
            if (!reporter.hasError()) {
//                ObsoleteGenerator generator = new ObsoleteGenerator(mips);
                RobustGenerator generator = new RobustGenerator(mips);
                generator.generate();
                generator.write();
            }


            // 关闭文件
            input.close();
            inputFile.close();
            error.close();
            errorFile.close();
            table.close();
            tableFile.close();
            ir.close();
            irFile.close();
            mips.close();
            mipsFile.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}