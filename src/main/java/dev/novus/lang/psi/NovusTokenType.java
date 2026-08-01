package dev.novus.lang.psi;

import com.intellij.psi.tree.IElementType;
import dev.novus.lang.NovusLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class NovusTokenType extends IElementType {
    public NovusTokenType(@NotNull @NonNls String debugName) { super(debugName, NovusLanguage.INSTANCE); }
    @Override public String toString() { return "NovusToken." + super.toString(); }
}

