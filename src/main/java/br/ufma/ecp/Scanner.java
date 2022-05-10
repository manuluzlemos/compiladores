package br.ufma.ecp;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Scanner {

    private byte[] input;
    private int current;
    private int start;

    private static final Map<String, TokenType> keywords;

    static{
        keywords = new HashMap<>();
        keywords.put("while", TokenType.WHILE);
        keywords.put("class", TokenType.CLASS);
        keywords.put("constructor", TokenType.CONSTRUCTOR);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("method", TokenType.METHOD);
        keywords.put("field", TokenType.FIELD);
        keywords.put("static", TokenType.STATIC);
        keywords.put("var", TokenType.VAR);
        keywords.put("int", TokenType.INT);
        keywords.put("char", TokenType.CHAR);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("void", TokenType.VOID);
        keywords.put("true", TokenType.TRUE);      
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("this", TokenType.THIS);
        keywords.put("let", TokenType.LET);
        keywords.put("do", TokenType.DO);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
    }
    
    public Scanner (byte[] input){
        this.input = input;
        current = 0;
        start = 0;
    }

    public Token nextToken(){
        skipWhitespace();//garante que, ao iniciar, não teremos espaço em branco

        start = current;
        char ch = peek();

        if(Character.isDigit(ch)){
            return number();
        }

        if(Character.isLetter(ch) || ch == '_'){
            return identifier();
        }

        if(ch == '\"'){
            return isString();
        }

        switch(ch){
            case '+':
                advance();
                return new Token(TokenType.PLUS, "+");
            case '-':
                advance();
                return new Token(TokenType.MINUS, "-");
            case '.':
                advance();
                return new Token(TokenType.DOT, ".");
            case '*':
                advance();
                return new Token(TokenType.ASTERISK, "*");
            case '/':
                advance();
                return new Token(TokenType.SLASH, "/");
            case '&':
                advance();
                return new Token(TokenType.AND, "&amp;");
            case '|':
                advance();
                return new Token(TokenType.OR, "|");
            
            case '<':
                advance();
                return new Token(TokenType.LT, "&lt;");
            case '>':
                advance();
                return new Token(TokenType.LT, "&gt;");

            case '=':
                advance();
                return new Token(TokenType.ASSIGN, "=");
            case '~':
                advance();
                return new Token(TokenType.ASSIGN, "~");
            
            case '{':
                advance();
                return new Token(TokenType.LBRACE, "{");
            case '}':
                advance();
                return new Token(TokenType.RBRACE, "}");
            
            case '(':
                advance();
                return new Token(TokenType.LPAREN, "(");
            case ')':
                advance();
                return new Token(TokenType.RPAREN, ")");

            case '[':
                advance();
                return new Token(TokenType.LBRACKET, "[");
            case ']':
                advance();
                return new Token(TokenType.RBRACKET, "]");

            case ',':
                advance();
                return new Token(TokenType.COMMA, ",");
            case ';':
                advance();
                return new Token(TokenType.SEMICOLON, ";");
            case 0:
                return new Token(TokenType.EOF, "EOF");
            default:
                advance();
                return new Token(TokenType.ILLEGAL, Character.toString(ch));
        }
    }

    private void skipWhitespace(){
        char ch = peek();
        while(ch == ' ' || ch == '\r' || ch == '\t' || ch == '\n' || isComment()){
            advance();
            ch = peek();
        }
    }

    private boolean isComment(){
        if(peek() == '/'){
            if ((current + 1) < input.length) {

                if ((char)input[current+1] == '/') { // Inicia com "//" e finaliza com "\n"
                    advance();
                    advance();
                    while(peek() != '\n'){
                        advance();
                    }
                    return true;
                    
                } else if((char)input[current+1] == '*') { // Inicia com "/*" e finaliza com "*/"
                    advance();
                    boolean end = false;
                    while(!end){
                        advance();
                        if(peek() == '*'){
                            if ((current+1) < input.length) {
                                if ((char)input[current+1] == '/') {
                                    advance();
                                    end = true;
                                }
                            }
                        }
                    }
                    advance();
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private Token isString(){
        int cont = 0;
        while(cont != 2){
            if(peek() == '\"'){
                cont++;
            }
            advance();
        }
        String id = new String(input, start+1, current-start-2, StandardCharsets.UTF_8);
        TokenType type = keywords.get(id);
        if (type == null) type = TokenType.STRING;
        Token token = new Token(type, id);
        return token;
    }

    private boolean isAlphaNumeric(char ch){
        return Character.isLetter(ch) || Character.isDigit(ch);
    }

    private Token identifier(){
        while(isAlphaNumeric(peek()) || peek() == '_'){
            advance();
        }
        String id = new String(input, start, current-start, StandardCharsets.UTF_8);
        TokenType type = keywords.get(id);
        if (type == null) type = TokenType.IDENTIFIER;
        Token token = new Token(type, id);
        return token;
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