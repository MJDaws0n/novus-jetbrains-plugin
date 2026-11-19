package dev.novus.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NovusCommandLineStateTest extends BasePlatformTestCase {
    @Override protected boolean runInDispatchThread() { return false; }
    public void testInteractiveCommandUsesTerminalEmulationMode() {
        GeneralCommandLine command = NovusCommandLineState.interactiveCommandLine(
            new GeneralCommandLine("/bin/sh", "-c", "true")
        );

        assertTrue(command instanceof PtyCommandLine);
        PtyCommandLine ptyCommand = (PtyCommandLine)command;
        assertFalse(ptyCommand.getPtyOptions().getConsoleMode());
    }

    public void testTerminalEmulationModeEchoesTypedInput() throws Exception {
        GeneralCommandLine command = NovusCommandLineState.interactiveCommandLine(
            new GeneralCommandLine(
                "/bin/sh", "-c",
                "printf 'Prompt: '; IFS= read -r value; printf 'Received: %s\\n' \"$value\""
            )
        );
        Process process = command.createProcess();
        var ready=new CountDownLatch(1);
        var output=CompletableFuture.supplyAsync(()->{
            var bytes=new ByteArrayOutputStream();
            try {
                int value;
                while((value=process.getInputStream().read())!=-1) {
                    bytes.write(value);
                    if(bytes.toString(StandardCharsets.UTF_8).contains("Prompt: ")) ready.countDown();
                }
                return bytes.toString(StandardCharsets.UTF_8);
            } catch(IOException e) { throw new UncheckedIOException(e); }
        });
        try {
            // The IDE application supplies LocalProcessService. Wait for the
            // PTY to finish initialising before sending input to its slave.
            assertTrue("interactive process did not become ready",ready.await(5,TimeUnit.SECONDS));
            process.getOutputStream().write("visible input\n".getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            assertTrue("interactive process did not finish",process.waitFor(5,TimeUnit.SECONDS));
            String text=output.get(5,TimeUnit.SECONDS);
            // The text appears once from terminal echo and once from the program.
            assertTrue(text,text.split("visible input",-1).length-1>=2);
        } finally {
            process.destroyForcibly();
        }
    }
}
