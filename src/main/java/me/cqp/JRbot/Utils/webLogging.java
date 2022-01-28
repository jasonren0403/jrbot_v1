package me.cqp.JRbot.Utils;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import me.cqp.JRbot.modules.signer;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class webLogging implements signer {
    // sort 字母序，逗号分隔键值
    private static final String test_url = "http://localhost:8080/jrbot/logging";
    private static final String weblog_url = "http://xxxx.com/jrbot/logging";
    private String level;  // debug,info,warning,error,fatal

    public static void addLog(String level, String category, String content) {
        addLog(level, category, content, Instant.now().plusMillis(TimeUnit.HOURS.toMillis(8)), false);
    }

    public static void addLog(String level, String category, String content, Instant log_date, boolean test) {
        String finalurl = test ? test_url : weblog_url;
        try {
            JSONObject jbo = webutils.getJSONObjectResp(finalurl, BotConstants.BOT_HEADER, Connection.Method.POST,
                    new HashMap<String, String>() {{
                        put("level", level);
                        put("category", category);
                        put("content", content);
                        put("log_date", log_date.toString());
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject(), new ArrayList<Map<String, String>>() {{
                        add(new HashMap<String, String>() {{
                            put("X-oper-key", new webLogging().generate_signature(level, category, content, log_date.toString()));
                        }});
                    }});
//            System.out.println(jbo.toString(2));
            if (webutils.checkJSONResp(jbo)) {
                // log ok
                System.out.println("Add success");
            }
        } catch (IOException e) {
            String msg = String.format("LOG内容：level:%s, category:%s, content:%s, log_date:%s", level, category, content, log_date.toString());
            CQ.sendPrivateMsg(**********L, "AddLog挂了.jpg" + "\r\n" + msg + "\r\n" +
                    e.toString().replaceAll("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]","[数据删除]"));
            e.printStackTrace();
        }
    }

    @SafeVarargs
    @Override
    public final <T> String generate_signature(T... args) {
        String s = Arrays.stream(args).map(T::toString).sorted().collect(Collectors.joining(""));
        String src = "jason" + s + "nosaj";
        String result = Message_Digest.sha("", src, "SHA-256");
//        System.out.println(s);
//        System.out.println(result);
        return result;
    }

    public static void main(String[] args) {
        Instant t = Instant.now();
        addLog("debug", "Test", "This is a test", t, false);
    }
}
