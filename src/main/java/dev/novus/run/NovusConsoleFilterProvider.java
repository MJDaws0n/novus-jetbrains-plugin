package dev.novus.run;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public final class NovusConsoleFilterProvider implements ConsoleFilterProvider {
    @Override public Filter @NotNull [] getDefaultFilters(@NotNull Project project){return new Filter[]{(line,end)->{
        var m=Pattern.compile("([^\\s:]+\\.nov):?\\s*(?:line\\s+)?(\\d+)[,:]\\s*(?:col(?:umn)?\\s+)?(\\d+)",Pattern.CASE_INSENSITIVE).matcher(line);
        if(!m.find())return null;var vf=LocalFileSystem.getInstance().findFileByPath(m.group(1));if(vf==null)return null;
        int start=end-line.length()+m.start(),finish=end-line.length()+m.end();return new Filter.Result(start,finish,new OpenFileHyperlinkInfo(project,vf,Integer.parseInt(m.group(2))-1,Integer.parseInt(m.group(3))-1));
    }};}
}

