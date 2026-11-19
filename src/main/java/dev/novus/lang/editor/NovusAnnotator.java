package dev.novus.lang.editor;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusSymbolResolver;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class NovusAnnotator implements Annotator {
    private static final Set<String> BUILTINS=NovusSymbolResolver.BUILTINS.keySet();
    @Override public void annotate(@NotNull PsiElement element,@NotNull AnnotationHolder holder) {
        if(element.getNode()==null) return;
        var type=element.getNode().getElementType();
        if(type==NovusTypes.STRING) { validateEscapes(element,holder); return; }
        if(type!=NovusTypes.IDENTIFIER) return;
        PsiElement parent=element.getParent();
        if(parent instanceof NovusNamedElement named && named.getNameIdentifier()==element) {
            var key=named.getDeclarationType()==NovusTypes.CLASS_DECL?NovusSyntaxHighlighter.CLASS:
                named.getDeclarationType()==NovusTypes.FIELD_DECL?NovusSyntaxHighlighter.FIELD:
                named.getDeclarationType()==NovusTypes.METHOD_DECL || named.getDeclarationType()==NovusTypes.CONSTRUCTOR_DECL?NovusSyntaxHighlighter.METHOD:
                named.getDeclarationType()==NovusTypes.FUNCTION_DECL?NovusSyntaxHighlighter.FUNCTION:
                named.getDeclarationType()==NovusTypes.PARAMETER_DECL?NovusSyntaxHighlighter.PARAMETER:
                named.getDeclarationType()==NovusTypes.MODULE_DECL?NovusSyntaxHighlighter.MODULE:NovusSyntaxHighlighter.VARIABLE;
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(key).create(); return;
        }
        if(parent!=null && parent.getNode().getElementType()==NovusTypes.IMPORT_DECL) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(NovusSyntaxHighlighter.MODULE).create(); return;
        }
        if("self".equals(element.getText()) && NovusSymbolResolver.containingClass(element)!=null) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(NovusSyntaxHighlighter.KEYWORD).create(); return;
        }
        if(parent!=null && parent.getNode().getElementType()==NovusTypes.TYPE_REF) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(NovusSyntaxHighlighter.CLASS).create(); return;
        }
        PsiElement resolved=NovusSymbolResolver.resolve(element);
        if(resolved instanceof NovusNamedElement named && named.getDeclarationType()==NovusTypes.CLASS_DECL) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(NovusSyntaxHighlighter.CLASS).create(); return;
        }
        if(resolved instanceof NovusNamedElement named && named.getDeclarationType()==NovusTypes.FIELD_DECL) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(NovusSyntaxHighlighter.FIELD).create(); return;
        }
        PsiElement next=significant(element,true);
        if(next!=null && next.getNode().getElementType()==NovusTypes.LPAREN) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(BUILTINS.contains(element.getText())?NovusSyntaxHighlighter.BUILTIN:NovusSyntaxHighlighter.FUNCTION_CALL).create();
        }
    }
    private static PsiElement significant(PsiElement e,boolean forward) {
        PsiElement n=forward?com.intellij.psi.util.PsiTreeUtil.nextLeaf(e):com.intellij.psi.util.PsiTreeUtil.prevLeaf(e);
        while(n!=null && (n.getNode().getElementType()==TokenType.WHITE_SPACE || NovusTypes.COMMENTS.contains(n.getNode().getElementType())))
            n=forward?com.intellij.psi.util.PsiTreeUtil.nextLeaf(n):com.intellij.psi.util.PsiTreeUtil.prevLeaf(n);
        return n;
    }
    private static void validateEscapes(PsiElement e,AnnotationHolder h) {
        String s=e.getText();
        for(int i=1;i<s.length()-1;i++) if(s.charAt(i)=='\\') {
            if(i+1>=s.length()-1){h.newAnnotation(HighlightSeverity.ERROR,"Unterminated escape sequence").range(e).create();return;}
            char c=s.charAt(++i);
            if("\\\"'nrt0".indexOf(c)<0 && c!='x' && c!='u') {
                h.newAnnotation(HighlightSeverity.ERROR,"Invalid Novus escape sequence \\"+c).range(e).create(); return;
            }
        }
    }
}

