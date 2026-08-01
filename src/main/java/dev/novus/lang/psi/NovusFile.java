package dev.novus.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.psi.FileViewProvider;
import dev.novus.lang.NovusFileType;
import dev.novus.lang.NovusLanguage;
import org.jetbrains.annotations.NotNull;

public final class NovusFile extends PsiFileBase {
    public NovusFile(@NotNull FileViewProvider viewProvider) { super(viewProvider, NovusLanguage.INSTANCE); }
    @Override public @NotNull NovusFileType getFileType() { return NovusFileType.INSTANCE; }
    @Override public String toString() { return "Novus File"; }
}

