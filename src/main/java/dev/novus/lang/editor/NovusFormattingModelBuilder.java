package dev.novus.lang.editor;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import dev.novus.lang.NovusLanguage;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class NovusFormattingModelBuilder implements FormattingModelBuilder {
    @Override public @NotNull FormattingModel createModel(@NotNull FormattingContext context) {
        CodeStyleSettings settings=context.getCodeStyleSettings();
        SpacingBuilder spacing=new SpacingBuilder(settings,NovusLanguage.INSTANCE)
            .aroundInside(NovusTypes.OPERATORS,NovusTypes.EXPRESSION).spaces(1)
            .after(NovusTypes.COMMA).spaces(1).before(NovusTypes.COMMA).spaces(0)
            .before(NovusTypes.SEMICOLON).spaces(0)
            .after(NovusTypes.COLON).spaces(1).before(NovusTypes.COLON).spaces(0)
            .before(NovusTypes.LPAREN).spaces(0)
            .between(NovusTypes.IF,NovusTypes.LPAREN).spaces(1)
            .between(NovusTypes.WHILE,NovusTypes.LPAREN).spaces(1)
            .between(NovusTypes.FOR,NovusTypes.LPAREN).spaces(1)
            .between(NovusTypes.RPAREN,NovusTypes.LBRACE).spaces(1)
            .between(NovusTypes.ELSE,NovusTypes.LBRACE).spaces(1);
        return FormattingModelProvider.createFormattingModelForPsiFile(context.getContainingFile(),new Block(context.getNode(),null,null,spacing),settings);
    }
    private static final class Block extends AbstractBlock {
        private final SpacingBuilder spacing;
        Block(ASTNode node,Wrap wrap,Alignment alignment,SpacingBuilder spacing){super(node,wrap,alignment);this.spacing=spacing;}
        @Override protected List<com.intellij.formatting.Block> buildChildren(){
            var out=new ArrayList<com.intellij.formatting.Block>(); ASTNode c=myNode.getFirstChildNode();
            while(c!=null){if(c.getElementType()!=TokenType.WHITE_SPACE && c.getTextLength()>0)out.add(new Block(c,null,null,spacing));c=c.getTreeNext();}return out;
        }
        @Override public @Nullable Spacing getSpacing(@Nullable com.intellij.formatting.Block child1,@NotNull com.intellij.formatting.Block child2){return spacing.getSpacing(this,child1,child2);}
        @Override public @NotNull ChildAttributes getChildAttributes(int newChildIndex){return new ChildAttributes(myNode.getElementType()==NovusTypes.BLOCK?Indent.getNormalIndent():Indent.getNoneIndent(),null);}
        @Override public @Nullable Indent getIndent(){
            ASTNode p=myNode.getTreeParent(); if(p!=null && p.getElementType()==NovusTypes.BLOCK && myNode.getElementType()!=NovusTypes.LBRACE && myNode.getElementType()!=NovusTypes.RBRACE)return Indent.getNormalIndent();
            return Indent.getNoneIndent();
        }
        @Override public boolean isLeaf(){return myNode.getFirstChildNode()==null;}
    }
}
