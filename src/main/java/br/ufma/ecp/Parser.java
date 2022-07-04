package br.ufma.ecp;

import static br.ufma.ecp.token.TokenType.*;

import br.ufma.ecp.SymbolTable.Kind;
import br.ufma.ecp.SymbolTable.Symbol;
import br.ufma.ecp.VMWriter.Segment;
import br.ufma.ecp.VMWriter.Command;
import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;


public class Parser {

    private static class ParseError extends RuntimeException {}

    private Scanner scan;
    private Token currentToken;
    private Token peekToken;
    //private String xmlOutput = "";
    private StringBuilder xmlOutput = new StringBuilder();
    private VMWriter vmWriter = new VMWriter();
    private SymbolTable symbolTable = new SymbolTable();

    private String className;

    private int ifLabelNum = 0;
    private int whileLabelNum = 0;

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
        expectPeek(IDENTIFIER);
        className = currentToken.value();
        expectPeek(LBRACE);
        
        while (peekTokenIs(STATIC) || peekTokenIs(FIELD)) {
            parseClassVarDec();
        }
    
        while (peekTokenIs(FUNCTION) || peekTokenIs(CONSTRUCTOR) || peekTokenIs(METHOD)) {
            parseSubroutineDec();
        }

        expectPeek(RBRACE);

        printNonTerminal("/class");
    }

    // subroutineCall -> subroutineName '(' expressionList ')' | (className|varName)
    // '.' subroutineName '(' expressionList ')
    void parseSubroutineCall() {
        String functionName;
        var numArgs = 0;

        var ident = currentToken.value();

        if (peekTokenIs(LPAREN)) { // é um método da própria classe
            expectPeek(LPAREN);
            vmWriter.writePush(Segment.POINTER, 0);
            numArgs = parseExpressionList();
            expectPeek(RPAREN);
            numArgs++;
            
            functionName = className + '.' + ident;

        } else { // pode ser um metodo de um outro objeto ou uma função
            expectPeek(DOT);
            expectPeek(IDENTIFIER);
            
            Symbol symbol = symbolTable.resolve(currentToken.value());
            functionName = currentToken.value();

            if(symbolTable.resolve(ident, symbol)){
                vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
                functionName = symbol.type() + "." + functionName;    
                numArgs = 1;
            } else {
                functionName = ident + "." + functionName;
            }

            expectPeek(LPAREN);
            numArgs = parseExpressionList() + numArgs; 
            expectPeek(RPAREN);
        }

        vmWriter.writeCall(functionName, numArgs);
    }

    void parseDo() {
        printNonTerminal("doStatement");
        expectPeek(DO);
        expectPeek(IDENTIFIER);
        parseSubroutineCall();
        vmWriter.writePop(Segment.TEMP, 0);
        expectPeek(SEMICOLON);

        printNonTerminal("/doStatement");
    }

    // 'var' type varName ( ',' varName)* ';'

    void parseVarDec() {
        printNonTerminal("varDec");

        expectPeek(VAR);
        var kind = SymbolTable.Kind.VAR;

        // 'int' | 'char' | 'boolean' | className
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        var type = currentToken.value();

        expectPeek(IDENTIFIER);
        var name = currentToken.value(); // varName

        symbolTable.define(name, type, kind);

        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);

            name = currentToken.value(); // varName
            symbolTable.define(name, type, kind);
        }

        expectPeek(SEMICOLON);
        printNonTerminal("/varDec");
    }

    // classVarDec → ( 'static' | 'field' ) type varName ( ',' varName)* ';'
    void parseClassVarDec() {
        printNonTerminal("classVarDec");

        expectPeek(FIELD, STATIC);
        SymbolTable.Kind kind = Kind.STATIC;
        if (currentTokenIs(FIELD))
            kind = Kind.FIELD;

        // 'int' | 'char' | 'boolean' | className
        expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
        String type = currentToken.value();

        expectPeek(IDENTIFIER);
        String name = currentToken.value();

        symbolTable.define(name, type, kind);

        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            expectPeek(IDENTIFIER);

            name = currentToken.value();
            symbolTable.define(name, type, kind);
        }

        expectPeek(SEMICOLON);
        printNonTerminal("/classVarDec");
    }

    void parseSubroutineDec() {

        ifLabelNum = 0;
        whileLabelNum = 0;
        
        symbolTable.startSubroutine();

        printNonTerminal("subroutineDec");

        if (peekTokenIs(CONSTRUCTOR)) {
            expectPeek(CONSTRUCTOR);
        }else if (peekTokenIs(FUNCTION)) {
            expectPeek(FUNCTION);
        } else {
            expectPeek(METHOD);
            symbolTable.define("this", className, Kind.ARG);
        }
        
        TokenType tokenType = currentToken.type;

        // 'int' | 'char' | 'boolean' | className
        expectPeek(VOID, INT, CHAR, BOOLEAN, IDENTIFIER);
        
        expectPeek(IDENTIFIER);

        var functionName = className + "." + currentToken.value();

        expectPeek(LPAREN);
        parseParameterList();
        expectPeek(RPAREN);
        parseSubroutineBody(functionName, tokenType);

        printNonTerminal("/subroutineDec");
    }

    void parseParameterList() {
        printNonTerminal("parameterList");

        SymbolTable.Kind kind = Kind.ARG;

        if (!peekTokenIs(RPAREN)){ // verifica se tem pelo menos uma expressao
            expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
            String type = currentToken.value();

            expectPeek(IDENTIFIER);
            String name = currentToken.value();
            symbolTable.define(name, type, kind);

            while (peekTokenIs(COMMA)) {
                expectPeek(COMMA);
                expectPeek(INT, CHAR, BOOLEAN, IDENTIFIER);
                type = currentToken.value();

                expectPeek(IDENTIFIER);
                name = currentToken.value();

                symbolTable.define(name, type, kind);
            }

        }

        printNonTerminal("/parameterList");
    }

    void parseSubroutineBody(String functionName, TokenType tokenType) {

        printNonTerminal("subroutineBody");
        expectPeek(LBRACE);
        while (peekTokenIs(VAR)) {
            parseVarDec();
        }

        var nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        if (tokenType == CONSTRUCTOR) {
            vmWriter.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        }else if (tokenType == METHOD) {
            vmWriter.writePush(Segment.ARG, 0);   
            vmWriter.writePop(Segment.POINTER, 0);
        }

        parseStatements();
        expectPeek(RBRACE);
        printNonTerminal("/subroutineBody");
    }

    // letStatement -> 'let' identifier( '[' expression ']' )? '=' expression ';'
    void parseLet() {
        printNonTerminal("letStatement");
        
        expectPeek(LET);
        expectPeek(IDENTIFIER);

        var isArray = false;

        String varName = currentToken.value();
        var symbol = symbolTable.resolve(currentToken.value());
        
        if(!symbolTable.resolve(varName, symbol)){
            throw error(peekToken, " - variable not defined");
        }

        while (peekTokenIs(LBRACKET)) {
            expectPeek(LBRACKET);
            parseExpression();

            vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
            vmWriter.writeArithmetic(Command.ADD);
            expectPeek(RBRACKET);
            isArray = true;
        }

        expectPeek(EQ);
        parseExpression();

        if(isArray){
            vmWriter.writePop(Segment.TEMP, 0); 
            vmWriter.writePop(Segment.POINTER, 1);
            vmWriter.writePush(Segment.TEMP, 0);
            vmWriter.writePop(Segment.THAT, 0);
        } else {
            vmWriter.writePop(kind2Segment(symbol.kind()), symbol.index());
        }

        expectPeek(SEMICOLON);
        printNonTerminal("/letStatement");
    }

    // 'while' '(' expression ')' '{' statements '}'
    void parseWhile() {
        printNonTerminal("whileStatement");

        String labelTrue = "WHILE_EXP" + Integer.toString(whileLabelNum);
        String labelFalse = "WHILE_END" + Integer.toString(whileLabelNum);
        whileLabelNum++;

        vmWriter.writeLabel(labelTrue);

        expectPeek(WHILE);

        expectPeek(LPAREN);

        parseExpression();

        vmWriter.writeArithmetic(Command.NOT);;
        vmWriter.writeIf(labelFalse);

        expectPeek(RPAREN);

        expectPeek(LBRACE);

        parseStatements();

        vmWriter.writeGoto(labelTrue);
        vmWriter.writeLabel(labelFalse);

        expectPeek(RBRACE);
        printNonTerminal("/whileStatement");
    }

    void parseIf() {
        printNonTerminal("ifStatement");

        String labelTrue = "IF_TRUE" + Integer.toString(ifLabelNum);
        String labelFalse = "IF_FALSE" + Integer.toString(ifLabelNum);
        String labelEnd = "IF_END" + Integer.toString(ifLabelNum);
        ifLabelNum++;

        expectPeek(IF);
        expectPeek(LPAREN);
        parseExpression();
        expectPeek(RPAREN);

        vmWriter.writeIf(labelTrue);
        vmWriter.writeGoto(labelFalse);
        vmWriter.writeLabel(labelTrue);

        expectPeek(LBRACE);
        parseStatements();
        expectPeek(RBRACE);

        if(peekTokenIs(ELSE)){
            vmWriter.writeGoto(labelEnd);
        }

        vmWriter.writeLabel(labelFalse);
        
        if (peekTokenIs(ELSE)){
            expectPeek(ELSE);
            expectPeek(LBRACE);
            
            parseStatements();

            expectPeek(RBRACE);
            vmWriter.writeLabel(labelEnd);
        }
        printNonTerminal("/ifStatement");
    }

    void parseStatements() {
        printNonTerminal("statements");
        while (peekToken.type == WHILE ||
                peekToken.type == IF ||
                peekToken.type == LET ||
                peekToken.type == DO ||
                peekToken.type == RETURN) {
            parseStatement();
        }

        printNonTerminal("/statements");
    }

    void parseStatement() {
        switch (peekToken.type) {
            case LET:
                parseLet();
                break;
            case WHILE:
                parseWhile();
                break;
            case IF:
                parseIf();
                break;
            case RETURN:
                parseReturn();
                break;
            case DO:
                parseDo();
                break;
            default:
                throw error(peekToken, "Expected a statement");
        }
    }

    // ReturnStatement -> 'return' expression? ';'
    void parseReturn() {
        printNonTerminal("returnStatement");

        expectPeek(RETURN);

        if (!peekTokenIs(SEMICOLON)) {
            parseExpression();
        }else{
            vmWriter.writePush(Segment.CONST, 0);
        }

        vmWriter.writeReturn();

        expectPeek(SEMICOLON);

        printNonTerminal("/returnStatement");
    }

    int parseExpressionList() {
        printNonTerminal("expressionList");

        int numArgs = 0;

        if (!peekTokenIs(RPAREN)){ // verifica se tem pelo menos uma expressao
            parseExpression();
            numArgs++;
        }

        // procurando as demais
        while (peekTokenIs(COMMA)) {
            expectPeek(COMMA);
            parseExpression();
            numArgs++;
        }

        printNonTerminal("/expressionList");
        return numArgs;
    }

    // expression -> term (op term)*
    void parseExpression() {
        printNonTerminal("expression");

        parseTerm();

        while (isOperator(peekToken.type)) { // ??????
            expectPeek(peekToken.type);
            TokenType op = currentToken.type;
            parseTerm();
            parseOperators(op);
        }
        printNonTerminal("/expression");
    }

    void parseOperators(TokenType op){
        if(op == ASTERISK){
            vmWriter.writeCall("Math.multiply", 2);
        } else if (op == SLASH){
            vmWriter.writeCall("Math.divide", 2);
        } else {
            Command c = Command.ADD;
            if(op == PLUS){
                c = Command.ADD;
            } else if (op == MINUS){
                c = Command.SUB;
            } else if (op == LT){
                c = Command.LT;
            } else if (op == GT){
                c = Command.GT;
            } else if (op == EQ){
                c = Command.EQ;
            } else if (op == AND){
                c = Command.AND;
            } else if (op == OR){
                c = Command.OR;
            }
            vmWriter.writeArithmetic(c);
        }
    }

    // term -> number | identifier | stringConstant | keywordConstant
    void parseTerm() {
        printNonTerminal("term");
        String strvalue;
        TokenType tokenType;

        switch (peekToken.type) {
            case INTEGER:
                expectPeek(INTEGER);
                vmWriter.writePush(Segment.CONST, Integer.parseInt(currentToken.value()));
                break;

            case STRING:
                expectPeek(STRING);
                strvalue = currentToken.value();
                vmWriter.writePush(Segment.CONST, strvalue.length());
                vmWriter.writeCall("String.new", 1);

                for(int i = 0; i < strvalue.length(); i++){
                    vmWriter.writePush(Segment.CONST, (int)strvalue.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }

                break;

            case FALSE:
            case NULL:
            case TRUE:
                expectPeek(FALSE, NULL, TRUE);
                vmWriter.writePush(Segment.CONST, 0);
                if(currentToken.type == TRUE){
                    vmWriter.writeArithmetic(Command.NOT);
                }
                break;

            case THIS:
                expectPeek(THIS);
                vmWriter.writePush(Segment.POINTER, 0);
                break;

            case IDENTIFIER:
                expectPeek(IDENTIFIER);

                if (peekTokenIs(LPAREN) || peekTokenIs(DOT)) {

                    parseSubroutineCall();

                } else { // variavel simples ou array
                    Symbol symbol = symbolTable.resolve(currentToken.value());

                    String varName = currentToken.value();
                    if(!symbolTable.resolve(varName, symbol)){
                        throw error(peekToken, " - variable not defined");
                    }

                    if (peekTokenIs(LBRACKET)) { // array
                        expectPeek(LBRACKET);
                        parseExpression();

                        vmWriter.writePush(kind2Segment(symbol.kind()), symbol.index());
                        vmWriter.writeArithmetic(Command.ADD);

                        expectPeek(RBRACKET);
                        vmWriter.writePop(Segment.POINTER, 1);
                        vmWriter.writePush(Segment.THAT, 0);
                    } else {
                        vmWriter.writePush(kind2Segment(symbol.kind()) ,symbol.index());
                    }
                }
                break;

            case LPAREN:
                expectPeek(LPAREN);
                parseExpression();
                expectPeek(RPAREN);
                break;

            case MINUS:
            case NOT:
                expectPeek(MINUS, NOT);
                tokenType = currentToken.type;
                parseTerm();

                if (tokenType == MINUS) {
                    vmWriter.writeArithmetic(Command.NEG);
                } else {
                    vmWriter.writeArithmetic(Command.NEG);
                }
                break;
            default:
                throw error(peekToken, "term expected");
        }
        printNonTerminal("/term");
    }

    // funções auxiliares
    public String XMLOutput() {
        return xmlOutput.toString();
    }

    public String VMOutput() {
        return vmWriter.vmOutput();
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
            throw error(peekToken, "Expected "+type.value);
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

    private Segment kind2Segment (Kind kind) {
        if (kind == Kind.STATIC) return Segment.STATIC;
        if (kind == Kind.FIELD) return Segment.THIS;
        if (kind == Kind.VAR) return Segment.LOCAL;
        if (kind == Kind.ARG) return Segment.ARG;
        return null;
    }
}