package dev.novus.lang.editor;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusSymbolResolver;
import org.jetbrains.annotations.Nullable;

public final class NovusDocumentationProvider extends AbstractDocumentationProvider {
    @Override public @Nullable String generateDoc(PsiElement element,@Nullable PsiElement originalElement) {
        if(element instanceof NovusNamedElement n) {
            String sig=n.getText().replaceAll("\\{[\\s\\S]*$","").strip();
            return "<div class='definition'><pre>"+escape(sig)+"</pre></div><div class='content'>Novus "+n.getKindName()+" declared in <code>"+escape(n.getContainingFile().getName())+"</code>.</div>";
        }
        String name=originalElement==null?element.getText():originalElement.getText(); String sig=NovusSymbolResolver.BUILTINS.get(name);
        if(sig!=null)return "<div class='definition'><pre>"+escape(sig)+"</pre></div><div class='content'>Compiler-provided Novus intrinsic.</div>";
        return null;
    }
    @Override public @Nullable String getQuickNavigateInfo(PsiElement element,PsiElement originalElement) {
        if(element instanceof NovusNamedElement n)return n.getKindName()+" "+n.getName()+" — "+n.getContainingFile().getName();
        return null;
    }
    private static String escape(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}

