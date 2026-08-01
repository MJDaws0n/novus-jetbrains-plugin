package dev.novus.lang.editor;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import dev.novus.lang.lexer.NovusLexer;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class NovusSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD=key("NOVUS_KEYWORD",DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE=key("NOVUS_TYPE",DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey NUMBER=key("NOVUS_NUMBER",DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING=key("NOVUS_STRING",DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey COMMENT=key("NOVUS_COMMENT",DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT=key("NOVUS_BLOCK_COMMENT",DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey OPERATOR=key("NOVUS_OPERATOR",DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey BRACES=key("NOVUS_BRACES",DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS=key("NOVUS_BRACKETS",DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey PARENS=key("NOVUS_PARENS",DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey COMMA=key("NOVUS_COMMA",DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey SEMICOLON=key("NOVUS_SEMICOLON",DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey BAD=key("NOVUS_BAD",HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey FUNCTION=key("NOVUS_FUNCTION",DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey FUNCTION_CALL=key("NOVUS_FUNCTION_CALL",DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey VARIABLE=key("NOVUS_VARIABLE",DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey PARAMETER=key("NOVUS_PARAMETER",DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey MODULE=key("NOVUS_MODULE",DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey BUILTIN=key("NOVUS_BUILTIN",DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    private static final TextAttributesKey[] EMPTY=new TextAttributesKey[0];
    private static final Map<IElementType,TextAttributesKey> MAP=new HashMap<>();
    static {
        for(var t:NovusTypes.KEYWORDS.getTypes()) MAP.put(t,KEYWORD);
        for(var t:NovusTypes.TYPES.getTypes()) MAP.put(t,TYPE);
        for(var t:NovusTypes.OPERATORS.getTypes()) MAP.put(t,OPERATOR);
        MAP.put(NovusTypes.INTEGER,NUMBER); MAP.put(NovusTypes.FLOAT,NUMBER); MAP.put(NovusTypes.STRING,STRING);
        MAP.put(NovusTypes.LINE_COMMENT,COMMENT); MAP.put(NovusTypes.BLOCK_COMMENT,BLOCK_COMMENT);
        MAP.put(NovusTypes.LBRACE,BRACES); MAP.put(NovusTypes.RBRACE,BRACES); MAP.put(NovusTypes.LBRACKET,BRACKETS); MAP.put(NovusTypes.RBRACKET,BRACKETS);
        MAP.put(NovusTypes.LPAREN,PARENS); MAP.put(NovusTypes.RPAREN,PARENS); MAP.put(NovusTypes.COMMA,COMMA); MAP.put(NovusTypes.SEMICOLON,SEMICOLON);
        MAP.put(NovusTypes.BAD_CHARACTER,BAD);
    }
    private static TextAttributesKey key(String name,TextAttributesKey fallback){return TextAttributesKey.createTextAttributesKey(name,fallback);}
    @Override public @NotNull Lexer getHighlightingLexer(){return new NovusLexer();}
    @Override public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType){var k=MAP.get(tokenType);return k==null?EMPTY:pack(k);}
}

