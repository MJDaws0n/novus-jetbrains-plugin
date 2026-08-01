package dev.novus.lang.psi;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import dev.novus.lang.NovusFileType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class NovusGotoSymbolContributor implements ChooseByNameContributor {
    @Override public String @NotNull [] getNames(Project project,boolean includeNonProjectItems){return items(project,includeNonProjectItems).stream().map(NavigationItem::getName).filter(java.util.Objects::nonNull).distinct().toArray(String[]::new);}
    @Override public NavigationItem @NotNull [] getItemsByName(String name,String pattern,Project project,boolean includeNonProjectItems){return items(project,includeNonProjectItems).stream().filter(i->name.equals(i.getName())).toArray(NavigationItem[]::new);}
    private static ArrayList<NavigationItem> items(Project p,boolean all){
        var out=new ArrayList<NavigationItem>(); var scope=all?GlobalSearchScope.allScope(p):GlobalSearchScope.projectScope(p); var pm=PsiManager.getInstance(p);
        for(var vf:FileTypeIndex.getFiles(NovusFileType.INSTANCE,scope)){var file=pm.findFile(vf);if(file!=null)out.addAll(PsiTreeUtil.findChildrenOfType(file,NovusNamedElement.class));}
        return out;
    }
}

