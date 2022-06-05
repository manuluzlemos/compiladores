package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;


import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;

public class Parser {

    private static class ParseError extends RuntimeException {}

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    private StringBuilder xmlOutput = new StringBuilder();

    public Parser(byte[] input) {
        scan = new Scanner(input);
        nextToken();
    }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = scan.nextToken();
    }

    void parse() {
        parseClass();
    }

    void parseClass() {
        printNonTerminal("class");
        
        expectPeek(CLASS);
        expectPeek(IDENTIFIER); // className
        expectPeek(LBRACE);

        // 0 ou várias declarações de variáveis da classe
        while (peekTokenIs(STATIC) || peekTokenIs(FIELD)){
            parseClassVarDec();
        }

        // 0 ou várias declarações de subroutinas da classe subroutineDec
        while (peekTokenIs(CONSTRUCTOR) || peekTokenIs(FUNCTION) || peekTokenIs(METHOD)){
            parseSubroutineDec();
        }        

        expectPeek(RBRACE);

        printNonTerminal("/class");
    }

    void parseSubroutineCall() {

    }

    void parseDo() {
        printNonTerminal("doStatement");

        expectPeek(DO);
        parseSubroutineCall();
        expectPeek(SEMICOLON);
    
        printNonTerminal("/doStatement");
    }

    void parseVarDec() {
        printNonTerminal("varDec");

        expectPeek(VAR);
        parseType();
        expectPeek(IDENTIFIER); // varName
        while(peekTokenIs(COMMA)){  // pode ter outras variáveis
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/varDec");
    }

    void parseClassVarDec() {
        printNonTerminal("classVarDec");

        expectPeek(STATIC, FIELD);
        parseType();
        expectPeek(IDENTIFIER); // varName 
        while(peekTokenIs(COMMA)){  // pode ter outras variáveis
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);
        }
        expectPeek(SEMICOLON);

        printNonTerminal("/classVarDec");
    }

    void parseType(){
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER); // int, char, boolean ou className
    }

    void parseSubroutineDec() {
        printNonTerminal("subroutineDec");

        expectPeek(CONSTRUCTOR, FUNCTION, METHOD);

        // void | type
        if(peekTokenIs(VOID)){
            expectPeek(VOID);
        }else{
            parseType();
        }

        expectPeek(IDENTIFIER); // subroutineName
        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);
        parseSubroutineBody();
   
        printNonTerminal("/subroutineDec");
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

        if (!peekTokenIs(RPAREN)){
            parseType(); 
            expectPeek(IDENTIFIER);
        }

        while (peekTokenIs(COMMA)){
            expectPeek(COMMA);
            parseType();
            expectPeek(IDENTIFIER);
        }

        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody() {
        printNonTerminal("subroutineBody");

        expectPeek(LBRACE);

        // 0 ou várias declarações de variáveis da subrotina 
        while (peekTokenIs(VAR)){
            parseVarDec();
        }

        parseStatements();
        expectPeek(RBRACE);
     
        printNonTerminal("/subroutineBody");
    }

    void parseLet() {
        printNonTerminal("letStatement");

        expectPeek(LET);
        expectPeek(IDENTIFIER); // varName

        if (peekTokenIs(LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();
            expectPeek(RBRACKET);
        }

        expectPeek(EQ);
        parseExpression();
        expectPeek(SEMICOLON);

        printNonTerminal("/letStatement");
    }

    void parseWhile() {
        printNonTerminal("whileStatement");

        expectPeek(WHILE);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);
  
        printNonTerminal("/whileStatement");
    }

    void parseIf() {
        printNonTerminal("ifStatement");

        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);
    
        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);

        //opcional ter o else
        if(peekTokenIs(ELSE)){
            expectPeek(ELSE);
            expectPeek(LBRACE);
            parseStatements();
            expectPeek(RBRACE);
        }

        printNonTerminal("/ifStatement");
    }

    void parseStatements() {
        printNonTerminal("statements");
        
        // 0 ou vários statements
        while (peekTokenIs(LET) || peekTokenIs(IF) || peekTokenIs(WHILE) || peekTokenIs(DO) || peekTokenIs(RETURN)) {
            parseStatement();
        }

        printNonTerminal("/statements");
    }

    void parseStatement() {
        switch (peekToken.type) {
            case LET:
                parseLet();
                break;
            case IF:
                parseIf();
                break;
            case WHILE:
                parseWhile();
                break;
            case DO:
                parseDo();
                break;
            case RETURN:
                parseReturn();
                break;
        }
    }

    void parseReturn() {
        printNonTerminal("returnStatement");

        expectPeek(RETURN);

        // opcional - inserir expressão como opcional 

        expectPeek(SEMICOLON);
        
        printNonTerminal("/returnStatement");
    }

    void parseExpressionList() {
        printNonTerminal("expressionList");

        printNonTerminal("/expressionList");
    }

    void parseExpression() {
        printNonTerminal("expression");
        parseTerm();
        while (isOperator(peekToken.type)) {
            expectPeek(peekToken.type);
            parseTerm();
        }
        printNonTerminal("/expression");
    }

    void parseTerm() {
        printNonTerminal("term");
        switch (peekToken.type) {
            case INTEGER:
                expectPeek(INTEGER);
                break;
            case STRING:
                expectPeek(STRING);
                break;
            case FALSE:
            case NULL:
            case TRUE:
            case THIS:
                expectPeek(FALSE, NULL, TRUE, THIS);
                break;
            case IDENTIFIER:
                expectPeek(IDENTIFIER);
                if (peekTokenIs (LBRACKET) ) {
                    expectPeek(LBRACKET);
                    parseExpression();
                    expectPeek(RBRACKET);
                }
                break;
            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                parseTerm();
                break;
            default:
                throw error (peekToken, "term expected");
        }
        printNonTerminal("/term");
    }

    // funções auxiliares
    public String XMLOutput() {
        return xmlOutput.toString();
    }

    private void printNonTerminal(String nterminal) {
        xmlOutput.append(String.format("<%s>\r\n", nterminal));
    }


    boolean peekTokenIs(TokenType type) {
        return peekToken.type == type;
    }

    boolean currentTokenIs(TokenType type) {
        return currentToken.type == type;
    }

    private void expectPeek(TokenType... types) {
        for (TokenType type : types) {
            if (peekToken.type == type) {
                expectPeek(type);
                return;
            }
        }

       throw error(peekToken, "Expected a statement");
    }

    private void expectPeek(TokenType type) {
        if (peekToken.type == type) {
            nextToken();
            xmlOutput.append(String.format("%s\r\n", currentToken.toString()));
        } else {
            throw error(peekToken, "Expected "+type.name());
        }
    }

    private static void report(int line, String where,
        String message) {
            System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
    }

    private ParseError error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.value() + "'", message);
        }
        return new ParseError();
    }

}