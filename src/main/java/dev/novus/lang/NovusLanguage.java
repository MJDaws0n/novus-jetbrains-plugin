package dev.novus.lang;

import com.intellij.lang.Language;

public final class NovusLanguage extends Language {
    public static final NovusLanguage INSTANCE = new NovusLanguage();
    private NovusLanguage() { super("Novus"); }
}

