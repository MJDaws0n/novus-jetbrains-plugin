package dev.novus.lang.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class NovusNamedElement extends ASTWrapperPsiElement implements PsiNameIdentifierOwner {
    public NovusNamedElement(@NotNull ASTNode node) { super(node); }

    @Override public @Nullable PsiElement getNameIdentifier() {
        ASTNode child=getNode().getFirstChildNode();
        while(child!=null) {
            if(child.getElementType()==NovusTypes.IDENTIFIER) return child.getPsi();
            child=child.getTreeNext();
        }
        return null;
    }
    @Override public @Nullable String getName() { PsiElement id=getNameIdentifier(); return id==null?null:id.getText(); }
    @Override public PsiElement setName(@NotNull String name) {
        PsiElement id=getNameIdentifier();
        if(id!=null) id.replace(NovusPsiFactory.createIdentifier(getProject(),name));
        return this;
    }
    public IElementType getDeclarationType() { return getNode().getElementType(); }
    public boolean isCallable() {
        var t=getDeclarationType();
        return t==NovusTypes.FUNCTION_DECL || t==NovusTypes.METHOD_DECL || t==NovusTypes.CONSTRUCTOR_DECL;
    }
    public boolean isStatic() { return getNode().findChildByType(NovusTypes.STATIC)!=null; }
    public boolean isPublic() { return getNode().findChildByType(NovusTypes.PUB)!=null; }
    public @Nullable String getTypeName() {
        ASTNode type=getNode().findChildByType(NovusTypes.TYPE_REF);
        return type==null?null:type.getText().replaceAll("\\s+","");
    }
    public String getKindName() {
        var t=getDeclarationType();
        if(t==NovusTypes.FUNCTION_DECL) return "function";
        if(t==NovusTypes.PARAMETER_DECL) return "parameter";
        if(t==NovusTypes.VARIABLE_DECL) return "variable";
        if(t==NovusTypes.MODULE_DECL) return "module";
        if(t==NovusTypes.CLASS_DECL) return "class";
        if(t==NovusTypes.FIELD_DECL) return "field";
        if(t==NovusTypes.METHOD_DECL) return "method";
        if(t==NovusTypes.CONSTRUCTOR_DECL) return "constructor";
        return "symbol";
    }
    @Override public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override public @Nullable String getPresentableText() { return getName(); }
            @Override public @Nullable String getLocationString() { return getContainingFile()==null?null:getContainingFile().getName(); }
            @Override public @Nullable Icon getIcon(boolean unused) { return NovusIcons.FILE; }
        };
    }
}

