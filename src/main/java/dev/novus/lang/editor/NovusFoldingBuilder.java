package dev.novus.lang.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class NovusFoldingBuilder extends FoldingBuilderEx {
    @Override public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root,@NotNull Document document,boolean quick) {
        var out=new ArrayList<FoldingDescriptor>();
        for(PsiElement e:PsiTreeUtil.collectElements(root,x->x.getNode()!=null && (x.getNode().getElementType()==NovusTypes.BLOCK || x.getNode().getElementType()==NovusTypes.IMPORT_DECL || x.getNode().getElementType()==NovusTypes.BLOCK_COMMENT))) {
            TextRange r=e.getTextRange(); if(r.getLength()>2 && document.getLineNumber(r.getStartOffset())<document.getLineNumber(r.getEndOffset())) out.add(new FoldingDescriptor(e.getNode(),r));
        }
        return out.toArray(FoldingDescriptor.EMPTY_ARRAY);
    }
    @Override public @Nullable String getPlaceholderText(@NotNull ASTNode node){return node.getElementType()==NovusTypes.IMPORT_DECL?"import …;":node.getElementType()==NovusTypes.BLOCK_COMMENT?"/*…*/":"{…}";}
    @Override public boolean isCollapsedByDefault(@NotNull ASTNode node){return false;}
}

