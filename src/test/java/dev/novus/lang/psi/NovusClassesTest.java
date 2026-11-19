package dev.novus.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import dev.novus.lang.NovusFileType;

import java.util.Set;
import java.util.stream.Collectors;

public final class NovusClassesTest extends BasePlatformTestCase {
    private static final String BALL="""
        class Ball {
            pub let x: i32;
            let secret: i32 = 1;
            pub static let count: i32 = 0;
            pub init(x: i32 = 10) { self.x = x; Ball.count++; }
            pub fn move(dx: i32) -> void { self.x += dx; }
            pub fn copy() -> Ball { return new Ball(self.x); }
            pub static fn origin() -> Ball { return new Ball(); }
        }
        """;

    public void testParsesClassesAndHighlightsWithoutErrors() {
        myFixture.configureByText(NovusFileType.INSTANCE,"module test;\n"+BALL+"""
            class Arena {
                pub let balls: []Ball = [new Ball(), new Ball(3)];
                let saved: Ball = null;
                pub init() { self.saved = self.balls[0]; }
                priv fn reset() -> void { self.saved.move(2); }
            }
            fn main() -> i32 { let ball: Ball = Ball.origin(); return ball.x; }
            """);
        assertEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),PsiErrorElement.class));
        var declarations=PsiTreeUtil.findChildrenOfType(myFixture.getFile(),NovusNamedElement.class);
        assertEquals(2,declarations.stream().filter(d->d.getDeclarationType()==NovusTypes.CLASS_DECL).count());
        assertEquals(2,declarations.stream().filter(d->d.getDeclarationType()==NovusTypes.CONSTRUCTOR_DECL).count());
        assertTrue(declarations.stream().anyMatch(d->d.getDeclarationType()==NovusTypes.FIELD_DECL && "x".equals(d.getName())));
        myFixture.testHighlighting(true,false,true);
    }

    public void testParsesQualifiedAndNestedClassArrayTypes() {
        myFixture.configureByText(NovusFileType.INSTANCE,"""
            module test;
            import ball[Ball] geometry;
            #if(os == "darwin") {
                class Holder {
                    pub let saved: [][]geometry.Ball;
                    pub init(value: geometry.Ball = null) { self.saved = [[value]]; }
                    pub fn first() -> geometry.Ball { return self.saved[0][0]; }
                }
            }
            fn first(values: []geometry.Ball) -> geometry.Ball { return values[0]; }
            """);
        assertEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),PsiErrorElement.class));
    }

    public void testRecoversFromInvalidClassMember() {
        myFixture.configureByText(NovusFileType.INSTANCE,"""
            module bad;
            class Broken { nonsense; pub let x: i32; }
            class Good { pub init() {} }
            fn main() -> i32 { return 0; }
            """);
        assertNotEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),PsiErrorElement.class));
        assertTrue(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),NovusNamedElement.class).stream()
            .anyMatch(d->d.getDeclarationType()==NovusTypes.FUNCTION_DECL && "main".equals(d.getName())));
    }

    public void testRejectsStaticConstructor() {
        myFixture.configureByText(NovusFileType.INSTANCE,"module bad; class Broken { pub static init() {} }");
        assertNotEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),PsiErrorElement.class));
    }

    public void testResolvesSelfPrivateFieldAndConstructorParameter() {
        configure("module test; class Ball { let secret: i32; pub init(value: i32) { self.<caret>secret = value; } }");
        assertResolves("secret",NovusTypes.FIELD_DECL);
        configure("module test; class Ball { let secret: i32; pub init(value: i32) { self.secret = <caret>value; } }");
        assertResolves("value",NovusTypes.PARAMETER_DECL);
    }

    public void testResolvesConstructorAndMethod() {
        configure("module test;\n"+BALL+"fn main() -> i32 { let b: Ball = new <caret>Ball(); return 0; }");
        assertResolves("Ball",NovusTypes.CLASS_DECL);
        configure("module test;\n"+BALL+"fn main() -> i32 { let b: Ball = new Ball(); b.<caret>move(1); return 0; }");
        assertResolves("move",NovusTypes.METHOD_DECL);
    }

    public void testCompletesInstanceMembersWithoutLeakingPrivateOrStaticMembers() {
        configure("module test;\n"+BALL+"fn main() -> i32 { let b: Ball = new Ball(); b.<caret>x; return 0; }");
        assertEquals(Set.of("x","move","copy"),memberNames());
        assertFalse(NovusSymbolResolver.visibleDeclarations(leaf()).stream().anyMatch(d->"move".equals(d.getName())));
        assertNotNull(myFixture.completeBasic());
        assertContainsElements(myFixture.getLookupElementStrings(),"x","move","copy");
    }

    public void testCompletesStaticMembers() {
        configure("module test;\n"+BALL+"fn main() -> i32 { Ball.<caret>count; return 0; }");
        assertEquals(Set.of("count","origin"),memberNames());
        assertResolves("count",NovusTypes.FIELD_DECL);
    }

    public void testResolvesMemberOnMethodResult() {
        configure("module test;\n"+BALL+"fn main() -> i32 { let b: Ball = new Ball(); return b.copy().<caret>x; }");
        assertResolves("x",NovusTypes.FIELD_DECL);
    }

    public void testResolvesMemberOnIndexedClassArray() {
        configure("module test;\n"+BALL+"fn main() -> i32 { let bs: [][]Ball = [[new Ball()]]; return bs[0][0].<caret>x; }");
        assertResolves("x",NovusTypes.FIELD_DECL);
    }

    public void testDoesNotResolvePrivateMemberOutsideClass() {
        configure("module test;\n"+BALL+"fn main() -> i32 { let b: Ball = new Ball(); return b.<caret>secret; }");
        assertNull(NovusSymbolResolver.resolve(leaf()));
    }

    public void testImportedClassAndQualifiedType() {
        myFixture.addFileToProject("ball.nov","module ball;\n"+BALL);
        configureProject("module main; import ball[Ball] geometry; fn main() -> i32 { let b: geometry.Ball = new geometry.Ball(); return b.<caret>x; }");
        assertResolves("x",NovusTypes.FIELD_DECL);
        assertEmpty(PsiTreeUtil.findChildrenOfType(myFixture.getFile(),PsiErrorElement.class));
    }

    public void testSelectiveImportDoesNotExportUnselectedClassesOrMethods() {
        myFixture.addFileToProject("ball.nov","module ball;\n"+BALL+"class Hidden {} fn helper() -> void {}");
        configureProject("module main; import ball[Ball] geometry; fn main() -> i32 { let b: geometry.Ball = new geometry.<caret>Ball(); return 0; }");
        assertEquals(Set.of("Ball"),memberNames());
        assertResolves("Ball",NovusTypes.CLASS_DECL);
    }

    private void configure(String text) { myFixture.configureByText(NovusFileType.INSTANCE,text); }
    private void configureProject(String text) {
        int caret=text.indexOf("<caret>");
        var file=myFixture.addFileToProject("main.nov",text.replace("<caret>",""));
        myFixture.configureFromExistingVirtualFile(file.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(caret);
    }
    private PsiElement leaf() { return myFixture.getFile().findElementAt(myFixture.getCaretOffset()); }
    private Set<String> memberNames() {
        return NovusSymbolResolver.memberDeclarations(leaf()).stream().map(NovusNamedElement::getName).collect(Collectors.toSet());
    }
    private void assertResolves(String name,com.intellij.psi.tree.IElementType type) {
        var target=NovusSymbolResolver.resolve(leaf());
        assertInstanceOf(target,NovusNamedElement.class);
        assertEquals(name,((NovusNamedElement)target).getName());
        assertEquals(type,((NovusNamedElement)target).getDeclarationType());
    }
}
