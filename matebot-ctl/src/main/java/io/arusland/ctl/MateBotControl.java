package io.arusland.ctl;

import io.javalin.Javalin;

import java.io.File;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-08
 */
public class MateBotControl {
    public static void main(String[] args) {
        Javalin app = Javalin.start(7000);
        app.get("/restart", ctx ->  {
            if (!new File("restart.sh").exists()) {
                ctx.result("Not found: restart.sh");
                return;
            }

            ExecUtils.runCommand("sh", "restart.sh");
            ctx.result("OK");
        });
    }
}
