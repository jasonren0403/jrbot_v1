package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static me.cqp.Jrbot.run_token;
import static me.cqp.JRbot.Utils.misc.webutils.checkJSONResp;

public interface BaseModule {

    enum DirProcessState {
        PROCESS_DIR, PROCESS_PARAM, END
    }

    /**
     * The description of the module
     *
     * @return String representation of the description
     */
    default String desc() {
        return "[" + name() + "]" + version();
    }

    /**
     * How the module's help is formed.
     * default implementation: (nodejs webapi)
     * GET /api/v1/bot/module/querySingle?type=name&goal=<模块id>&action=show_help
     *
     * @return the help message or "no help specified"
     */
    default String helpMsg() {
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/querySingle",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("type", "id");
                        put("goal", name().replace("me.cqp.jrbot.", ""));
                        put("action", "show_help");
                    }}, run_token, BotWebMethods.URLENCODED
                    , new JSONObject());
            if (checkJSONResp(resp)) {
                String help = resp.getJSONObject("contents").getString("help");
                if (help.isEmpty()) help = "no help specified";
                else help += StringUtils.lineSeparator
                        + "详见" + helpUrl();
                return help.replace("\\n", StringUtils.lineSeparator).replace("\\r\\n", StringUtils.lineSeparator)
                        .replace("\\t", "\t");
            } else {
                return "no help specified";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "no help specified";
        }
    }

    /**
     * Go through the module
     *
     * @return 1 if the module have outputs
     */
    int processDirectives(@Nonnull List<String> directives);

    /**
     * The version of the bot module
     *
     * @return the number version
     */
    default double version() {
        return 1.0;
    }

    /**
     * The name of the module
     *
     * @return the name(with me.cqp domain)
     */
    String name();

    /**
     * The root api endpoint that the module use
     *
     * @return the String representation of the api endpoint
     */
    default String apiEndpointName() {
        return String.format("%s:%d/api/v%d/bot/", BotConstants.BOT_ROOT, BotConstants.LISTEN_PORT, BotConstants.API_VERSION);
    }

    /**
     * The url of help document describing the module
     *
     * @return where online help document in
     */
    default String helpUrl() {
        return String.format("%s/%s/index.html", BotConstants.HELP_ROOT, name().replace("me.cqp.jrbot.", ""));
    }
}
