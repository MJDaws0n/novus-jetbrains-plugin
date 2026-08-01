package dev.novus.lang.lexer;

import com.intellij.psi.tree.IElementType;
import dev.novus.lang.psi.NovusTypes;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public final class NovusLexerTest {
    @Test public void lexesCompleteCompilerSurface(){
        String code="module app; #if(os == \"linux\") { import lib/my-lib[print] std; } fn main(x: []u64 = [0xFF, 0b10]) -> i32 { for (let i: i32 = 0; i < 3; i++) { x[0] <<= 1; } return 0; }";
        var lexer=new NovusLexer();lexer.start(code);var types=new ArrayList<IElementType>();while(lexer.getTokenType()!=null){types.add(lexer.getTokenType());lexer.advance();}
        assertTrue(types.containsAll(java.util.List.of(NovusTypes.MODULE,NovusTypes.HASH,NovusTypes.IF,NovusTypes.IMPORT,NovusTypes.FN,NovusTypes.LBRACKET,NovusTypes.U64,NovusTypes.INTEGER,NovusTypes.FOR,NovusTypes.INC,NovusTypes.SHL)));
        assertFalse(types.contains(NovusTypes.BAD_CHARACTER));
    }
    @Test public void handlesCommentsFloatsAndEscapedStrings(){
        String code="// hi\n/* block */ let n: f64 = 1.25e-3; let s: str = \"a\\n\";";var l=new NovusLexer();l.start(code);var types=new ArrayList<IElementType>();while(l.getTokenType()!=null){types.add(l.getTokenType());l.advance();}
        assertTrue(types.contains(NovusTypes.LINE_COMMENT));assertTrue(types.contains(NovusTypes.BLOCK_COMMENT));assertTrue(types.contains(NovusTypes.FLOAT));assertTrue(types.contains(NovusTypes.STRING));
    }
}
