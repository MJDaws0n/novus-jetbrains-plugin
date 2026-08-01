package dev.novus.run;

import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class NovusRunConfigurationEditor extends SettingsEditor<NovusRunConfiguration> {
    private final Project project; private final TextFieldWithBrowseButton source=new TextFieldWithBrowseButton(); private final TextFieldWithBrowseButton workDir=new TextFieldWithBrowseButton();
    private final JBTextField target=new JBTextField(); private final JBTextField compilerArgs=new JBTextField(); private final JBTextField programArgs=new JBTextField(); private final JBCheckBox buildOnly=new JBCheckBox("Build only (do not execute binary)");
    public NovusRunConfigurationEditor(Project project){this.project=project;source.addBrowseFolderListener(project,FileChooserDescriptorFactory.createSingleFileDescriptor("nov"));workDir.addBrowseFolderListener(project,FileChooserDescriptorFactory.createSingleFolderDescriptor());}
    @Override protected void resetEditorFrom(@NotNull NovusRunConfiguration c){source.setText(c.sourcePath);workDir.setText(c.workingDirectory);target.setText(c.target);compilerArgs.setText(c.compilerArguments);programArgs.setText(c.programArguments);buildOnly.setSelected(c.buildOnly);}
    @Override protected void applyEditorTo(@NotNull NovusRunConfiguration c){c.sourcePath=source.getText().trim();c.workingDirectory=workDir.getText().trim();c.target=target.getText().trim();c.compilerArguments=compilerArgs.getText().trim();c.programArguments=programArgs.getText().trim();c.buildOnly=buildOnly.isSelected();}
    @Override protected @NotNull JComponent createEditor(){return FormBuilder.createFormBuilder().addLabeledComponent("Source file:",source).addLabeledComponent("Working directory:",workDir).addLabeledComponent("Target (for example linux/amd64):",target).addLabeledComponent("Compiler arguments:",compilerArgs).addLabeledComponent("Program arguments:",programArgs).addComponent(buildOnly).addComponentFillVertically(new JPanel(),0).getPanel();}
}

