package br.ufma.ecp;

import static br.ufma.ecp.TokenType.*;

public class Parser {

    private Scanner scan;
    private Token currentToken;

    public Parser (byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = scan.nextToken();
    }

    private void match(TokenType type) {
        if (currentToken.type == type) {
            nextToken();
        } else {
            throw new Error("Syntax Error - expected " + type + " found " + currentToken.lexeme);
        }
    }

    void parser() {
        expr();
    }

    void expr() {
        number();
        oper();
    }

    void oper() {
        if (currentTokenIs(PLUS)) {
            match(PLUS);
            number();
            System.out.println("add");
            oper();
        } else if (currentTokenIs(MINUS)) {
            match(MINUS);
            number();
            System.out.println("sub");
            oper();
        } else if (currentTokenIs(EOF)) {
            // vazio --> indica o final da entrada/arquivo
        } else {
            throw new Error("syntax error found " + currentToken.lexeme);
        }
    }

    void number() {
        System.out.println("push " + currentToken.lexeme);
        match(NUMBER);
    }

    boolean currentTokenIs (TokenType type){
        return currentToken.type == type;
    }
    
}