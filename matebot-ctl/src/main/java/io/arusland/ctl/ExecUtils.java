package io.arusland.ctl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Ruslan Absalyamov
 * @since 03.04.17
 */
public class ExecUtils {
    public static String runCommand(String... cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        StringBuilder sb = new StringBuilder();

        sb.append("Stdout:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            sb.append(s);
            sb.append("\n");
        }

        boolean captionPrinted = false;

        while ((s = stdError.readLine()) != null) {
            if (!captionPrinted) {
                captionPrinted = true;
                sb.append("\nStdError:\n");
            }

            sb.append(s);
            sb.append("\n");
        }

        return sb.toString();
    }
}
