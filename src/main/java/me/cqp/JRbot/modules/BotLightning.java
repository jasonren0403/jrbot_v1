package me.cqp.JRbot.modules;

import me.cqp.JRbot.PersonelConfig;
import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.enumerate.Authority;
import org.meowy.cqp.jcq.message.MsgBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.cqp.JRbot.PersonelConfig.bot_qq;
import static me.cqp.JRbot.Utils.misc.webutils.checkJSONResp;
import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class BotLightning implements BaseModule{
    private static ArrayList<Long> list = new ArrayList<>();
    private final long fromGroup;
    private final long fromQQ;

    public static double struck_pro;
    static{
        JSONObject jbo = null;
        try {
            jbo = webutils.getJSONObjectResp(
                    "http://xxxx.com/jrbot/module/lightning/settings",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    null,
                    run_token,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
                    );
        } catch (IOException e) {
            CQ.logError("Lightning::Init","Inner API communication error, {}",e.toString());
            e.printStackTrace();
        }
        if(webutils.checkJSONResp(jbo)){
            struck_pro = jbo.getJSONObject("content").getDouble("struck_probability");
        }else{
            struck_pro = 0.1;  //0.1 by default
            CQ.logInfo("BotLightning::Init","Init struck probability failed! Use 0.1 for fallback");
        }
    }

    public static ArrayList<Long> getCurrentAliveGroups(){
        return list;
    }

    /**
     * POST /api/v1/bot/module/lightning/settings/struck_probability  val = {value}
     *
     * @param probability new struck probability to be set
     */
    public static void setStruckProbability(double probability) {
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/settings/struck_probability",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("val", String.valueOf(probability));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                struck_pro = probability;
                CQ.logInfo("[Module Struck settings]", "Successfully set struck probability to " + probability);
            } else {
                CQ.logWarning("[Module Struck settings]", debug.err_mesg_for_json_resp(resp));
            }
        } catch (IOException e) {
            CQ.logError("Lightning","Inner API communication error, {}",e.toString());
            e.printStackTrace();
        }

    }

    public BotLightning(long fromGroup1, long fromQQ1){
        this.fromGroup = fromGroup1;
        this.fromQQ = fromQQ1;
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        directives.remove("lightning");
        directives.remove("挂闪电");
        if (CQ.getGroupMemberInfo(fromGroup, bot_qq)!=null&&CQ.getGroupMemberInfo(fromGroup,bot_qq).getAuthority() == Authority.MEMBER) {
            CQ.sendGroupMsg(fromGroup, "<Info>Bot 没权限，劈不动人，请将bot设置为管理员以获得更好的挨劈体验(bushi");
        }
        if(directives.size() != 1){
            if(directives.isEmpty()){
                lightning_init();
                return 1;
            }
            CQ.sendGroupMsg(fromGroup,helpMsg());
            return 1;
        }
        String dir = directives.get(0);
        switch(dir){
            case "挂闪电":
            case "on":{
                lightning_init();
                break;
            }
            case "过河拆桥":
            case "off":{
                close_lightning();
                break;
            }
            case "闪电统计":
            case "stat":{
                lightning_statistics();
                break;
            }
            default:
                CQ.sendGroupMsg(fromGroup,helpMsg());
                break;
        }
        return 1;
    }

    /**
     * JRbot lightning: 模拟三国杀闪电！挂闪电后用户说的每一句话都会进行判定，若判定成功则目标用户被禁言随机时间(10~60s)
     * 调用条件：bot权限为管理员以上
     */
    public void perform_struck() {
        MsgBuffer mb = new MsgBuffer();
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/state",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("fromGroup", String.valueOf(fromGroup));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());

            if (checkJSONResp(resp)&&resp.getJSONObject("content").optBoolean("state_on", false)) {
                if (PersonelConfig.isBotManageable(fromGroup, fromQQ, bot_qq)) {
                    JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/struck",
                            BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {
                                {
                                    put("fromGroup", String.valueOf(fromGroup));
                                    put("fromQQ", String.valueOf(fromQQ));
                                }
                            }, run_token, BotWebMethods.URLENCODED, new JSONObject());
                    if (checkJSONResp(jbo)) {
                        int time = jbo.getJSONObject("content").getInt("struckTime");
                        CQ.setGroupBan(fromGroup, fromQQ, time);

                        mb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("boom！获得").append(time).append("s 冷却时间").face(36, 54).sendGroupMsg();


                        JSONObject ignored = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/state",
                                BotConstants.BOT_HEADER, Connection.Method.POST,new HashMap<String,String>(){{
                                    put("fromGroup", String.valueOf(fromGroup));
                                    put("on",String.valueOf(false));
                                }},run_token,BotWebMethods.URLENCODED,new JSONObject());
                        if(webutils.checkJSONResp(ignored)){
                            list.remove(fromGroup);
                        }
                    }
                    else {
                        CQ.logWarning("Lightning::struck", "cannot struck people. SEE DEBUG for more info");
                        mb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("far").sendGroupMsg();
                    }
                }
                else {
                    mb.setTarget(fromGroup).setCoolQ(CQ).append("您被劈中了，但是烟不了你233").newLine()
                            .append("(闪电将继续传递)").sendGroupMsg();
                }
            }else {
                CQ.logWarning("Lightning::struck", "cannot struck people. SEE DEBUG for more info");
                mb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("miss").sendGroupMsg();
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Exception at group "+fromGroup+e.toString());
            e.printStackTrace();
        }

    }

    private void close_lightning(){
        if(CQ.getGroupMemberInfo(fromGroup,fromQQ)==null || CQ.getGroupMemberInfo(fromGroup,fromQQ).getAuthority()==Authority.MEMBER){
            CQ.sendGroupMsg(fromGroup,"想拆除闪电不是那么容易的-_-");
            return;
        }
        MsgBuffer msb = new MsgBuffer();
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/state",
                    BotConstants.BOT_HEADER, Connection.Method.POST,new HashMap<String,String>(){{
                        put("fromGroup", String.valueOf(fromGroup));
                        put("on",String.valueOf(false));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            if(checkJSONResp(resp)){
                list.remove(fromGroup);
                msb.setCoolQ(CQ).setTarget(fromGroup).append("闪电已被拆除！").sendGroupMsg();
            }else{
                CQ.logWarning("BotLightning","闪电拆除失败");
                CQ.sendGroupMsg(BotConstants.ALARM_GROUP,resp.toString(3));
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup,"[Error] 闪电拆除失败");
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Exception at group "+fromGroup+e.toString());
            e.printStackTrace();
        }

    }

    public void lightning_statistics() {
        MsgBuffer msb = new MsgBuffer();
        int count, time;
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/timequery",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("fromQQ", String.valueOf(fromQQ));
                        if (fromGroup != -1) {
                            put("fromGroup", String.valueOf(fromGroup));
                        }
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (checkJSONResp(resp)) {
                if (fromGroup == -1) {
                    count = resp.getJSONObject("content").optInt("sumcount", 0);
                    time = resp.getJSONObject("content").optInt("sumtime", 0);
                    if (time == 0) {
                        msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("在哪里都还没被劈中过呢，你是欧洲人吗ヽ(ｏ`皿′ｏ)ﾉ").sendGroupMsg();
                    } else {
                        msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("根据我的记录，您在本bot所在的所有群已经被劈过").append(count).append("次，" + "累计")
                                .append(String.valueOf(time)).append("秒").sendGroupMsg();
                    }
                } else {
                    count = resp.getJSONObject("content").optInt("struck_count", 0);
                    time = resp.getJSONObject("content").optInt("struck_time", 0);
                    if (time == 0) {
                        msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("还没被劈中过呢，你是欧洲人吗ヽ(ｏ`皿′ｏ)ﾉ").sendGroupMsg();
                    } else {
                        msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("您在本群已经被劈过").append(count).append("次，" + "累计")
                                .append(String.valueOf(time)).append("秒").sendGroupMsg();
                    }
                }
            } else {
                CQ.sendGroupMsg(fromGroup,"内部服务器出错，请稍后再查询挨劈状况");
                CQ.logWarning("BotFunction::Lightning", "Cannot query lightning statistics now. SEE DEBUG for more details");
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup,"内部服务器出错，请稍后再查询挨劈状况");
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Exception at group "+fromGroup+e.toString());
            e.printStackTrace();
        }


    }

    public void lightning_init() {
        MsgBuffer msg = new MsgBuffer();
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/state",
                    BotConstants.BOT_HEADER, Connection.Method.GET, new HashMap<String, String>() {{
                        put("fromGroup", String.valueOf(fromGroup));
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (checkJSONResp(resp)) {
                boolean lightning_stat = resp.getJSONObject("content").optBoolean("state_on", false);
                if (!lightning_stat) {
                    //POST /api/v1/bot/module/lightning/state fromGroup={}&on=[true|false]
                    JSONObject postnew = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/lightning/state",
                            BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                                put("fromGroup", String.valueOf(fromGroup));
                                put("on", String.valueOf(true));
                            }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
                    if (checkJSONResp(postnew)) {
                        list.add(fromGroup);
                        msg.setCoolQ(CQ).append("闪电已经挂上了！被劈可能会获得10~60秒的安静时间！").face(178).sendGroupMsg(fromGroup);
                    } else {
                        msg.setCoolQ(CQ).append("闪电太忙了，不如等会再试试？").sendGroupMsg(fromGroup);
                        CQ.logWarning("Lightning::newState", "Cannot change lightning state now. SEE DEBUG for more details");
                        CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"lightning from group "+ fromGroup+"\n"+postnew.toString(3));
                    }
                } else {
                    msg.setCoolQ(CQ).setTarget(fromGroup).append("还想再挂？没有了哼哼哼╭(╯^╰)╮").sendGroupMsg();
                }
            } else {
                CQ.sendGroupMsg(fromGroup,"内部服务器出错，请稍后再挂闪电233");
                CQ.logWarning("Lightning::GetState", "Cannot init lightning now. SEE DEBUG for more details");
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup,"内部服务器出错，请稍后再挂闪电233");
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Exception at group "+fromGroup+e.toString());
            e.printStackTrace();
        }

    }

    public String name(){
        return "me.cqp.jrbot.lightning";
    }
}
