package dev.novus.lang.editor;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import dev.novus.lang.psi.NovusIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public final class NovusColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS={
        new AttributesDescriptor("Keyword",NovusSyntaxHighlighter.KEYWORD),new AttributesDescriptor("Type",NovusSyntaxHighlighter.TYPE),
        new AttributesDescriptor("Number",NovusSyntaxHighlighter.NUMBER),new AttributesDescriptor("String",NovusSyntaxHighlighter.STRING),
        new AttributesDescriptor("Line comment",NovusSyntaxHighlighter.COMMENT),new AttributesDescriptor("Block comment",NovusSyntaxHighlighter.BLOCK_COMMENT),
        new AttributesDescriptor("Operator",NovusSyntaxHighlighter.OPERATOR),new AttributesDescriptor("Function declaration",NovusSyntaxHighlighter.FUNCTION),
        new AttributesDescriptor("Function call",NovusSyntaxHighlighter.FUNCTION_CALL),new AttributesDescriptor("Variable",NovusSyntaxHighlighter.VARIABLE),
        new AttributesDescriptor("Parameter",NovusSyntaxHighlighter.PARAMETER),new AttributesDescriptor("Module/import alias",NovusSyntaxHighlighter.MODULE),
        new AttributesDescriptor("Builtin",NovusSyntaxHighlighter.BUILTIN),new AttributesDescriptor("Class",NovusSyntaxHighlighter.CLASS),
        new AttributesDescriptor("Field",NovusSyntaxHighlighter.FIELD),new AttributesDescriptor("Method/constructor",NovusSyntaxHighlighter.METHOD)
    };
    @Override public @Nullable Icon getIcon(){return NovusIcons.FILE;}
    @Override public @NotNull SyntaxHighlighter getHighlighter(){return new NovusSyntaxHighlighter();}
    @Override public @NotNull String getDemoText(){return """
        module demo;
        import lib/std std;

        class Ball {
            pub let x: i32;
            priv static let created: i32 = 0;
            pub init(x: i32 = 10) { self.x = x; Ball.created++; }
            pub fn move(dx: i32) -> void { self.x += dx; }
        }

        // Novus sample
        fn greet(name: str = "world") -> void {
            let values: []i32 = [1, 2, 3];
            std.print("Hello, " + name);
        }

        fn main() -> i32 {
            let ball: Ball = new Ball();
            ball.move(2);
            greet();
            return 0;
        }
        """;}
    @Override public @Nullable Map<String,TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap(){return null;}
    @Override public AttributesDescriptor @NotNull [] getAttributeDescriptors(){return DESCRIPTORS;}
    @Override public ColorDescriptor @NotNull [] getColorDescriptors(){return ColorDescriptor.EMPTY_ARRAY;}
    @Override public @NotNull String getDisplayName(){return "Novus";}
}

