package dev.novus.toolchain;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(name="NovusSettings",storages=@Storage("novus.xml"))
@Service(Service.Level.PROJECT)
public final class NovusSettings implements PersistentStateComponent<NovusSettings.State> {
    public static final class State { public String compilerPath=detect("novus","/usr/bin/novus"); public String noxPath=detect("nox","/usr/local/bin/nox"); }
    private State state=new State();
    public static NovusSettings getInstance(Project p){return p.getService(NovusSettings.class);}
    @Override public @NotNull State getState(){return state;}
    @Override public void loadState(@NotNull State state){this.state=state;}
    private static String detect(String name,String fallback){var f=PathEnvironmentVariableUtil.findInPath(name);return f==null?fallback:f.getAbsolutePath();}
}

