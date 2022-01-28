package me.cqp.JRbot;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;

import me.cqp.JRbot.entity.jobs.backupSQL;
import me.cqp.JRbot.modules.Arcaea.ArcClient;
import me.cqp.JRbot.modules.Arcaea.ArcClientOffline;
import me.cqp.JRbot.modules.Arcaea.ArcDao;
import me.cqp.JRbot.modules.Arcaea.Arcaea;
import me.cqp.JRbot.modules.BotLightning;
import me.cqp.JRbot.modules.Dice;
import me.cqp.JRbot.modules.Repeat;
import me.cqp.JRbot.Utils.SysInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.entity.enumerate.Authority;
import org.meowy.cqp.jcq.message.CQCode;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.message.PrivateMsg;
import org.meowy.cqp.jcq.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;
import static me.cqp.JRbot.PersonelConfig.*;
import static me.cqp.JRbot.PersonelConfig.get_inner_name;

public class privatefunction {

    private static void testCQAPI(String command) {
        MsgBuffer mb = new MsgBuffer();
        CQ.logDebug("testCQAPI", String.format("In cqapi test, command %s", command));
        switch (command.trim()) {
            case "图片":
            case "pic": {
                if (CQ.canSendImage()) {
                    mb.setCoolQ(CQ).append("发送图片测试").image("testImage.jpeg").sendPrivateMsg(developer_qq);
                } else {
                    CQ.logWarning("CQAPITest", "Current CQ version cannot support image.");
                }

                break;
            }
            case "语音":
            case "record": {
                if (CQ.canSendRecord()) {
                    mb.setCoolQ(CQ).append("发送语音测试").record("testAudio.amr").sendPrivateMsg(developer_qq);
                } else {
                    CQ.logWarning("CQAPITest", "Current CQ version cannot send record!");
                }

                break;
            }
            case "分享音乐":
            case "shared_music": {
                mb.setCoolQ(CQ).append("分享歌曲测试").music("https://music.163.com/#/song?id=434659966",
                        "http://m701.music.126.net/20190410135705/53ef6e5dc7fb4a033111b6ecfad829f5/jdyyaac/540c/0708/020c/cf3fd1a09a0b2127d5c75f9e53ad02b2.m4a",
                        "Conflict (Vocaloid Version)",
                        "In a desperate conflict with a ruthless enemy....",
                        "http://p2.music.126.net/y5Z-8Vnz6DVLQkDKdn2Hpw==/109951163927003280.jpg").sendPrivateMsg(developer_qq);

                break;
            }
            case "握手":
            case "shake": {
                mb.setCoolQ(CQ).append("握手消息测试").shake().sendPrivateMsg(developer_qq);
                break;
            }
            case "定位":
            case "location": {
                mb.setCoolQ(CQ).append("定位消息测试").location(90, 90, 15, "Test", "Test location").sendPrivateMsg(developer_qq);
                break;
            }
            case "分享":
            case "share": {
                mb.setCoolQ(CQ).append("分享消息测试").share("www.f*********.**", "***********", "Test content", "").sendPrivateMsg(developer_qq);
                break;
            }
            case "名片":
            case "contact": {
                mb.setCoolQ(CQ).append("名片测试").contact("qq", developer_qq).sendPrivateMsg(developer_qq);
                break;
            }
            default:
                CQ.sendPrivateMsg(developer_qq, "未指定测试项目");
        }
    }

    /**
     * @param pm 主函数封装好的message对象
     * @return 0, 主函数将return MSG_IGNORE
     * 1, 主函数将return MSG_INTERCEPT
     */
    public static int PrivateMsgParser(PrivateMsg pm) {
        MsgBuffer mb = new MsgBuffer();
        long fromQQ = pm.getQQId();
        String msg = pm.getMsg();

        msg = CQCode.decode(msg);
        if (msg.startsWith("#init") || msg.startsWith("#初始化")) {
            // init <group-id>
            msg = msg.replaceAll("#init", "").replaceAll("#初始化", "").trim();

            if (!msg.matches("\\d{5,11}")) mb.setCoolQ(CQ).setTarget(fromQQ).append("请输入正确的群号！").sendPrivateMsg();
            else {
                long l = Long.parseUnsignedLong(msg);
                Group c = CQ.getGroupInfo(l);
                if (c == null) {
                    mb.setCoolQ(CQ).setTarget(fromQQ).append("请输入正确的群号！").sendPrivateMsg();
                } else {
                    Member m = CQ.getGroupMemberInfo(l, fromQQ, true);
                    if (m == null || m.getAuthority() == Authority.MEMBER) {
                        mb.setCoolQ(CQ).setTarget(fromQQ).append("请让此群的管理员私聊调用此指令").sendPrivateMsg();
                    } else {
                        publicfunction.InitGroup(l);
                        mb.setCoolQ(CQ).setTarget(fromQQ).append("初始化中，请稍候").sendPrivateMsg();
                    }
                }
            }
            return 1;
        }
        if (msg.startsWith("sudo") && fromQQ == developer_qq) {
            if (msg.startsWith("sudo modprobe")) {
                int index = "sudo modprobe".indexOf('e');
                bot_module_setting(msg.substring(index).trim());
            } else if ("sudo GO SILENT".equals(msg)) {
                if (PersonelConfig.toggle_pub_msg(true)) CQ.sendPrivateMsg(developer_qq, "关闭消息接收成功！");
                else CQ.sendPrivateMsg(developer_qq, "关闭接收消息失败！");
            } else if ("sudo GO SILENT /false".equals(msg)) {
                if (PersonelConfig.toggle_pub_msg(false)) CQ.sendPrivateMsg(developer_qq, "打开消息接收成功！");
                else CQ.sendPrivateMsg(developer_qq, "打开接收消息失败！");
            } else if ("sudo stats".equals(msg)) {
                debug.get_stats();
            } else if ("sudo sysstat".equals(msg)) {
                CQ.sendPrivateMsg(developer_qq, SysInfo.sysDebug());
            } else if ("sudo backsql".equals(msg)) {
                backupSQL.mainJob();
                CQ.sendPrivateMsg(developer_qq, "ok");
            } else {
                String sb = "可用sudo命令列表" + "\n" + "modprobe " + "模块管理" + "\n" +
                        "GO SILENT " + "关闭消息接收，/false打开" + "\n" +
                        "stats " + "获得当前状况" + "\n" +
                        "sysstat " + "系统运行时状况" + "\n" +
                        "backsql " + "备份bot数据库";
                CQ.sendPrivateMsg(developer_qq, sb);
            }
        } else if (msg.startsWith("#")) {
            if (fromQQ == developer_qq) {
                if (msg.startsWith("##")) {
                    bot_module_setting(msg.substring(2).trim());
                } else {
                    bot_base_setting(msg.substring(1).trim());
                }
            }
            if (msg.contains("反馈") && (msg.startsWith("#反馈") || msg.startsWith("# 反馈"))) {
                if (msg.length() > 3) {
                    String todeveloper = msg.substring(msg.indexOf("馈") + 1).trim();
                    debug.debug_message(fromQQ + "在" + new Date().toString() + "时向您反馈如下内容：" + StringUtils.lineSeparator + todeveloper);
                    //todo: add to request database
                    mb.setCoolQ(CQ).setTarget(fromQQ).append("您的反馈已送达开发者，请等待回复~").face(176).sendPrivateMsg();
                } else
                    mb.setCoolQ(CQ).setTarget(fromQQ).append("请仍以“#反馈”开头，重新输入反馈内容").sendPrivateMsg();
            }
        } else {
            if (under_maintence) {
                CQ.sendPrivateMsg(fromQQ, "Bot维护中，请稍后使用其私聊功能");
            }
            if (msg.startsWith("私聊掷色子") || msg.startsWith("dice") || msg.startsWith("扔色子")) {
                msg = msg.replace("私聊掷色子", "").replace("dice", "")
                        .replace("扔色子", "");
                new Dice(fromQQ).processDirectives(new ArrayList<>(Arrays.asList(msg.split(" "))));
            } else if (msg.startsWith("arc查最近") || msg.startsWith("arcaea查最近") || msg.startsWith("recent")) {
                Arcaea arc = new Arcaea(fromQQ);
                arc.getRecent();
            } else if (msg.startsWith("arc查最棒") || msg.startsWith("arcaea查最棒") || msg.startsWith("best")) {
                String query;
                if (msg.contains("棒")) {
                    query = msg.substring(msg.indexOf('棒') + 1).trim();
                } else {
                    query = msg.replace("best", "").trim();
                }
                String[] querys = query.split(" ");
                Arcaea arc = new Arcaea(fromQQ);
                if (querys.length == 2) {
                    String song = querys[0];
                    String diff = querys[1];
                    arc.getBestPlay(song, diff);
                } else {
                    System.out.println(Arrays.toString(querys));
                    CQ.sendPrivateMsg(fromQQ, "Usage: best <歌曲> <难度>");
                }

            }
        }
        return 1;
    }


    private static void bot_module_setting(String msg) {
        //提前合并双引号括起来的内容
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(msg);
        while (m.find())
            list.add(m.group(1).replace("\"", ""));

        if (list.contains("/?") || list.size() == 0) {
            show_module_setting_help();
            return;
        }
        if (list.contains("repeat")) {
            //  ## repeat set-pro <a double value>
            if (list.indexOf("repeat") == list.size() - 1) {
                CQ.sendPrivateMsg(developer_qq, "usage: 设置复读几率：repeat set-pro <一个0~1间的小数值>");
                return;
            }
            if (list.contains("set-pro")) {
                if (list.indexOf("set-pro") == list.size() - 1) {
                    CQ.sendPrivateMsg(developer_qq, "缺少参数！usage: repeat set-pro <num>");
                } else {
                    String pro = list.get(list.indexOf("set-pro") + 1);
                    if (pro.matches("0\\.[0-9]+")) {
                        Repeat.setRepeatProbability(Double.parseDouble(pro));
                        CQ.sendPrivateMsg(developer_qq, "设置成功！");
                    } else {
                        CQ.sendPrivateMsg(developer_qq, String.format("参数 %s 错误！", pro));
                    }
                }
            } else {
                CQ.sendPrivateMsg(developer_qq, "usage: 设置复读几率：repeat set-pro <一个0~1间的小数值>");
            }
        } else if (list.contains("lightning")) {
            // ## lightning set-pro <a double value>
            // ## lightning set-off <groupid>
            if (list.indexOf("lightning") == list.size() - 1) {
                CQ.sendPrivateMsg(developer_qq, "usage: 设置挨劈几率：lightning set-pro <一个0~1间的小数值>");
                return;
            }
            if (list.contains("set-pro")) {
                if (list.indexOf("set-pro") == list.size() - 1) {
                    CQ.sendPrivateMsg(developer_qq, "缺少参数！usage: lightning set-pro <num>");
                } else {
                    String pro = list.get(list.indexOf("set-pro") + 1);
                    if (pro.matches("0\\.[0-9]+")) {
                        BotLightning.setStruckProbability(Double.parseDouble(pro));
                        CQ.sendPrivateMsg(developer_qq, "设置成功！");
                    } else {
                        CQ.sendPrivateMsg(developer_qq, String.format("参数 %s 错误！", pro));
                    }
                }
            } else {
                CQ.sendPrivateMsg(developer_qq, "usage: 设置挨劈几率：lightning set-pro <一个0~1间的小数值>");
            }
        } else if (list.contains("arcaea")) {
            if (list.indexOf("arcaea") == list.size() - 1) {
                CQ.sendPrivateMsg(developer_qq, "usage: reinit 重新初始化此模块\n update /maj 更新主版本\n update /api 更新arcapi");
                return;
            }
            if (list.contains("reinit")) {
                ArcClient.reinit();
                Arcaea.users = ArcDao.poolInit();
                CQ.sendPrivateMsg(developer_qq, "重新初始化成功！");
            } else if (list.contains("update")) {
                int index = list.indexOf("update");
                // ## arcaea update /maj
                if (index == list.size() - 2 || index == list.size() - 1) {
                    CQ.sendPrivateMsg(developer_qq, "usage: update [/maj|/api]");
                    return;
                }
                String sec = list.get(index + 1);
                if ("/maj".equals(sec)) {
                    String ver = list.get(index + 2);
                    ArcClient.updateMAJVER(ver);
                    CQ.sendPrivateMsg(developer_qq, "更新Arcaea主版本成功！");
                } else if ("/api".equals(sec)) {
                    int apiver = Integer.parseInt(list.get(index + 2));
                    ArcClient.updateAPIVER(apiver);
                    CQ.sendPrivateMsg(developer_qq, "更新ArcAPI版本成功！");
                } else {
                    CQ.sendPrivateMsg(developer_qq, "usage: update [/maj|/api]");
                }
            } else if (list.contains("arc-debug")) {
                CQ.sendPrivateMsg(developer_qq, ArcClient.debug_msg());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Pool Users: ").append(StringUtils.lineSeparator);
                sb2.append("------------------------").append(StringUtils.lineSeparator);
                sb2.append(String.format("%-8s|%-56s|%-36s|%-10s\n", "uid", "authorization", "deviceid", "platform"));
                for (ArcClientOffline o : ArcDao.getUsers()) {
                    sb2.append(String.format("%-8d|%-56s|%-36s|%-10s", o.getArcuid(), o.getAuthorization(),
                            o.getDeviceId(), o.getPlatform())).append(StringUtils.lineSeparator);
                }
                sb2.append("------------------------").append(StringUtils.lineSeparator);
                CQ.sendPrivateMsg(developer_qq, sb2.toString());
            } else if (list.contains("set-token")) {
                int index = list.indexOf("set-token");
                int index2 = list.indexOf("/id");
                if (-1 == index2) {
                    CQ.sendPrivateMsg(developer_qq, "需要指定修改token的arcuid！");
                } else {
                    if (index2 == list.size() - 1) {
                        CQ.sendPrivateMsg(developer_qq, "未指定id和token！");
                    } else if (index2 == list.size() - 2) {
                        CQ.sendPrivateMsg(developer_qq, "未指定token！");
                    } else {
                        long arcuid = Long.parseLong(list.get(index2 + 1));
                        String accessToken = list.get(index2 + 2).replace("\"", "");
                        CQ.logInfo("Params", "arcuid:{},accesstoken:{}", arcuid, accessToken);
                        if (!accessToken.startsWith("Bearer ")) {
                            CQ.sendPrivateMsg(developer_qq, "token需要以Bearer 开头");
                        } else {
                            if (ArcDao.UpdateAccessToken(arcuid, accessToken)) {
                                CQ.sendPrivateMsg(developer_qq, String.format("更新成功！%s", accessToken));
                            } else {
                                CQ.sendPrivateMsg(developer_qq, "更新失败！");
                            }
                        }
                    }
                }
            } else if (list.contains("add-connect-subscribe")) {
                CQ.sendPrivateMsg(developer_qq, list.subList(1, list.size()).toString());
//                connectTask.subscribeAlert()
            } else {
                CQ.sendPrivateMsg(developer_qq, "usage: reinit 重新初始化此模块\n update /maj 更新主版本\n update /api 更新arcapi\nshow-pool 查看帐号池状况\nset-token /id [id] [token] 手动设置新的token\nadd-connect-subscribe 添加新的订阅");
            }
        }
    }

    private static void show_module_setting_help() {
        MsgBuffer msb = new MsgBuffer();
        msb.setCoolQ(CQ).setTarget(developer_qq)
                .append("可用命令列表").newLine()
                .append("[repeat]").newLine()
                .append("set-pro <小数> 设置复读几率").newLine()
                .append("[lightning]").newLine()
                .append("set-pro <小数> 设置挨劈几率").newLine()
                .append("[arcaea]").newLine()
                .append("reinit 重新初始化此模块").newLine()
                .append("update /maj 更新Arcaea主版本").newLine()
                .append("update /api 更新ArcApi主版本").newLine()
                .append("arc-debug 查看模块状况").newLine()
                .append("set-token /id [id] [token] 手动设置新的token").newLine()
                .append("以上所有命令需要以##开头").newLine()
                .append("# x 进入bot debug命令").newLine()
                .sendPrivateMsg();
    }

    private static void bot_base_setting(String msg) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(msg);
        while (m.find())
            list.add(m.group(1).replace("\"", ""));

        if (list.contains("/?")) {
            show_debug_help();
            return;
        }
        if (list.contains("--reload") || list.contains("/reload")) {
            CQ.logInfo("Settings::Reload", "重新载入设置中");
            PersonelConfig.reloadSettings();
            CQ.sendPrivateMsg(developer_qq, "Bot全局设置重载成功！");
            CQ.logInfoSuccess("Settings::Reload", "载入完成");
        } else if (list.contains("--list-modules") || list.contains("/ls")) {
            ModuleInnerManage("show-list", "", "");
        } else if (list.contains("--add-modules") || list.contains("/install")) {
            list.remove("--add-modules");
            list.remove("/install");
            if (!list.contains("/id") || !list.contains("/name")) {
                CQ.sendPrivateMsg(developer_qq, "未指定id和name！");
                return;
            }
            int id_index = list.indexOf("/id");
            String module_id = list.get(id_index + 1);
            int name_index = list.indexOf("/name");
            if (name_index - id_index == 1) {
                CQ.sendPrivateMsg(developer_qq, "未指定id！添加bot可安装模块需要参数/id /name）");
                return;
            } else if (name_index == list.size() - 1) {
                CQ.sendPrivateMsg(developer_qq, "未指定name！添加bot可安装模块需要参数/id /name）");
                return;
            }
            String module_name = list.get(name_index + 1);
            ModuleInnerManage("add", module_id, module_name);
        } else if (list.contains("--remove-modules") || list.contains("/uninstall")) {
            list.remove("--remove-modules");
            list.remove("/uninstall");
            if (!list.contains("/id")) {
                CQ.sendPrivateMsg(developer_qq, "未指定id！移除bot可安装模块需要参数/id ）");
                return;
            }
            int id_index = list.indexOf("/id");
            if (id_index == list.size() - 1) {
                CQ.sendPrivateMsg(developer_qq, "未指定id！移除bot可安装模块需要参数/id ");
                return;
            }
            String module_id = list.get(id_index + 1);
            ModuleInnerManage("delete", module_id, "");
        } else if (list.contains("--test") || list.contains("/test")) {
            testCQAPI(msg.replace("--test", "").replace("/test", ""));
        } else if (list.contains("--show-groups") || list.contains("/groups")) {
            List<Group> list2 = CQ.getGroupList();
            StringBuilder sb = new StringBuilder();
            sb.append("当前群列表--CQ.getGroupList");
            sb.append(StringUtils.lineSeparator);
            for (Group p : list2) {
                sb.append("groupid: ").append(p.getId()).append(" groupname: ").append(p.getName()).append(StringUtils.lineSeparator);
            }
            CQ.sendPrivateMsg(developer_qq, sb.toString());
        } else if (list.contains("--announce")) {
            int ind = msg.indexOf('"') + 1;
            int lastind = msg.indexOf('"', ind);
            String s1 = msg.substring(ind, lastind).replace("\\n", StringUtils.lineSeparator).replace("\\t", "\t");
            // # --announce "xxxxxx" /send-groups 114514,1919810
            if (list.contains("/send-groups")) {
                List<String> l = list.subList(list.indexOf("/send-groups") + 1, list.size());
                for (String s : l) {
                    for (String c : s.split(",")) {
                        try {
                            CQ.logDebug("BotAnnouncement", "Send announcement to " + c);
                            CQ.sendGroupMsg(Long.parseUnsignedLong(c), "[开发者公告]" + StringUtils.lineSeparator + s1);
                        } catch (Exception e) {
                            CQ.logWarning("BotAnnouncement", e.getMessage());
                        }
                    }

                }
            } else {
                List<Group> list2 = CQ.getGroupList();
                CQ.logDebug("BotAnnouncement", "群发公告");
                for (Group p : list2) {
                    CQ.sendGroupMsg(p.getId(), "[开发者公告]" + StringUtils.lineSeparator + s1);
                }
            }
        } else if (list.contains("--show-installed")) {
            String url = "http://xxxx.com/jrbot/group/stat";
            HashMap<String, String> map = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            if (!(list.contains("/groups"))) {
                map = null;
                sb.append("全部群的已安装模块列表").append(StringUtils.lineSeparator);
            } else {
                int index = list.indexOf("/groups");
                if (index != list.size() - 1) {
                    String ifNum = list.get(index + 1);
                    if (ifNum.matches("\\d{5,12}")) {
                        map.put("groupid", ifNum);
                    }
                }
            }
            try {
                JSONObject resp = webutils.getJSONObjectResp
                        (url, BotConstants.BOT_HEADER, Connection.Method.GET, map, run_token, BotWebMethods.URLENCODED, new JSONObject());
                if (webutils.checkJSONResp(resp)) {
                    JSONArray infos = resp.getJSONArray("contents");
                    int len = infos.length();
                    sb.append(String.format("%6s |%6s | %6s", "module_id", "enabled", "admin_only"));
                    for (int i = 0; i < len; i++) {
                        JSONObject o = infos.getJSONObject(i);
                        sb.append(StringUtils.lineSeparator).append("Group ").append(o.getLong("groupid"));
                        JSONObject o2 = o.getJSONObject("function_stat");
                        if (!o2.isEmpty()) {
                            Iterator<String> it = o2.keys();
                            while (it.hasNext()) {
                                String name = it.next();
                                JSONObject o3 = o2.getJSONObject(name);
                                sb.append(StringUtils.lineSeparator).append(String.format("%6s | %6s | %6s", name, o3.optBoolean("enabled", false), o3.optBoolean("manage_only", false)));
                            }
                        } else {
                            CQ.logWarning("show-group-installs", "Warning! the function_stat of group {} is empty! ", o.getLong("groupid"));
                        }
                    }
                    CQ.sendPrivateMsg(developer_qq, sb.toString());
                } else {
                    CQ.sendPrivateMsg(developer_qq, "usage: --show-installed [/groups <groupid>]");
                }
            } catch (IOException e) {
                CQ.sendPrivateMsg(developer_qq, e.toString());
                e.printStackTrace();
            }

        } else if (list.contains("--init-group")) {
            //--init-group <id>  // --init-group /gid <id> /silent
            boolean silent = false;
            if (list.contains("/silent")) {
                list.remove("/silent");
                silent = true;
            }
            list.remove("--init-group");
            if (list.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "usage: --init-group <id>  // --init-group /gid <id> (/silent)");
                return;
            }
            long gid = 0L;
            if (list.contains("/gid") && list.indexOf("/gid") != list.size() - 1) {
                try {
                    gid = Long.parseLong(list.get(list.indexOf("/gid") + 1));
                } catch (NumberFormatException ignored) {

                }
            } else {
                try {
                    gid = Long.parseLong(list.get(0));
                } catch (NumberFormatException ignored) {

                }
            }
            Group p = CQ.getGroupInfo(gid);
            if (p != null) {
                CQ.logDebug("Private::InitGroupRemote", p.toString());
                if (PersonelConfig.prepare_for_new_group(p)) {
                    CQ.sendPrivateMsg(developer_qq, "初始化成功！");
                    if (!silent) {
                        CQ.sendGroupMsg(gid, "[开发者提醒] bot已被开发者初始化~");
                    }

                } else {
                    CQ.sendPrivateMsg(developer_qq, "初始化失败！");
                }
            } else {
                CQ.sendPrivateMsg(developer_qq, "请输入合法的群号！");
            }
        } else if (list.contains("--install-remote")) {
            //--install-remote <module_id|module_name> /gid <id>

            list.remove("--install-remote");
            if (list.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "usage: --install-remote <module_id|module_name> /gid <id>");
                return;
            }
            long gid = 0L;
            String module_name = list.remove(0);
            list.remove("/gid");
            if (list.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "Must specify groupid!");
                return;
            }
            try {
                gid = Long.parseLong(list.get(0));
                if (CQ.getGroupInfo(gid) == null) throw new Exception("Invalid group id!");
            } catch (Exception e) {
                CQ.logWarning("Private::install-remote", e.getMessage());
            }
            List<String> installable = readSupportedInstallList();
            String inner_name = get_inner_name(module_name);
            if (!installable.contains(inner_name) || !installable.contains(module_name)) {
                CQ.sendPrivateMsg(developer_qq, String.format("这个不能装吧？可安装id" +
                        "列表：[%s] 可使用# /ls查看对应关系", installable.toString()));
                return;
            }
            if (change_func_state(inner_name, gid, true, "")) {
                CQ.sendPrivateMsg(developer_qq, "安装成功！");
            } else {
                CQ.sendPrivateMsg(developer_qq, "安装失败！");
            }
        } else if (list.contains("--remove-remote")) {
            //todo: there is bug!!!
            //--remove-remote <module_id|module_name> /gid <id>
            list.remove("--remove-remote");
            if (list.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "usage: --remove-remote <module_id|module_name> /gid <id>");
                return;
            }
            long gid = 0L;
            String module_name = list.remove(0);
            list.remove("/gid");
            if (list.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "Must specify groupid!");
                return;
            }
            try {
                gid = Long.parseLong(list.get(0));
                if (CQ.getGroupInfo(gid) == null) throw new Exception("Invalid group id!");
            } catch (Exception e) {
                CQ.logWarning("Private::remove-remote", e.getMessage());
            }
            List<String> listc = get_group_func_list(gid, "all");
            if (!listc.contains(module_name)) {
                CQ.sendPrivateMsg(developer_qq, String.format("这个群没有装过这个模块！已安装列表：%s", listc.toString()));
                return;
            }
            String inner_name = get_inner_name(module_name);
            if (inner_name.isEmpty()) {
                CQ.sendPrivateMsg(developer_qq, "Empty inner_name get!");
            } else if (change_func_state(inner_name, gid, false, "")) {
                CQ.sendPrivateMsg(developer_qq, "模块[" + module_name + "]移除成功，若仍想使用该模块");
            } else {
                CQ.sendPrivateMsg(developer_qq, "模块[" + module_name + "]卸载失败，请私聊本bot反馈bug");
            }
        } else {
            show_debug_help();
        }
    }

    private static void show_debug_help() {
        MsgBuffer msb = new MsgBuffer();
        msb.setCoolQ(CQ).setTarget(developer_qq)
                .append("可用Debug命令列表").newLine()
                .append("--announce：").append("发送bot公告信息(须将公告信息包含在英文双引号中,前后不能再有引号,可以用/send-groups指定要发送信息的群组，内部用英文逗号分离)").newLine()
                .append("--reload或/reload: ").append("重载bot全局设置").newLine()
                .append("--list-modules或/ls: ").append("查看bot所有可安装模块列表").newLine()
                .append("--add-modules或/install: ").append("添加bot可安装模块（需要参数/id /name）").newLine()
                .append("--remove-modules或/uninstall: ").append("移除bot可安装模块（并不会从群中卸载模块，需要参数/id ）").newLine()
                .append("--test或/test: ").append("测试酷Q API 发图(pic)/语音(record)/分享音乐(shared_music)/分享(share)/窗口震动(shake)/定位(location)/名片(contact)").newLine()
                .append("--show-groups或/groups：").append("查看当前bot入群列表").newLine()
                .append("--show-installed：").append("查看某群内安装应用的列表，加入/groups 参数来指定要查看的群").newLine()
                .append("--init-group：<gid> [/silent]").append("远程初始化某群设置").newLine()
                .append("--install-remote：").append("远程安装某群模块").newLine()
                .append("--remove-remote：").append("远程卸载某群模块").newLine()
                .append("/?: ").append("重新显示本帮助").newLine()
                .append("以上所有命令需要以#开头").newLine()
                .append("sudo GO SILENT").append("：关闭群消息接收功能").append(" /false：重新打开").newLine()
                .append("sudo modprobe xx或## xx 进入模块设置命令")
                .sendPrivateMsg();
    }

    private static void ModuleInnerManage(String command, String module_id, String module_name) {
        CQ.logInfo("Settings::ModuleInnerManage", String.format("触发模块列表debug，动作%s", command));
        MsgBuffer mb = new MsgBuffer();
        HashMap<String, String> map = new HashMap<>();
        Connection.Method cm;
        String api_url = "";
        switch (command) {
            case "show-list":
                // get
                cm = Connection.Method.GET;
                api_url = "http://xxxx.com/jrbot/module/List";
                break;
            case "add":
                // post id = {id}&name = {name}
                if (module_id.isEmpty() || module_name.isEmpty()) {
                    mb.setCoolQ(CQ).setTarget(developer_qq).append("模块添加操作，模块id和名称不能为空！")
                            .sendPrivateMsg();
                    return;
                }
                cm = Connection.Method.POST;
                api_url = "http://xxxx.com/jrbot/module/List/add";
                map.put("id", module_id);
                map.put("name", module_name);
                break;
            case "delete":
                // post id = {id}
                if (module_id.isEmpty()) {
                    mb.setCoolQ(CQ).setTarget(developer_qq).append("模块删除操作，模块id不能为空！")
                            .sendPrivateMsg();
                    return;
                }
                cm = Connection.Method.POST;
                map.put("id", module_id);
                api_url = "http://xxxx.com/jrbot/module/List/delete";
                break;
            default:
                mb.setCoolQ(CQ).setTarget(developer_qq)
                        .append("模块管理部分可用命令列表：").newLine()
                        .append("[待填帮助]").newLine()
                        .sendPrivateMsg();
                return;
        }
        try {
            JSONObject jbo = webutils.getJSONObjectResp(api_url, BotConstants.BOT_HEADER, cm
                    , map, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                switch (command) {
                    case "show-list":
                        mb.setCoolQ(CQ).setTarget(developer_qq).append("模块id-name对照表").newLine();
                        JSONArray ja = jbo.getJSONObject("content").getJSONArray("available_module_list");
                        int l = ja.length();
                        for (int i = 0; i < l; i++) {
                            JSONObject c = ja.getJSONObject(i);
                            mb.append("id: ").append(c.getString("id")).append("  name: ").append(c.getString("name"))
                                    .newLine();
                        }
                        mb.sendPrivateMsg();
                        break;
                    case "delete":
                        mb.setTarget(developer_qq).setCoolQ(CQ).append("模块id[").append(module_id).append("]删除成功！")
                                .sendPrivateMsg();
                        break;
                    case "add":
                        mb.setTarget(developer_qq).setCoolQ(CQ).append("模块[").append(module_name).append("]添加成功！")
                                .sendPrivateMsg();
                        break;
                }
            } else {
                mb.setCoolQ(CQ).setTarget(developer_qq)
                        .append("操作失败！").newLine()
                        .append(jbo.toString()).sendPrivateMsg();
            }
        } catch (IOException e) {
            e.printStackTrace();
            mb.setCoolQ(CQ).setTarget(developer_qq)
                    .append("操作失败！").newLine()
                    .append(e.toString()).sendPrivateMsg();
        }

    }


}
