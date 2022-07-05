package br.ufma.ecp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;


class Pessoa {
    String name;
    public Pessoa (String name) {
        this.name = name;
    }
}

public class App {

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

    public static void main( String[] args )
    {

        String s1 = "Ola Mundo";
        String s2 = new String ("Ola Mundo");

        if (s1.equals(s2)) {
            System.out.println("ok");
        } else {
            System.out.println("deu ruim");
        }

        Pessoa p1 = new Pessoa ("joão");
        Pessoa p2 = new Pessoa ("joão");

        if (p1 == p2) {
            System.out.println("ok");
        } else {
            System.out.println("p1 e p2 sao diferentes");
        }

    }
}
