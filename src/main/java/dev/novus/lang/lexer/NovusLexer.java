package dev.novus.lang.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NovusLexer extends LexerBase {
    private CharSequence buffer = "";
    private int end;
    private int start;
    private int position;
    private IElementType tokenType;

    @Override public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer=buffer; this.end=endOffset; this.start=startOffset; this.position=startOffset; locateToken();
    }
    @Override public int getState() { return 0; }
    @Override public @Nullable IElementType getTokenType() { return tokenType; }
    @Override public int getTokenStart() { return start; }
    @Override public int getTokenEnd() { return position; }
    @Override public void advance() { start=position; locateToken(); }
    @Override public @NotNull CharSequence getBufferSequence() { return buffer; }
    @Override public int getBufferEnd() { return end; }

    private void locateToken() {
        if (start >= end) { tokenType=null; position=end; return; }
        position=start;
        char c=at(position);
        if (Character.isWhitespace(c)) {
            while (position<end && Character.isWhitespace(at(position))) position++;
            tokenType=TokenType.WHITE_SPACE; return;
        }
        if (c=='/' && peek(1)=='/') {
            position+=2; while(position<end && at(position)!='\n' && at(position)!='\r') position++;
            tokenType=NovusTypes.LINE_COMMENT; return;
        }
        if (c=='/' && peek(1)=='*') {
            position+=2;
            while(position<end && !(at(position)=='*' && peekAt(position,1)=='/')) position++;
            if(position<end) position=Math.min(end, position+2);
            tokenType=NovusTypes.BLOCK_COMMENT; return;
        }
        if (c=='"' || c=='\'') {
            char quote=c; position++;
            while(position<end) {
                char x=at(position++);
                if(x=='\\' && position<end) { position++; continue; }
                if(x==quote || x=='\n' || x=='\r') break;
            }
            tokenType=NovusTypes.STRING; return;
        }
        if (Character.isDigit(c)) { lexNumber(); return; }
        if (isIdentStart(c)) {
            position++; while(position<end && isIdentPart(at(position))) position++;
            tokenType=NovusTypes.KEYWORD_MAP.getOrDefault(buffer.subSequence(start,position).toString(),NovusTypes.IDENTIFIER); return;
        }
        String two=position+1<end ? buffer.subSequence(position,position+2).toString() : "";
        IElementType multi=switch(two) {
            case "->"->NovusTypes.ARROW; case "=="->NovusTypes.EQ; case "!="->NovusTypes.NEQ;
            case "<="->NovusTypes.LTE; case ">="->NovusTypes.GTE; case "&&"->NovusTypes.AND; case "||"->NovusTypes.OR;
            case "<<"->NovusTypes.SHL; case ">>"->NovusTypes.SHR; case "+="->NovusTypes.PLUS_ASSIGN; case "-="->NovusTypes.MINUS_ASSIGN;
            case "*="->NovusTypes.STAR_ASSIGN; case "/="->NovusTypes.SLASH_ASSIGN; case "%="->NovusTypes.PERCENT_ASSIGN;
            case "++"->NovusTypes.INC; case "--"->NovusTypes.DEC; default->null;
        };
        if(multi!=null) { position+=2; tokenType=multi; return; }
        tokenType=switch(c) {
            case '('->NovusTypes.LPAREN; case ')'->NovusTypes.RPAREN; case '{'->NovusTypes.LBRACE; case '}'->NovusTypes.RBRACE;
            case '['->NovusTypes.LBRACKET; case ']'->NovusTypes.RBRACKET; case ';'->NovusTypes.SEMICOLON; case ':'->NovusTypes.COLON;
            case ','->NovusTypes.COMMA; case '.'->NovusTypes.DOT; case '#'->NovusTypes.HASH; case '='->NovusTypes.ASSIGN;
            case '+'->NovusTypes.PLUS; case '-'->NovusTypes.MINUS; case '*'->NovusTypes.STAR; case '/'->NovusTypes.SLASH;
            case '%'->NovusTypes.PERCENT; case '&'->NovusTypes.AMPERSAND; case '!'->NovusTypes.BANG; case '|'->NovusTypes.PIPE;
            case '^'->NovusTypes.CARET; case '~'->NovusTypes.TILDE; case '<'->NovusTypes.LT; case '>'->NovusTypes.GT;
            default->NovusTypes.BAD_CHARACTER;
        };
        position++;
    }

    private void lexNumber() {
        if(at(position)=='0' && (peekAt(position,1)=='x'||peekAt(position,1)=='X')) {
            position+=2; while(position<end && Character.digit(at(position),16)>=0) position++;
            tokenType=NovusTypes.INTEGER; return;
        }
        if(at(position)=='0' && (peekAt(position,1)=='b'||peekAt(position,1)=='B')) {
            position+=2; while(position<end && (at(position)=='0'||at(position)=='1')) position++;
            tokenType=NovusTypes.INTEGER; return;
        }
        while(position<end && Character.isDigit(at(position))) position++;
        boolean floating=false;
        if(position<end && at(position)=='.' && position+1<end && Character.isDigit(at(position+1))) {
            floating=true; position++; while(position<end && Character.isDigit(at(position))) position++;
        }
        if(position<end && (at(position)=='e'||at(position)=='E')) {
            int p=position+1; if(p<end && (at(p)=='+'||at(p)=='-')) p++;
            if(p<end && Character.isDigit(at(p))) { floating=true; position=p+1; while(position<end && Character.isDigit(at(position))) position++; }
        }
        tokenType=floating?NovusTypes.FLOAT:NovusTypes.INTEGER;
    }
    private char at(int i) { return buffer.charAt(i); }
    private char peek(int delta) { return peekAt(position,delta); }
    private char peekAt(int base,int delta) { int p=base+delta; return p<end?at(p):'\0'; }
    private static boolean isIdentStart(char c) { return c=='_' || Character.isLetter(c); }
    private static boolean isIdentPart(char c) { return c=='_' || Character.isLetterOrDigit(c); }
}

