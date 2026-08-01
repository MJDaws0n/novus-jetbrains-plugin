package dev.novus.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.options.SettingsEditor;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class NovusRunConfiguration extends LocatableConfigurationBase<NovusCommandLineState> {
    public String sourcePath="", target="", compilerArguments="", programArguments="", workingDirectory=""; public boolean buildOnly=false;
    public NovusRunConfiguration(Project p,ConfigurationFactory f,String name){super(p,f,name);}
    @Override public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor(){return new NovusRunConfigurationEditor(getProject());}
    @Override public void checkConfiguration() throws RuntimeConfigurationException {
        if(sourcePath.isBlank())throw new RuntimeConfigurationError("Novus source file is required");
        File f=new File(sourcePath);if(!f.isFile())throw new RuntimeConfigurationError("Novus source file does not exist: "+sourcePath);
        if(!sourcePath.endsWith(".nov"))throw new RuntimeConfigurationWarning("Source file does not use the .nov extension");
    }
    @Override public @Nullable NovusCommandLineState getState(@NotNull Executor executor,@NotNull ExecutionEnvironment environment) throws ExecutionException{return new NovusCommandLineState(environment,this);}
    @Override public void writeExternal(@NotNull Element e){super.writeExternal(e);JDOMExternalizerUtil.writeField(e,"sourcePath",sourcePath);JDOMExternalizerUtil.writeField(e,"target",target);JDOMExternalizerUtil.writeField(e,"compilerArguments",compilerArguments);JDOMExternalizerUtil.writeField(e,"programArguments",programArguments);JDOMExternalizerUtil.writeField(e,"workingDirectory",workingDirectory);JDOMExternalizerUtil.writeField(e,"buildOnly",Boolean.toString(buildOnly));PathMacroManager.getInstance(getProject()).collapsePathsRecursively(e);}
    @Override public void readExternal(@NotNull Element e){PathMacroManager.getInstance(getProject()).expandPaths(e);super.readExternal(e);sourcePath=value(e,"sourcePath","");target=value(e,"target","");compilerArguments=value(e,"compilerArguments","");programArguments=value(e,"programArguments","");workingDirectory=value(e,"workingDirectory","");buildOnly=Boolean.parseBoolean(value(e,"buildOnly","false"));}
    private static String value(Element e,String key,String fallback){String v=JDOMExternalizerUtil.readField(e,key);return v==null?fallback:v;}
    public String effectiveWorkingDirectory(){if(!workingDirectory.isBlank())return FileUtil.toSystemDependentName(workingDirectory);File p=new File(sourcePath).getAbsoluteFile().getParentFile();return p==null?getProject().getBasePath():p.getPath();}
}
