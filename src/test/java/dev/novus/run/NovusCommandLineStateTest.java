package dev.novus.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PtyCommandLine;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NovusCommandLineStateTest {
    @Test
    public void interactiveCommandUsesTerminalEmulationMode() {
        GeneralCommandLine command = NovusCommandLineState.interactiveCommandLine(
            new GeneralCommandLine("/bin/sh", "-c", "true")
        );

        assertTrue(command instanceof PtyCommandLine);
        PtyCommandLine ptyCommand = (PtyCommandLine)command;
        assertFalse(ptyCommand.getPtyOptions().getConsoleMode());
    }

    @Test
    public void terminalEmulationModeEchoesTypedInput() throws Exception {
        GeneralCommandLine command = NovusCommandLineState.interactiveCommandLine(
            new GeneralCommandLine(
                "/bin/sh", "-c",
                "printf 'Prompt: '; IFS= read -r value; printf 'Received: %s\\n' \"$value\""
            )
        );
        Process process = command.createProcess();
        process.getOutputStream().write("visible input\n".getBytes(StandardCharsets.UTF_8));
        process.getOutputStream().flush();

        assertTrue("interactive process did not finish", process.waitFor(5, TimeUnit.SECONDS));
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // The text appears once from terminal echo and once from the program.
        assertTrue(output, output.split("visible input", -1).length - 1 >= 2);
    }
}
