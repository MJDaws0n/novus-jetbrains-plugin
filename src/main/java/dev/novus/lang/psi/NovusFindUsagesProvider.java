package dev.novus.lang.psi;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import dev.novus.lang.lexer.NovusLexer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NovusFindUsagesProvider implements FindUsagesProvider {
    @Override public @Nullable WordsScanner getWordsScanner(){return new DefaultWordsScanner(new NovusLexer(),com.intellij.psi.tree.TokenSet.create(NovusTypes.IDENTIFIER),NovusTypes.COMMENTS,NovusTypes.STRINGS);}
    @Override public boolean canFindUsagesFor(@NotNull PsiElement e){return e instanceof NovusNamedElement;}
    @Override public @Nullable String getHelpId(@NotNull PsiElement e){return null;}
    @Override public @NotNull String getType(@NotNull PsiElement e){return e instanceof NovusNamedElement n?n.getKindName():"symbol";}
    @Override public @NotNull String getDescriptiveName(@NotNull PsiElement e){return e instanceof NovusNamedElement n&&n.getName()!=null?n.getName():e.getText();}
    @Override public @NotNull String getNodeText(@NotNull PsiElement e,boolean useFullName){return getDescriptiveName(e);}
}
