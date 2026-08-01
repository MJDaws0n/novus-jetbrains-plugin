package dev.novus.lang.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import dev.novus.lang.psi.NovusTypes;

public final class NovusQuoteHandler extends SimpleTokenSetQuoteHandler {
    public NovusQuoteHandler(){super(NovusTypes.STRINGS);}
}

