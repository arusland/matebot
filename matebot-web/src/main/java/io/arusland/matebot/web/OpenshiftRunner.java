package io.arusland.matebot.web;

import java.io.File;
import java.io.IOException;

/**
 * Created by ruslan on 27.12.2016.
 */
public class OpenshiftRunner {
    public static void run(String envJavaHome, String envAppDir) throws IOException, InterruptedException {
        File javaHome = new File(System.getenv(envJavaHome));
        File appDir = new File(System.getenv(envAppDir), "matebot");

        if (javaHome.exists() && appDir.exists()) {
            String cmd = javaHome + "/bin/java -jar matebot.jar";
            Process p = Runtime.getRuntime()
                    .exec(cmd, null, appDir);

            return;
        }

        throw new RuntimeException("Invalid environment! JAVA_HOME: " + javaHome
                + "; APP_DIR: " + appDir);
    }

    public static void run() throws IOException, InterruptedException {
        run("OPENSHIFT_JBOSSEWS_JDK8", "OPENSHIFT_REPO_DIR");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OpenshiftRunner.run("JAVA_HOME", "APP_DIR");
    }
}
