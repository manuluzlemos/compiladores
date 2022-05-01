package br.ufma.ecp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
            String input = "8790+1";
            //Parser p = new Passer(input.getBytes());
            Scanner p = new Scanner(input.getBytes());
            System.out.println(p.nextToken());
            System.out.println(p.nextToken());
            System.out.println(p.nextToken());
    }
}
