package br.ufma.ecp;

import java.util.Arrays;
import java.util.List;

public class Token {
    
    final TokenType type;
    final String lexeme;

    List<String> symbols = Arrays.asList(
        "+", "-", ".", "*", "/", "&", "|", "<", ">", 
        "=", "~", "{", "}", "(", ")", "[", "]", ",", ";");

    List<String> keywords = Arrays.asList(
        "while", "class", "constructor", "function", "method",
        "field", "static", "var", "int", "char", "boolean", "void", 
        "let", "do", "if", "else", "return");
    
    List<String> keywordsConst = Arrays.asList(
        "true", "false", "null", "this"
    );
    
    public Token(TokenType type, String lexeme){
        this.type = type;
        this.lexeme = lexeme;
    }

    public String toString(){
        String categoria = type.toString();
        if(symbols.contains(lexeme)){
            categoria = "symbol";
        } else if(keywords.contains(lexeme)){
            categoria = "keyword";
        } else if(keywordsConst.contains(lexeme)){
            categoria = "keywordConstant";
        } else if (categoria == "NUMBER"){
            categoria = "integerConstant";
        } else if (categoria == "STRING"){
            categoria = "stringConstant";
        } else if (categoria == "IDENTIFIER"){
            categoria = "identifier";
        }

        return "<" + categoria + "> " + lexeme + " </" + categoria + ">"; 
    }
}
