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
        return resolve(identifier,0);
    }
    private static @Nullable PsiElement resolve(PsiElement identifier,int depth) {
        if(depth>20) return null;
        String name=identifier.getText(); PsiFile file=identifier.getContainingFile(); if(file==null)return null;
        PsiElement prev=significant(identifier,false);
        if(prev!=null && prev.getNode().getElementType()==NovusTypes.DOT) {
            PsiElement qualifier=significant(prev,false);
            if(qualifier!=null) {
                for(var d:aliasDeclarations(file,qualifier.getText())) if(name.equals(d.getName())) return d;
                ValueType owner=typeOf(qualifier,depth+1);
                if(owner!=null && owner.arrayDepth()==0)
                    for(var d:members(owner,identifier)) if(name.equals(d.getName())) return d;
            }
            return null;
        }
        if("self".equals(name)) return containingClass(identifier);
        for(var d:locals(identifier)) if(name.equals(d.getName())) return d;
        for(var d:exports(file)) if(name.equals(d.getName())) return d;
        for(ImportInfo info:imports(file)) if(info.alias()==null || info.alias().isBlank()) {
            if(!info.selectors().isEmpty()&&!info.selectors().contains(name))continue;
            for(var d:exports(resolveImport(file,info.path()))) if(name.equals(d.getName())) return d;
        }
        return resolveAlias(file,name);
    }

    public static List<NovusNamedElement> visibleDeclarations(PsiElement place) {
        LinkedHashMap<String,NovusNamedElement> out=new LinkedHashMap<>(); PsiFile file=place.getContainingFile(); if(file==null)return List.of();
        for(var d:locals(place)) if(d.getName()!=null) out.putIfAbsent(d.getName(),d);
        for(var d:exports(file)) if(d.getName()!=null) out.putIfAbsent(d.getName(),d);
        for(ImportInfo info:imports(file)) if(info.alias()==null || info.alias().isBlank()) {
            for(var d:exports(resolveImport(file,info.path())))
                if(d.getName()!=null && (info.selectors().isEmpty()||info.selectors().contains(d.getName())))out.putIfAbsent(d.getName(),d);
        }
        return new ArrayList<>(out.values());
    }
    public static List<NovusNamedElement> aliasDeclarations(PsiFile file,String alias) {
        for(ImportInfo info:imports(file)) if(alias.equals(info.alias()))
            return exports(resolveImport(file,info.path())).stream()
                .filter(d->info.selectors().isEmpty() || info.selectors().contains(d.getName())).toList();
        return List.of();
    }
    private static List<NovusNamedElement> exports(@Nullable PsiFile file) {
        if(file==null) return List.of();
        return PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class).stream()
            .filter(d->PsiTreeUtil.getParentOfType(d,NovusNamedElement.class,true)==null)
            .filter(d->d.getDeclarationType()==NovusTypes.FUNCTION_DECL || d.getDeclarationType()==NovusTypes.CLASS_DECL || d.getDeclarationType()==NovusTypes.VARIABLE_DECL).toList();
    }
    private static List<NovusNamedElement> locals(PsiElement place) {
        var fn=containingFunction(place); if(fn==null) return List.of();
        return PsiTreeUtil.findChildrenOfType(fn,NovusNamedElement.class).stream()
            .filter(d->(d.getDeclarationType()==NovusTypes.PARAMETER_DECL || d.getDeclarationType()==NovusTypes.VARIABLE_DECL)
                && containingFunction(d)==fn && d.getTextOffset()<=place.getTextOffset())
            .filter(d->{
                if(d.getDeclarationType()==NovusTypes.PARAMETER_DECL) return true;
                PsiElement scope=d.getParent();
                while(scope!=null && scope!=fn && scope.getNode().getElementType()!=NovusTypes.BLOCK) scope=scope.getParent();
                return scope!=null && PsiTreeUtil.isAncestor(scope,place,false);
            }).sorted(Comparator.comparingInt(NovusNamedElement::getTextOffset).reversed()).toList();
    }
    public static boolean isMemberReference(PsiElement place) {
        PsiElement prev=significant(place,false);
        return prev!=null && prev.getNode().getElementType()==NovusTypes.DOT;
    }
    public static List<NovusNamedElement> memberDeclarations(PsiElement place) {
        if(!isMemberReference(place)) return List.of();
        PsiElement qualifier=significant(significant(place,false),false); if(qualifier==null) return List.of();
        var aliases=aliasDeclarations(place.getContainingFile(),qualifier.getText());
        if(resolveAlias(place.getContainingFile(),qualifier.getText())!=null) return aliases;
        ValueType owner=typeOf(qualifier,0);
        return owner==null || owner.arrayDepth()!=0?List.of():members(owner,place);
    }
    private static List<NovusNamedElement> members(ValueType owner,PsiElement place) {
        boolean internal=containingClass(place)==owner.declaration();
        return PsiTreeUtil.findChildrenOfType(owner.declaration(),NovusNamedElement.class).stream()
            .filter(d->d.getDeclarationType()==NovusTypes.FIELD_DECL || d.getDeclarationType()==NovusTypes.METHOD_DECL)
            .filter(d->d.isStatic()==owner.staticAccess() && (internal || d.isPublic())).toList();
    }
    private static @Nullable ValueType typeOf(PsiElement expression,int depth) {
        if(depth>20) return null;
        var token=expression.getNode().getElementType();
        if(token==NovusTypes.IDENTIFIER) {
            if("self".equals(expression.getText())) {
                var owner=containingClass(expression);
                return owner==null?null:new ValueType(owner,0,false);
            }
            PsiElement resolved=resolve(expression,depth+1);
            if(resolved instanceof NovusNamedElement d) {
                if(d.getDeclarationType()==NovusTypes.CLASS_DECL) return new ValueType(d,0,true);
                return declaredType(d);
            }
        } else if(token==NovusTypes.RPAREN || token==NovusTypes.RBRACKET) {
            var open=token==NovusTypes.RPAREN?NovusTypes.LPAREN:NovusTypes.LBRACKET;
            int balance=1; PsiElement start=significant(expression,false);
            while(start!=null) {
                var t=start.getNode().getElementType();
                if(t==token) balance++; else if(t==open && --balance==0) break;
                start=significant(start,false);
            }
            PsiElement base=start==null?null:significant(start,false); if(base==null) return null;
            if(token==NovusTypes.RBRACKET) {
                ValueType array=typeOf(base,depth+1);
                return array==null || array.arrayDepth()==0?null:new ValueType(array.declaration(),array.arrayDepth()-1,false);
            }
            PsiElement resolved=resolve(base,depth+1);
            if(resolved instanceof NovusNamedElement d) {
                if(d.getDeclarationType()==NovusTypes.CLASS_DECL) return new ValueType(d,0,false);
                if(d.isCallable()) return declaredType(d);
            }
        }
        return null;
    }
    private static @Nullable ValueType declaredType(NovusNamedElement d) {
        String name=d.getTypeName(); if(name==null) return null;
        int dimensions=0;
        while(name.startsWith("[]")) { dimensions++; name=name.substring(2); }
        PsiFile source=d.getContainingFile();
        int dot=name.indexOf('.');
        List<NovusNamedElement> candidates;
        if(dot>=0) {
            candidates=aliasDeclarations(source,name.substring(0,dot)); name=name.substring(dot+1);
        } else {
            candidates=new ArrayList<>(exports(source));
            for(ImportInfo info:imports(source)) if(info.alias()==null || info.alias().isBlank())
                for(var imported:exports(resolveImport(source,info.path())))
                    if(info.selectors().isEmpty() || info.selectors().contains(imported.getName())) candidates.add(imported);
        }
        for(var candidate:candidates) if(candidate.getDeclarationType()==NovusTypes.CLASS_DECL && name.equals(candidate.getName()))
            return new ValueType(candidate,dimensions,false);
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
    private static NovusNamedElement containingFunction(PsiElement place){PsiElement p=place.getParent();while(p!=null){if(p instanceof NovusNamedElement n&&n.isCallable())return n;p=p.getParent();}return null;}
    public static @Nullable NovusNamedElement containingClass(PsiElement place){PsiElement p=place.getParent();while(p!=null){if(p instanceof NovusNamedElement n&&n.getDeclarationType()==NovusTypes.CLASS_DECL)return n;p=p.getParent();}return null;}
    private record ValueType(NovusNamedElement declaration,int arrayDepth,boolean staticAccess) {}
    public record ImportInfo(String path,String alias,Set<String> selectors) {}
}
