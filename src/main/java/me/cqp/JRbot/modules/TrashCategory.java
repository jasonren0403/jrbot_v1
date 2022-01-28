package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class TrashCategory implements BaseModule {

    private final long fromGroup;
    private final long fromQQ;

    public TrashCategory(long fromGroup, long fromQQ) {
        this.fromGroup = fromGroup;
        this.fromQQ = fromQQ;
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        MsgBuffer mb = new MsgBuffer();
        directives.remove("trash_category");
        directives.remove("垃圾分类");
        String message = "";
        StringBuilder sb = new StringBuilder();
        for (String s : directives) {
            sb.append(trash_category_inner(s));
            sb.append(StringUtils.lineSeparator);
        }
        message = sb.toString();
        if (message.isEmpty()) {
            mb.setCoolQ(CQ).setTarget(fromGroup).append(helpMsg()).sendGroupMsg();
        } else {
            mb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append(message).sendGroupMsg();
        }
        return 1;
    }


    private String trash_category_inner(String trash) {
        if (trash.equals("")) return "";
        String[] categories = new String[]{"可回收垃圾", "有害垃圾", "湿垃圾", "干垃圾"};
        String message = "";
        try {
            JSONObject jbo = webutils.getJSONObjectResp("xxxxx",
                    BotConstants.WEB_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {
                        {
                            put("garbageName", trash);
                        }
                    }, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (!"success".equals(jbo.getString("msg"))) {
                String random_trash = categories[new Random().nextInt(categories.length)];
                message = trash + "是" + random_trash + "（我瞎说的";
            } else {
                //默认code为200
                message += trash + "是";
                //System.out.println(jbo.toString(3));
                assert (jbo.getInt("code") == 200);
                JSONArray newslist = jbo.getJSONArray("data");
                int len = newslist.length();
                int[] count = new int[4];
                for (int i = 0; i < len; i++) {
                    JSONObject jb = newslist.getJSONObject(i);
                    String type = jb.optString("gtype", "");
                    int index;
                    for (int j = 0; j < categories.length; j++) {
                        if (categories[j].equals(type)) {
                            index = j;
                            count[index]++;
                            break;
                        }
                    }

                }
                int max = 0, max_index = 0;
                for (int j = 0; j < 4; j++) {
                    if (max < count[j]) {
                        max_index = j;
                        max = count[j];
                    }
                }
                String atrash = categories[max_index];
                message += atrash;
            }
            return message;
        } catch (Exception e) {
            CQ.logFatal("trash_category", e.getMessage());
            String random_trash = categories[new Random().nextInt(categories.length)];
            message = trash + "是" + random_trash + "（我瞎说的";
            return message;
        }
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.trash_category";
    }
}
