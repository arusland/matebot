package io.arusland.ctl;

import io.javalin.BasicAuthCredentials;
import io.javalin.Javalin;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author Ruslan Absalyamov
 * @since 2017-11-08
 */
public class MateBotControl {
    private static final String HARDCODED_PASSWORD = "90036c98e0979ef73ddb80566745e99ccfdd9f336d59b41c553cc300a007857c17a6aafed98565b49269c65db1419c2a5df518757da89131affb15d4dc0bb9dc";
    private static final Logger log = LoggerFactory.getLogger(MateBotControl.class);

    public static void main(String[] args) {
        Javalin app = Javalin.start(7000);
        app.get("/restart", ctx -> {
            if (!new File("restart.sh").exists()) {
                ctx.result("Not found: restart.sh");
                return;
            }

            BasicAuthCredentials basicAuthCredentials = ctx.basicAuthCredentials();

            if (basicAuthCredentials == null ||
                    !HARDCODED_PASSWORD.equals(DigestUtils.sha512Hex(basicAuthCredentials.getPassword()))) {
                ctx.status(401)
                        .header("WWW-Authenticate", "Basic realm=\"Password for matebot.io :)\"")
                        .result("Unauthorized guest!");
                return;
            }

            ctx.renderMustache("/templates/mustache/restart.mustache");

            log.info("Show restart form");
        }).post("/restart", ctx -> {
            log.info("Restarting...");

            ExecUtils.runCommand("sh", "restart.sh");

            ctx.redirect("/restart", 302);
        });
    }
}
