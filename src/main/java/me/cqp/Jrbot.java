package me.cqp;

import me.cqp.JRbot.PersonelConfig;
import me.cqp.JRbot.Utils.SysInfo;
import me.cqp.JRbot.Utils.misc.JobUtils;
import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.botManageGUI.ConfigWindow;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import me.cqp.JRbot.modules.Arcaea.Arcaea;
import me.cqp.JRbot.modules.BotLightning;
import me.cqp.JRbot.modules.CloudBlocklist;
import me.cqp.JRbot.modules.Repeat;
import me.cqp.JRbot.privatefunction;
import me.cqp.JRbot.publicfunction;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.entity.*;
import org.meowy.cqp.jcq.entity.enumerate.Authority;
import org.meowy.cqp.jcq.entity.enumerate.EventType;
import org.meowy.cqp.jcq.event.JcqApp;
import org.meowy.cqp.jcq.event.JcqListener;
import org.meowy.cqp.jcq.message.CoolQCode;
import org.meowy.cqp.jcq.message.GroupMsg;
import org.meowy.cqp.jcq.message.MsgBuffer;
import org.meowy.cqp.jcq.message.PrivateMsg;
import org.meowy.cqp.jcq.util.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static me.cqp.JRbot.PersonelConfig.*;
import static me.cqp.JRbot.Utils.misc.DateUtils.getDateStr;
import static me.cqp.JRbot.Utils.misc.dbutils.testConnection;
import static me.cqp.JRbot.modules.BotLightning.getCurrentAliveGroups;
import static me.cqp.JRbot.modules.BotLightning.struck_pro;
import static me.cqp.JRbot.publicfunction.GroupManage;
import static me.cqp.JRbot.publicfunction.ModuleManager;


public class Jrbot extends JcqApp implements ICQVer, IMsg, IRequest, JcqListener {
    public static CoolQ CQ;
    private boolean enable = false;
    public static String tempfilestr = "";
    public static String run_token = "";
    private static Scheduler scheduler = null;
    public static CoolQCode CC;
    public static ArrayList<Long> botlist = new ArrayList<>();
//    private final BotWebSocketServer server = new BotWebSocketServer(new InetSocketAddress("localhost", BotConstants.WEBSOCKET_PORT),true);

    public Jrbot(CoolQ CQ) {
        super(CQ);
        Jrbot.CQ = CQ;
        CC = new CoolQCode();
    }

    /**
     * 本函数【禁止】处理其他任何代码，以免发生异常情况。
     * 如需执行初始化代码请在 startup 事件中执行（Type=1001）。
     */
    public String appInfo() {
        // 应用AppID,规则见 http://d.cqp.me/Pro/开发/基础信息#appid
        String AppID = "me.cqp.jrbot";// 记住编译后的文件和json也要使用appid做文件名
        return CQAPIVER + "," + AppID;
    }

    /**
     * 酷Q启动 (Type=1001)<br>
     * 本方法会在酷Q【主线程】中被调用。<br>
     * 请在这里执行插件初始化代码。<br>
     * 请务必尽快返回本子程序，否则会卡住其他插件以及主程序的加载。
     *
     * @return 请固定返回0
     */
    public int startup() {
        // 获取应用数据目录(无需储存数据时，请将此行注释)
        String appDirectory = CQ.getAppDirectory();
        // 返回如：D:\CoolQ\app\org.meowy.cqp.jcq\app\包名
        // 应用的所有数据、配置【必须】存放于此目录，避免给用户带来困扰。
        CQ.logDebug("Settings::appDirectory", appDirectory);
        String datestr = getDateStr();
        File f = Paths.get(appDirectory, "temp", datestr).toFile();
        tempfilestr = f.getAbsolutePath();
        CQ.logDebug("Startup::create_temp", "temp file path:" + tempfilestr);
        if (!f.mkdirs()) {
            CQ.logWarning("Startup::create_temp", "Failed to create directory,maybe it has already been created.");
        } else {
            CQ.logInfo("Startup::create_temp", "temp dir create success.");
        }
        File backupsqls = Paths.get(appDirectory, "temp", "backupsqls").toFile();
        if (!backupsqls.exists()) {
            if (backupsqls.mkdirs()) {
                CQ.logInfo("Startup::create_temp", "Create directory for backup sqls");
            }
        }
        run_token = webutils.getToken();

        if (run_token.isEmpty()) {
            webLogging.addLog("error","Startup","warning! run token is empty!");
            return 0;
        }

        try {
            scheduler = JobUtils.getInstance();
            JobUtils.processJobs();
        } catch (SchedulerException e) {
            e.printStackTrace();
            CQ.logError("Scheduler", e);
            webLogging.addLog("error","Startup:scheduler", e.toString());
        }
        debug.checkAPIUsage();


        if (!testConnection()) {
            webLogging.addLog("error","Startup","warning! Database connection is lost!");
            return 0;
        }

        initSettings();
//        server.start();

        return 0;
    }

    /**
     * 酷Q退出 (Type=1002)<br>
     * 本方法会在酷Q【主线程】中被调用。<br>
     * 无论本应用是否被启用，本函数都会在酷Q退出前执行一次，请在这里执行插件关闭代码。
     *
     * @return 请固定返回0，返回后酷Q将很快关闭，请不要再通过线程等方式执行其他代码。
     * FIXME: 2020/8/20 test if it runs well
     */
    public int exit() {
//        timer.shutdown();
        try {
            JobUtils.getInstance().shutdown();
        } catch (SchedulerException e) {
            webLogging.addLog("warning","Scheduler",e.toString());
        }
        try {
            webutils.getDocument("http://xxx.com/jrbot/deactivate",
                    BotConstants.BOT_HEADER, Connection.Method.POST, null, run_token, BotWebMethods.URLENCODED, "");
        } catch (IOException ignored) {

        }
//        try {
//            server.stop();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }


        return 0;
    }

    /**
     * 应用已被启用 (Type=1003)<br>
     * 当应用被启用后，将收到此事件。<br>
     * 如果酷Q载入时应用已被启用，则在 {@link #startup startup}(Type=1001,酷Q启动) 被调用后，本函数也将被调用一次。<br>
     * 如非必要，不建议在这里加载窗口。
     *
     * @return 请固定返回0。
     */
    public int enable() {
        enable = true;
        return 0;
    }

    /**
     * 应用将被停用 (Type=1004)<br>
     * 当应用被停用前，将收到此事件。<br>
     * 如果酷Q载入时应用已被停用，则本函数【不会】被调用。<br>
     * 无论本应用是否被启用，酷Q关闭前本函数都【不会】被调用。
     *
     * @return 请固定返回0。
     */
    public int disable() {
        enable = false;
        return 0;
    }

    /**
     * 私聊消息 (Type=21)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType 子类型，11/来自好友 1/来自在线状态 2/来自群 3/来自讨论组
     * @param msgId   消息ID
     * @param fromQQ  来源QQ
     * @param msg     消息内容
     * @param font    字体
     * @return 返回值*不能*直接返回文本 如果要回复消息，请调用api发送<br>
     * 这里 返回  {@link IMsg#MSG_INTERCEPT MSG_INTERCEPT} - 截断本条消息，不再继续处理<br>
     * 注意：应用优先级设置为"最高"(10000)时，不得使用本返回值<br>
     * 如果不回复消息，交由之后的应用/过滤器处理，这里 返回  {@link IMsg#MSG_IGNORE MSG_IGNORE} - 忽略本条消息
     */
    public int privateMsg(int subType, int msgId, long fromQQ, String msg, int font) {
        // 这里处理消息
        PrivateMsg pm = new PrivateMsg(EventType.PrivateMsg, msg, msgId, fromQQ);
        switch (privatefunction.PrivateMsgParser(pm)) {
            case 0:
            default:
                return MSG_IGNORE;
            case 1:
                return MSG_INTERCEPT;
        }
    }

    /**
     * 群消息 (Type=2)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType       子类型，目前固定为1
     * @param msgId         消息ID
     * @param fromGroup     来源群号
     * @param fromQQ        来源QQ号
     * @param fromAnonymous 来源匿名者
     * @param msg           消息内容
     * @param font          字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMsg(int subType, int msgId, long fromGroup, long fromQQ, String fromAnonymous, String msg,
                        int font) {
        // 如果消息来自匿名者
        if (fromQQ == 1000000L || (under_maintence && fromQQ != developer_qq))
            return MSG_IGNORE;
//        if (fromQQ == 80000000L && !fromAnonymous.equals("")) {
//            // 将匿名用户信息放到 anonymous 变量中
//            Anonymous anonymous = CQ.getAnonymous(fromAnonymous);
//
//        }
        boolean hasCalls = false;

        Map<Long, String> map = trigger;

        // region Bot manage
        if (msg.startsWith("sudo")) {

            //shortcut to manage module
            if (msg.startsWith("sudo apt")) {
                int ind = msg.indexOf('t');
                String[] args = msg.substring(ind + 1).trim().split(" ");
                ModuleManager(fromGroup, fromQQ, args);
            }
            //shortcut to groupmod module
            else if (msg.startsWith("sudo groupmod")) {
                int ind = msg.indexOf('d', 4);
                String msg2 = msg.substring(ind + 1).trim();
                GroupManage(new GroupMsg(msg2, msgId, fromGroup, fromQQ));
            } else if (msg.startsWith("sudo trigger-get")) {
                CQ.sendGroupMsg(fromGroup, String.format("当前群可以使用[%s]来触发本群功能", map.getOrDefault(fromGroup, trigger_word)));
            } else if (msg.equals("sudo sysinfo") && fromQQ == developer_qq) {
                CQ.sendGroupMsg(fromGroup, SysInfo.sysDebug());
            } else {
                CQ.logDebug("SUDO COMMAND", "Not implemented");
            }
            return MSG_INTERCEPT;
        } else if (msg.startsWith("#")) {
            // enter manage mode
            if (fromQQ == developer_qq && "#sys".equals(msg)) {
                CQ.sendGroupMsg(fromGroup, SysInfo.sysDebug());
                return MSG_INTERCEPT;
            }
            if (!map.containsKey(fromGroup) && !is_group_activated(fromGroup)) {
                if ("#init".equals(msg) && CQ.getGroupMemberInfo(fromGroup, fromQQ).getAuthority() != Authority.MEMBER) {
                    publicfunction.InitGroup(fromGroup);
                }
                if ("#quit".equals(msg) && CQ.getGroupMemberInfo(fromGroup, fromQQ).getAuthority() != Authority.MEMBER) {
                    CQ.setGroupLeave(fromGroup, false);
                }
                return MSG_IGNORE;
            }
            String s = msg.substring(1);
            if (s.startsWith("#")) {
                ModuleManager(fromGroup, fromQQ, s.substring(1).trim().split(" "));
            } else {
                GroupMsg gm = new GroupMsg(msg, msgId, fromGroup, fromQQ);
                GroupManage(gm);
            }
            return MSG_INTERCEPT;
        } else if (msg.startsWith("/")) {
            Map<String, Map<String, Boolean>> map1 = getEnabledStatbyGroup(fromGroup);
            if(msg.startsWith("/arc")&&map1.getOrDefault("arcaea",new HashMap<>()).getOrDefault("enabled",false)){
                new Arcaea(fromGroup,fromQQ).processDirectives(new ArrayList<>(Arrays.asList(msg.split(" "))));
            }
            else if(msg.startsWith("/lightning")&&map1.getOrDefault("lightning",new HashMap<>()).getOrDefault("enabled",false)){
                new BotLightning(fromGroup,fromQQ).lightning_init();
            }
            return MSG_INTERCEPT;
        }
        //endregion

        String trigger = map.getOrDefault(fromGroup, trigger_word);
        if (trigger == null || trigger.isEmpty()) {
            trigger = trigger_word;
        }

        if (msg.startsWith(trigger)) {
            hasCalls = true;
            msg = msg.substring(trigger.length()).trim();
        }

        GroupMsg gm = new GroupMsg(msg, msgId, fromGroup, fromQQ);

        switch (publicfunction.GroupTriggerParser(gm, hasCalls)) {
            case 0:
            default:
                if (getCurrentAliveGroups().contains(fromGroup)) {
                    double r = Math.random();
                    CQ.logDebug("Lightning", String.format("%f,%f", r, struck_pro));
                    if (r <= struck_pro && !isBot(fromQQ)) {
                        BotLightning bl = new BotLightning(fromGroup, fromQQ);
                        bl.perform_struck();
                    }
                }
//                Repeat.addMsg(msg,fromGroup);
                // todo: repeat 的消息队列应该在这里判断
                //      else if (r <= repeat_pro) {
                //            repeat(fromGroup, msg.getMsg());
                //      }
                return MSG_IGNORE;
            case 1:
//                Repeat.clearGroup(fromGroup);
                return MSG_INTERCEPT;
        }

    }


    /**
     * 讨论组消息 (Type=4)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype     子类型，目前固定为1
     * @param msgId       消息ID
     * @param fromDiscuss 来源讨论组
     * @param fromQQ      来源QQ号
     * @param msg         消息内容
     * @param font        字体
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int discussMsg(int subtype, int msgId, long fromDiscuss, long fromQQ, String msg, int font) {
        return MSG_IGNORE;
    }

    /**
     * 群文件上传事件 (Type=11)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType   子类型，目前固定为1
     * @param sendTime  发送时间(时间戳)// 10位时间戳
     * @param fromGroup 来源群号
     * @param fromQQ    来源QQ号
     * @param file      上传文件信息
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupUpload(int subType, int sendTime, long fromGroup, long fromQQ, String file) {
        GroupFile groupFile = CQ.getGroupFile(file);
        if (groupFile == null) { // 解析群文件信息，如果失败直接忽略该消息
            return MSG_IGNORE;
        }
        // 这里处理消息
        return MSG_IGNORE;
    }

    /**
     * 群事件-管理员变动 (Type=101)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/被取消管理员 2/被设置管理员
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupAdmin(int subtype, int sendTime, long fromGroup, long beingOperateQQ) {
        if (beingOperateQQ == bot_qq) {
            switch (subtype) {
                case 1:
                    CQ.sendGroupMsg(fromGroup, "[提示]群管理及挂闪电功能将无法正常运行");
                    break;
                case 2:
                    break;
                default:
                    CQ.logFatal("onGroupAdmin", "Invalid value for group subtype: {}", subtype);
            }
        }
        return MSG_IGNORE;
    }

    /**
     * 群事件-群成员减少 (Type=102)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/群员离开 2/群员被踢
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(仅子类型为2时存在)
     * @param beingOperateQQ 被操作QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberDecrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 这里处理消息
        if (subtype == 1) {
            String msg = exit.getOrDefault(fromGroup, "");
            if (!msg.isEmpty()) {
                CQ.sendGroupMsg(fromGroup, msg);
            }
            CQ.logInfo("GroupMemberChange", beingOperateQQ + "离开了群" + fromGroup);
        }
        if (subtype == 2) {
            if (beingOperateQQ == bot_qq) {
                //bot being kicked out
                Group p = CQ.getGroupInfo(fromGroup,true);
                webLogging.addLog("warning","GroupLeave::Kicked","Bot kicked from group "+(p == null ? fromGroup : p.toString()),
                        Instant.now(),false);
                prepare_for_group_dropout(fromGroup);
                Repeat.onGroupDrop(fromGroup);
            }
        }
        return MSG_IGNORE;
    }

    /**
     * 群事件-群成员增加 (Type=103)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype        子类型，1/管理员已同意 2/管理员邀请
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ(即管理员QQ)
     * @param beingOperateQQ 被操作QQ(即加群的QQ)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupMemberIncrease(int subtype, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ) {
        // 这里处理消息
        MsgBuffer mb = new MsgBuffer();
        if (beingOperateQQ == bot_qq) {
            mb.setCoolQ(CQ).append("欢迎使用JRbot！为了保证bot功能的正确运行，在使用之前，请务必阅读" +
                    "JRbot使用协议（xxx.html）及使用方法，")
                    .append("并在7天内及时使用指令激活使用bot。")
                    .newLine()
                    .append("有任何其他问题，请使用bot提供的反馈功能")
                    .newLine()
                    .append("[警告]如果7天内未激活，bot将退出群聊")
                    .sendGroupMsg(fromGroup);
            Group newg = CQ.getGroupInfo(fromGroup,true);
            if (PersonelConfig.prepare_for_new_group(newg)) {
                current_groups = CQ.getGroupList();
                webLogging.addLog("info","GroupAdd","Bot add to group " + newg.toString(),
                        Instant.now(),false);
            }
            return MSG_IGNORE;
        }
        String welcomeMsg = welcome.getOrDefault(fromGroup, PersonelConfig.welcomeMsg);
        if (!welcomeMsg.isEmpty()) {
            mb.setCoolQ(CQ).at(beingOperateQQ).append(welcomeMsg).sendGroupMsg(fromGroup);
        }
        return MSG_IGNORE;
    }

    /**
     * 好友事件-好友已添加 (Type=201)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype  子类型，目前固定为1
     * @param sendTime 发送时间(时间戳)
     * @param fromQQ   来源QQ
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int friendAdd(int subtype, int sendTime, long fromQQ) {
        // 这里处理消息
        MsgBuffer msb = new MsgBuffer();
        msb.setCoolQ(CQ).setTarget(fromQQ).append("欢迎添加JRbot为好友！可以把我拉入其他群玩耍哦~")
                .newLine().append("基本使用方法请参考这里~").append("xxx.html").sendPrivateMsg();

        CQ.sendPrivateMsg(fromQQ, "有任何意见可以使用'#反馈 [要反馈内容]'私聊本bot~");

        webLogging.addLog("info","FriendAdd","已添加" + fromQQ + "为好友");

        return MSG_IGNORE;
    }

    /**
     * 请求-好友添加 (Type=301)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，目前固定为1
     * @param sendTime     发送时间(时间戳)
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddFriend(int subtype, int sendTime, long fromQQ, String msg, String responseFlag) {
        // 这里处理消息

        /**
         * REQUEST_ADOPT 通过
         * REQUEST_REFUSE 拒绝
         */
        if (under_maintence) {
            CQ.setFriendAddRequest(responseFlag, IRequest.REQUEST_REFUSE, "Bot 功能维护中，请暂缓添加好友");
        } else {
            CQ.setFriendAddRequest(responseFlag, IRequest.REQUEST_ADOPT);
        }

//        CQ.logInfo("FriendAdd", "欲添加" + fromQQ + "为好友，该请求发送于" + new Date(sendTime) + "。附言为" + msg + StringUtils.lineSeparator + "反馈标识为" + responseFlag);
        return MSG_IGNORE;
    }

    /**
     * 请求-群添加 (Type=302)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subtype      子类型，1/他人申请入群 2/自己(即登录号)受邀入群
     * @param sendTime     发送时间(时间戳)
     * @param fromGroup    来源群号
     * @param fromQQ       来源QQ
     * @param msg          附言
     * @param responseFlag 反馈标识(处理请求用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int requestAddGroup(int subtype, int sendTime, long fromGroup, long fromQQ, String msg,
                               String responseFlag) {
//        CQ.logDebug("addgroup","msg: {},response flag:{} ",msg,responseFlag);
        // 这里处理消息
        /**
         * REQUEST_ADOPT 通过
         * REQUEST_REFUSE 拒绝
         * REQUEST_GROUP_ADD 群添加
         * REQUEST_GROUP_INVITE 群邀请
         */
        if (under_maintence) return MSG_IGNORE;
        if (subtype == 1) { // 本号为群管理，判断是否为他人申请入群

        }

        if (subtype == 2) {
            ArrayList<String> arr = new ArrayList<String>() {{
                add("check-abuse");
                add("group");
                add(String.valueOf(fromGroup));
            }};
            CloudBlocklist cbl = CloudBlocklist.getInstance();
            if (cbl.addSession(fromGroup).processDirectives(arr) == 0) {
                CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_ADOPT, "ok");// 同意进受邀群
                Repeat.onGroupAdd(fromGroup);
                cbl.clearSession();
            } else {
                CQ.setGroupAddRequest(responseFlag, REQUEST_GROUP_INVITE, REQUEST_REFUSE, String.format("此群目前处于黑名单中，联系机器人开发者qq %d 来解封或使用#反馈私聊bot", developer_qq));
                webLogging.addLog("info", "GroupAdd::BlocklistRefused",
                        String.format("Trying to add group %d [msg: %s]", fromGroup, msg), Instant.now(), false);
                CQ.logInfo("GroupAdd", "Trying to add group {} [msg: {}]", fromGroup, msg);
            }
        }

        return MSG_IGNORE;
    }

    /**
     * 群事件-群禁言 (Type=104)<br>
     * 本方法会在酷Q【线程】中被调用。<br>
     *
     * @param subType        子类型，1/被解禁 2/被禁言
     * @param sendTime       发送时间(时间戳)
     * @param fromGroup      来源群号
     * @param fromQQ         操作者QQ
     * @param beingOperateQQ 被操作QQ(若为全群禁言/解禁，则本参数为 0)
     * @param duration       禁言时长(单位 秒，仅子类型为2时可用)
     * @return 关于返回值说明, 见 {@link #privateMsg 私聊消息} 的方法
     */
    public int groupBan(int subType, int sendTime, long fromGroup, long fromQQ, long beingOperateQQ, long duration) {
        if (subType == 2) {
            if (beingOperateQQ == CQ.getLoginQQ()) {
                // post abuse use
                webLogging.addLog("info","GroupBan","Bot banned by "+fromQQ + " for "+duration,
                        Instant.now(),false);
            }
        }
        return MSG_IGNORE;
    }

    /**
     * 本函数会在JCQ【线程】中被调用。
     *
     * @return 固定返回0
     */
    public int display_author_info() {
        JOptionPane.showMessageDialog(null, "jrbot!" + StringUtils.lineSeparator +
                "Developer:J.R" + StringUtils.lineSeparator + "从2019/2/6开始，使用java+酷q配合开发");
        return 0;
    }

    /**
     * 本函数会在酷Q【线程】中被调用。
     * todo:将弹出个人配置菜单
     *
     * @return 固定返回0
     */
    public int show_configurations() {
        JOptionPane.showMessageDialog(null, "开发中");
        return 0;
    }

    public int open_webmanager() {
        try (Socket connect = new Socket()) {
            connect.connect(new InetSocketAddress("localhost", BotConstants.LISTEN_PORT), 100);//建立连接
            boolean res = connect.isConnected();//通过现有方法查看连通状态
            CQ.logDebug("WebManager::Connected", String.valueOf(res));//true为连通
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "webapi连接未开启！", "Error!", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
        if (java.awt.Desktop.isDesktopSupported()) {
            try {
                // 创建一个URI实例
                java.net.URI uri = java.net.URI.create(BotConstants.BOT_WEBMANAGER_ROOT);
                // 获取当前系统桌面扩展
                java.awt.Desktop dp = java.awt.Desktop.getDesktop();
                // 判断系统桌面是否支持要执行的功能
                if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    // 获取系统默认浏览器打开链接
                    dp.browse(uri);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "打开浏览器出错！", "Error!", JOptionPane.ERROR_MESSAGE);
            }
        }
        return 0;
    }

    public int module_manager() {
        new ConfigWindow();
        return 0;
    }

}
