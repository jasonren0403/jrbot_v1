package me.cqp.JRbot;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.entity.enumerate.Authority;
import org.meowy.cqp.jcq.util.StringUtils;

import java.io.IOException;
import java.util.*;

import static me.cqp.JRbot.Utils.misc.webutils.checkJSONResp;
import static me.cqp.JRbot.Utils.misc.webutils.getToken;
import static me.cqp.Jrbot.*;


public class PersonelConfig {
    public static String welcomeMsg;  //全局欢迎语
    public static String trigger_word; //全局触发词

    public static String usage_main;  //全局使用方法
    public static List<Group> current_groups; //当前进入的群列表
    public static final long developer_qq = ***********L;
    public static long bot_qq;

    public static Map<Long, String> welcome;
    public static Map<Long, String> trigger;
    public static Map<Long, String> exit;

    public static boolean under_maintence;  //if true, do not receive public messages

    /**
     * GET /api/v1/bot/debug/botlist
     *
     * @return an array containing bot_qqids
     */
    public static ArrayList<Long> get_botlist() {
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/botlist",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null,
                    run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                JSONArray ja = resp.getJSONArray("contents");
                ArrayList<Long> arr = new ArrayList<>();
                for (int i = 0; i < ja.length(); i++) {
                    arr.add(ja.getJSONObject(i).getLong("bot_qqid"));
                }
                CQ.logInfoSuccess("Settings::botlist", arr.toString());
                return arr;
            } else {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }


    }

    /**
     * GET /api/v1/bot/group/stat groupid={}
     *
     * @param groupId group to get stat from
     * @return map describing the enabled stat
     */
    public static Map<String, Map<String, Boolean>> getEnabledStatbyGroup(long groupId) {
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/stat",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {
                        {
                            put("groupid", String.valueOf(groupId));
                        }
                    }, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)&&resp.has("contents")&&resp.getJSONArray("contents").length()>0) {
                JSONObject jbo = resp.getJSONArray("contents").getJSONObject(0).getJSONObject("function_stat");
                Iterator<String> it = jbo.keys();
                Map<String, Map<String, Boolean>> map = new HashMap<>();
                while (it.hasNext()) {
                    Map<String, Boolean> boolmap = new HashMap<>();
                    String name = it.next();
                    boolean manage_only = jbo.getJSONObject(name).optBoolean("manage_only", false);
                    boolean enabled = jbo.getJSONObject(name).optBoolean("enabled", true);
                    boolmap.put("manage_only", manage_only);
                    boolmap.put("enabled", enabled);

                    map.put(name, boolmap);
                }
                CQ.logDebug(String.format("GetEnabledStatbyGroup::%d", groupId), map.toString());
                return map;
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            return new HashMap<>();
        }

    }

    public static void reloadSettings() {
        CQ.logInfoSend("Settings", "Reloading settings...");
        run_token = getToken();
        botlist = get_botlist();
        initFromAPI();
        CQ.logInfoSuccess("Settings", "Reload success");
    }


    public static void initSettings() {
        CQ.logInfoSend("Settings", "Init...");
        initFromAPI();
        botlist.addAll(get_botlist());
        current_groups = CQ.getGroupList();

        CQ.logDebug("Settings", "Copyright " + developer_qq);
        CQ.logInfoSuccess("Settings", "Init done");
    }

    /**
     * GET /api/v1/bot/module/querySingle?type=[name|id]&goal=<模块外显名|模块id>&action=full_desc
     *
     * @param module_name 模块内部名称
     * @return String describes the module
     */
    public static String get_module_desc(String module_name, String queryType) {
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/querySingle",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("type", queryType);
                        put("goal", module_name);
                        put("action", "full_desc");
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                StringBuilder sb = new StringBuilder();
                JSONObject base = resp.getJSONObject("contents");
                sb.append("模块介绍").append(StringUtils.lineSeparator).append("模块名称：")
                        .append(base.getString("name")).append(StringUtils.lineSeparator);
                sb.append("开发者：").append(base.getString("developer")).append(StringUtils.lineSeparator);
                sb.append("模块简介：").append(base.getString("introduction"));
                return sb.toString();
            } else {
                CQ.logWarning("Settings::get_module_desc", debug.err_mesg_for_json_resp(resp));
                return "";
            }
        } catch (IOException e) {
            return "";
        }

    }

    /**
     * GET /api/v1/bot/group/modules?groupid={}<&display=[module_name|inner_name]>
     * todo:在api级别支持display=参数
     *
     * @param groupid group id for operation
     * @return available func_list in certain group
     */
    public static List<String> get_group_func_list(long groupid, String display) {
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/module",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {
                        {
                            put("groupid", String.valueOf(groupid));
//                        put("display", display);
                        }
                    }, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                JSONArray ja = resp.getJSONArray("contents");
                List<String> list = new ArrayList<>();
                if ("module_name".equals(display)) {
                    for (int j = 0; j < ja.length(); j++) {
                        list.add(ja.getJSONObject(j).getString("name"));
                    }
                } else if ("inner_name".equals(display)) {
                    for (int j = 0; j < ja.length(); j++) {
                        list.add(ja.getJSONObject(j).getString("module_inner"));
                    }
                } else {
                    for (int k = 0; k < ja.length(); k++) {
                        list.add(ja.getJSONObject(k).getString("name"));
                        list.add(ja.getJSONObject(k).getString("module_inner"));
                    }
                }
                return list;
            } else {
                CQ.logWarning(String.format("Settings::GroupFuncList:%d", groupid), debug.err_mesg_for_json_resp(resp));
                return new ArrayList<>();
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }

    }

    /**
     * GET /api/v1/bot/module/querySingle?type=name&goal=<模块id>&action=full_desc
     *
     * @param inner_name the inner represent of the module("module_id")
     * @return the name of the module("name")
     */
    public static String get_display_name(String inner_name) {
        try {
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/querySingle",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("type", "id");
                        put("goal", inner_name);
                        put("action", "full_desc");
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                String i = jbo.getJSONObject("contents").getString("name");
                CQ.logDebug("Debug::DisplayName", inner_name + "->" + i);
                return i;
            }
            return "";
        } catch (IOException e) {
            return "";
        }

    }

    /**
     * GET /api/v1/bot/module/querySingle?type=name&goal=<模块外显名>&action=full_desc
     *
     * @param display_name the name of the module("name")
     * @return the inner represent of the module("module_id")
     */
    public static String get_inner_name(String display_name) {
        try {
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/querySingle",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("type", "name");
                        put("goal", display_name);
                        put("action", "full_desc");
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                String i = jbo.getJSONObject("contents").getString("id");
                CQ.logDebug("Debug::InnerName", display_name + "->" + i);
                return i;
            }
            return "";
        } catch (IOException e) {
            return "";
        }

    }

    /**
     * POST /api/v1/bot/group/module/statChange fromGroup={}&fun_name={}&action=[enable|disable|make_admin|make_pub]
     * <p>
     * or POST /api/v1/bot/module/[install|uninstall] fromGroup = {}&module_name={}
     *
     * @param fun_name  the name of the function to change stat
     * @param fromGroup the QQ group where stat to be changed
     * @param action    the action to be executed,
     *                  if this is not an empty string, the other param is ignored
     * @param install   true if the module is going to be installed, false uninstalled
     * @return if the state successful change
     */
    //此处的fun_name就是inner_name
    public static boolean change_func_state(String fun_name, long fromGroup, boolean install, String action) {
        Map<String, String> map = new HashMap<>();
        String url;
        map.put("fromGroup", String.valueOf(fromGroup));
        if (fun_name.isEmpty()) {
            CQ.logInfo("BotSettings::change_state", "fun_name is empty!");
            return false;
        }
        if (!action.isEmpty()) {
            map.put("fun_name", fun_name);
            map.put("action", action);
            url = "http://xxxx.com/jrbot/group/statChange";
        } else {
            map.put("module_name", fun_name);
            if (install) {
                url = "http://xxxx.com/jrbot/module/install";
            } else {
                url = "http://xxxx.com/jrbot/module/uninstall";
            }
        }
        try {
            JSONObject resp = webutils.getJSONObjectResp(url, BotConstants.BOT_HEADER, Connection.Method.POST, map, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                CQ.logInfo("BotSettings:GroupStatChange", "success");
                return true;
            } else if (resp.getInt("error_code") == 40025) {
                CQ.sendGroupMsg(fromGroup, "已经是这个状态了，别再动一遍了~~");
                return false;
            } else {
                CQ.logWarning("BotSettings:GroupStatChange", debug.err_mesg_for_json_resp(resp));
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * GET /api/v1/bot/debug/stat
     * GET /api/v1/bot/group/stat
     */
    private static void initFromAPI() {
        CQ.logDebug("Settings", "Reading from config...");
        try {
            JSONObject bot_main = webutils.getJSONObjectResp("http://xxxx.com/jrbot/stat",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED
                    , new JSONObject());
            if (webutils.checkJSONResp(bot_main)) {
                JSONObject group_main = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/stat",
                        BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token,
                        BotWebMethods.URLENCODED, new JSONObject());
                if (webutils.checkJSONResp(group_main)) {
                    JSONObject bot_base_json = bot_main.getJSONObject("content");
                    welcomeMsg = bot_base_json.getString("default_welcome_message");
                    trigger_word = bot_base_json.getString("default_trigger");
                    String current_lan = bot_base_json.optString("current_language", "zh-cn");
                    usage_main = bot_base_json.getJSONObject("usage_main").getString(current_lan);
                    bot_qq = CQ.getLoginQQ();
                    welcome = getWelcome();
                    trigger = getTrigger();
                    exit = getExitMsg();
                    under_maintence = bot_base_json.optBoolean("is_silent", true);
                } else {
                    CQ.logError("Settings::main", "Cannot get group main setting, a restart or retry is required.");
                    CQ.logError("Settings::main", "response: " + group_main.toString());
                }
            } else {
                CQ.logError("Settings::main", "Cannot get bot main setting, a restart or retry is required.");
                CQ.logError("Settings::main", "response: " + bot_main.toString());
            }
            CQ.logDebug("Settings", "Read over.");
        } catch (IOException e) {
            CQ.logError("Settings::init", e.getMessage());
        }

    }

    /**
     * GET /api/v1/bot/module/List
     *
     * @return A list of available module for installing
     */
    public static List<String> readSupportedInstallList() {
        try {
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/List",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null,
                    run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (!webutils.checkJSONResp(jbo)) {
                webLogging.addLog("warning","Settings::supportedInstallListError", debug.err_mesg_for_json_resp(jbo));
                return new ArrayList<>();
            }
            JSONArray ja = jbo.getJSONObject("content").getJSONArray("available_module_list");
            webLogging.addLog("debug","Settings::SupportedInstallList", ja.toString());
            List<Object> list = ja.toList();
            List<String> list2 = new ArrayList<>();
            for (Object o : list) {
                list2.add(String.valueOf(o));
            }
            return list2;
        } catch (IOException e) {
            return new ArrayList<>();
        }

    }

    /**
     * POST /api/v1/bot/group/triggerEdit fromGroup={}&newTrigger={}
     *
     * @param fromgroup the group where bot stays in
     * @param Trigger   the new "trigger word" to be set
     */
    public static boolean setGroupTrigger(long fromgroup, String Trigger) {
        if (Trigger.trim().equals("#默认") || Trigger.trim().equals("--default") || Trigger.trim().equals("/default")) {
            Trigger = "";
        }
        String finalTrigger = Trigger;
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/triggerEdit",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("fromGroup", String.valueOf(fromgroup));
                        put("newTrigger", finalTrigger.isEmpty() ? trigger_word : finalTrigger);
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                trigger = getTrigger();
                webLogging.addLog("info","GroupManage::TriggerEdit", fromgroup + "'s trigger word has changed to " + (finalTrigger.isEmpty() ? trigger_word : finalTrigger));
                return true;
            } else {
                webLogging.addLog("warning","GroupManage::TriggerEdit", debug.err_mesg_for_json_resp(resp));
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * POST /api/v1/bot/group/welcomeEdit fromGroup={}&newWelcome={}
     *
     * @param fromgroup the group where bot stays in
     * @param Msg       the new welcome message to be set
     */
    public static boolean setGroupWelcomeMsg(long fromgroup, String Msg) {
        if (Msg.trim().equals("#默认") || Msg.trim().equals("--default") || Msg.trim().equals("/default")) {
            Msg = "";
        }
        String finalMsg = Msg;
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/welcomeEdit",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("fromGroup", String.valueOf(fromgroup));
                        put("newWelcome", finalMsg.isEmpty() ? welcomeMsg : finalMsg);
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                welcome = getWelcome();
                webLogging.addLog("info","GroupManage::WelcomeEdit", fromgroup + "'s welcome message has changed to " + (finalMsg.isEmpty() ? trigger_word : finalMsg));
                return true;
            } else {
                webLogging.addLog("warning","GroupManage::WelcomeEdit", debug.err_mesg_for_json_resp(resp));
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * GET /api/v1/bot/group/stat/welcome
     *
     * @return map(fromGroup, welcomeMessage)
     */
    public static Map<Long, String> getWelcome() {
        Map<Long, String> cmap = new HashMap<>();
        try {
            JSONObject j = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/stat/welcome",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(j)) {
                JSONArray arr = j.getJSONArray("contents");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    if (!c.getString("welcome_message").isEmpty()) {
                        cmap.put(c.getLong("groupid"), c.getString("welcome_message"));
                    } else {
                        cmap.put(c.getLong("groupid"), welcomeMsg);
                    }
                }
                CQ.logInfo("Settings::GetWelcome", String.format("successfully get welcome map: %s", cmap.toString()));
                return cmap;
            } else {
                webLogging.addLog("warning","Settings::GetWelcome", debug.err_mesg_for_json_resp(j));
                return new HashMap<>();
            }

        } catch (IOException e) {
            return cmap;
        }
    }

    /**
     * GET /api/v1/bot/group/stat/triggers
     *
     * @return map(fromGroup, trigger)
     */
    public static Map<Long, String> getTrigger() {
        Map<Long, String> cmap = new HashMap<>();
        try {
            JSONObject j = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/stat/triggers",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(j)) {
                JSONArray arr = j.getJSONArray("contents");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    if (!c.getString("grouptrigger").isEmpty()) {
                        cmap.put(c.getLong("groupid"), c.getString("grouptrigger"));
                    } else {
                        cmap.put(c.getLong("groupid"), trigger_word);
                    }

                }
                CQ.logInfo("Settings::GetTrigger", String.format("successfully get trigger map: %s", cmap.toString()));
                return cmap;
            } else {
                webLogging.addLog("warning","Settings::GetTrigger", debug.err_mesg_for_json_resp(j));
                return new HashMap<>();
            }
        } catch (IOException e) {
            return cmap;
        }

    }

    /**
     * GET /api/v1/bot/group/stat/exitmsg
     *
     * @return map(fromGroup, exitmsg)
     */
    public static Map<Long, String> getExitMsg() {
        Map<Long, String> cmap = new HashMap<>();
        try {
            JSONObject j = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/stat/exitmsg",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(j)) {
                JSONArray arr = j.getJSONArray("contents");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject c = arr.getJSONObject(i);
                    cmap.put(c.getLong("groupid"), c.optString("exit_message", ""));
                }
                CQ.logInfo("Settings::GetExitMsg", "successfully get exitmsg map: {}", cmap.toString());
                return cmap;
            } else {
                webLogging.addLog("warning","Settings::GetExitMsg", debug.err_mesg_for_json_resp(j));
                return new HashMap<>();
            }
        } catch (IOException ioex) {
            return cmap;
        }

    }

    /**
     * POST /api/v1/bot/group/exitmsg fromGroup = {} &newExitMsg = {}
     */
    public static boolean setExitMsg(long fromGroup, String newExitMsg) {
        try {
            JSONObject j = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/exitmsg",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("fromGroup", String.valueOf(fromGroup));
                        put("newExitMsg", newExitMsg);
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(j)) {
                webLogging.addLog("info","Settings::SetExitMsg", String.format("successfully set exitmsg [%s] to group %d", newExitMsg, fromGroup));
                exit = getExitMsg();
                return true;
            } else {
                webLogging.addLog("warning","Settings::SetExitMsg", debug.err_mesg_for_json_resp(j));
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * POST /api/v1/bot/group/delete groupid = {value}
     *
     * @param gid group id for dropout
     */
    public static void prepare_for_group_dropout(long gid) {
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/delete",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("groupid", String.valueOf(gid));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                CQ.logInfo("GroupInfo::GroupDelete", "success");
            } else {
                webLogging.addLog("warning","GroupInfo::GroupDelete", debug.err_mesg_for_json_resp(resp));
            }
        } catch (IOException iodex) {
            webLogging.addLog("warning","GroupInfo::GroupDelete", "You may clean group " + gid + "'s config by yourself");
        }
    }

    /**
     * GET /api/v1/bot/group/isActivated?fromGroup={}
     * @param gid the group id
     * @return whether the action is successful
     */
    public static boolean is_group_activated(long gid){
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/isActivated",
                    BotConstants.BOT_HEADER, Connection.Method.GET,new HashMap<String,String>(){{
                        put("fromGroup",String.valueOf(gid));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            if(webutils.checkJSONResp(resp)){
                return resp.getJSONObject("contents").getBoolean("activated");
            }
            return false;
        }
        catch(IOException ioex){
            CQ.logWarning("groupActivate",gid + " activation failed!");
            return false;
        }
    }

    /**
     * POST /api/v1/bot/group/activate fromGroup = {}
     * @param gid the group id
     * @return whether the action is successful
     */
    public static boolean activate_group(long gid){
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/activate",
                    BotConstants.BOT_HEADER, Connection.Method.POST,new HashMap<String,String>(){{
                        put("fromGroup",String.valueOf(gid));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            if(!webutils.checkJSONResp(resp)&&resp.optInt("error_code",0)==40025){
                CQ.sendGroupMsg(gid,"已经激活过了~");
            }
            return webutils.checkJSONResp(resp);
        }
        catch(IOException ioex){
            webLogging.addLog("warning","GroupInfo::Activate",gid + " activation failed!");
            return false;
        }
    }

    /**
     * POST /api/v1/bot/group/add
     * {
     * groupid:xxx,
     * groupname:yyy,
     * grouptrigger:"",
     * welcome_message:"",
     * exit_message:"",
     * function_stat:{}
     * }
     *
     * @param p group to add
     */
    public static boolean prepare_for_new_group(Group p) {
        JSONObject newgroup = new JSONObject();
        JSONObject function_state = new JSONObject();
        HashMap<String, Boolean> fun = new HashMap<>();
        fun.put("enabled", true);
        fun.put("manage_only", true);
        function_state.put("base", fun);
        function_state.put("group_manage", fun);
        newgroup.put("groupid", p.getId());
        newgroup.put("groupname", p.getName());
        newgroup.put("grouptrigger", "");
        newgroup.put("welcome_message", "");
        newgroup.put("exit_message", "");
        newgroup.put("function_stat", function_state);

        try {
            JSONObject j = webutils.getJSONObjectResp("http://xxxx.com/jrbot/group/add",
                    BotConstants.BOT_HEADER, Connection.Method.POST, null, run_token, BotWebMethods.JSON, newgroup);
            if (webutils.checkJSONResp(j)) {
                if (j.getJSONObject("inserted_group").getInt("groupid") == p.getId()) {
                    trigger = getTrigger();
                    welcome = getWelcome();
                    exit = getExitMsg();
                    webLogging.addLog("info","GroupInfo::GroupAdd", "New group information added:"+p.toString());
                    return true;
                } else {
                    CQ.logWarning("GroupInfo::GroupAdd", "emmm...something went wrong");
                    return false;
                }
            } else {
                webLogging.addLog("warning","GroupInfo::GroupAdd", debug.err_mesg_for_json_resp(j));
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 判断bot可管理当前群，依据为bot的authority级别大于member的级别且member为一个普通群成员。
     *
     * @param fromGroup      来源群
     * @param beingoperateQQ 被操作qq
     * @param operateQQ      操作人qq(使用命令辅助管理的人)
     * @return bot可管理当前群-true
     */
    public static boolean isBotManageable(long fromGroup, long beingoperateQQ, long operateQQ) {
        if (beingoperateQQ == -1) return true;
        Member operator = CQ.getGroupMemberInfo(fromGroup, operateQQ,true);
        Member m1 = CQ.getGroupMemberInfo(fromGroup, beingoperateQQ,true);
        Member bot;
        if (operateQQ == bot_qq) {
            bot = operator;
        } else {
            bot = CQ.getGroupMemberInfo(fromGroup, PersonelConfig.bot_qq,true);
        }

        if (m1 == null)
            return !(bot.getAuthority() == Authority.MEMBER) && !(operator.getAuthority() == Authority.MEMBER);

        return bot.getAuthority().value() > m1.getAuthority().value() && m1.getAuthority() == Authority.MEMBER && operator.getAuthority().value() > 1;
    }

    /**
     * POST /api/v1/bot/debug/stat/silent
     *
     * @param close if true, stop receiving message from group
     */
    public static boolean toggle_pub_msg(boolean close) {
        try {
            JSONObject jbo = webutils.getJSONObjectResp("https://********.**********.***/" +
                            "api/v1/jrbot/stat/silent", BotConstants.BOT_HEADER, Connection.Method.POST,
                    new HashMap<String, String>() {{
                        put("val", String.valueOf(close));
                    }},
                    run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                under_maintence = close;
                CQ.logInfoRecv("Bot_RECV_PUBMSG", "Turned off: {}", close);
                return true;
            } else {
                CQ.logWarning("Bot_RECV_PUBMSG", "Failed to perform action!");
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    public static boolean ModuleCanUse(long fromGroup, long fromQQ, String module_name) {
        if (fromQQ == developer_qq) return true;  // developer can always use the module without installing module
        Member m = CQ.getGroupMemberInfo(fromGroup, fromQQ,true);
        if (m == null) return false;  // the member is not valid member of a group
        List<String> l = get_group_func_list(fromGroup, "module_inner");
        if (!l.contains(module_name)) return false;  // not a valid module in group
        Map<String, Map<String, Boolean>> c = getEnabledStatbyGroup(fromGroup);
        if (!getEnabledState(c, module_name)) return false; // the state of a module is not open
        switch (m.getAuthority()) {
            case MEMBER:
            default:
                return !getManageOnlyState(c, module_name);
            case ADMIN:
            case OWNER:
                return true;
        }
    }

    protected static boolean getEnabledState(Map<String, Map<String, Boolean>> map, String fun_name) {
        Map<String, Boolean> bmap = map.get(fun_name);
        return bmap != null ? bmap.getOrDefault("enabled", false) : false;
    }

    protected static boolean getManageOnlyState(Map<String, Map<String, Boolean>> map, String fun_name) {
        Map<String, Boolean> bmap = map.get(fun_name);
        return bmap != null ? bmap.getOrDefault("manage_only", false) : false;
    }

    /**
     * POST /api/v1/bot/debug/extend-setting
     * json {"type":"module|base"[(type=module时),"name":""],"content":{"f1o":"bar","bar":4}}
     */
    public static boolean extend_capability(String type, String name, Map<String, Object> kvs) {
        if (kvs == null || kvs.isEmpty()) return false;
        JSONObject c = new JSONObject();
        c.put("type", type);
        if ("module".equals(type)) {
            c.put("name", name);
        }
        JSONObject jbo = new JSONObject();
        for (Map.Entry<String, Object> entry : kvs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jbo.accumulate(key, value);
        }

        c.put("content", jbo);
        try {
            JSONObject resp = webutils.getJSONObjectResp("http://localhost:3001" +
                            "/api/v1/bot/debug/extend-setting", BotConstants.BOT_HEADER, Connection.Method.POST,
                    null, run_token, BotWebMethods.JSON, c);
            if (checkJSONResp(resp)) {
                CQ.logInfo("Add Capability", "success");
                return true;
            } else {
                CQ.logWarning("Add Capability", "error: {}", resp.toString());
                return false;
            }
        } catch (IOException e) {
            webLogging.addLog("warning","Settings::extend-capability", e.toString());
            return false;
        }

    }

    public static boolean isBot(long targetqq) {
        return botlist.contains(targetqq);
    }

}
