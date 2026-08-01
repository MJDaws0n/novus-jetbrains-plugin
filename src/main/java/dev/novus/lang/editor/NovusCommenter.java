package dev.novus.lang.editor;

import com.intellij.lang.Commenter;
import org.jetbrains.annotations.Nullable;

public final class NovusCommenter implements Commenter {
    @Override public String getLineCommentPrefix(){return "//";}
    @Override public String getBlockCommentPrefix(){return "/*";}
    @Override public String getBlockCommentSuffix(){return "*/";}
    @Override public @Nullable String getCommentedBlockCommentPrefix(){return null;}
    @Override public @Nullable String getCommentedBlockCommentSuffix(){return null;}
}

