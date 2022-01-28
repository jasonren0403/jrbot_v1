package me.cqp.JRbot.modules.Arcaea;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import me.cqp.JRbot.entity.jobs.connect;
import me.cqp.JRbot.modules.BaseModule;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.message.MsgBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static me.cqp.JRbot.Utils.PicGenService.getArcGenTask;
import static me.cqp.JRbot.modules.Arcaea.ArcClient.toggleHide;
import static me.cqp.JRbot.modules.Arcaea.ArcClient.toggleMode;
import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;


public class Arcaea implements BaseModule {

    public static ArrayList<ArcClient> users = new ArrayList<>();

    static {
        if (ArcClient.useClient) {
            users = ArcDao.poolInit();
        }
    }

    private final long fromGroup;
    private final long fromQQ;
    public static final Map<String, Integer> diffculty_mapping = new HashMap<String, Integer>() {{
        put("pst", 0);
        put("past", 0);
        put("prs", 1);
        put("present", 1);
        put("ftr", 2);
        put("future", 2);
        put("byd", 3);
        put("beyond", 3);
    }};

    public static final Map<Short, String> intToStr_diff_mapping = new HashMap<Short, String>() {{
        put((short) 0, "PST");
        put((short) 1, "PRS");
        put((short) 2, "FTR");
        put((short) 3, "BYD");
    }};

    enum ArcDirState {
        ACCOUNT_BIND, ACCOUNT_UNBIND, CONNECT, RECENT, BESTPLAY, RANDOM, CLOUDPLAY, CONFIG
    }

    public Arcaea(long QQ) {
        this(-1L, QQ);
    }

    public Arcaea(long group, long QQ) {
        this.fromGroup = group;
        this.fromQQ = QQ;
    }

    private ArrayList<String> difficulties() {
        return new ArrayList<String>() {{
            add("PST");
            add("PRS");
            add("FTR");
            add("BYD");
        }};
    }

    private ArcClient randomChoice() {
        int size = users.size();
        Random r = new Random(System.currentTimeMillis());
        if (ArcClient.useClient) return users.get(r.nextInt(size));
        else return null;
    }

    public int unbindArcAccount() {
        CQ.logInfo("Arcaea::UnbindArcAccount", "fromQQ:{}", fromQQ);
        try{
            JSONObject jbo1 = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ+"/uname"
                    ,BotConstants.BOT_HEADER,Connection.Method.GET,null,run_token,BotWebMethods.URLENCODED,new JSONObject());
            String uname = jbo1.getJSONObject("content").optString("arcuname","");
            if(!uname.isEmpty()){
                MsgBuffer msb = new MsgBuffer();
                JSONObject jbo2 = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ,
                        BotConstants.BOT_HEADER, Connection.Method.DELETE,null,run_token,BotWebMethods.URLENCODED,new JSONObject());
                if(webutils.checkJSONResp(jbo2)){
                    msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).
                            append("解绑成功！").append(uname).sendGroupMsg();
                    return 1;
                }
                else {
                    msb.setCoolQ(CQ).setTarget(fromGroup).append("<Error> 请稍后重试").sendGroupMsg();
                    return 0;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;

    }

    private String code() throws NoSuchAlgorithmException {
        String[] table = "qwertyuiopasdfghjklzxcvbnm1234567890".split("");
        MessageDigest md = MessageDigest.getInstance("MD5");
        Calendar c = Calendar.getInstance();
        int zoneOffset = c.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = c.get(java.util.Calendar.DST_OFFSET);
        c.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        String origin = String.format("%dori%dwol%doihs%dotas", c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_MONTH));
        byte[] raw = origin.getBytes();
        String res = Hex.encodeHexString(md.digest(raw));
        ;
        char[] mid = res.toCharArray();
        List<String> ret = IntStream.range(0, mid.length).mapToObj(val -> table[(int) mid[val] % 36]).collect(Collectors.toList());
        return ret.get(1) + ret.get(20) + ret.get(4) + ret.get(30) + ret.get(2) + ret.get(11) + ret.get(23);
    }

    public int connect(boolean alsoSubscribeAlert, boolean alsoUnsubscribeAlert) {
        assert !(alsoSubscribeAlert && alsoUnsubscribeAlert);
        if (alsoSubscribeAlert && connect.subscribeAlert(fromGroup)) {
            CQ.sendGroupMsg(fromGroup, "订阅每日提醒成功！新的连接码将于每日北京时间约8点时发送至群中");
            CQ.logInfo("Arcaea::connect-alert", "new subscribe fromGroup:{}", fromGroup);
        }
        if (alsoUnsubscribeAlert && connect.unsubscribeAlert(fromGroup)) {
            CQ.sendGroupMsg(fromGroup, "退订每日提醒成功！可使用/subscribe开关重新订阅");
            CQ.logInfo("Arcaea::connect-alert", "new unsubscribe fromGroup:{}", fromGroup);
            return 1;
        }
        Connection.Response res = null;
        try {
            res = Jsoup.connect("https://lowest.world/connect")
                    .header("User-Agent", BotConstants.WEB_HEADER)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();

            try {
                CQ.sendGroupMsg(fromGroup, "Connection failed! 使用本地算法：[" + code() + "]");
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                noSuchAlgorithmException.printStackTrace();
            }
            webLogging.addLog("warning","BotArcaea::connect", e.toString());
            return 1;
        }
        /* {code=421fchz, start_time=1590730782040} */
        String code = res.cookie("code");
        if ("".equals(code)) {
            code = res.cookies().get("code");
        }
        if (!"".equals(code)) {
            MsgBuffer msb = new MsgBuffer();
            msb.setCoolQ(CQ).setTarget(fromGroup).append("Connection complete.").newLine()
                    .append("今日的连接密码为[").append(code).append("]").newLine()
                    .append("tip：可以通过/subscribe选项订阅每日提醒(每天北京时间8点左右发布新的code)").newLine()
                    .sendGroupMsg();

            return 1;
        }

        CQ.sendGroupMsg(fromGroup, "Connection failed! 与世界失去连接……");
        return 1;
    }

    private int CloudPlay(String song, String difficulty) {
        difficulty = difficulty.toLowerCase();
        switch (difficulty) {
            case "pst":
            case "past":
                difficulty = "PST";
                break;
            case "prs":
            case "present":
                difficulty = "PRS";
                break;
            case "ftr":
            case "future":
                difficulty = "FTR";
                break;
            case "byd":
            case "beyond":
            case "byn":
                difficulty = "BYD";
                break;
            default:
                CQ.sendGroupMsg(fromGroup, "不合法的难度名称，请输入PST|PRS|FTR|BYD之一");
                return 1;
        }
        String truesong = getSongName(song);
        if (!truesong.isEmpty() && !truesong.contains("<Error>")) {
            try {
                JSONObject jbo = webutils.getJSONObjectResp(
                        "http://xxxx.com/webpage/arcaea_meta.php",
                        BotConstants.BOT_HEADER,
                        Connection.Method.GET,
                        new HashMap<String, String>() {{
                            put("start", String.valueOf(1));
                            put("count", String.valueOf(10));
                            put("goal", "song");
                            put("title", truesong);
                            put("filter", "PST-notecount,PST-level,PRS-notecount,PRS-level,FTR-notecount,FTR-level,BYD-notecount,BYD-level");
                        }},
                        null,
                        BotWebMethods.URLENCODED,
                        new JSONObject()
                );
                if (webutils.checkJSONResp(jbo) && jbo.getJSONArray("contents").length() > 0) {
                    JSONObject j = jbo.getJSONArray("contents").getJSONObject(0);
                    int notecount = j.optInt(difficulty + "-notecount", 0);
                    String level = j.optString(difficulty + "-level", "<undefined>");
                    if (notecount == 0) {
                        CQ.sendGroupMsg(fromGroup, "难度不存在！（或物量数未录入数据库）");
                    } else {
                        Random r = new Random(System.currentTimeMillis());
                        int lost = r.nextInt(Math.min(20, (int) Math.floor(notecount * 0.25)));
                        int far = r.nextInt(Math.min(616, notecount - lost + 1));
                        int pure = notecount - lost - far;
                        int big_pures = r.nextInt((int) (pure * 0.25)) + (int) (pure * 0.75);

                        long scr = ArcScore.calScoreForSong(notecount, far, lost, pure, big_pures);
                        String rate = ArcScore.RateForScore(scr, far, lost);
                        MsgBuilder msb = new MsgBuilder();
                        msb.setCoolQ(CQ).setTarget(fromGroup).append(truesong).append(String.format("[%s %s]云游玩结果：", difficulty, level)).newLine()
                                .append("Pure: ").append(pure).append('/').append("(+").append(big_pures).append(')').newLine()
                                .append("Far: ").append(far).append(" Lost: ").append(lost).newLine()
                                .append("Score: ").append(scr).append("    ").append(rate)
                                .sendGroupMsg();
                    }
                } else {
                    CQ.sendGroupMsg(fromGroup, "服务器正忙，请稍候再云游玩歌曲QaQ");
                }
            } catch (IOException e) {
                CQ.sendGroupMsg(fromGroup, "服务器正忙，请稍候再云游玩歌曲QaQ");
            }
        } else {
            CQ.sendGroupMsg(fromGroup, "歌曲不存在！（或别称/物量未录入数据库）");
        }
        return 1;
    }

    /**
     * http://xxxx.com/webpage/arcaea_meta.php?random=true&goal=[song|pack]
     *
     * @param type song|pack
     */
    protected int random(String type) {
        if (!"song".equals(type) && !"pack".equals(type)) {
            return 0;
        }
        try {
            JSONObject jbo = webutils.getJSONObjectResp(
                    "http://xxxx.com/webpage/arcaea_meta.php",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("random", String.valueOf(true));
                        put("goal", type);
                    }},
                    null,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );
            if (webutils.checkJSONResp(jbo)) {
                MsgBuilder mb = new MsgBuilder();
                mb.setTarget(fromGroup).setCoolQ(CQ).append("Lethe 建议您收割");
                if (type.equals("pack"))
                    mb.append('[').append(jbo.getJSONArray("contents").getJSONObject(0).optString("packname_en", "Arcaea")).append(']').append("曲包哦~");
                else {
                    mb.append(jbo.getJSONArray("contents").getJSONObject(0).optString("title_localized_en", "tutorial"));
                    /* difficulty */
                    ArrayList<String> s = new ArrayList<>();
                    if (jbo.getJSONArray("contents").getJSONObject(0).optInt("PST-level", -1) == -1) {
                        s.add("PST");
                    }
                    if (jbo.getJSONArray("contents").getJSONObject(0).optInt("PRS-level", -1) == -1) {
                        s.add("PRS");
                    }
                    if (jbo.getJSONArray("contents").getJSONObject(0).optInt("FTR-level", -1) == -1) {
                        s.add("FTR");
                    }
                    if (jbo.getJSONArray("contents").getJSONObject(0).optInt("BYD-level", -1) == -1) {
                        s.add("BYD");
                    }
                    ArrayList<String> sel = difficulties();
                    sel.removeAll(s);
                    if (sel.size() != 0) {
                        Random r = new Random(System.currentTimeMillis());
                        int ra = r.nextInt(sel.size());
                        String diff = sel.get(ra);
                        String key = diff + "-level";
                        mb.append('[').append(diff).append(' ').append(jbo.getJSONArray("contents").getJSONObject(0).getInt(key)).append("]").append("哟~");
                    }
                }
                mb.sendGroupMsg();
                return 1;
            } else {
                CQ.sendGroupMsg(fromGroup, "服务器忙，请稍后再试！");
                CQ.logWarning("Arcaea::random", "{}", jbo.toString());
                return 0;
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup, "服务器忙，请稍后再试！");
            CQ.logError("Arcaea::random", "Inner communication error: {}", e.toString());
            e.printStackTrace();
            return 0;
        }
    }

    public int bindArcAccount(String bindcode) {
        MsgBuffer msb = new MsgBuffer();
        CQ.logInfo("Arcaea::BindArcAccount", "ucode:{}", bindcode);
        if (bindcode.isEmpty()) {
            msb.setCoolQ(CQ).setTarget(fromGroup).image("arcaea/id_help.jpg").newLine()
                    .append("需要输入要绑定的arcID哦~")
                    .sendGroupMsg();
            return 1;
        }
        if (!bindcode.matches("\\d{9}")) {
            msb.setCoolQ(CQ).setTarget(fromGroup).image("arcaea/id_help.jpg").newLine()
                    .append("arcID格式不对哦~应该是9位数字吧？")
                    .sendGroupMsg();
            return 1;
        }

        String username = "";
        long uid = -1L;

        // /arcbind -1 解绑 alias /arcunbind
        if ("-1".equals(bindcode)) {
            return unbindArcAccount();
        }
        if (ArcClient.useClient) {
            ArcClient random = randomChoice();
            assert random != null;
//            CQ.logDebug("RandomChoice", random.toString());
            try {
                random.clearFriends();
                Map<String, Long> m = random.addFriend(bindcode);
                if (m.size() != 0) {
                    for (Map.Entry<String, Long> m1 : m.entrySet()) {
                        username = m1.getKey();
                        uid = m1.getValue();
                    }
                    //todo: add to database
                    msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).
                            append("绑定成功！").append(username).sendGroupMsg();
                } else {
                    msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).
                            append("绑定成功！").sendGroupMsg();
                    webLogging.addLog("warning","ArcAccountBind", "AddFriend may fault?");
                }
            } catch (ArcRuntimeException are) {
                CQ.logWarning("Arcaea", are);
            }
        }
        else {
            // get userinfo from userinfo api
            String userinfoapi = apiEndpointName() + "userinfo";
            try {
                JSONObject c = webutils.getJSONObjectResp(userinfoapi,
                        BotConstants.BOT_HEADER, Connection.Method.GET,
                        new HashMap<String, String>() {{
                            put("usercode", bindcode);
                        }}, null, BotWebMethods.URLENCODED, new JSONObject());
                if (webutils.checkJSONResp(c, "status", 0)) {
                    /* cache them in the database*/
                    String user_name = c.getJSONObject("content").getString("name");
                    long user_id = c.getJSONObject("content").getLong("user_id");
                    double rating = c.getJSONObject("content").getDouble("rating");
                    boolean ratinghide = (rating == -1);
                    JSONObject o = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind",
                            BotConstants.BOT_HEADER, Connection.Method.POST,new HashMap<String,String>(){{
                                put("qqId", String.valueOf(fromQQ));
                                put("arcCode",bindcode);
                                put("arcID",String.valueOf(user_id));
                                put("arcuname",user_name);
                            }},run_token,BotWebMethods.URLENCODED,new JSONObject());
                    if(webutils.checkJSONResp(o)){
                        if (ratinghide) {
                            msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).
                                    append("绑定成功！").append(user_name).sendGroupMsg();
                        } else {
                            msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).
                                    append("绑定成功！").append(user_name).append('[').append(ArcUser.ratingStar(rating/100))
                                    .append(rating/100).append(']').sendGroupMsg();
                        }
                    }
                    else{
                        msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append("绑定失败或内部出错，请检查usercode是否正确").sendGroupMsg();
                    }
                } else {
                    int status = c.optInt("status",-233);
                    msb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append('[').append(status)
                            .append(']').append("绑定失败或内部出错，请检查usercode是否正确").sendGroupMsg();
                    webLogging.addLog("warning","BotArcBinder::upstreamAPI", String.format("error code:%d", c.optInt("status", -233)));
                }
            } catch (IOException e) {
                CQ.sendGroupMsg(fromGroup, "服务器正忙，请稍后再试~");
                webLogging.addLog("warning","BotArcaea::bindUser",String.format("绑定ArcID失败, ucode: %s , fromQQ: %d", bindcode, fromQQ));
                e.printStackTrace();
            }
        }
        return 1;
    }

    /**
     * 切换默认查分模式 txt(0)/img(1)/json(-1)/hide/!hide(only mantainer or debugger)
     * @param selection 模式选择
     */
    private void config(String selection){
        boolean res;
        switch (selection){
            case "img":
                res = toggleMode(fromQQ,1);
                break;
            case "txt":
                res = toggleMode(fromQQ,0);
                break;
            case "json":
                res = toggleMode(fromQQ,-1);
                break;
            case "hide":
                res = toggleHide(fromQQ,true);
                break;
            case "!hide":
                res = toggleHide(fromQQ,false);
                break;
            default:
                CQ.sendGroupMsg(fromGroup,"usage: arc /config [txt|img|hide|!hide]");
                return;
        }
        if(res) CQ.sendGroupMsg(fromGroup,"切换["+selection+"]成功");
        else CQ.sendGroupMsg(fromGroup,"切换失败");
    }

    private String getSongName(String userrequest) {
        CQ.logInfo("Arcaea::GetSongInfo", "request:{}", userrequest);
        if (userrequest.isEmpty()) return "";
        return ArcScore.getSongName(userrequest);
    }

    public int getRecent() {
        MsgBuffer mb = new MsgBuffer();
        if (fromGroup == -1L) mb.setCoolQ(CQ).setTarget(fromQQ);
        else mb.setCoolQ(CQ).setTarget(fromGroup);
        try {
            JSONObject jbo1 = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ
                    ,BotConstants.BOT_HEADER,Connection.Method.GET,null,run_token,BotWebMethods.URLENCODED,new JSONObject());

            String ucode = jbo1.optString("arcucode","");
            int mode = jbo1.optInt("mode",0);
            boolean hide = jbo1.optBoolean("hide",false);
            if (ucode.isEmpty() && fromGroup != -1L) {
                mb.append("你还未绑定呐~请先使用arc /bind [加好友用到的ArcID号] 绑定~").sendGroupMsg();
                return 1;
            }
            JSONObject jbo = webutils.getJSONObjectResp(
                    apiEndpointName() + "userinfo",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("usercode", ucode);
                        put("recent", String.valueOf(true));
                    }},
                    null,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );

            if (webutils.checkJSONResp(jbo, "status", 0)) {
                JSONObject content = jbo.getJSONObject("content");
                if (!content.has("recent_score")) {
                    mb.append("打过至少一首歌再来查吧");
                    if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
                    else mb.sendGroupMsg(fromGroup);
                    return 1;
                }
                JSONObject recent = content.getJSONObject("recent_score");

                ArcScore arcScore = ArcScore.fromJSON(recent);
                ArcUser u = ArcUser.fromJSON(content);
                if (arcScore != null && u != null) {
                    boolean byd = arcScore.getDifficulty() == 3;

                    switch(mode){
                        case 1: //img
                            if(fromGroup==-1L) CQ.sendPrivateMsg(fromQQ,"稍等片刻，正在生成图片……[如未返回结果，请稍后重试]");
                            else CQ.sendGroupMsg(fromGroup,"稍等片刻，正在生成图片……[如未返回结果，请稍后重试]");
                            // async
                            ExecutorService executorService = Executors.newCachedThreadPool();
                            CompletionService<File> completionService = new ExecutorCompletionService<>(
                                    executorService);

                            completionService.submit(getArcGenTask(u,arcScore,hide,fromQQ));
                            File f = completionService.take().get();
                            //new ArcResultPic(u, s, hide).mask(Color.WHITE, 0.75f).
                            //                drawUserInfo(u).drawPlayInfo(s, false)
                            //                .build();
                            if(f!=null){
                                mb.image(f);
                                f.deleteOnExit();
                            }else{
                                mb.append("生成失败了……稍后重试吧");
                            }
                            executorService.shutdown();
                            break;
                        case 0: //txt
                            mb.image(String.format(ArcResultPic.resolve_bg_path(byd, "jpg"), arcScore.getSong_id())).newLine()
                                    .append(ArcScore.formatResultString(arcScore.getSong_name(), content.getString("name"), ucode, arcScore, hide));
                            break;
                        case -1: //json
                            mb.append(content.toString(2));
                            break;
                        default:
                            break;
                    }
                    if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
                    else mb.sendGroupMsg(fromGroup);
                }
                return 1;
            } else if (jbo.has("status")) {
                String notice;
                switch (jbo.getInt("status")) {
                    case -1:  //invalid usercode
                        notice = "usercode不对？建议重新绑定";
                        break;
                    case -2:  //allocate failed
                    case -3:  //clear friend list failed
                    case -4:  //add friend failed
                    case -5:  //internal error occurred
                    case -233: //unknown error occurred
                    default:
                        notice = "["+jbo.getInt("status")+"]"+"内部出错，请稍后再试";
                        break;
                }
                mb.append(notice);
                if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
                else mb.sendGroupMsg(fromGroup);
                return 0;
            } else {
                webLogging.addLog("warning","BotArcaea", String.format("Unknown error: %s",jbo.toString()));
                return 0;
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            mb.append("[Error] 服务器可能出了点问题 请稍候再使用~");
            webLogging.addLog("error","BotArcaea::GetRecent", e.getMessage());
            if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
            else mb.sendGroupMsg(fromGroup);
        }
        return 0;
    }

    public int getBestPlay(String reqsong, String reqdiff) {
        if ("byd".equals(reqdiff.toLowerCase())) reqdiff = "byn";
        CQ.logInfo("Arcaea::GetBestPlay", "song:{}, difficulty:{}", reqsong, reqdiff);
        MsgBuffer mb = new MsgBuffer();
        if(fromGroup==-1L) mb.setCoolQ(CQ).setTarget(fromQQ);
        else mb.setCoolQ(CQ).setTarget(fromGroup);
        if (reqsong.isEmpty()) {
            mb.append("嗯？你要找什么曲目？usage: arc /best <song> <diff>").sendGroupMsg();
            return 1;
        } else if (reqdiff.isEmpty()) {
            mb.append("嗯？你要找什么难度？usage: arc /best <song> <diff>").sendGroupMsg();
            return 1;
        }
        String finalReqdiff = reqdiff;
        try {
            JSONObject jbo1 = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ,
                    BotConstants.BOT_HEADER,Connection.Method.GET,null,run_token,BotWebMethods.URLENCODED,new JSONObject());

            String ucode = jbo1.optString("arcucode","");
            String uname = jbo1.optString("arcuname","");
            int mode = jbo1.optInt("mode",0);
            boolean hide = jbo1.optBoolean("hide",false);
            if (ucode.isEmpty() || uname.isEmpty()) {
                mb.append("你还未绑定呐~请先使用arc /bind [加好友用到的ArcID号] 绑定~").sendGroupMsg();
                return 1;
            }
            JSONObject jbo = webutils.getJSONObjectResp(
                    apiEndpointName() + "userbest",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("songname", reqsong);
                        put("difficulty", finalReqdiff);
                        put("usercode", ucode);
                    }},
                    null,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );

            JSONObject jbo2 = webutils.getJSONObjectResp(apiEndpointName()+"userinfo",
                    BotConstants.BOT_HEADER, Connection.Method.GET,
                    new HashMap<String,String>(){{
                        put("usercode",ucode);
                    }},null,BotWebMethods.URLENCODED,new JSONObject());

            if (webutils.checkJSONResp(jbo, "status", 0)&&webutils.checkJSONResp(jbo2,"status",0)) {
                JSONObject content = jbo.getJSONObject("content");
                content.put("name", uname);
                ArcScore arcScore = ArcScore.fromJSON(content);
                ArcUser arcUser = ArcUser.fromJSON(jbo2.getJSONObject("content"));
                if (arcScore != null&&arcUser!=null) {
                    switch(mode){
                        case -1: //json
                            mb.append(content.toString(2));
                            break;
                        case 1:  //img
                            if(fromGroup==-1L) CQ.sendPrivateMsg(fromQQ,"稍等片刻，正在生成图片……[如未返回结果，请稍后重试]");
                            else CQ.sendGroupMsg(fromGroup,"稍等片刻，正在生成图片……[如未返回结果，请稍后重试]");
                            // async
                            ExecutorService executorService = Executors.newCachedThreadPool();
                            CompletionService<File> completionService = new ExecutorCompletionService<>(
                                    executorService);
                            completionService.submit(getArcGenTask(arcUser,arcScore,hide,fromQQ));
                            File f = completionService.take().get();
                            //new ArcResultPic(u, s, hide).mask(Color.WHITE, 0.75f).
                            //                drawUserInfo(u).drawPlayInfo(s, false)
                            //                .build();
                            if(f!=null){
                                mb.image(f);
                                f.deleteOnExit();
                            }else{
                                mb.append("生成失败了……稍后重试吧");
                            }
                            executorService.shutdown();
                            break;
                        case 0: //txt
                            boolean byd = arcScore.getDifficulty() == 3;
                            mb.append(ArcScore.formatResultString(arcScore.getSong_name(), content.getString("name"), ucode, arcScore, hide));
                            break;
                        default:
                            break;
                    }
                    if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
                    else mb.sendGroupMsg(fromGroup);
                }
                return 1;
            } else if (jbo.has("status")) {
                String notice;
                switch (jbo.getInt("status")) {
                    case -1:  //invalid usercode
                        notice = "usercode不对？建议重新绑定";
                        break;
                    case -2:  //invalid songname
                        notice = "歌曲名不对该怎么查呢？";
                        break;
                    case -3:  //invalid difficulty
                    case -4:  //invalid difficulty (map format failed)
                        notice = "合法难度为pst/prs/ftr/byd之一";
                        break;
                    case -5:  //not recorded
                        notice = "欲查阅的歌曲还没有存入数据库！";
                        break;
                    case -6:  //too many records
                        notice = "换个别称再来查吧";
                        break;
                    case -8:  //no beyond level
                        notice = "该曲无beyond难度，想查得等lowiro出";
                        break;
                    case -14: //not played yet
                        notice = "打过一首歌/这首歌再来查吧";
                        break;
                    case -9:  //allocate an arc account failed
                    case -10: //clear friend list failed
                    case -11: //add friend failed
                    case -7:  //internal error
                    case -12: //internal error
                    case -13: //internal error
                    case -233: //unknown error occurred
                    default:
                        notice = "["+jbo.getInt("status")+"]"+"内部出错，请稍后再试";
                        break;
                }
                mb.append(notice);
                if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
                else mb.sendGroupMsg(fromGroup);
                return 0;
            } else {
                webLogging.addLog("warning","BotArcaea", String.format("Unknown error: %s", jbo.toString()));
                return 0;
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            mb.append("[Error] 服务器可能出了点问题 请稍候再使用~");
            if (fromGroup == -1L) mb.sendPrivateMsg(fromQQ);
            else mb.sendGroupMsg(fromGroup);
        }
        return 0;
    }

    @Override
    public String helpMsg() {
        return "[Arcaea module ver 1.6.6 help]" +
                "\n 所有命令前需要加上本群的触发词（详情使用 sudo trigger-get命令获取）\n" +
                "arc /bind [加好友时用到的9位Arc code号] 绑定查询功能\n" +
                "arc /help或/? 显示此帮助\n" +
//                "arc /random [pack|song] 随机今日收割\n" +
//                "arc /cloudplay <song> <diff> 云游玩\n" +
                "arc /connect 获取https://lowest.world/connect今日解锁密码\n" +
                "arc /config 1. [img|txt] 切换查分展示方式 2. [hide|!hide]设定是否隐藏必要信息\n" +
                "[以下功能需要首先使用arc /bind 命令绑定]\n" +
                "arc /unbind 取消绑定查询功能（alias: arc /bind -1）\n" +
                "arc /best <歌曲名> [pst|prs|ftr|byn] 查最佳成绩\n" +
                "arc /recent 查新鲜出炉的分数\n" + helpUrl();
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        directives.remove("arcaea");
        directives.remove("arc");
        directives.remove("/arc");
        directives.remove("韵律源点");
        CQ.logDebug("BotArcaea",directives.toString());
        if (directives.size() == 0) {
            // user only types "<trigger> arc" , get his or her score as result.
            return getRecent();
        }
        int index = 0;
        int ret = 0;
        ArcDirState state = null;
        DirProcessState s = DirProcessState.PROCESS_DIR;
        int len = directives.size();
        boolean alsosubscribes = false;
        boolean alsounsubscribes = false;
        mainloop:
        while (index < len && s != DirProcessState.END) {
            String current = directives.get(index);
            State:
            switch (s) {
                case PROCESS_DIR:
                    switch (current) {
                        case "config":
                        case "/config":
                            state = ArcDirState.CONFIG;
                            s = DirProcessState.PROCESS_PARAM;
                            index ++;
                            break;
                        case "cloudplay":
                        case "/cloudplay":
                            CQ.sendGroupMsg(fromGroup,"功能维护中，暂缓使用");
                            s = DirProcessState.END;
//                            state = ArcDirState.CLOUDPLAY;
//                            s = DirProcessState.PROCESS_PARAM;
//                            index++;
                            break;
                        case "connect":
                        case "/connect":
                            state = ArcDirState.CONNECT;
                            if (directives.contains("/subscribe")) alsosubscribes = true;
                            if (directives.contains("/unsubscribe")) alsounsubscribes = true;
                            if (alsosubscribes && alsounsubscribes) {
                                CQ.sendGroupMsg(fromGroup, "你到底想怎么样.jpg");
                                s = DirProcessState.END;
                                break;
                            }
                            ret = connect(alsosubscribes, alsounsubscribes);
                            s = DirProcessState.END;
                            break;
                        case "random":
                        case "/random":
                            CQ.sendGroupMsg(fromGroup,"功能维护中，暂缓使用");
                            s = DirProcessState.END;
//                            state = ArcDirState.RANDOM;
//                            if (index != len - 1) ret = random(directives.get(index + 1));
//                            s = DirProcessState.END;
                            break;
                        case "help":
                        case "/help":
                        case "/?":
                        case "?":
                            CQ.sendGroupMsg(fromGroup, helpMsg());
                            ret = 1;
                            s = DirProcessState.END;
                            index++;
                            break;
                        case "bind":
                        case "/bind":
                        case "/arcbind":
                        case "/bindarc":
                            if (directives.indexOf(current) == len - 1) {
                                ret = bindArcAccount("");
                                s = DirProcessState.END;
                                break State;
                            }
                            state = ArcDirState.ACCOUNT_BIND;
                            s = DirProcessState.PROCESS_PARAM;
                            index++;
                            break;
                        case "unbind":
                        case "/unbind":
                        case "/unbindarc":
                        case "/arcunbind":
                            state = ArcDirState.ACCOUNT_UNBIND;
                            s = DirProcessState.PROCESS_PARAM;
                            break;
                        case "best":
                        case "/best":
                        case "/bestplay":
                            if (directives.indexOf(current) == len - 2) {
                                ret = getBestPlay(directives.get(index + 1), "");
                                s = DirProcessState.END;
                                break State;
                            } else if (directives.indexOf(current) == len - 1) {
                                ret = getBestPlay("", "");
                                s = DirProcessState.END;
                                break State;
                            }
                            index++;
                            state = ArcDirState.BESTPLAY;
                            s = DirProcessState.PROCESS_PARAM;
                            break;
                        case "recent":
                        case "/recent":
                            state = ArcDirState.RECENT;
                            s = DirProcessState.PROCESS_PARAM;
                            break;
                        default:
                            break mainloop;
                    }
                    break;
                case PROCESS_PARAM:
                    switch (state) {
                        case CONFIG:
                            config(current);
                            ret = 1;
                            s = DirProcessState.END;
                            break;
                        case CLOUDPLAY:
                            // current-songname next-diff
                            String song = current;
                            if (index != directives.size() - 1) {
                                String diff = directives.get(index + 1);
                                ret = CloudPlay(song, diff);
                            }
                            s = DirProcessState.END;
                            break;
                        case ACCOUNT_BIND:
                            ret = bindArcAccount(current);
                            s = DirProcessState.END;
                            break;
                        case ACCOUNT_UNBIND:
                            ret = unbindArcAccount();
                            s = DirProcessState.END;
                            break;
                        case BESTPLAY:
                            if (index + 1 < len) {
                                String diff = directives.get(index + 1);
                                ret = getBestPlay(current, diff);
                            }
                            s = DirProcessState.END;
                            break;
                        case RECENT:
                            ret = getRecent();
                            s = DirProcessState.END;
                            break;
                        default:
                            s = DirProcessState.END;
                            break;
                    }
                    break;
                case END:
                default:
                    break mainloop;
            }
        }
        return ret;
    }

    @Override
    public double version() {
        return 1.6;
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.arcaea";
    }

    @Override
    public String apiEndpointName() {
        return "http://xxxx.com/v2/";
    }
}
