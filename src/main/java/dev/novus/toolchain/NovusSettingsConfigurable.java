package dev.novus.toolchain;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class NovusSettingsConfigurable implements Configurable {
    private final Project project; private JBTextField compiler; private JBTextField nox;
    public NovusSettingsConfigurable(Project project){this.project=project;}
    @Override public @Nls String getDisplayName(){return "Novus";}
    @Override public @Nullable JComponent createComponent(){
        var s=NovusSettings.getInstance(project).getState(); compiler=new JBTextField(s.compilerPath); nox=new JBTextField(s.noxPath);
        return FormBuilder.createFormBuilder().addLabeledComponent("Novus compiler:",compiler).addLabeledComponent("Nox package manager:",nox).addComponentFillVertically(new JPanel(),0).getPanel();
    }
    @Override public boolean isModified(){var s=NovusSettings.getInstance(project).getState();return !compiler.getText().equals(s.compilerPath)||!nox.getText().equals(s.noxPath);}
    @Override public void apply(){var s=NovusSettings.getInstance(project).getState();s.compilerPath=compiler.getText().trim();s.noxPath=nox.getText().trim();}
    @Override public void reset(){var s=NovusSettings.getInstance(project).getState();compiler.setText(s.compilerPath);nox.setText(s.noxPath);}
}

