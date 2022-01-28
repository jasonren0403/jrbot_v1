package me.cqp.JRbot.entity.jobs;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.meowy.cqp.jcq.util.StringUtils;
import org.quartz.JobExecutionContext;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class connect implements BaseBotJob {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        Connection.Response res = null;
        try {
            res = Jsoup.connect("https://lowest.world/connect")
                    .header("User-Agent", BotConstants.WEB_HEADER)
                    .method(Connection.Method.GET)
                    .execute();
        }catch (IOException e) {
            e.printStackTrace();
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP, "使用本地算法\n"+e.toString());
            try {
                doSendMsg(connect(),true);
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                CQ.sendGroupMsg(BotConstants.ALARM_GROUP, "本地算法不存在\n"+noSuchAlgorithmException.toString());
            }
            return;
        }
        String code = res.cookie("code");
        if ("".equals(code)) {
            code = res.cookies().get("code");
        }
        doSendMsg(code,false);

    }

    private void doSendMsg(String code,boolean exp_occurs){
        ArrayList<Long> groups = get_alert_groups();
        CQ.logInfoSuccess("Connect","Alarm groups: {}",groups.toString());
        for(long l:groups){
            if(CQ.getGroupInfo(l)!=null){
                String str = String.format("今日的连接密码是[%s]", code)+ StringUtils.lineSeparator+"(在线版查询网页正在编写中，敬请期待)";
                if(exp_occurs)
                    str += StringUtils.lineSeparator+"(本次计算使用本地算法，具体算法来自 https://bbs.arcaea.cn/d/18-30/15)";
                CQ.sendGroupMsg(l,str);
            }
        }
    }

    /**
     * Local lowest.world/connect algorithm
     * @return the calculated code
     * @throws NoSuchAlgorithmException if there are no md5 algorithm
     */
    private static String connect() throws NoSuchAlgorithmException {
        String[] table = "qwertyuiopasdfghjklzxcvbnm1234567890".split("");
        MessageDigest md = MessageDigest.getInstance("MD5");
        Calendar c = Calendar.getInstance();
        int zoneOffset = c.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = c.get(java.util.Calendar.DST_OFFSET);
        c.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        String origin = String.format("%dori%dwol%doihs%dotas", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));
        byte[] raw = origin.getBytes();
        String res = Hex.encodeHexString(md.digest(raw));;
        char[] mid = res.toCharArray();
        List<String> ret = IntStream.range(0, mid.length).mapToObj(val -> table[(int) mid[val] % 36]).collect(Collectors.toList());
        return ret.get(1) + ret.get(20) + ret.get(4) + ret.get(30) + ret.get(2) + ret.get(11) + ret.get(23);
    }

    private ArrayList<Long> get_alert_groups(){
        try{
            JSONObject jbo = webutils.getJSONObjectResp("http://xxx.com/jrbot/module/arcaea/connect",
                    BotConstants.BOT_HEADER, Connection.Method.GET,null,run_token, BotWebMethods.URLENCODED,new JSONObject());
            if(webutils.checkJSONResp(jbo)){
                ArrayList<Long> gps = new ArrayList<>();
                JSONArray ja = jbo.getJSONObject("content").getJSONArray("groups");
                for(int i =0;i<ja.length();i++){
                    gps.add(ja.getLong(i));
                }
                return new ArrayList<>(new HashSet<>(gps));
            }
        } catch (IOException e) {
            CQ.logError("connectTask","Inner API communication error, {}",e.toString());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static boolean subscribeAlert(long fromGroup){
        try{
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/connect/subscribe",
                    BotConstants.BOT_HEADER,Connection.Method.POST,new HashMap<String, String>(){{
                        put("groupId",String.valueOf(fromGroup));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            return webutils.checkJSONResp(jbo);
        }catch(IOException e){
            CQ.logError("connectTask","Inner API communication error, {}",e.toString());
            return false;
        }

    }

    public static boolean unsubscribeAlert(long fromGroup){
        try{
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/connect/unsubscribe",
                    BotConstants.BOT_HEADER,Connection.Method.POST,new HashMap<String, String>(){{
                        put("groupId",String.valueOf(fromGroup));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            return webutils.checkJSONResp(jbo);
        } catch (IOException e) {
            CQ.logError("connectTask","Inner API communication error, {}",e.toString());
            return false;
        }
    }
}
