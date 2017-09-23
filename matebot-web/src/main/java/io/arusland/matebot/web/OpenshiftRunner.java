package io.arusland.matebot.web;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by ruslan on 27.12.2016.
 */
public class OpenshiftRunner {
    public static void run(String envJavaHome, String envAppDir) throws IOException, InterruptedException {
        File javaHome = new File(System.getenv(envJavaHome));
        File appDir = new File(System.getenv(envAppDir), "matebot");

        if (javaHome.exists() && appDir.exists()) {
            File pidFile = new File(appDir, "logs/matebot.pid");

            if (pidFile.exists()) {
                killProcess(pidFile);
            }

            String cmd = javaHome + "/bin/java -jar matebot.jar";
            Runtime.getRuntime()
                    .exec(cmd, null, appDir);

            return;
        }

        throw new RuntimeException("Invalid environment! JAVA_HOME: " + javaHome
                + "; APP_DIR: " + appDir);
    }

    private static void killProcess(File pidFile) {
        try {
            int pid = Integer.parseInt(new String(Files.readAllBytes(pidFile.toPath()), StandardCharsets.UTF_8));
            Runtime.getRuntime().exec("kill -9 " + pid);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void run() throws IOException, InterruptedException {
        run("OPENSHIFT_JBOSSEWS_JDK8", "OPENSHIFT_REPO_DIR");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OpenshiftRunner.run("JAVA_HOME", "APP_DIR");
    }
}
