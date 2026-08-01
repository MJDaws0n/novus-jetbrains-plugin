package dev.novus.actions;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import dev.novus.toolchain.NovusSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class NoxAction extends AnAction {
    private final String command; private final boolean refresh;
    protected NoxAction(String command,boolean refresh){this.command=command;this.refresh=refresh;}
    protected List<String> arguments(Project project){return List.of(command);}
    @Override public final void actionPerformed(@NotNull AnActionEvent e){
        Project p=e.getProject();if(p==null)return;List<String> args=arguments(p);if(args==null)return;
        String nox=NovusSettings.getInstance(p).getState().noxPath;var all=new ArrayList<String>();all.add(nox);all.addAll(args);
        ToolProcess.run(p,new GeneralCommandLine(all).withWorkDirectory(p.getBasePath()).withCharset(StandardCharsets.UTF_8),"Nox: "+String.join(" ",args),refresh);
    }
    @Override public void update(@NotNull AnActionEvent e){e.getPresentation().setEnabled(e.getProject()!=null);}
    @Override public @NotNull ActionUpdateThread getActionUpdateThread(){return ActionUpdateThread.BGT;}

    public static final class Init extends NoxAction { public Init(){super("init",true);} }
    public static final class Update extends NoxAction { public Update(){super("update",true);} }
    public static final class Check extends NoxAction { public Check(){super("check",false);} }
    public static final class ListPackages extends NoxAction { public ListPackages(){super("list",false);} }
    public static final class Pull extends NoxAction {
        public Pull(){super("pull",true);}
        @Override protected List<String> arguments(Project p){String pkg=Messages.showInputDialog(p,"Package name or GitHub URL:","Nox Pull",null);return pkg==null||pkg.isBlank()?null:List.of("pull",pkg.trim());}
    }
}

