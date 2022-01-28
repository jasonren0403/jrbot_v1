package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.misc.DateUtils;
import me.cqp.JRbot.Utils.Message_Digest;
import me.cqp.JRbot.Utils.misc.info;
import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.QQInfo;
import org.meowy.cqp.jcq.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static me.cqp.Jrbot.CQ;

public class CloudBlocklist implements BaseModule, signer {

    private static final CloudBlocklist cbl = new CloudBlocklist();

    private long session_group = 0L;

    private CloudBlocklist() {
    }

    public static CloudBlocklist getInstance() {
        return cbl;
    }

    @SafeVarargs
    public final <T> String generate_signature(T... args) {
        String s = Arrays.stream(args).map(T::toString).sorted().collect(Collectors.joining(""));
        String src = "jason" + s + "nosaj";
        String result = Message_Digest.sha("", src, "SHA-256");
//        System.out.println(s);
//        System.out.println(result);
        return result;
    }

    public CloudBlocklist addSession(long group_or_private) {
        this.session_group = group_or_private;
        return this;
    }

    public CloudBlocklist clearSession() {
        this.session_group = 0;
        return this;
    }

    /**
     * POST /jrbot/blocklist
     * id=?&type=?&warn_count=1&is_banned=false&ban_time_start_day={}&ban_time_finish_day={}&ban_reason={}
     *
     * @param target groupnum or qqid
     * @param type   one of("personel","group","all")
     */
    boolean addBlockData(long target, String type, boolean ban) {
        int warn_count = 1;
        String ban_time_start_day = DateUtils.getDateStr();
        String ban_time_finish_day = DateUtils.getDateStr(3);
        try {
            JSONObject jbo = webutils.getJSONObjectResp(apiEndpointName(), BotConstants.BOT_HEADER, Connection.Method.POST,
                    new HashMap<String, String>() {{
                        put("id", String.valueOf(target));
                        put("type", type);
                        put("warn_count", String.valueOf(warn_count));
                        put("is_banned", String.valueOf(ban));
                        put("ban_time_start_day", ban_time_start_day);
                        put("ban_time_finish_day", ban_time_finish_day);
                        put("ban_reason", "");
                    }}, null, BotWebMethods.URLENCODED, new JSONObject(), new ArrayList<Map<String, String>>() {{
                        add(new HashMap<String, String>() {{
                            put("X-oper-key", generate_signature(target, type, warn_count, ban, ban_time_start_day, ban_time_finish_day, ""));
                        }});
                    }});
//            System.out.println(jbo.toString(2));
            if (webutils.checkJSONResp(jbo)) {
                System.out.println("Add Success!");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * PUT /jrbot/blocklist/{type}/{id}
     * operation = incr_warning
     */
    boolean incr_warning(long target, String type) {
        try {
            JSONObject jbo = webutils.getJSONObjectResp(apiEndpointName() + "/" + type + "/" + target,
                    BotConstants.BOT_HEADER, Connection.Method.PUT, new HashMap<String, String>() {{
                        put("operation", "incr_warning");
                    }}, null, BotWebMethods.URLENCODED, new JSONObject(), new ArrayList<Map<String, String>>() {{
                        add(new HashMap<String, String>() {{
                            put("X-oper-key", generate_signature("incr_warning"));
                        }});
                    }});
//            System.out.println(jbo.toString(2));
            if (webutils.checkJSONResp(jbo)) {
                System.out.println("incr success");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * DELETE /jrbot/blocklist/{type}/{id}
     *
     * @param target
     * @param type
     * @return
     */
    boolean delBlockData(long target, String type) {
        try {
            JSONObject jbo = webutils.getJSONObjectResp(apiEndpointName() + "/" + type + "/" + target,
                    BotConstants.BOT_HEADER, Connection.Method.DELETE,
                    null, null, BotWebMethods.URLENCODED, new JSONObject(), new ArrayList<Map<String, String>>() {{
                        add(new HashMap<String, String>() {{
                            put("X-oper-key", generate_signature(target, type, "delete_block"));
                        }});
                    }});
            // TODO: 2020/7/30 issue: jbo always become {} cannot sure whether successfully deleted 
//            System.out.println(jbo.toString(2));
//            if(webutils.checkJSONResp(jbo)){
//                System.out.println("delete success");
//                return true;
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /jrbot/blocklist?limit=5&offset=0
     *
     * @return <target, abuse_type>
     */
    Map<Long, String> getAbuseInfo(long target, int limit, int offset) {
        Map<Long, String> map = new HashMap<>();
        try {
            JSONObject jbo = webutils.getJSONObjectResp(apiEndpointName(), BotConstants.BOT_HEADER, Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("target", String.valueOf(target));
                        put("limit", String.valueOf(limit));
                        put("offset", String.valueOf(offset));
                    }}, null, BotWebMethods.URLENCODED, new JSONObject());
//            System.out.println(jbo.toString(2));
            if (webutils.checkJSONResp(jbo)) {
                JSONArray contents = jbo.getJSONArray("contents");
                if (contents.length() > 0) {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject current = contents.getJSONObject(i);
                        if (current.optLong("id") == target) {
                            map.put(target, current.getString("type"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * GET /jrbot/blocklist?type={}&id={}
     */
    Map<String, Integer> getAbuseInfo(long target, String type) {
        Map<String, Integer> map = new HashMap<>();
        try {
            JSONObject jbo = webutils.getJSONObjectResp(apiEndpointName(), BotConstants.BOT_HEADER, Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("type", type);
                        put("id", String.valueOf(target));
                    }}, null, BotWebMethods.URLENCODED, new JSONObject());
//            System.out.println(jbo.toString(2));
            if (webutils.checkJSONResp(jbo)) {
                JSONArray contents = jbo.getJSONArray("contents");
                if (contents.length() > 0) {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject current = contents.getJSONObject(i);
                        if (current.optLong("id") == target) {
                            if (current.getInt("is_banned") == 1) {
                                map.put("banned", 1);
                            } else {
                                map.put("warned", current.optInt("warn_count", 0));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * There is a change definition of returning value here
     *
     * @return 0 - No problem with the target
     * 1 - abuse has been found on the target
     */
    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        if (directives.size() == 0) return 0;
        String command = directives.get(0);
        int retval = 0;
        try {
            Map<String, Integer> m;
            QQInfo f = null;
            Group p = null;
            if (directives.contains("personel")) {
                f = info.getStrangerInfo(Long.parseLong(directives.get(1)));
                CQ.logDebug("CloudBlock",f.toString());
                directives.remove("personel");
            } else if (directives.contains("group")) {
                p = info.getGroupInfo(Long.parseLong(directives.get(1)));
                CQ.logDebug("CloudBlock",p.toString());
                directives.remove("group");
            }
            if (f == null && p == null) {
                // please specify a valid id!
                if (this.session_group != 0) CQ.sendGroupMsg(this.session_group, "请指定合法的群号/qq号！");
                return 0;
            }
            m = getAbuseInfo(Long.parseLong(directives.get(2)), directives.get(1));
            Set<Map.Entry<String, Integer>> c = m.entrySet();
            String s = "";
            int t = 0;
            for (Map.Entry<String, Integer> me : c) {
                s = me.getKey();
                t = me.getValue();
            }
            switch (command) {
                case "check-abuse":
                    // usage: check-abuse [group|personel] <targetId>
                    if (m.size() > 0) retval = 1;
                    if (this.session_group != 0) {
                        if (retval == 1) {
                            String addtional = "状态：" + ((s.equals("banned") ? "封禁" : "警告") + ((s.equals("banned")) ? "" : t + "次"));
                            CQ.sendGroupMsg(this.session_group, "此用户/群目前在黑名单中！具体原因请查询黑名单网页" + StringUtils.lineSeparator + addtional);
                        } else CQ.sendGroupMsg(this.session_group, "恭喜！今后还请保持正常使用哦！");
                    }
                    break;
                case "add-new":
                    // add-new [group/personel] <targetId>
                    if (this.session_group != 0) {
                        if (m.size() > 0)   //already exists!
                            CQ.sendGroupMsg(this.session_group, "此用户/群已在黑名单中");
                        else {
                            boolean result = this.addBlockData(Long.parseLong(directives.get(1)), f != null ? "personel" : "group", false);
                            if (result)
                                CQ.sendGroupMsg(this.session_group, "添加成功！");
                            else CQ.sendGroupMsg(this.session_group, "添加失败！");
                        }
                    }else if(m.size()==0){
                        this.addBlockData(Long.parseLong(directives.get(1)), f != null ? "personel" : "group", false);
                    }
                    retval = 1;
                    break;
                case "promote-warning":
                    // usage: promote-warning [group|personel] <targetId>
                    if(this.session_group !=0){
                        if(m.size()==0) CQ.sendGroupMsg(this.session_group,"请先添加！");
                        else {
                            if(this.incr_warning(Long.parseLong(directives.get(1)),f != null ? "personel" : "group"))
                                CQ.sendGroupMsg(this.session_group, "ok");
                            else CQ.sendGroupMsg(this.session_group, "failed");
                            retval = 1;
                        }
                    }else if(m.size()!=0){
                        this.incr_warning(Long.parseLong(directives.get(1)),f != null ? "personel" : "group");
                        retval = 1;
                    }
                    break;
                case "delete-block":
                    // usage: delete-block
                    if(this.session_group != 0){
                        if(m.size()==0)  // not exist
                            CQ.sendGroupMsg(this.session_group,"此用户/群已经不在黑名单中");
                        else{
                            this.delBlockData(Long.parseLong(directives.get(1)),f!=null? "personel" : "group");
                            CQ.sendGroupMsg(this.session_group,"ok");
                        }
                    }else{
                        this.delBlockData(Long.parseLong(directives.get(1)),f!=null? "personel" : "group");
                    }
                    retval = 0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return retval;
    }

    @Override
    public String apiEndpointName() {
        return "http://xxxx.com/jrbot/blocklist";
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.blocklist";
    }
}
