package dev.novus.lang.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NovusBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS={new BracePair(NovusTypes.LPAREN,NovusTypes.RPAREN,false),new BracePair(NovusTypes.LBRACKET,NovusTypes.RBRACKET,false),new BracePair(NovusTypes.LBRACE,NovusTypes.RBRACE,true)};
    @Override public BracePair @NotNull [] getPairs(){return PAIRS;}
    @Override public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType l,@Nullable IElementType context){return true;}
    @Override public int getCodeConstructStart(PsiFile file,int openingBraceOffset){return openingBraceOffset;}
}

