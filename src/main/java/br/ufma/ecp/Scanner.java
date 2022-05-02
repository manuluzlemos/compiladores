package br.ufma.ecp;

import java.nio.charset.StandardCharsets;

public class Scanner {

    private byte[] input;
    private int current;
    private int start;
    
    public Scanner (byte[] input){
        this.input = input;
        current = 0;
        start = 0;
    }

    public Token nextToken(){
        start = current;
        char ch = peek();

        if(Character.isDigit(ch)){
            return number();
        }

        switch(ch){
            case '+':
                advance();
                return new Token(TokenType.PLUS, "+");
            case '-':
                advance();
                return new Token(TokenType.MINUS, "-");
            case '0':
                return new Token(TokenType.EOF, "EOF");
            default:
                break;
        }
        return null;
    }

    private Token number(){
        while(Character.isDigit(peek())){
            advance();
        }
        String s = new String(input, start, current-start, StandardCharsets.UTF_8);
        Token token = new Token(TokenType.NUMBER, s);
        return token;
    }

    private void advance(){
        char ch = peek();
        if (ch != 0) {
            current++;
        }
    }

    private void match(char c){
        if (c == peek()) {
            current++;
        } else {
            throw new Error("syntax error");
        }
    }

    private char peek(){
        if (current < input.length) {
            return (char)input[current];
        } else {
            return 0;
        }
    }
}