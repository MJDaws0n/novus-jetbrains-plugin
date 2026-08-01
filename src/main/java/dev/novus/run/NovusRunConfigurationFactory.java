package dev.novus.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class NovusRunConfigurationFactory extends ConfigurationFactory {
    public NovusRunConfigurationFactory(ConfigurationType type){super(type);}
    @Override public @NotNull String getId(){return NovusRunConfigurationType.ID;}
    @Override public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project){return new NovusRunConfiguration(project,this,"Novus");}
}

