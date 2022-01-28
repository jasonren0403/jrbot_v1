package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.message.MsgBuilder;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class Repeat implements BaseModule {
    private static Map<Long,Queue<String>> queueMap = new HashMap<>();
    public static double repeat_pro;
    public static double break_repeat_pro;
    private static final String setting_url = "http://xxxx.com/jrbot/module/repeat/settings";

    static {
        List<Group> l = CQ.getGroupList();
        for(Group p:l){
            queueMap.put(p.getId(),new LinkedList<>());
        }
        JSONObject jbo = null;
        try {
            jbo = webutils.getJSONObjectResp(
                    setting_url,
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    null,
                    run_token,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );
            if (webutils.checkJSONResp(jbo)) {
                repeat_pro = jbo.getJSONObject("content").optDouble("repeat_probability", 0.1);
                break_repeat_pro = jbo.getJSONObject("content").optDouble("break_repeat_probability", 0.05);
            } else {
                repeat_pro = 0.1;  //0.1 by default
                break_repeat_pro = 0.5;  // 0.5 by default, note that it is the represent of P(break-repeat|Repeat)
                System.out.println("Init probability failed! Use 0.1/0.05 for fallback");
            }
        } catch (IOException e) {
            repeat_pro = 0.1;  //0.1 by default
            break_repeat_pro = 0.5;  // 0.5 by default, note that it is the represent of P(break-repeat|Repeat)
            CQ.logError("Repeat::Init","failed to init repeat module because {}",e.toString());
        }

    }

    enum RepeatDisposal {
        PROCESS_REPEAT, BREAK_REPEAT
    }

    public static void reinit(){
        List<Group> l = CQ.getGroupList();
        for(Group p:l){
            queueMap.put(p.getId(),new LinkedList<>());
        }
    }

    public static void onGroupAdd(long fromGroup){
        queueMap.put(fromGroup,new LinkedList<>());
    }

    public static void onGroupDrop(long fromGroup){
        queueMap.remove(fromGroup);
    }

    private static boolean allMsgAlikes(long fromGroup) {
        int i = queueMap.get(fromGroup).size();
        if (i <= 1) return false;
        String[] msg = queueMap.get(fromGroup).toArray(new String[i]);
        for (int j = 0; j < i; j++) {
            for (int k = j + 1; k < i; i++) {
                if (!msg[j].equals(msg[k]))
                    return false;
            }
        }
        return true;
    }

    /**
     * POST /api/v1/bot/module/repeat/settings/repeat_probability  val = {value}
     *
     * @param probability new repeat probability to be set
     */
    public static void setRepeatProbability(double probability) {
        try{
            JSONObject resp = webutils.getJSONObjectResp(setting_url + "/repeat_probability",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("val", String.valueOf(probability));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                repeat_pro = probability;
                CQ.logInfo("[Module Repeat settings]", "Successfully set repeat probability to {}", probability);
            } else {
                CQ.logWarning("[Module Repeat settings]", debug.err_mesg_for_json_resp(resp));
            }
        } catch (IOException e) {
            CQ.logError("Repeat::Setting","{}",e.toString());
            e.printStackTrace();
        }

    }

    /**
     * POST /api/v1/bot/module/repeat/settings/break_repeat_probability val = {value}
     *
     * @param new_pro new break-repeat probability to be set
     */
    public static void setBreakRepeatProbability(double new_pro) {
        try{
            JSONObject resp = webutils.getJSONObjectResp(setting_url + "/break_repeat_probability",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("val", String.valueOf(new_pro));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                break_repeat_pro = new_pro;
                CQ.logInfo("[Module Repeat settings]", "Successfully set break repeat probability to {}", new_pro);
            } else {
                CQ.logWarning("[Module Repeat settings]", debug.err_mesg_for_json_resp(resp));
            }
        } catch (IOException e) {
            CQ.logError("Repeat::Setting","{}",e.toString());
            e.printStackTrace();
        }

    }

    public static void clearGroup(long fromGroup){
        queueMap.get(fromGroup).clear();
    }

    public static void addMsg(String msg, long fromGroup) {
        CQ.logDebug("Repeat","msgqueue:{}",queueMap.get(fromGroup).toString());
        queueMap.get(fromGroup).offer(msg);
        performAction(msg,fromGroup);
    }

    private static void performAction(String msg,long fromGroup) {
        double r = Math.random();
        if (!allMsgAlikes(fromGroup)) {
            queueMap.get(fromGroup).clear();
            queueMap.get(fromGroup).offer(msg);
        } else {
            if (queueMap.get(fromGroup).size() >= 3) queueMap.get(fromGroup).clear();
            else if (r <= repeat_pro) {
                String rp = queueMap.get(fromGroup).element();
                queueMap.get(fromGroup).clear();
                r = Math.random();
                MsgBuilder msb = new MsgBuilder();

                if(r<=break_repeat_pro){
                    msb.setCoolQ(CQ).setTarget(fromGroup).append("一位群友砸坏了复读机并打断复读").sendGroupMsg();
                }
                else{
                    msb.setCoolQ(CQ).setTarget(fromGroup).append(rp).sendGroupMsg();
                }
            }
        }
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        return 1;
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.repeat";
    }

    @Override
    public String apiEndpointName() {
        return BaseModule.super.apiEndpointName() + "repeat/";
    }
}
