package dev.novus.lang.psi;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import dev.novus.lang.NovusLanguage;
import org.jetbrains.annotations.NotNull;

public final class NovusReferenceContributor extends PsiReferenceContributor {
    @Override public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement().withLanguage(NovusLanguage.INSTANCE),new PsiReferenceProvider(){
            @Override public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement e,@NotNull ProcessingContext context) {
                if(e.getNode()==null || e.getNode().getElementType()!=NovusTypes.IDENTIFIER)return PsiReference.EMPTY_ARRAY;
                if(e.getParent() instanceof NovusNamedElement n && n.getNameIdentifier()==e)return PsiReference.EMPTY_ARRAY;
                if(e.getParent()!=null && e.getParent().getNode().getElementType()==NovusTypes.IMPORT_DECL)return PsiReference.EMPTY_ARRAY;
                return new PsiReference[]{new NovusReference(e)};
            }
        });
    }
    private static final class NovusReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
        NovusReference(PsiElement e){super(e,true);}
        @Override public ResolveResult @NotNull [] multiResolve(boolean incompleteCode){PsiElement r=NovusSymbolResolver.resolve(getElement());return r==null?ResolveResult.EMPTY_ARRAY:new ResolveResult[]{new PsiElementResolveResult(r)};}
        @Override public PsiElement resolve(){var a=multiResolve(false);return a.length==0?null:a[0].getElement();}
        @Override public Object @NotNull [] getVariants(){return NovusSymbolResolver.visibleDeclarations(getElement()).toArray();}
        @Override public PsiElement handleElementRename(@NotNull String newName){return getElement().replace(NovusPsiFactory.createIdentifier(getElement().getProject(),newName));}
    }
}

