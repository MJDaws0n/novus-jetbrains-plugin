package dev.novus.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.Executor;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.terminal.TerminalExecutionConsoleBuilder;
import dev.novus.toolchain.NovusSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class NovusCommandLineState extends CommandLineState {
    private final NovusRunConfiguration configuration;
    public NovusCommandLineState(ExecutionEnvironment env,NovusRunConfiguration configuration){super(env);this.configuration=configuration;}
    @Override protected @NotNull ProcessHandler startProcess() throws ExecutionException {
        GeneralCommandLine cmd=createCommandLine(configuration);
        var handler=new OSProcessHandler(cmd);ProcessTerminatedListener.attach(handler);return handler;
    }
    @Override protected ConsoleView createConsole(@NotNull Executor executor) throws ExecutionException {
        if(!configuration.buildOnly&&!SystemInfo.isWindows)return new TerminalExecutionConsoleBuilder(getEnvironment().getProject()).build();
        return super.createConsole(executor);
    }
    public static GeneralCommandLine createCommandLine(NovusRunConfiguration c) throws ExecutionException {
        String compiler=NovusSettings.getInstance(c.getProject()).getState().compilerPath;
        if(compiler.isBlank())throw new ExecutionException("Configure the Novus compiler path in Settings | Tools | Novus");
        List<String> compile=new ArrayList<>();compile.add(compiler);
        if(!c.target.isBlank())compile.add("--target="+c.target);
        compile.addAll(ParametersListUtil.parse(c.compilerArguments));compile.add(c.sourcePath);
        String work=c.effectiveWorkingDirectory();
        if(c.buildOnly)return new GeneralCommandLine(compile).withWorkDirectory(work).withCharset(java.nio.charset.StandardCharsets.UTF_8);
        String binary=expectedBinary(c);
        List<String> program=ParametersListUtil.parse(c.programArguments);
        GeneralCommandLine command;
        if(SystemInfo.isWindows) {
            StringBuilder line=new StringBuilder();compile.forEach(x->line.append(cmdQuote(x)).append(' '));line.append("&& ").append(cmdQuote(binary));program.forEach(x->line.append(' ').append(cmdQuote(x)));
            command=new GeneralCommandLine("cmd.exe","/d","/s","/c",line.toString());
        } else {
            List<String> params=new ArrayList<>();params.add("/bin/sh");params.add("-c");
            int n=compile.size();StringBuilder script=new StringBuilder();for(int i=1;i<=n;i++)script.append("\"$").append(i).append("\" ");
            script.append("|| exit $?; shift ").append(n).append("; exec \"$@\"");params.add(script.toString());params.add("novus-run");params.addAll(compile);params.add(binary);params.addAll(program);
            command=new GeneralCommandLine(params);
        }
        // Interactive Unix runs need a PTY configured for terminal emulation.
        // PtyCommandLine's default console mode suppresses local echo when it
        // is attached to TerminalExecutionConsole, making normal input look
        // like password entry.
        if(!SystemInfo.isWindows)command=interactiveCommandLine(command);
        return command.withWorkDirectory(work).withCharset(java.nio.charset.StandardCharsets.UTF_8);
    }
    static GeneralCommandLine interactiveCommandLine(GeneralCommandLine command) {
        return new PtyCommandLine(command).withConsoleMode(false);
    }
    private static String expectedBinary(NovusRunConfiguration c) throws ExecutionException {
        String module="output";
        try {var m=Pattern.compile("(?m)^\\s*module\\s+([A-Za-z_][A-Za-z0-9_.]*)").matcher(Files.readString(Path.of(c.sourcePath)));if(m.find())module=m.group(1);}catch(Exception e){throw new ExecutionException("Cannot read Novus source: "+e.getMessage(),e);}
        module=module.replaceAll("[. /\\\\]","_");String target=c.target.isBlank()?hostTarget():c.target;String[] p=target.split("/",2);String os=p.length>0?p[0]:"linux",arch=p.length>1?p[1]:"amd64";
        arch=switch(arch){case "amd64","x86_64"->"x86_64";case "386","x86"->"x86";case "aarch64","arm64"->"arm64";default->arch;};
        return new File(c.effectiveWorkingDirectory(),"build"+File.separator+os+"_"+arch+File.separator+module+("windows".equals(os)?".exe":"")).getPath();
    }
    private static String hostTarget(){String os=SystemInfo.isMac?"darwin":SystemInfo.isWindows?"windows":"linux";String a=System.getProperty("os.arch","").toLowerCase();String arch=a.contains("aarch64")||a.contains("arm64")?"arm64":a.contains("86")&&!a.contains("64")?"386":"amd64";return os+"/"+arch;}
    private static String cmdQuote(String s){return "\""+s.replace("\"","\"\"")+"\"";}
}
