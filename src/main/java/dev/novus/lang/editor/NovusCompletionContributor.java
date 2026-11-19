package dev.novus.lang.editor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import dev.novus.lang.NovusLanguage;
import dev.novus.lang.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class NovusCompletionContributor extends CompletionContributor {
    private static final List<String> KEYWORDS=List.of("module","import","fn","let","return","if","else","for","while","break","continue","true","false","class","new","pub","priv","static","init","null");
    private static final List<String> TYPES=List.of("void","bool","str","u8","u16","u32","u64","i8","i16","i32","i64","f32","f64");
    public NovusCompletionContributor(){
        extend(CompletionType.BASIC,PlatformPatterns.psiElement().withLanguage(NovusLanguage.INSTANCE),new CompletionProvider<>(){
            @Override protected void addCompletions(@NotNull CompletionParameters p,@NotNull ProcessingContext c,@NotNull CompletionResultSet result){complete(p,result);}
        });
    }
    private static void complete(CompletionParameters p,CompletionResultSet result) {
        PsiElement place=p.getPosition(); String before=p.getEditor().getDocument().getText().substring(0,p.getOffset());
        if(NovusSymbolResolver.isMemberReference(place)) {
            for(var d:NovusSymbolResolver.memberDeclarations(place))addDeclaration(result,d);
            return;
        }
        if(inImport(before)) { addImportPaths(place,result); return; }
        KEYWORDS.forEach(k->result.addElement(LookupElementBuilder.create(k).bold().withTypeText("keyword")));
        TYPES.forEach(t->result.addElement(LookupElementBuilder.create(t).withTypeText("type").withIcon(NovusIcons.FILE)));
        result.addElement(snippet("fn","function declaration"," name() -> void {\n    \n}"));
        result.addElement(snippet("class","class declaration"," Name {\n    pub init() {\n        \n    }\n}"));
        if(NovusSymbolResolver.containingClass(place)!=null)
            result.addElement(LookupElementBuilder.create("self").bold().withTypeText("current instance"));
        result.addElement(snippet("main","program entry point","fn main() -> i32 {\n    return 0;\n}"));
        result.addElement(snippet("for","for loop"," (let i: i32 = 0; i < count; i++) {\n    \n}"));
        for(var e:NovusSymbolResolver.BUILTINS.entrySet()) result.addElement(LookupElementBuilder.create(e.getKey()).withTypeText(e.getValue(),true).withIcon(NovusIcons.FILE));
        for(var d:NovusSymbolResolver.visibleDeclarations(place))addDeclaration(result,d);
        for(var i:NovusSymbolResolver.imports(place.getContainingFile()))if(i.alias()!=null)result.addElement(LookupElementBuilder.create(i.alias()).withTypeText("import alias").withIcon(NovusIcons.FILE));
    }
    private static void addDeclaration(CompletionResultSet result,NovusNamedElement d) {
        if(d.getName()==null)return; String signature=d.getText().replaceAll("\\s+"," "); if(signature.length()>100)signature=signature.substring(0,100)+"…";
        result.addElement(LookupElementBuilder.createWithIcon(d).withTypeText(signature,true));
    }
    private static LookupElementBuilder snippet(String lookup,String type,String suffix) {
        return LookupElementBuilder.create(lookup).bold().withTypeText(type).withInsertHandler((ctx,item)->{
            int at=ctx.getTailOffset(); ctx.getDocument().insertString(at,suffix); ctx.getEditor().getCaretModel().moveToOffset(Math.max(at,at+suffix.indexOf("\n    ")+5));
        });
    }
    private static boolean inImport(String text){int line=Math.max(text.lastIndexOf('\n'),text.lastIndexOf('\r'));return text.substring(line+1).stripLeading().startsWith("import ");}
    private static void addImportPaths(PsiElement place,CompletionResultSet result) {
        var vf=place.getContainingFile().getVirtualFile(); if(vf==null||vf.getParent()==null)return;
        addChildren(vf.getParent(),"",result,0);
    }
    private static void addChildren(com.intellij.openapi.vfs.VirtualFile dir,String prefix,CompletionResultSet result,int depth){
        if(depth>3)return;
        for(var f:dir.getChildren()) {
            if(f.isDirectory()) { result.addElement(LookupElementBuilder.create(prefix+f.getName()).withTypeText("directory")); addChildren(f,prefix+f.getName()+"/",result,depth+1); }
            else if("nov".equalsIgnoreCase(f.getExtension()))result.addElement(LookupElementBuilder.create(prefix+f.getNameWithoutExtension()).withTypeText("Novus module"));
        }
    }
}

