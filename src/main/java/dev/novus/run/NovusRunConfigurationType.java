package dev.novus.run;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.openapi.util.NotNullLazyValue;
import dev.novus.lang.psi.NovusIcons;

public final class NovusRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID="NovusRunConfiguration";
    public NovusRunConfigurationType(){super(ID,"Novus","Build or run a Novus program", NotNullLazyValue.createValue(()->NovusIcons.FILE));addFactory(new NovusRunConfigurationFactory(this));}
}

