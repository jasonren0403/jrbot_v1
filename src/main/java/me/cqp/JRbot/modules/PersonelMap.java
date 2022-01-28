package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;
import static me.cqp.JRbot.Utils.misc.webutils.checkJSONResp;

public class PersonelMap implements BaseModule {
    private final long fromQQ;
    private final long fromGroup;

    public PersonelMap(long QQ, long Group) {
        this.fromQQ = QQ;
        this.fromGroup = Group;
    }

    enum MapState {
        DOWNLOAD_MAP, FIND_MAP
    }

    /**
     * JRbot 下谱 <谱面名称>
     *
     * @param isPrivate if true, send the result directly to fromQQ
     * @param fromGroup the group where bot stay in
     * @param fromQQ    the Person who want to find map
     * @param mapName   the name of the map requested
     * @param mapType   imd|cyt|[arc|stepmania] now supports imd|cyt
     */
    public static int GetPersonelMaps(boolean isPrivate, long fromGroup, long fromQQ, String mapName, String mapType) {
        //dl_map imd 病名为爱
        //支持用双引号括起有空格的曲目
        //dl_map <type> <mapName> [/private]
        MsgBuffer mb = new MsgBuffer();
        StringBuilder sb = new StringBuilder();
        try{
            //仍要做这一步，因为我们需要mid
            JSONObject jbo = webutils.getJSONObjectResp(
                    "http://xxxx.com/fanmade/musicgame_maps.php",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("action", "aliasToId");
                        put("alias", mapName);
                    }},
                    run_token,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );
            if (checkJSONResp(jbo)) {
                JSONArray ja = jbo.getJSONArray("contents");
                String mid = "";
                int len = ja.length();
                for (int i = 0; i < len; i++) {
                    JSONObject jb = ja.getJSONObject(i);
                    if (jb.getString("type").equalsIgnoreCase(mapType)) {
                        mid = jb.getString("mapId");
                        break;
                    }
                }
                if (!mid.isEmpty()) {
                    String finalMid = mid;
                    JSONObject result = webutils.getJSONObjectResp(
                            "http://xxxx.com/fanmade/musicgame_maps.php",
                            BotConstants.BOT_HEADER,
                            Connection.Method.GET,
                            new HashMap<String, String>() {{
                                put("action", "query");
                                put("subaction", "show_song_info");
                                put("mid", finalMid);
                            }},
                            run_token,
                            BotWebMethods.URLENCODED,
                            new JSONObject()
                    );
                    if (checkJSONResp(result)) {
                        JSONObject songinfo = result.getJSONArray("contents").getJSONObject(0);
                        String name = songinfo.getString("mapName_trueName");
                        String composer = songinfo.getString("composer");
                        String type = songinfo.getString("type");
                        boolean isMultifinger = songinfo.getBoolean("isMultiFinger");
                        boolean isMultilevel = songinfo.getBoolean("isMultiLevel");
                        String dl_link = songinfo.getString("dl_link");
                        String dl_code = songinfo.getString("dl_code");
                        String lastupdated = songinfo.getString("last_updated");
                        sb.append("[歌曲信息]").append(StringUtils.lineSeparator).append("曲目名称：").append(name).append("     作曲/演唱：").append(composer)
                                .append(StringUtils.lineSeparator).append("类型：").append(type).append(StringUtils.lineSeparator)
                                .append("是否多指：").append(isMultifinger ? "是" : "否").append("    有多个难度：").append(isMultilevel ? "是" : "否").append(StringUtils.lineSeparator).
                                append("下载链接：").append(dl_link).append("     提取码：").append(dl_code.isEmpty() ? "无" : dl_code).append(StringUtils.lineSeparator)
                                .append("上次更新日期：").append(lastupdated);
                        if (isPrivate) {
                            mb.setCoolQ(CQ).append("查询结果已发送至私聊！").sendGroupMsg(fromGroup);
                            CQ.sendPrivateMsg(fromQQ, sb.toString());
//                        mb.setCoolQ(CQ).append(sb.toString()).sendPrivateMsg(fromQQ);
                        } else {
                            mb.setCoolQ(CQ).at(fromQQ).newLine().append(sb.toString()).sendGroupMsg(fromGroup);
                        }
                    } else {
                        CQ.logWarning("Personelmap::download", "Cannot get songInfo!");
                        CQ.logWarning("Personelmap::download", debug.err_mesg_for_json_resp(result));
                    }
                } else {
                    CQ.logWarning("Personelmap::download", "Cannot get mapid!");
                }
            } else {
                sb.append("没有找到这个谱面呐（可能是我还没做过或没有登记到数据库）").append(StringUtils.lineSeparator)
                        .append("换个谱面再试一试？");
                mb.setCoolQ(CQ).at(fromQQ).append(sb.toString()).sendGroupMsg(fromGroup);
                CQ.logWarning("Personelmap::download", debug.err_mesg_for_json_resp(jbo));
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup,"内部出错，请稍后重试");
        }

        return 1;
    }

    /**
     * JRbot 找谱 <谱面名称> 若成功则返回下载链接及使用说明，
     *
     * @param isPrivate  if true, send the result directly to fromQQ
     * @param fromGroup  the group where bot stay in
     * @param fromQQ     the Person who want to find map
     * @param mapNamereq the name of the map requested
     */
    public static int findPersonelMaps(boolean isPrivate, long fromGroup, long fromQQ, String mapNamereq) {
        //find_map 私发 imd 病名为爱
        //find_map 病名为爱
        //find_map Legend 4k /private /trueName
        MsgBuffer mb = new MsgBuffer();
        try{
            JSONObject jbo = webutils.getJSONObjectResp(
                    "http://xxxx.com/fanmade/musicgame_maps.php",
                    BotConstants.BOT_HEADER,
                    Connection.Method.GET,
                    new HashMap<String, String>() {{
                        put("action", "aliasToId");
                        put("alias", mapNamereq);
                    }},
                    run_token,
                    BotWebMethods.URLENCODED,
                    new JSONObject()
            );
            StringBuilder sb = new StringBuilder();

            if (checkJSONResp(jbo)) {
                JSONArray ja = jbo.getJSONArray("contents");
                if (ja.isEmpty()) {
                    // 其实不可能走到这里，因为找到为空的话服务器的success就是false了
                    sb.append("没有找到这个谱面呐（可能是我还没做过或没有登记到数据库）").append(StringUtils.lineSeparator)
                            .append("换个谱面再试一试？");
                } else if (ja.length() > 1) {

                    sb.append("找到多个可能的结果").append(StringUtils.lineSeparator);
                    StringBuilder xiaosb = new StringBuilder();
                    for (int i = 0; i < ja.length(); i++) {
                        String mapId = ja.getJSONObject(0).getString("mapId");
                        String mapName = ja.getJSONObject(0).getString("mapName_trueName");
                        String type = ja.getJSONObject(0).getString("type");
                        sb.append(type).append("谱面: ").append(mapName).append(StringUtils.lineSeparator);
                        xiaosb.append("[").append(mapId).append("]").append(type).append("谱面: ").append(mapName).append(StringUtils.lineSeparator);
                    }
                    CQ.logDebug("Personelmap::find", xiaosb.toString());
                    sb.append(sb).append("请使用dl_map "+"type "+"<mapname> 命令获取下载链接");
                } else {
                    String mapId = ja.getJSONObject(0).getString("mapId");
                    String mapName = ja.getJSONObject(0).getString("mapName_trueName");
                    String type = ja.getJSONObject(0).getString("type");
                    CQ.logInfo("Personelmap::find", String.format("Request for [%s] map," +
                            " mapId: %s, mapName: %s", type, mapId, mapName));
                    sb.append("找到一个可能的结果").append(StringUtils.lineSeparator);
                    sb.append(type).append("谱面: ").append(mapName).append(StringUtils.lineSeparator);
                    sb.append("请使用dl_map <type> <mapname> 命令获取下载链接");
                }
                //send the message
                if (isPrivate) {
                    mb.setCoolQ(CQ).append("查询结果已发送至私聊！").sendGroupMsg(fromGroup);
//                mb.setCoolQ(CQ).append(sb.toString()).sendPrivateMsg(fromQQ);
                    CQ.sendPrivateMsg(fromQQ, sb.toString());
                } else {
                    mb.setCoolQ(CQ).at(fromQQ).append(StringUtils.lineSeparator).append(sb.toString()).sendGroupMsg(fromGroup);
                }
            } else {
                sb.append("没有找到这个谱面呐（可能是我还没做过或没有登记到数据库）").append(StringUtils.lineSeparator)
                        .append("换个谱面再试一试？");
                mb.setCoolQ(CQ).at(fromQQ).append(sb.toString()).sendGroupMsg(fromGroup);
                CQ.logWarning("Personelmap::find", debug.err_mesg_for_json_resp(jbo));
            }
        } catch (IOException e) {
            CQ.sendGroupMsg(fromGroup,"内部出错，请稍后重试");
        }

        return 1;
    }

    @Override
    public String helpMsg() {
        return "[谱面综合帮助]" + StringUtils.lineSeparator +
                "找谱: <当前群触发词> find_map [要查找的谱面名称（必填）] [/private|私发（选填，将查询结果发送至私聊中）]" + StringUtils.lineSeparator +
                "下谱：<当前群触发词> dl_map [谱面类型（必填）] [要下载的谱面名称（必填）] [/private|私发（选填，将查询结果发送至私聊中）]" + StringUtils.lineSeparator +
                "请用空格分隔各参数"+StringUtils.lineSeparator+
                "帮助网址：http://******.**/static/jrbot/help/available-modules/get_personal_maps/index.html";
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        if (directives.size() == 0) {
            // user only types "<trigger> get_map or dl_map" , show help to him
            CQ.sendGroupMsg(fromGroup, helpMsg());
            return 1;
        }
        CQ.logInfo("Module PersonelMap", "OK with param {}", directives.toString());
        MapState s = null;
        DirProcessState dir = DirProcessState.PROCESS_DIR;
        int index = 0;
        int ret = 0;
        int len = directives.size();
        while (index < len && dir != DirProcessState.END) {
            String current = directives.get(index);
            switch (dir) {
                case PROCESS_DIR:
                    switch (current) {
                        case "/?":
                        case "help":
                        case "/help":
                            CQ.sendGroupMsg(fromGroup, helpMsg());
                            ret = 1;
                            dir = DirProcessState.END;
                            index++;
                            break;
                        case "dl_map":
                        case "下谱":
                            dir = DirProcessState.PROCESS_PARAM;
                            s = MapState.DOWNLOAD_MAP;
                            index++;
                            break;
                        case "find_map":
                        case "找谱":
                            dir = DirProcessState.PROCESS_PARAM;
                            s = MapState.FIND_MAP;
                            index++;
                            break;
                    }
                    break;
                case PROCESS_PARAM:
                    switch (s) {
                        case FIND_MAP:
                            //find_map Legend [/private]
                            //findPersonelMaps(boolean isPrivate, long fromGroup, long fromQQ, String mapNamereq)
                            if(directives.contains("/help")){
                                CQ.sendGroupMsg(fromGroup, helpMsg());
                                ret = 1;
                                dir = DirProcessState.END;
                                continue;
                            }
                            boolean isPrivate = false;
                            String mapName;
                            if (directives.contains("/private") || directives.contains("私发")) {
                                isPrivate = true;
                                directives.remove("/private");
                                directives.remove("私发");
                            }
                            int index2 = directives.indexOf("find_map");
                            if (index2 != directives.size() - 2) {
                                CQ.sendGroupMsg(fromGroup, "usage: find_map <mapName> [/private]");

                            } else {
                                mapName = directives.get(index2 + 1);
                                ret = findPersonelMaps(isPrivate, fromGroup, fromQQ, mapName);
                            }
                            dir = DirProcessState.END;
                            break;
                        case DOWNLOAD_MAP:
                            //dl_map <type> <mapName> [/private]
                            //GetPersonelMaps(boolean isPrivate, long fromGroup, long fromQQ, String mapName, String mapType)
                            if(directives.contains("/help")){
                                CQ.sendGroupMsg(fromGroup, helpMsg());
                                ret = 1;
                                dir = DirProcessState.END;
                                continue;
                            }
                            boolean isPrivate2 = false;
                            String type, mapName2;
                            if (directives.contains("/private") || directives.contains("私发")) {
                                isPrivate2 = true;
                                directives.remove("/private");
                                directives.remove("私发");
                            }
                            int index3 = directives.indexOf("dl_map");
                            if (index3 != directives.size() - 3) {
                                CQ.sendGroupMsg(fromGroup, "usage: dl_map <type> <mapName> [/private]");
                            } else if (directives.contains("/type") && directives.contains("/name")) {
                                index3 = directives.indexOf("/type");
                                int index4 = directives.indexOf("/name");
                                if (index4 - index3 == 1) {
                                    CQ.sendGroupMsg(fromGroup, "类型错误！");
                                } else if (index4 == directives.size() - 1) {
                                    CQ.sendGroupMsg(fromGroup, "未指定谱面名称！请用引号括起带空格的名称");
                                } else {
                                    type = directives.get(index3 + 1);
                                    mapName2 = directives.get(index4 + 1);
                                    ret = GetPersonelMaps(isPrivate2, fromGroup, fromQQ, mapName2, type);
                                }
                            } else {
                                type = directives.get(index3 + 1);
                                mapName2 = directives.get(index3 + 2);
                                ret = GetPersonelMaps(isPrivate2, fromGroup, fromQQ, mapName2, type);
                            }
                            dir = DirProcessState.END;
                            break;
                    }
                case END:
                default:
                    break;
            }
        }
        return ret;
    }

    @Override
    public double version() {
        return 1.0;
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.PersonelMap";
    }
}
