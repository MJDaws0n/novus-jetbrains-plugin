package dev.novus.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.project.Project;
import dev.novus.lang.NovusFileType;

public final class NovusPsiFactory {
    private NovusPsiFactory() {}
    public static PsiElement createIdentifier(Project project,String name) {
        var file=PsiFileFactory.getInstance(project).createFileFromText("dummy.nov",NovusFileType.INSTANCE,"fn "+name+"() -> void {}",0,true);
        return PsiTreeUtil.findChildrenOfType(file,PsiElement.class).stream()
            .filter(e->e.getNode()!=null && e.getNode().getElementType()==NovusTypes.IDENTIFIER).findFirst().orElseThrow();
    }
}

