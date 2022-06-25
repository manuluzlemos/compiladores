package br.ufma.ecp;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class GeneratorCodeTest {


    @Test
    public void termExpressionVar () {
        var input = """
            class Main {
            
              function void main () {
                  var int x, y;
                  let x = 42;
                  let y = x;
              }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 2
            push constant 42
            pop local 0
            push local 0
            pop local 1
                """;
        assertEquals(expected, actual);
    }

    @Test
    public void termExpressionLiteral () {
        var input = """
            class Main {
            
              function void main () {
                  var int x;
                  let y = 42;
              }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Main.main 1
            push constant 42
            pop local 0
                """;
        assertEquals(expected, actual);
    }

    @Test
    public void termExpression () {
        var input = """
            class Point {
              field int x, y;
              constructor Point new(int Ax, int Ay) { 
                var int w;             
                let x = Ax;
                let y = Ay;
                let w = 42;
                let x = w;
                return this;
             }
            }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();
        String actual = parser.VMOutput();
        String expected = """
            function Point.new 1
            push argument 0
            pop this 0
            push argument 1
            pop this 1
            push constant 42
            pop local 0
            push local 0
            pop this 0
                """;
        assertEquals(expected, actual);
    }


    @Test
    public void writeFunctionTest() {

        var input = """
            class Main {
                function int soma (int x, int y) {
                       return 2 * x;
                }
               
                function void main () {
                       var int d;
                       let d = Main.soma(4,5);
                       return;
                 }
               
               }
            """;;
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parse();


        String actual = parser.VMOutput();
        String expected = """
            function Main.soma 0
            function Main.main 1
                """;
        assertEquals(expected, actual);
 
 
    }
    
}
