package dev.novus.lang.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import java.util.Map;

public final class NovusTypes {
    private NovusTypes() {}
    private static IElementType t(String name) { return new NovusTokenType(name); }
    private static IElementType e(String name) { return new NovusElementType(name); }

    public static final IElementType IDENTIFIER=t("IDENTIFIER"), INTEGER=t("INTEGER"), FLOAT=t("FLOAT"), STRING=t("STRING"),
        LINE_COMMENT=t("LINE_COMMENT"), BLOCK_COMMENT=t("BLOCK_COMMENT"), BAD_CHARACTER= TokenType.BAD_CHARACTER;
    public static final IElementType MODULE=t("module"), IMPORT=t("import"), FN=t("fn"), LET=t("let"), RETURN=t("return"),
        TRUE=t("true"), FALSE=t("false"), IF=t("if"), ELSE=t("else"), FOR=t("for"), WHILE=t("while"),
        BREAK=t("break"), CONTINUE=t("continue"), CLASS=t("class"), NEW=t("new"), PUB=t("pub"), PRIV=t("priv"),
        STATIC=t("static"), NULL=t("null");
    public static final IElementType VOID=t("void"), BOOL=t("bool"), STR=t("str"), U8=t("u8"), U16=t("u16"),
        U32=t("u32"), U64=t("u64"), I8=t("i8"), I16=t("i16"), I32=t("i32"), I64=t("i64"), F32=t("f32"), F64=t("f64");
    public static final IElementType LPAREN=t("("), RPAREN=t(")"), LBRACE=t("{"), RBRACE=t("}"),
        LBRACKET=t("["), RBRACKET=t("]"), SEMICOLON=t(";"), COLON=t(":"), COMMA=t(","), DOT=t("."), HASH=t("#");
    public static final IElementType ARROW=t("->"), ASSIGN=t("="), PLUS=t("+"), MINUS=t("-"), STAR=t("*"), SLASH=t("/"),
        PERCENT=t("%"), AMPERSAND=t("&"), BANG=t("!"), PIPE=t("|"), CARET=t("^"), TILDE=t("~"), SHL=t("<<"), SHR=t(">>"),
        EQ=t("=="), NEQ=t("!="), LT=t("<"), GT=t(">"), LTE=t("<="), GTE=t(">="), AND=t("&&"), OR=t("||"),
        PLUS_ASSIGN=t("+="), MINUS_ASSIGN=t("-="), STAR_ASSIGN=t("*="), SLASH_ASSIGN=t("/="), PERCENT_ASSIGN=t("%="), INC=t("++"), DEC=t("--");

    public static final IElementType FILE=e("FILE"), MODULE_DECL=e("MODULE_DECL"), IMPORT_DECL=e("IMPORT_DECL"),
        FUNCTION_DECL=e("FUNCTION_DECL"), PARAMETER_DECL=e("PARAMETER_DECL"), VARIABLE_DECL=e("VARIABLE_DECL"),
        CLASS_DECL=e("CLASS_DECL"), FIELD_DECL=e("FIELD_DECL"), METHOD_DECL=e("METHOD_DECL"), CONSTRUCTOR_DECL=e("CONSTRUCTOR_DECL"),
        COMPTIME_IF=e("COMPTIME_IF"), BLOCK=e("BLOCK"), STATEMENT=e("STATEMENT"), EXPRESSION=e("EXPRESSION"), CALL_EXPR=e("CALL_EXPR"), TYPE_REF=e("TYPE_REF");

    public static final TokenSet COMMENTS=TokenSet.create(LINE_COMMENT,BLOCK_COMMENT);
    public static final TokenSet STRINGS=TokenSet.create(STRING);
    public static final TokenSet TYPES=TokenSet.create(VOID,BOOL,STR,U8,U16,U32,U64,I8,I16,I32,I64,F32,F64);
    public static final TokenSet KEYWORDS=TokenSet.create(MODULE,IMPORT,FN,LET,RETURN,TRUE,FALSE,IF,ELSE,FOR,WHILE,BREAK,CONTINUE,CLASS,NEW,PUB,PRIV,STATIC,NULL);
    public static final TokenSet OPERATORS=TokenSet.create(ARROW,ASSIGN,PLUS,MINUS,STAR,SLASH,PERCENT,AMPERSAND,BANG,PIPE,CARET,TILDE,
        SHL,SHR,EQ,NEQ,LT,GT,LTE,GTE,AND,OR,PLUS_ASSIGN,MINUS_ASSIGN,STAR_ASSIGN,SLASH_ASSIGN,PERCENT_ASSIGN,INC,DEC);
    public static final TokenSet NAMED_DECLARATIONS=TokenSet.create(FUNCTION_DECL,PARAMETER_DECL,VARIABLE_DECL,MODULE_DECL,CLASS_DECL,FIELD_DECL,METHOD_DECL,CONSTRUCTOR_DECL);

    public static final Map<String,IElementType> KEYWORD_MAP=Map.ofEntries(
        Map.entry("module",MODULE),Map.entry("import",IMPORT),Map.entry("fn",FN),Map.entry("let",LET),Map.entry("return",RETURN),
        Map.entry("true",TRUE),Map.entry("false",FALSE),Map.entry("if",IF),Map.entry("else",ELSE),Map.entry("for",FOR),
        Map.entry("while",WHILE),Map.entry("break",BREAK),Map.entry("continue",CONTINUE),Map.entry("void",VOID),Map.entry("bool",BOOL),
        Map.entry("str",STR),Map.entry("u8",U8),Map.entry("u16",U16),Map.entry("u32",U32),Map.entry("u64",U64),
        Map.entry("i8",I8),Map.entry("i16",I16),Map.entry("i32",I32),Map.entry("i64",I64),Map.entry("f32",F32),Map.entry("f64",F64),
        Map.entry("class",CLASS),Map.entry("new",NEW),Map.entry("pub",PUB),Map.entry("priv",PRIV),Map.entry("static",STATIC),Map.entry("null",NULL));
}

