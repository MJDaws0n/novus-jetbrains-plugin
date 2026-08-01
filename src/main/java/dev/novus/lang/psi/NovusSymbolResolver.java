package dev.novus.lang.psi;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NovusSymbolResolver {
    private NovusSymbolResolver() {}
    public static final Map<String,String> BUILTINS=Map.ofEntries(
        Map.entry("mov","mov(reg, value) -> void"),Map.entry("lea","lea(reg, address) -> void"),Map.entry("push","push(reg) -> void"),
        Map.entry("pop","pop(reg) -> u64"),Map.entry("syscall","syscall() -> void"),Map.entry("int","int(vector) -> void"),
        Map.entry("call","call(address) -> void"),Map.entry("ret","ret() -> void"),Map.entry("nop","nop() -> void"),
        Map.entry("setreg","setreg(reg, value) -> void"),Map.entry("getreg","getreg(reg) -> u64"),Map.entry("setflag","setflag(flag, value) -> void"),
        Map.entry("getflag","getflag(flag) -> bool"),Map.entry("array_append","array_append(array, value) -> void"),Map.entry("array_pop","array_pop(array) -> T"),
        Map.entry("len","len(str | array) -> i32"),Map.entry("load8","load8(address) -> u8"),Map.entry("store_byte","store_byte(address, value) -> void"),
        Map.entry("load32","load32(address) -> u32"),Map.entry("load64","load64(address) -> u64"),Map.entry("win_call","win_call(function, ...) -> u64"),
        Map.entry("i64_to_f64","i64_to_f64(value) -> f64"),Map.entry("f64_to_i64","f64_to_i64(value) -> i64"),
        Map.entry("f64_bits","f64_bits(value) -> u64"),Map.entry("f64_from_bits","f64_from_bits(bits) -> f64"),Map.entry("gc_collect","gc_collect() -> void")
    );
    private static final Pattern IMPORT=Pattern.compile("\\bimport\\s+([^;\\s\\[]+)(?:\\s*\\[([^]]*)])?(?:\\s+([A-Za-z_][A-Za-z0-9_]*))?\\s*;");

    public static @Nullable PsiElement resolve(PsiElement identifier) {
        String name=identifier.getText(); PsiFile file=identifier.getContainingFile(); if(file==null)return null;
        PsiElement prev=significant(identifier,false);
        if(prev!=null && prev.getNode().getElementType()==NovusTypes.DOT) {
            PsiElement qualifier=significant(prev,false);
            if(qualifier!=null) {
                PsiFile imported=resolveAlias(file,qualifier.getText());
                NovusNamedElement found=findDeclaration(imported,name,true); if(found!=null)return found;
            }
        }
        NovusNamedElement scope=containingFunction(identifier);
        if(scope!=null) {
            for(var d:PsiTreeUtil.findChildrenOfType(scope,NovusNamedElement.class))
                if(name.equals(d.getName()) && d.getTextOffset()<=identifier.getTextOffset()) return d;
        }
        for(var d:PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class))
            if(name.equals(d.getName()) && (d.getDeclarationType()==NovusTypes.FUNCTION_DECL || isGlobal(d)))return d;
        for(ImportInfo info:imports(file)) if(info.alias()==null || info.alias().isBlank()) {
            if(!info.selectors().isEmpty()&&!info.selectors().contains(name))continue;
            NovusNamedElement found=findDeclaration(resolveImport(file,info.path()),name,true); if(found!=null)return found;
        }
        return null;
    }

    public static List<NovusNamedElement> visibleDeclarations(PsiElement place) {
        LinkedHashMap<String,NovusNamedElement> out=new LinkedHashMap<>(); PsiFile file=place.getContainingFile(); if(file==null)return List.of();
        NovusNamedElement fn=containingFunction(place);
        if(fn!=null)
            for(var d:PsiTreeUtil.findChildrenOfType(fn,NovusNamedElement.class)) if(d.getName()!=null && d.getTextOffset()<=place.getTextOffset())out.put(d.getName(),d);
        for(var d:PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class)) if(d.getName()!=null && (d.getDeclarationType()==NovusTypes.FUNCTION_DECL || isGlobal(d)))out.putIfAbsent(d.getName(),d);
        for(ImportInfo info:imports(file)) if(info.alias()==null || info.alias().isBlank()) {
            PsiFile imported=resolveImport(file,info.path()); if(imported!=null) for(var d:PsiTreeUtil.findChildrenOfType(imported,NovusNamedElement.class))
                if(d.getDeclarationType()==NovusTypes.FUNCTION_DECL && d.getName()!=null && (info.selectors().isEmpty()||info.selectors().contains(d.getName())))out.putIfAbsent(d.getName(),d);
        }
        return new ArrayList<>(out.values());
    }
    public static List<NovusNamedElement> aliasDeclarations(PsiFile file,String alias) {
        PsiFile target=resolveAlias(file,alias); if(target==null)return List.of();
        return PsiTreeUtil.findChildrenOfType(target,NovusNamedElement.class).stream().filter(d->d.getDeclarationType()==NovusTypes.FUNCTION_DECL).toList();
    }
    private static boolean isGlobal(NovusNamedElement d) {
        return d.getDeclarationType()==NovusTypes.VARIABLE_DECL && PsiTreeUtil.getParentOfType(d,NovusNamedElement.class,true)==null;
    }
    private static NovusNamedElement findDeclaration(PsiFile file,String name,boolean callable) {
        if(file==null)return null;
        for(var d:PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class)) if(name.equals(d.getName()) && (!callable || d.getDeclarationType()!=NovusTypes.MODULE_DECL))return d;
        return null;
    }
    public static @Nullable PsiFile resolveAlias(PsiFile file,String alias) {
        for(ImportInfo i:imports(file)) if(alias.equals(i.alias()))return resolveImport(file,i.path()); return null;
    }
    public static @Nullable PsiFile resolveImport(PsiFile source,String rawPath) {
        VirtualFile vf=source.getVirtualFile(); if(vf==null || vf.getParent()==null)return null;
        String path=rawPath.endsWith(".nov")?rawPath:rawPath+".nov";
        VirtualFile target=VfsUtil.findRelativeFile(path,vf.getParent());
        if(target==null) { VirtualFile dir=VfsUtil.findRelativeFile(rawPath,vf.getParent()); if(dir!=null && dir.isDirectory())target=dir.findChild("main.nov"); }
        return target==null?null:PsiManager.getInstance(source.getProject()).findFile(target);
    }
    public static List<ImportInfo> imports(PsiFile file) {
        var out=new ArrayList<ImportInfo>(); Matcher m=IMPORT.matcher(file.getText()); while(m.find()){
            var selectors=new LinkedHashSet<String>();if(m.group(2)!=null)for(String s:m.group(2).split(","))if(!s.isBlank())selectors.add(s.trim());
            out.add(new ImportInfo(m.group(1),m.group(3),selectors));
        } return out;
    }
    private static PsiElement significant(PsiElement e,boolean forward) {
        PsiElement n=forward?PsiTreeUtil.nextLeaf(e):PsiTreeUtil.prevLeaf(e);
        while(n!=null && (n.getText().isBlank() || NovusTypes.COMMENTS.contains(n.getNode().getElementType())))n=forward?PsiTreeUtil.nextLeaf(n):PsiTreeUtil.prevLeaf(n);
        return n;
    }
    private static NovusNamedElement containingFunction(PsiElement place){PsiElement p=place.getParent();while(p!=null){if(p instanceof NovusNamedElement n&&n.getDeclarationType()==NovusTypes.FUNCTION_DECL)return n;p=p.getParent();}return null;}
    public record ImportInfo(String path,String alias,Set<String> selectors) {}
}
