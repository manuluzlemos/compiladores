package br.ufma.ecp;

import static br.ufma.ecp.TokenType.*;

public class Parser {

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;

    public Parser (byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }


    void parser(){
        parserLet();
    }

    // letStatement -> 'let' identifier ( '[' expression ']' )? '=' expression ';'
    void parserLet(){
        System.out.println("<letStatement>");
        expectPeek(LET);
        expectPeek(IDENTIFIER);
        expectPeek(ASSIGN);
        parserExpression();
        expectPeek(SEMICOLON);
        System.out.println("</letStatement>");
    }

    // expression -> term (op term)*
    void parserExpression(){
        System.out.println("<expression>");
        parseTerm();
        parseOper();
        System.out.println("</expression>");
    }

    // op -> + | - | * | / | < | > | =
    void parseOper() {
        if (peekTokenIs(PLUS)) {
            expectPeek(PLUS);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(MINUS)) {
            expectPeek(MINUS);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(ASTERISK)) {
            expectPeek(ASTERISK);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(SLASH)) {
            expectPeek(SLASH);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(LT)) {
            expectPeek(LT);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(BT)) {
            expectPeek(BT);
            parseTerm();
            parseOper();
        } else if (peekTokenIs(SEMICOLON)) {
            // vazio --> indica o final da entrada/arquivo
        } else {
            throw new Error("syntax error found " + currentToken.lexeme);
        }
    }

    // term -> number | stringConstant | keywordConstant | identifier
    void parseTerm () {
        System.out.println("<term>");
        switch (peekToken.type) {
            case NUMBER:
                expectPeek(NUMBER);
                break;
            case IDENTIFIER:
                expectPeek(IDENTIFIER);
                break;
            case STRING: 
                expectPeek(STRING);
                break;
            case TRUE:
                expectPeek(TRUE);
                break;
            case FALSE:
                expectPeek(FALSE);
                break;
            case NULL:
                expectPeek(NULL);
                break;
            case THIS:
                expectPeek(THIS);
                break;
            default:
                ;
        }
        System.out.println("</term>");
    }

    // funções auxiliares

    boolean peekTokenIs (TokenType type){
        return peekToken.type == type;
    }

    boolean currentTokenIs (TokenType type){
        return currentToken.type == type;
    }

    private void expectPeek(TokenType type) {
        if (peekToken.type == type) {
            nextToken();
            System.out.println(currentToken);
        } else {
            throw new Error("Syntax Error - expected " + type + " found " + peekToken.lexeme);
        }
    }
    
}