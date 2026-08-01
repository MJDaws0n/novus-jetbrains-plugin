package dev.novus.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import dev.novus.lang.psi.NovusFile;
import org.jetbrains.annotations.NotNull;

public final class NovusRunConfigurationProducer extends LazyRunConfigurationProducer<NovusRunConfiguration> {
    @Override public @NotNull ConfigurationFactory getConfigurationFactory(){return ConfigurationTypeUtil.findConfigurationType(NovusRunConfigurationType.class).getConfigurationFactories()[0];}
    @Override protected boolean setupConfigurationFromContext(@NotNull NovusRunConfiguration c,@NotNull ConfigurationContext context,@NotNull Ref<PsiElement> sourceElement){
        PsiElement e=context.getPsiLocation();if(e==null||!(e.getContainingFile() instanceof NovusFile)||e.getContainingFile().getVirtualFile()==null)return false;
        c.sourcePath=e.getContainingFile().getVirtualFile().getPath();c.workingDirectory=e.getContainingFile().getVirtualFile().getParent().getPath();c.setName("Run "+e.getContainingFile().getName());sourceElement.set(e.getContainingFile());return true;
    }
    @Override public boolean isConfigurationFromContext(@NotNull NovusRunConfiguration c,@NotNull ConfigurationContext context){PsiElement e=context.getPsiLocation();return e!=null&&e.getContainingFile()!=null&&e.getContainingFile().getVirtualFile()!=null&&c.sourcePath.equals(e.getContainingFile().getVirtualFile().getPath());}
}
