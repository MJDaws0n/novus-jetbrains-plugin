package dev.novus.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import dev.novus.lang.psi.NovusIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class NovusFileType extends LanguageFileType {
    public static final NovusFileType INSTANCE = new NovusFileType();
    private NovusFileType() { super(NovusLanguage.INSTANCE); }
    @Override public @NotNull String getName() { return "Novus"; }
    @Override public @NotNull String getDescription() { return "Novus source file"; }
    @Override public @NotNull String getDefaultExtension() { return "nov"; }
    @Override public Icon getIcon() { return NovusIcons.FILE; }
}

