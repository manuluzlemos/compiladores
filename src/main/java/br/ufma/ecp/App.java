package br.ufma.ecp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static br.ufma.ecp.TokenType.*;

public class App 
{

    private static String fromFile() {
        File file = new File("Main.jack");

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file.toPath());
            String textoDoArquivo = new String(bytes, "UTF-8");
            return textoDoArquivo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    } 

    public static void main( String[] args ){
        
        String input = "879 + 1 + manu - 999 + emanuelle";
        Parser p = new Parser(input.getBytes());
        p.parser();
        

        /*
        String input = "879 + manu1 - 1 * oi";
        Scanner scan = new Scanner(input.getBytes());
        for(Token tk = scan.nextToken(); tk.type != EOF; tk = scan.nextToken()){
            System.out.println(tk);
        }
        */
    }
}
