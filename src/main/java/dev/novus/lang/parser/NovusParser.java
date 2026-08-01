package dev.novus.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;

import static dev.novus.lang.psi.NovusTypes.*;

public final class NovusParser implements PsiParser {
    private PsiBuilder b;

    @Override public @NotNull ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        b=builder; PsiBuilder.Marker file=b.mark();
        skipComments();
        if(at(MODULE)) parseModule();
        while(!b.eof()) {
            skipComments(); if(b.eof()) break;
            if(at(IMPORT)) parseImport();
            else if(at(FN)) parseFunction();
            else if(at(LET)) parseVariable(true);
            else if(at(HASH)) parseComptimeIf();
            else { b.error("Expected module, import, function, global variable, or #if declaration"); recoverTopLevel(); }
        }
        file.done(root); return builder.getTreeBuilt();
    }

    private void parseModule() {
        var m=b.mark(); advance(); expect(IDENTIFIER,"Expected module name"); expect(SEMICOLON,"Expected ';' after module declaration"); m.done(MODULE_DECL);
    }
    private void parseImport() {
        var m=b.mark(); advance();
        while(!b.eof() && !at(SEMICOLON) && !at(FN) && !at(LET) && !at(IMPORT) && !at(HASH)) advance();
        expect(SEMICOLON,"Expected ';' after import declaration"); m.done(IMPORT_DECL);
    }
    private void parseFunction() {
        var m=b.mark(); advance(); expect(IDENTIFIER,"Expected function name");
        if(expect(LPAREN,"Expected '(' after function name")) {
            while(!b.eof() && !at(RPAREN)) {
                skipComments(); if(at(RPAREN)) break;
                parseParameter();
                if(at(COMMA)) advance(); else if(!at(RPAREN)) { b.error("Expected ',' or ')' after parameter"); advance(); }
            }
            expect(RPAREN,"Expected ')' after parameters");
        }
        expect(ARROW,"Expected '->' before return type"); parseType();
        if(at(LBRACE)) parseBlock(); else b.error("Expected function body");
        m.done(FUNCTION_DECL);
    }
    private void parseParameter() {
        var m=b.mark(); expect(IDENTIFIER,"Expected parameter name"); expect(COLON,"Expected ':' after parameter name"); parseType();
        if(at(ASSIGN)) { advance(); parseExpressionUntil(COMMA,RPAREN); }
        m.done(PARAMETER_DECL);
    }
    private void parseType() {
        var m=b.mark();
        while(at(LBRACKET)) {
            advance();
            expect(RBRACKET,"Expected ']' in array type");
        }
        if(NovusTypes.TYPES.contains(token()) || at(IDENTIFIER)) advance(); else b.error("Expected type name");
        m.done(TYPE_REF);
    }
    private void parseVariable(boolean global) {
        var m=b.mark(); advance(); expect(IDENTIFIER,"Expected variable name"); expect(COLON,"Expected ':' after variable name"); parseType();
        expect(ASSIGN,"Expected '=' in variable declaration"); parseExpressionUntil(SEMICOLON); expect(SEMICOLON,"Expected ';' after variable declaration");
        m.done(VARIABLE_DECL);
    }
    private void parseComptimeIf() {
        var m=b.mark(); advance(); expect(IF,"Expected 'if' after '#'");
        if(at(LPAREN)) consumeBalanced(LPAREN,RPAREN); else b.error("Expected compile-time condition");
        if(at(LBRACE)) parseBlock(); else b.error("Expected '{' after #if condition");
        m.done(COMPTIME_IF);
    }
    private void parseBlock() {
        var m=b.mark(); expect(LBRACE,"Expected '{'");
        while(!b.eof() && !at(RBRACE)) {
            skipComments(); if(at(RBRACE)) break;
            if(at(FN)) parseFunction();
            else if(at(IMPORT)) parseImport();
            else if(at(LET)) parseVariable(false);
            else if(at(HASH)) parseComptimeIf();
            else if(at(IF)||at(WHILE)||at(FOR)) parseControl();
            else if(at(LBRACE)) parseBlock();
            else parseStatement();
        }
        expect(RBRACE,"Expected '}'"); m.done(BLOCK);
    }
    private void parseControl() {
        var m=b.mark(); IElementType kind=token(); advance();
        if(kind==FOR && at(LPAREN)) {
            advance();
            if(at(LET)) parseVariable(false); else { parseExpressionUntil(SEMICOLON); expect(SEMICOLON,"Expected ';' after for initializer"); }
            parseExpressionUntil(SEMICOLON); expect(SEMICOLON,"Expected ';' after for condition");
            parseExpressionUntil(RPAREN); expect(RPAREN,"Expected ')' after for clauses");
        } else if(at(LPAREN)) consumeBalanced(LPAREN,RPAREN); else b.error("Expected '(' after '"+kind+"'");
        if(at(LBRACE)) parseBlock(); else b.error("Expected block");
        if(kind==IF && at(ELSE)) { advance(); if(at(IF)) parseControl(); else if(at(LBRACE)) parseBlock(); else b.error("Expected if or block after else"); }
        m.done(STATEMENT);
    }
    private void parseStatement() {
        var m=b.mark();
        if(at(RETURN)||at(BREAK)||at(CONTINUE)) advance();
        while(!b.eof() && !at(SEMICOLON) && !at(RBRACE)) {
            if(at(LPAREN)) consumeBalanced(LPAREN,RPAREN);
            else if(at(LBRACKET)) consumeBalanced(LBRACKET,RBRACKET);
            else advance();
        }
        if(at(SEMICOLON)) advance(); else if(!at(RBRACE)) b.error("Expected ';'");
        m.done(STATEMENT);
    }
    private void parseExpressionUntil(IElementType... stops) {
        var m=b.mark();
        while(!b.eof() && !oneOf(stops)) {
            if(at(LPAREN)) consumeBalanced(LPAREN,RPAREN);
            else if(at(LBRACKET)) consumeBalanced(LBRACKET,RBRACKET);
            else if(at(LBRACE)) consumeBalanced(LBRACE,RBRACE);
            else advance();
        }
        m.done(EXPRESSION);
    }
    private void consumeBalanced(IElementType open,IElementType close) {
        int depth=0;
        while(!b.eof()) {
            if(at(open)) depth++; else if(at(close)) { depth--; advance(); if(depth==0) return; continue; }
            advance();
        }
        b.error("Expected closing '"+close+"'");
    }
    private void recoverTopLevel() {
        do { advance(); } while(!b.eof() && !at(FN) && !at(LET) && !at(IMPORT) && !at(HASH));
    }
    private boolean oneOf(IElementType... types) { for(var t:types) if(at(t)) return true; return false; }
    private boolean at(IElementType t) { return token()==t; }
    private IElementType token() { return b.getTokenType(); }
    private void advance() { b.advanceLexer(); }
    private boolean expect(IElementType t,String message) { skipComments(); if(at(t)){advance();return true;} b.error(message); return false; }
    private void skipComments() { while(NovusTypes.COMMENTS.contains(token())) advance(); }
}
