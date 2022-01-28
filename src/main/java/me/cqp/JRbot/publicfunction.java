package me.cqp.JRbot;

import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.modules.Arcaea.Arcaea;
import me.cqp.JRbot.modules.*;
import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.Member;
import org.meowy.cqp.jcq.entity.enumerate.Authority;
import org.meowy.cqp.jcq.message.CoolQCode;
import org.meowy.cqp.jcq.message.GroupMsg;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.cqp.JRbot.PersonelConfig.*;
import static me.cqp.Jrbot.CQ;

public class publicfunction {

    private static String deleteAllAts(String todelete, int atCount) {
        String temp = todelete;
        for (int i = 0; i < atCount; i++) {
            Pattern p = Pattern.compile("\\[CQ:at,qq=\\d+]");
            Matcher m = p.matcher(todelete);
            if (m.find()) {
                String b = m.group(0);
                todelete = todelete.replace(b, "");
            }
        }
        CQ.logDebug("Debug::DeleteAllAts", temp + "->" + todelete);
        return todelete;
    }

    private static String deleteCQsignals(String todelete) {
        String temp = todelete;
        while (true) {
            Pattern p = Pattern.compile("\\[CQ:.+]");
            Matcher matcher = p.matcher(todelete);
            if (matcher.find()) {
                String b = matcher.group(0);
                todelete = todelete.replace(b, "");
            } else {
                CQ.logDebug("Debug::DeleteCQSignals", temp + "->" + todelete);
                return todelete;
            }
        }
    }

    /**
     * @param gm Jrbot.java传入的封装好的group message对象，已经去掉了触发词
     * @return 0, 主函数将return MSG_IGNORE
     * 1,主函数将return MSG_INTERCEPT
     */
    public static int GroupTriggerParser(GroupMsg gm, boolean _hasCall) {
        // using at
        boolean callsBot = _hasCall;
        CoolQCode CC = gm.getCoolQCode();
        String msg = gm.getMsg();
        long fromGroup = gm.getGroupId();
        long fromQQ = gm.getQQId();
        if (CC.getAt() == bot_qq) {
            callsBot = true;
            Pattern reg = Pattern.compile("\\[CQ:at,qq=" + bot_qq + "]");
            Matcher m = reg.matcher(msg);
            if (m.find()) {
                String b = m.group(0);
                msg = msg.replace(b, "").trim();
            }
        }
        //region WannaBan
        boolean banflag = msg.contains("烟我") || msg.contains("我要冷静") || msg.contains("精致睡眠") || msg.contains("好好听课");
        //[CQ:at,qq=*********]
        if (banflag) {
            IwannaBan(msg, fromQQ, fromGroup);
            return 1;
        }
        //endregion
        //region trash category
        if (msg.matches("^\\S+.*是什么垃圾$") && ModuleCanUse(fromGroup, fromQQ, "trash_category")) {
            msg = msg.replaceAll("\\s+", " ");
            msg = msg.replace("是什么垃圾", "");
            List<String> s = new ArrayList<>(Arrays.asList(msg.split(" ")));
            new TrashCategory(fromGroup, fromQQ).processDirectives(s);
            return 1;
        }
        //endregion
        ArrayList<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(msg);
        while (m.find())
            list.add(m.group(1).replace("\"", ""));
        if (list.isEmpty()) {
            list.add(msg);
        }

        return msgParser(list, fromGroup, fromQQ, callsBot);
    }

    /**
     * @param arg       参数列表
     * @param fromGroup 来源群
     * @param fromQQ    调用者qq
     * @return 1, triggerparser将return 1
     */
    private static int msgParser(ArrayList<String> arg, long fromGroup, long fromQQ, boolean callsBot) {
        MsgBuffer mb = new MsgBuffer();
        if (arg == null || arg.isEmpty() || !callsBot) return 0;
        CQ.logDebug("msgParser(public)", arg.toString());

        int retval = 0;
        String command;
        if (arg.get(0).matches("\\[CQ:.+,qq=\\d+]") && arg.size() > 1) command = arg.get(1);
        else command = arg.get(0);
        switch (command) {
            case "function":
            case "功能": {
                DisplayFunctions(fromGroup);
                retval = 1;
                break;
            }
            case "help":
            case "帮助": {
                mb.setCoolQ(CQ).setTarget(fromGroup).at(fromQQ).append(usage_main).newLine()
                        .append("当前群内触发词：").append(trigger.getOrDefault(fromGroup, "<暂未设置>")).newLine()
                        .append("在线帮助网页地址：http://****.com/static/jrbot/help/index.html").sendGroupMsg();
                retval = 1;
                break;
            }
            case "select":
            case "选择": {
                if (!ModuleCanUse(fromGroup, fromQQ, "select")) retval = 0;
                else {
                    Select s = new Select(fromGroup, fromQQ);
                    retval = s.processDirectives(arg);
                }
                break;
            }
            case "dice":
            case "扔色子": {
                if (!ModuleCanUse(fromGroup, fromQQ, "dice")) retval = 0;
                else {
                    Dice dice = new Dice(fromGroup, fromQQ);
                    retval = dice.processDirectives(arg);
                }
                break;
            }
            case "lightning":
            case "挂闪电": {
                if (!ModuleCanUse(fromGroup, fromQQ, "lightning")) retval = 0;
                else {
                    BotLightning bl = new BotLightning(fromGroup, fromQQ);
                    bl.processDirectives(arg);
                    retval = 1;
                }
                break;
            }
            case "闪电统计": {
                if (!ModuleCanUse(fromGroup, fromQQ, "lightning")) retval = 0;
                else {
                    BotLightning bl = new BotLightning(fromGroup, fromQQ);
                    bl.lightning_statistics();
                    retval = 1;
                }
                break;
            }
            case "密码学":
            case "crypto": {
                if (!ModuleCanUse(fromGroup, fromQQ, "crypto")) retval = 0;
                else {
                    Crypto cry = new Crypto(fromGroup, fromQQ);
                    retval = cry.processDirectives(arg);
                }
                break;
            }
            case "find_map":
            case "找谱":
            case "dl_map":
            case "下谱": {
                if (fromGroup == ******* || fromGroup == *******) {   //测试环境
                    PersonelMap m = new PersonelMap(fromQQ, fromGroup);
                    retval = m.processDirectives(arg);
                }
                break;
            }
            case "arc":
            case "arcaea":
            case "韵律源点": {
                if (!ModuleCanUse(fromGroup, fromQQ, "arcaea")) retval = 0;
                else {
                    Arcaea arc = new Arcaea(fromGroup, fromQQ);
                    retval = arc.processDirectives(arg);
                }
                break;
            }
            case "trash_category":
            case "垃圾分类": {
                if (!ModuleCanUse(fromGroup, fromQQ, "trash_category")) retval = 0;
                else {
                    TrashCategory tc = new TrashCategory(fromGroup, fromQQ);
                    retval = tc.processDirectives(arg);
                }
                break;
            }
            default: {
                retval = 0;
            }
        }
        return retval;
    }

    /**
     * 模块帮助，usage:##[module_stat|模块信息] [module_name]
     *
     * @param fromGroup 来源群
     * @param params    调用参数列表
     */
    private static void ModuleStat(long fromGroup, String[] params) {
        CQ.logDebug("Module::Stat", "ok");
        String user_spec_display_name = params[1];
        List<String> list = get_group_func_list(fromGroup, "full");
        if (!list.contains(user_spec_display_name)) CQ.sendGroupMsg(fromGroup, "<Error>没有这个模块，请重试...");
        else {
            String desc = get_module_desc(user_spec_display_name, "name");
            if (desc.length() != 0) {
                CQ.sendGroupMsg(fromGroup, desc);
            } else {
                CQ.sendGroupMsg(fromGroup, "usage: ##module_stat [模块名]");
                CQ.logWarning("Module::Stat", "There may be some problems with the module.");
            }
        }
    }

    /**
     * 模块管理器，列出模块列表并进行管理
     * usage:##[启用|停用|添加|删除|设置仅管理|取消仅管理|模块信息] [module_name]
     *
     * @param fromGroup 来源群
     * @param operateQQ 操作者qq
     * @param params    参数列表
     */
    public static void ModuleManager(long fromGroup, long operateQQ, String[] params) {
        MsgBuffer mb = new MsgBuffer();
        if (params.length == 1) {
            mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                    .append("##模块信息(module_help) [模块名称]").newLine()
                    .append("## /?或 ## help重新显示本帮助")
                    .sendGroupMsg();
            return;
        }
        String module_name = params[1];
        if ((module_name.equals("base") || module_name.equals("group_manage")) && operateQQ != developer_qq) {
            CQ.sendGroupMsg(fromGroup, "<Error>不可操作此模块");
            return;
        }
        String command = params[0];

        Member m = CQ.getGroupMemberInfo(fromGroup, operateQQ,true);

        CQ.logDebug("ModuleManager", "Operate member:{} Action:{} module:{}", m.toString(), command, module_name);
//Operate member:Member{groupId=529733244, qqId=2709496932, nick='test0743', card='', gender=FEMALE, age=23, area='阿尔巴尼亚', addTime=Sat Jul 04 22:32:07 CST 2020, lastTime=Sun Jul 05 15:35:58 CST 2020, levelName='冒泡', authority=MEMBER, title='', titleExpire=Thu Jan 01 08:00:00 CST 1970, bad=false, modifyCard=true} Action:install module:2709496932

        if (getManageOnlyState(getEnabledStatbyGroup(fromGroup), module_name) && m.getAuthority() == Authority.MEMBER)
            CQ.sendGroupMsg(fromGroup, "<Error>只有管理员才能操作此模块");
        else {
            switch (command.replace("##", "")) {
                case "/?":
                case "help": {
                    mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                            .append("##模块信息(module_help) [模块名称]").newLine()
                            .append("## /?或 ## help重新显示本帮助")
                            .sendGroupMsg();
                    break;
                }
                case "模块信息":
                case "module_help": {
                    ModuleStat(fromGroup, params);
                    break;
                }
                case "安装":
                case "添加":
                case "install": {
                    if (m.getAuthority() == Authority.MEMBER && operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        List<String> installable = readSupportedInstallList();
                        String inner_name = get_inner_name(module_name);
                        if (!installable.contains(inner_name) || !installable.contains(module_name))
                            CQ.sendGroupMsg(fromGroup, "你在胡乱添加些什么东西啊x(安装/卸载/启用/停用请使用模块名称)");
                        else {
                            if (inner_name.isEmpty()) {
                                CQ.logWarning("ModuleManager", "Empty inner_name get!");
                            } else if (change_func_state(inner_name, fromGroup, true, "")) {
                                CQ.logInfo("ModuleManager", fromGroup + " Installed " + module_name);
                                CQ.sendGroupMsg(fromGroup, "模块[" + module_name + "]安装成功，但为停用状态，请使用“##启用 模块名”命令启用该模块。");
                            } else {
                                CQ.logWarning("ModuleManager", fromGroup + " failed to install " + module_name);
                                CQ.sendGroupMsg(fromGroup, "模块[" + module_name + "]安装失败，请私聊本bot反馈bug");
                            }
                        }
                    }
                    break;
                }
                case "卸载":
                case "删除":
                case "uninstall": {
                    if (m.getAuthority() == Authority.MEMBER || operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        List<String> list = get_group_func_list(fromGroup, "all");
                        if (!list.contains(module_name)) {
                            CQ.sendGroupMsg(fromGroup, "没有安装模块就想移除的家伙事屑x(安装/卸载/启用/停用请使用模块名称)");
                        } else {
                            String inner_name = get_inner_name(module_name);
                            if (inner_name.isEmpty()) {
                                CQ.logWarning("ModuleManager", "Empty inner_name get!");
                            } else if (change_func_state(inner_name, fromGroup, false, "")) {
                                CQ.sendGroupMsg(fromGroup, "模块[" + module_name + "]移除成功，若仍想使用该模块，请稍后运行“##添加 模块名”命令添加该模块");
                                CQ.logInfo("ModuleManager", "Group " + fromGroup + " uninstalled " + module_name);
                            } else {
                                CQ.logWarning("ModuleManager", fromGroup + " failed to uninstall " + module_name);
                                CQ.sendGroupMsg(fromGroup, "模块[" + module_name + "]卸载失败，请私聊本bot反馈bug");
                            }
                        }
                    }
                    break;
                }
                case "开启":
                case "启用":
                case "enable":
                case "enable_module": {
                    if (CQ.getGroupMemberInfo(fromGroup, operateQQ,true).getAuthority() == Authority.MEMBER && operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        String inner_name = get_inner_name(module_name);
                        if (inner_name != null && !inner_name.isEmpty()) {
                            if (change_func_state(inner_name, fromGroup, false, "enable")) {
                                CQ.sendGroupMsg(fromGroup, "功能[" + module_name + "]已在本群启用！");
                                CQ.logInfo("ModuleManager", "Group " + fromGroup + " enabled " + module_name);
                            } else {
                                CQ.logInfo("ModuleManager::Operations:Enable", "Attempt to enable module twice!");
                            }
                        } else {
                            mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                                    .append("##模块信息(module_help) [模块外部名称]").newLine()
                                    .append("## /?或 ## help重新显示本帮助")
                                    .sendGroupMsg();
                            CQ.logError("ModuleManager::Operations:Enable", "group: " + fromGroup + ":" + inner_name + " FAILED TO ENABLE");
                        }
                    }
                    break;
                }
                case "关闭":
                case "停用":
                case "disable":
                case "disable_module": {
                    if (CQ.getGroupMemberInfo(fromGroup, operateQQ,true).getAuthority() == Authority.MEMBER && operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        String inner_name = get_inner_name(module_name);
                        if (inner_name != null && !inner_name.isEmpty()) {
                            if (change_func_state(inner_name, fromGroup, false, "disable")) {
                                CQ.sendGroupMsg(fromGroup, "功能[" + module_name + "]已在本群停用！");
                                CQ.logInfo("ModuleManager", fromGroup + " disabled " + module_name);
                            } else {
                                CQ.logInfo("ModuleManager::Operations:Disable", "Attempt to disable module twice!");
                            }
                        } else {
                            mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                                    .append("##模块信息(module_help) [模块名称]").newLine()
                                    .append("## /?或 ## help重新显示本帮助")
                                    .sendGroupMsg();
                            CQ.logError("ModuleManager::Operations:Disable", "group: " + fromGroup + ":" + inner_name + " FAILED TO DISABLE");
                        }
                    }
                    break;
                }
                case "设置仅管理":
                case "set_admin_only": {
                    if (CQ.getGroupMemberInfo(fromGroup, operateQQ,true).getAuthority() == Authority.MEMBER && operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        String inner_name = get_inner_name(module_name);
                        if (inner_name != null && !inner_name.isEmpty()) {
                            if (change_func_state(inner_name, fromGroup, false, "make_admin")) {
                                CQ.sendGroupMsg(fromGroup, "当前只有管理员和群主才可使用功能[" + module_name + "]！");
                                CQ.logInfo("ModuleManager", fromGroup + " SET ADMIN ONLY FOR " + module_name);
                            } else {
                                break;
                            }
                        } else {
                            mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                                    .append("##模块信息(module_help) [模块名称]").newLine()
                                    .append("## /?或 ## help重新显示本帮助")
                                    .sendGroupMsg();
                        }

                    }
                    break;
                }
                case "取消仅管理":
                case "unset_admin_only": {
                    if (CQ.getGroupMemberInfo(fromGroup, operateQQ,true).getAuthority() == Authority.MEMBER && operateQQ != developer_qq)
                        CQ.sendGroupMsg(fromGroup, "你不是管理员为什么要这么做嘤嘤嘤qwq");
                    else {
                        String inner_name = get_inner_name(module_name);
                        if (inner_name != null && !inner_name.isEmpty()) {
                            if (change_func_state(inner_name, fromGroup, false, "make_pub")) {
                                CQ.sendGroupMsg(fromGroup, "当前所有人都可使用功能[" + module_name + "]！");
                                CQ.logInfo("ModuleManager", fromGroup + " REVOKE ADMIN ONLY FOR " + module_name);
                            } else {
                                break;
                            }
                        } else {
                            mb.setCoolQ(CQ).setTarget(fromGroup).append("usage:##[启用(enable)|停用(disable)|添加(install)|删除(uninstall)|设置仅管理(set_admin_only)|取消仅管理(unset_admin_only)] [模块名称]").newLine()
                                    .append("##模块信息(module_help) [模块名称]").newLine()
                                    .append("## /?或 ## help重新显示本帮助")
                                    .sendGroupMsg();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }

    }


    /**
     * JRbot function:display function that the robot give
     *
     * @param fromGroup 来源群
     */
    private static void DisplayFunctions(long fromGroup) {
        StringBuilder sb = new StringBuilder();
        sb.append("[本群功能列表]").append(StringUtils.lineSeparator);
        Map<String, Map<String, Boolean>> map = getEnabledStatbyGroup(fromGroup);
        Set<String> set = map.keySet();
        for (String key : set) {
            Map<String, Boolean> map2 = map.get(key);
            String name = get_display_name(key);
            String state;
            if (name == null) continue;
            if (map2.getOrDefault("enabled", false)) {
                if (map2.getOrDefault("manage_only", false)) {
                    state = "[仅管理]";
                } else {
                    state = "[已启用]";
                }
            } else {
                state = "[未启用]";
            }
            String temp = String.format("%-6s|%s", state, name);
            sb.append(temp).append(StringUtils.lineSeparator);
        }
        CQ.sendGroupMsg(fromGroup, sb.toString());
        CQ.sendGroupMsg(fromGroup, usage_main);
        CQ.sendGroupMsg(fromGroup, "当前群内触发词：" + trigger.getOrDefault(fromGroup, "<暂未设置>"));
    }


    /**
     * 隐藏功能：求烟 调用者立刻被禁言
     * 触发：<at> or <trigger> 我要冷静&烟我-10秒，精致睡眠-8小时
     * 识别烟我xx秒/xx分/xx小时
     * 调用条件：bot权限为管理员及以上
     *
     * @param msg       群成员发送的信息
     * @param fromQQ    群成员qq号
     * @param fromGroup 群来源
     */
    private static void IwannaBan(String msg, long fromQQ, long fromGroup) {
        if (isBotManageable(fromGroup, fromQQ, bot_qq)) {
            if (msg.contains("烟我")) {
                Pattern pattern = Pattern.compile("[^\\d]+([\\d]+)[^\\d]+.*");
                Matcher matcher = pattern.matcher(msg);
                //提取数字
                if (matcher.find()) {
                    int num = Integer.parseInt(matcher.group(1));
                    if (msg.contains("分钟") || msg.contains("min"))
                        CQ.setGroupBan(fromGroup, fromQQ, num * 60L);
                    else if (msg.contains("小时") || msg.contains("hours") || msg.contains("hrs"))
                        CQ.setGroupBan(fromGroup, fromQQ, (long) num * 60 * 60);
                    else if (msg.contains("秒") || msg.contains("sec") || msg.contains("seconds")) {
                        CQ.setGroupBan(fromGroup, fromQQ, Math.max(num, 10));
                    } else
                        CQ.setGroupBan(fromGroup, fromQQ, 10);
                } else
                    CQ.setGroupBan(fromGroup, fromQQ, 10);
            }
            if (msg.contains("我要冷静"))
                CQ.setGroupBan(fromGroup, fromQQ, 15);
            if (msg.contains("精致睡眠")){
//                CQ.setGroupBan(fromGroup, fromQQ, 8 * 60 * 60);
                CQ.sendGroupMsg(fromGroup,"晚安！~~");
            }

            if (msg.contains("好好听课"))
                CQ.setGroupBan(fromGroup, fromQQ, 50 * 60);
        } else {
            CQ.sendGroupMsg(fromGroup, "嘤嘤嘤，烟不了你呀...");
        }
    }

    /**
     * bot init 初始化群设置
     *
     * @param fromGroup 来源群
     */
    public static void InitGroup(long fromGroup) {
        CQ.sendGroupMsg(fromGroup, "初始化中...请稍后尝试调用bot的部分功能");
        Group p = CQ.getGroupInfo(fromGroup);

        if (p != null) {
            if (activate_group(p.getId())) {
                CQ.sendGroupMsg(p.getId(), "激活成功~");
            } else {
                CQ.logWarning("GroupManage::Init", "FAILED!");
            }
        }
        CQ.logWarning("GroupManage::Init", "Groupid: " + fromGroup + " INIT");

    }

    /**
     * 群管理，针对群信息的管理模块，支持at
     * usage：1. #<设置动作> <设置对象> 2.#init|#初始化
     * 例如：#设置禁言 [CQ:at,qq=xxx]
     *
     * @param gm 群信息
     */
    public static void GroupManage(GroupMsg gm) {
        long fromGroup = gm.getGroupId();
        long fromQQ = gm.getQQId();
        CoolQCode CC = gm.getCoolQCode();
        String msg = gm.getMsg();

        // check user authority
        Member operator = CQ.getGroupMemberInfo(fromGroup,fromQQ,true);
        if (operator == null) {
            webLogging.addLog("warning", "GroupManage",
                    String.format("Get group member failed on group %d, QQId %d", fromGroup, fromQQ));
            return;
        }
        if (operator.getAuthority() == Authority.MEMBER && fromQQ != developer_qq) {
            return;
        }

        //check bot authority and warn
        Member bot = CQ.getGroupMemberInfo(fromGroup,bot_qq,true);
        if (bot == null) {
            webLogging.addLog("warning","GroupManage",
                    String.format("Get Bot member failed on group %d", fromGroup));
            return;
        }
        if (bot.getAuthority() == Authority.MEMBER) {
            CQ.logWarning("GroupManage::Warning", "NOT ADMIN AT GROUP id: " + fromGroup);
        }
        List<Long> operateQQs = CC.getAts();
        //filter duplicates
        Set<Long> middleHashSet = new HashSet<>(operateQQs);
        operateQQs = new ArrayList<>(middleHashSet);
        List<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(msg);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }
        long operateQQ = (CC.getAt() == -1000 || CC.getAt() == -1) ? -1 : CC.getAt();
        String command = matchList.get(0);
        if (operateQQ == -1) {
            command = deleteCQsignals(command);
            command = deleteAllAts(command, operateQQs.size());
        }

        MsgBuffer mb = new MsgBuffer();
        switch (command.replace("#", "")) {
            case "/?":
            case "/help": {
                mb.setCoolQ(CQ).setTarget(fromGroup).append("[群管理]帮助：").newLine()
                        .append("支持动作：设置禁言(ban)/解除禁言(remove-ban)").append("(ban人，at目标").newLine()
                        .append("设置欢迎语(set-welcome)").append("(进群欢迎 ").append("#set_welcome [新欢迎语|/default]").newLine()
                        .append("设置触发词(set-trigger)").append("(怎么调用本bot ").append("#set_trigger [新触发词|/default]").newLine()
                        .append("设置退出语(set-exitmsg)").append("(退群嘲讽 ").append("#set_exitmsg [新退出语|/off]").newLine()
                        .append("调用方式：").append("#<动作> <动作参数>（中间有空格）").newLine()
                        .append("详细帮助网址：https://www.****.com/static/jrbot/help/manage-group-with-bot/index.html").sendGroupMsg();
                break;
            }
            case "init":
            case "reinit":
                if(activate_group(fromGroup)){
                    CQ.sendGroupMsg(fromGroup,"激活成功！欢迎使用bot的各种功能！");
                }
                break;
            case "quit":
                if(operator.getAuthority()==Authority.OWNER) CQ.setGroupLeave(fromGroup, false);
                else CQ.sendGroupMsg(fromGroup,"真的想让我走吗QwQ 请让群主来跟我说[#quit]哦");
                break;
            case "ban":
            case "shut":
            case "设置禁言": {
                for (long l : operateQQs) {
                    Member m = CQ.getGroupMemberInfo(fromGroup, l,true);
                    if (m == null || l == bot_qq) continue;
                    if (!isBotManageable(fromGroup, l, fromQQ)) {
                        mb.setCoolQ(CQ).append("无法禁言[").append(m.getNick()).append("]：").append("权限不满足条件！").emoji(128557).sendGroupMsg(fromGroup);
                        continue;
                    }
                    CQ.setGroupBan(fromGroup, l, 60);
                    mb.setCoolQ(CQ).append("[").append(m.getNick()).append("]已被禁言，请遵守规则！").sendGroupMsg(fromGroup);
                }
                CQ.logInfo("GroupManage", operateQQs.toString() + "from group" + fromGroup + " is banned for 60s");
                break;
            }
            case "unban":
            case "remove-ban":
            case "解除禁言": {
                for (long l : operateQQs) {
                    Member m = CQ.getGroupMemberInfo(fromGroup, fromQQ,true);
                    if (m == null || l == bot_qq || !isBotManageable(fromGroup, l, fromQQ)) continue;
                    CQ.setGroupBan(fromGroup, l, 0);
                    mb.setCoolQ(CQ).append("[").append(m.getQQId()).append("]已解除禁言，下不为例！").sendGroupMsg(fromGroup);
                    CQ.logInfo("GroupManage", l + "from group" + fromGroup + " is out of ban.");
                }
                break;
            }
            case "set-welcome":
            case "设置欢迎语": {
                if (matchList.contains("--default") || matchList.contains("/default")) {
                    if (setGroupWelcomeMsg(fromGroup, "")) {
                        CQ.sendGroupMsg(fromGroup, "欢迎语回复默认状态：" + welcomeMsg);
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                } else {
                    if (matchList.size() >= 2 && setGroupWelcomeMsg(fromGroup, matchList.get(1))) {
                        CQ.sendGroupMsg(fromGroup, "已设置本群欢迎语为：" + matchList.get(1));
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                }
                break;
            }
            case "set-trigger":
            case "设置触发词": {
                if (matchList.contains("--default") || matchList.contains("/default")) {
                    if (setGroupTrigger(fromGroup, "")) {
                        CQ.sendGroupMsg(fromGroup, "触发词回复默认状态：" + trigger_word);
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                } else {
                    if (matchList.size() >= 2 && setGroupTrigger(fromGroup, matchList.get(1))) {
                        CQ.sendGroupMsg(fromGroup, "已设置本群触发词为：" + matchList.get(1));
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                }
                break;
            }
//            case "revoke":
//            case "撤回": {
//                //CQ.deleteMsg(int msgId)
//                int msgid = 0;
//                CQ.deleteMsg(msgid);
//                CQ.logInfo("GroupManage::RevokeMsg", "Delete msgid: " + msgid + "from group: " + fromGroup);
//                break;
//            }
            case "set-exitmsg":
            case "设置退出语": {
                if (matchList.size() == 1) {
                    CQ.sendGroupMsg(fromGroup, "嗯？你要设定什么退出语？usage: #set-exitmsg <exit-msg>");
                } else if (matchList.get(1).equals("/off")) {
                    if (setExitMsg(fromGroup, "")) {
                        CQ.sendGroupMsg(fromGroup, "退出语已关闭");
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                } else {
                    if (setExitMsg(fromGroup, matchList.get(1))) {
                        CQ.sendGroupMsg(fromGroup, String.format("下次再有大佬退群时，bot会发出 [%s] 的感叹", matchList.get(1)));
                    } else {
                        CQ.sendGroupMsg(fromGroup, "设置失败，请稍后再试。");
                    }
                }
                break;
            }
            case "set-whitelist":
            case "设置快速批准":
                //todo :implement this
                break;
            default:
                break;
        }
    }

}
