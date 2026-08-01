package dev.novus.lang.psi;

import com.intellij.psi.tree.IElementType;
import dev.novus.lang.NovusLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class NovusElementType extends IElementType {
    public NovusElementType(@NotNull @NonNls String debugName) { super(debugName, NovusLanguage.INSTANCE); }
}

