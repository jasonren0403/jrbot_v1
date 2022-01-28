package me.cqp.JRbot;

import me.cqp.JRbot.entity.BotConstants;
import org.json.JSONObject;
import org.meowy.cqp.jcq.util.StringUtils;

import java.util.Date;

import static me.cqp.Jrbot.CQ;

public class debug {

    public static void debug_message(String message) {
        CQ.sendGroupMsg(BotConstants.ALARM_GROUP,message);
        CQ.logDebug("[Personel Debug]", message);
    }

    private static void get_group_settings() {
        CQ.logInfo("[Personel Debug]", "See [DEBUG] messages for details");
        CQ.sendPrivateMsg(PersonelConfig.developer_qq, "debug ok,See [DEBUG] messages in logs for details");
        //welcome trigger
    }

    public static void checkAPIUsage() {
        boolean sendImage = false, sendRecord = false;
        if (CQ.canSendRecord()) sendRecord = true;
        if (CQ.canSendImage()) sendImage = true;
        debug.debug_message("CQAPI status: sendRecord: " + sendRecord + " sendImage: " + sendImage);
    }

    public static void get_stats() {
        String sb = "全局欢迎语：" + PersonelConfig.welcomeMsg + StringUtils.lineSeparator +
                "全局触发词：" + PersonelConfig.trigger_word + StringUtils.lineSeparator +
                "应用目录：" + CQ.getAppDirectory() + StringUtils.lineSeparator +
                "本bot的qq：" + PersonelConfig.bot_qq + " " + "开发者qq：" + PersonelConfig.developer_qq + StringUtils.lineSeparator +
                "当前登录的机器人QQ：" + CQ.getLoginQQ() + " 昵称：" + CQ.getLoginNick() + StringUtils.lineSeparator +
                "当前系统时间：" + new Date().toString() + StringUtils.lineSeparator +
                "是否为静音状态：" + PersonelConfig.under_maintence;
        debug_message(sb);
    }

    public static String err_mesg_for_json_resp(JSONObject errresp) {
        assert !errresp.getBoolean("success");
        int errcode = errresp.optInt("error_code", 0);
        StringBuilder sb = new StringBuilder();
        sb.append("Error code: ").append(errcode).append(" ").append("Error message: ");
        if (errcode == 0) {
            sb.append("Unknown error.");
        } else sb.append(errresp.optString("error_message", "unknown error."));
        return sb.toString();
    }
}
