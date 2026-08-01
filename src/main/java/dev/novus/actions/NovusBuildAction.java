package dev.novus.actions;

import com.intellij.openapi.actionSystem.*;
import dev.novus.lang.psi.NovusFile;
import dev.novus.run.*;
import org.jetbrains.annotations.NotNull;

public final class NovusBuildAction extends AnAction {
    @Override public void actionPerformed(@NotNull AnActionEvent event){
        var project=event.getProject();var file=event.getData(CommonDataKeys.PSI_FILE);if(project==null||!(file instanceof NovusFile)||file.getVirtualFile()==null)return;
        var type=new NovusRunConfigurationType();var factory=new NovusRunConfigurationFactory(type);var config=new NovusRunConfiguration(project,factory,"Build "+file.getName());
        config.sourcePath=file.getVirtualFile().getPath();config.workingDirectory=file.getVirtualFile().getParent().getPath();config.buildOnly=true;
        try{ToolProcess.run(project,NovusCommandLineState.createCommandLine(config),"Build "+file.getName(),true);}catch(Exception e){com.intellij.openapi.ui.Messages.showErrorDialog(project,e.getMessage(),"Novus Build");}
    }
    @Override public void update(@NotNull AnActionEvent e){e.getPresentation().setEnabledAndVisible(e.getProject()!=null&&e.getData(CommonDataKeys.PSI_FILE) instanceof NovusFile);}
    @Override public @NotNull ActionUpdateThread getActionUpdateThread(){return ActionUpdateThread.BGT;}
}

