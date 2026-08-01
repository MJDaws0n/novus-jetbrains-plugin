package dev.novus.lang.parser;

import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import dev.novus.lang.NovusFileType;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusTypes;

public final class NovusParserTest extends BasePlatformTestCase {
    public void testParsesDeclarationsAndControlFlow(){
        var file=PsiFileFactory.getInstance(getProject()).createFileFromText("main.nov",NovusFileType.INSTANCE,"""
            module test;
            import lib/std std;
            let total: i64 = 0;
            fn add(a: i32, b: i32 = 1) -> i32 { return a + b; }
            fn main() -> i32 {
                let xs: []i32 = [1, 2];
                for (let i: i32 = 0; i < 2; i++) { array_append(xs, i); }
                return add(xs[0]);
            }
            """);
        assertEmpty(PsiTreeUtil.findChildrenOfType(file,PsiErrorElement.class));
        var declarations=PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class);
        assertTrue(declarations.stream().anyMatch(d->d.getDeclarationType()==NovusTypes.FUNCTION_DECL&&"main".equals(d.getName())));
        assertTrue(declarations.stream().anyMatch(d->d.getDeclarationType()==NovusTypes.PARAMETER_DECL&&"a".equals(d.getName())));
        assertTrue(declarations.stream().anyMatch(d->d.getDeclarationType()==NovusTypes.VARIABLE_DECL&&"xs".equals(d.getName())));
    }
    public void testReportsMissingSemicolon(){
        var file=PsiFileFactory.getInstance(getProject()).createFileFromText("bad.nov",NovusFileType.INSTANCE,"module bad fn main() -> i32 { return 0; }");
        assertFalse(PsiTreeUtil.findChildrenOfType(file,PsiErrorElement.class).isEmpty());
    }
    public void testParsesNestedArrayTypes(){
        var file=PsiFileFactory.getInstance(getProject()).createFileFromText("nested.nov",NovusFileType.INSTANCE,"""
            module nested;
            let grid: [][]str = [["a"], ["b"]];
            fn first(values: [][]str) -> []str { return values[0]; }
            fn main() -> i32 {
                let cube: [][][]i32 = [[[1]]];
                return 0;
            }
            """);
        assertEmpty(PsiTreeUtil.findChildrenOfType(file,PsiErrorElement.class));
    }
}
