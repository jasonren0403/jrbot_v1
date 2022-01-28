package me.cqp.JRbot.modules.bof;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import me.cqp.JRbot.modules.BaseModule;
import org.json.JSONObject;
import org.jsoup.Connection;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class main implements BaseModule {
    /**
     * POST /api/v1/bot/module/bof/updateInfo entryBMSNum={}&imprNum={}&last_updated={}
     * still not in use!
     *
     * @param entryNum total teams in bof
     * @param imprNum  total impress numbers
     */
    public static void overwrite_bof_datas(int entryNum, int imprNum) {
        try{
            JSONObject resp = webutils.getJSONObjectResp("http://localhost:3001/api/v1/bot/module/bof/updateInfo",
                    BotConstants.BOT_HEADER, Connection.Method.POST, new HashMap<String, String>() {{
                        put("entryBMSnum", String.valueOf(entryNum));
                        put("imprNum", String.valueOf(imprNum));
                        put("last_updated", new Date().toString());
                    }}, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(resp)) {
                CQ.logInfo("[Module bof]", "bofxv data updated.");
            } else {
                CQ.logWarning("[Module bof]", debug.err_mesg_for_json_resp(resp));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String name() {
        return "me.cqp.jrbot.bof";
    }

    @Override
    public String apiEndpointName() {
        return BaseModule.super.apiEndpointName()+"bof/";
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        return 0;
    }
    //region bofxv2019
//                case "bofxv查询":
//                case "bofxv2019":
//                case "bofxv": {
//                    if (!getEnabledState(getEnabledStatbyGroup(fromGroup),"bofxv_search")) returnvalue = 0;
//                    else {
//                        StringBuilder sb = new StringBuilder();
//                        int num = 0;
//                        boolean exception_thrown = false;
//                        if (arg.length == 3) {
//                            try {
//                                num = Integer.valueOf(arg[1]);
//                            } catch (NumberFormatException nfe) {
//                                exception_thrown = true;
//                            }
//                            if(arg[1].contains("update")){
//                                sb.append("正在更新数据中……请稍后重新调用本触发词");
//                                List<BofSong> bofSongs = bofxv_contents(false,"");
//                                init_or_update_bofxv_contents(bofSongs,false);
//                            }
//                            if (exception_thrown || num <= 0) {
//                                //bofxv [team|song|composer] <query_string> 查找歌曲 输出全部信息 默认直接跟着的string为歌曲名
//                                //type支持team_name，song_name，composer
//                                if (arg[1].substring(1).equals("队伍") || arg[1].startsWith("team")) {
//                                    String query = arg[2];//arg[1].replaceFirst("[Tt]eam|队伍", "");
//
//                                    List<BofSong> bofSongs = get_bofxv_contents(0, query, "team_name");
//                                    if(bofSongs.size()>0){
//                                        sb.append("——队伍信息——").append(System.lineSeparator());
//                                        sb.append("——参赛歌曲——").append(System.lineSeparator());
//                                        for(BofSong bofSong:bofSongs){
//                                            sb.append(bofSong.getSong_name()).append("：").append(bofSong.getComposer()).append(System.lineSeparator());
//                                        }
//                                        sb.append("——队伍总排名——").append(System.lineSeparator());
//                                        for(BofSong bofSong2: bofxv_contents(true,arg[2])){
//                                            sb.append("总分：").append(bofSong2.getReserved()).append(System.lineSeparator());
//                                            sb.append("中位分：").append(bofSong2.getMedian_pts()).append(System.lineSeparator());
//                                            sb.append("总评价数：").append(bofSong2.getRating()).append(System.lineSeparator());
//                                        }
//                                    }
//                                    else{
//                                        sb.append("后台更新数据中，请稍候……");
//                                    }
//                                } else if (arg[1].substring(1).equals("歌曲") || arg[1].startsWith("song")) {
//                                    String query = arg[2];//arg[1].replaceFirst("歌曲|[Ss]ong", "");
//
//                                    List<BofSong> bofSongs = get_bofxv_contents(0, query, "song_name");
//                                    if(bofSongs.size()>0){
//                                        sb.append("-——歌曲信息——").append(System.lineSeparator());
//                                        BofSong bofSong = bofSongs.get(0);
//                                        sb.append("歌曲名：").append(bofSong.getSong_name()).append("作曲家：").append(bofSong.getComposer());
//                                        sb.append("总分：").append(bofSong.getTotal_pts()).append("中位分：").append(bofSong.getMedian_pts());
//                                        sb.append("总评星数：").append(bofSong.getStars()).append("所属队伍：").append(bofSong.getTeam_name());
//                                    }
//                                    else{
//                                        sb.append("你确定这首歌在bofxv2019存在吗emmm");
//                                    }
//                                } else if (arg[1].substring(1).equals("作曲家") || arg[1].startsWith("composer")) {
//                                    String query = arg[2];//arg[1].replaceFirst("作曲家|[Cc]omposer", "");
//
//                                    List<BofSong> bofSongs = get_bofxv_contents(0, query, "composer");
//                                    if(bofSongs.size()>0){
//                                        sb.append("——作曲家相关曲目——").append(System.lineSeparator());
//                                        for(BofSong bofSong:bofSongs){
//                                            sb.append("歌曲名：").append(bofSong.getSong_name()).append("总分：").append(bofSong.getTotal_pts()).append("中位分：")
//                                                    .append(bofSong.getMedian_pts()).append("总评星数：").append(bofSong.getStars());
//                                        }
//                                    }
//                                    else{
//                                        sb.append("你确定这个作曲家参加了bofxv2019吗qwq");
//                                    }
//                                } else {
//                                    List<BofSong> bofSongs = get_bofxv_contents(0, arg[1], "song_name");
//                                    sb.append("-——默认查询：歌曲信息——").append(System.lineSeparator());
//                                    if(bofSongs.size()>0){
//                                        BofSong bofSong = bofSongs.get(0);
//                                        sb.append("歌曲名：").append(bofSong.getSong_name()).append("作曲家：").append(bofSong.getComposer());
//                                        sb.append("总分：").append(bofSong.getTotal_pts()).append("中位分：").append(bofSong.getMedian_pts());
//                                        sb.append("总评星数：").append(bofSong.getStars()).append("所属队伍：").append(bofSong.getTeam_name());
//                                    }
//                                    else{
//                                        sb.append("你确定bofxv2019有这首歌吗qaq");
//                                    }
//                                }
//                            } else {
//                                //bofxv <nums>从第x名开始5个人，打印队伍，歌曲，作者，总点数，中位点数，星数
//
//                                List<BofSong> bofSongs = get_bofxv_contents(num, "", "");
//                                if(bofSongs.size()<=0){
//                                    sb.append("查询失败……");
//                                }
//                                else{
//                                    sb.append("bof歌曲排名（前").append(num).append("名）").append(System.lineSeparator());
//                                    for (BofSong bofSong : bofSongs) {
//                                        sb.append("歌曲名：").append(bofSong.getSong_name()).append("作曲家：").append(bofSong.getComposer());
//                                        sb.append("总分：").append(bofSong.getTotal_pts()).append("中位分：").append(bofSong.getMedian_pts());
//                                        sb.append("总评星数：").append(bofSong.getStars()).append("所属队伍：").append(bofSong.getTeam_name());
//                                        sb.append(System.lineSeparator());
//                                    }
//                                }
//                            }
//                        }
//                        mb.setCoolQ(CQ).setTarget(fromGroup).append(sb.toString()).sendGroupMsg();
//                    }
//                    returnvalue = 1;
//                    break;
//                }
    //endregion
}
