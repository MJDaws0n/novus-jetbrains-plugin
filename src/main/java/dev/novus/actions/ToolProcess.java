package dev.novus.actions;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.project.ProjectUtil;
import org.jetbrains.annotations.NotNull;

final class ToolProcess {
    private ToolProcess(){}
    static void run(Project project,GeneralCommandLine command,String title,boolean refresh){
        try {
            var handler=new OSProcessHandler(command);ProcessTerminatedListener.attach(handler);
            if(refresh)handler.addProcessListener(new ProcessListener(){@Override public void processTerminated(@NotNull ProcessEvent event){var dir=ProjectUtil.guessProjectDir(project);if(dir!=null)VfsUtil.markDirtyAndRefresh(true,true,true,dir);}});
            new RunContentExecutor(project,handler).withTitle(title).withActivateToolWindow(true).run();
        } catch(ExecutionException e){NotificationGroupManager.getInstance().getNotificationGroup("Novus").createNotification(title+" failed",e.getMessage(), NotificationType.ERROR).notify(project);}
    }
}
