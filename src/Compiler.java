import Lexer.Lexer;
import Lexer.Token;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class Compiler {
    public static final String inputFilePath = "testfile.txt";
    public static final String outputFilePath = "output.txt";
    public static void main(String[] args) {
        try {
            FileReader inputFile = new FileReader(inputFilePath);
            BufferedReader input = new BufferedReader(inputFile);
            FileWriter outputFile = new FileWriter(outputFilePath);
            BufferedWriter output = new BufferedWriter(outputFile);

            Lexer lexer = new Lexer(input);
            ArrayList<Token> tokens = lexer.analyze();
            for (Token token :
                    tokens) {
                output.write(token.getType() + " " + token.getRaw()+"\n");
            }

            input.close();
            output.close();
            inputFile.close();
            outputFile.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}