package dev.novus.lang.editor;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import dev.novus.lang.psi.NovusNamedElement;
import dev.novus.lang.psi.NovusTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class NovusStructureViewFactory implements PsiStructureViewFactory {
    @Override public @Nullable StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile file) {
        return new TreeBasedStructureViewBuilder(){@Override public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor){return new Model(file,editor);}};
    }
    private static final class Model extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
        Model(PsiFile file,Editor editor){super(file,editor,new Element(file));withSuitableClasses(NovusNamedElement.class);}
        @Override public Sorter @NotNull [] getSorters(){return new Sorter[]{Sorter.ALPHA_SORTER};}
        @Override public boolean isAlwaysShowsPlus(StructureViewTreeElement element){return false;}
        @Override public boolean isAlwaysLeaf(StructureViewTreeElement element){return element.getValue() instanceof NovusNamedElement;}
    }
    private static final class Element implements StructureViewTreeElement {
        private final com.intellij.psi.PsiElement value;
        Element(com.intellij.psi.PsiElement e){value=e;}
        @Override public Object getValue(){return value;}
        @Override public TreeElement @NotNull [] getChildren(){
            return PsiTreeUtil.findChildrenOfType(value,NovusNamedElement.class).stream()
                .filter(n->n.getDeclarationType()==NovusTypes.FUNCTION_DECL || (n.getDeclarationType()==NovusTypes.VARIABLE_DECL && PsiTreeUtil.getParentOfType(n,NovusNamedElement.class,true)==null))
                .map(Element::new).toArray(TreeElement[]::new);
        }
        @Override public @NotNull ItemPresentation getPresentation(){if(value instanceof NovusNamedElement n)return n.getPresentation();return new ItemPresentation(){@Override public String getPresentableText(){return value instanceof PsiFile f?f.getName():"Novus";}@Override public javax.swing.Icon getIcon(boolean unused){return dev.novus.lang.psi.NovusIcons.FILE;}};}
        @Override public void navigate(boolean requestFocus){if(value instanceof com.intellij.pom.Navigatable n)n.navigate(requestFocus);}
        @Override public boolean canNavigate(){return value instanceof com.intellij.pom.Navigatable n&&n.canNavigate();}
        @Override public boolean canNavigateToSource(){return value instanceof com.intellij.pom.Navigatable n&&n.canNavigateToSource();}
    }
}
