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
        term();
        oper();
    }

    void term(){
        if (currentTokenIs(NUMBER)) {
            number();
        } else if (currentTokenIs(IDENTIFIER)) {
            identifier();
        } else {
            throw new Error("syntax error found " + currentToken.lexeme);
        }
    }

    /* GRAMÁTICA

    oper -> term oper 
     | - term oper 
     | ϵ 
    
    term -> number | identifier
    */

    void oper() {
        if (currentTokenIs(PLUS)) {
            match(PLUS);
            term();
            System.out.println("add");
            oper();
        } else if (currentTokenIs(MINUS)) {
            match(MINUS);
            term();
            System.out.println("sub");
            oper();
        } else if (currentTokenIs(EOF)) {
            // vazio --> indica o final da entrada/arquivo
        } else {
            throw new Error("syntax error found " + currentToken.lexeme);
        }
    }

    void number(){
        System.out.println("push " + currentToken.lexeme);
        match(NUMBER);
    }

    void identifier(){
        System.out.println("push " + currentToken.lexeme);
        match(IDENTIFIER);
    }

    boolean currentTokenIs (TokenType type){
        return currentToken.type == type;
    }
    
}