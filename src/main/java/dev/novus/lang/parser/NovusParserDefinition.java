package dev.novus.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import dev.novus.lang.NovusLanguage;
import dev.novus.lang.lexer.NovusLexer;
import dev.novus.lang.psi.NovusFile;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;

public final class NovusParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE = new IFileElementType(NovusLanguage.INSTANCE);
    @Override public @NotNull Lexer createLexer(Project project) { return new NovusLexer(); }
    @Override public @NotNull PsiParser createParser(Project project) { return new NovusParser(); }
    @Override public @NotNull IFileElementType getFileNodeType() { return FILE; }
    @Override public @NotNull TokenSet getWhitespaceTokens() { return TokenSet.create(TokenType.WHITE_SPACE); }
    @Override public @NotNull TokenSet getCommentTokens() { return NovusTypes.COMMENTS; }
    @Override public @NotNull TokenSet getStringLiteralElements() { return NovusTypes.STRINGS; }
    @Override public @NotNull PsiElement createElement(ASTNode node) {
        return NovusTypes.NAMED_DECLARATIONS.contains(node.getElementType()) ? new NovusNamedElement(node) : new com.intellij.extapi.psi.ASTWrapperPsiElement(node);
    }
    @Override public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) { return new NovusFile(viewProvider); }
}

