package dev.novus.run;

import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NovusRunLineMarkerContributor extends RunLineMarkerContributor {
    @Override public @Nullable Info getInfo(@NotNull PsiElement element){
        if(element.getNode()==null||element.getNode().getElementType()!=NovusTypes.IDENTIFIER||!"main".equals(element.getText()))return null;
        NovusNamedElement fn=PsiTreeUtil.getParentOfType(element,NovusNamedElement.class,false);if(fn==null||fn.getDeclarationType()!=NovusTypes.FUNCTION_DECL||fn.getNameIdentifier()!=element)return null;
        return new Info(AllIcons.RunConfigurations.TestState.Run,ExecutorAction.getActions(1),e->"Run Novus program");
    }
}

