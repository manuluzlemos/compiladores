package br.ufma.ecp;

public enum TokenType {
    // symbols - operators
    PLUS, // '+'
    MINUS, // '-'
    DOT, // '.'
    ASTERISK, // '*'
    SLASH, // '/' 
    AND, // '&' equivalente a &amp;
    OR, // '|' 
    LT, // '<' equivalente a &lt;
    BT, // '>' equivalente a &gt/
    ASSIGN, // '='
    NOT, // '~'
    
    // symbols - delimiters
    LBRACE, // '{'
    RBRACE, // '}'
    LPAREN, // '('
    RPAREN, // ')'
    LBRACKET, // '['
    RBRACKET, // ']'
    COMMA, // ','
    SEMICOLON, // ';'

    NUMBER,

    IDENTIFIER,

    STRING,

    // keywords
    WHILE,
    CLASS, 
    CONSTRUCTOR, 
    FUNCTION, 
    METHOD, 
    FIELD, 
    STATIC, 
    VAR, 
    INT, 
    CHAR, 
    BOOLEAN, 
    VOID, 
    TRUE, 
    FALSE, 
    NULL, 
    THIS, 
    LET, 
    DO, 
    IF, 
    ELSE, 
    RETURN,

    EOF,

    ILLEGAL
}
