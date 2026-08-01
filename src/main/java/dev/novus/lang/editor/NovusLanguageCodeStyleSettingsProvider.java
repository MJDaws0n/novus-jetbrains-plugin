package dev.novus.lang.editor;

import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import dev.novus.lang.NovusLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NovusLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @Override public @NotNull Language getLanguage(){return NovusLanguage.INSTANCE;}
    @Override public @Nullable String getCodeSample(@NotNull SettingsType settingsType){return """
        module sample;
        fn main() -> i32 {
            let values: []i32 = [1, 2, 3];
            for (let i: i32 = 0; i < len(values); i++) {
                values[i] += 1;
            }
            return 0;
        }
        """;}
    @Override public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer,@NotNull SettingsType settingsType){
        if(settingsType==SettingsType.SPACING_SETTINGS)consumer.showStandardOptions("SPACE_AFTER_COMMA","SPACE_AROUND_ASSIGNMENT_OPERATORS","SPACE_AROUND_LOGICAL_OPERATORS","SPACE_AROUND_EQUALITY_OPERATORS","SPACE_AROUND_RELATIONAL_OPERATORS","SPACE_AROUND_ADDITIVE_OPERATORS","SPACE_AROUND_MULTIPLICATIVE_OPERATORS");
        if(settingsType==SettingsType.INDENT_SETTINGS)consumer.showStandardOptions("INDENT_SIZE","TAB_SIZE","USE_TAB_CHARACTER","KEEP_INDENTS_ON_EMPTY_LINES");
    }
}

