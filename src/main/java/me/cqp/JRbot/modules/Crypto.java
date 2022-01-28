package me.cqp.JRbot.modules;

import me.cqp.JRbot.Utils.Message_Digest;
import me.cqp.JRbot.Utils.crypto;
import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.message.MsgBuffer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class Crypto implements BaseModule {
    private final long fromGroup;
    private final long fromQQ;

    enum CryptoDirState {
        MESSAGE_DIGEST, CRYPT
    }

    public Crypto(long group, long QQ) {
        this.fromGroup = group;
        this.fromQQ = QQ;
    }

    /**
     * GET /api/v1/bot/module/crypto/algorithms
     *
     * @return a list of string representing the supported algorithms
     */
    public static List<String> getProvidedCryptoAlgorithms() {
        try{
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/crypto/algorithms",
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                JSONArray ja = jbo.getJSONObject("content").getJSONArray("provided_algorithms");
                List<Object> list = ja.toList();
                List<String> l2 = new ArrayList<>();
                for (Object obj : list) {
                    if (obj instanceof String)
                        l2.add((String) obj);
                }
                return l2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private int crypt_inner(String command, boolean encode, String msg, String key) {
        int ret = 0;
        String restxt;
        MsgBuffer msb = new MsgBuffer();
        msb.setCoolQ(CQ);
        String a;
        if (encode) {
            a = "加密";
        } else {
            a = "解密";
        }
        switch (command) {
            case "base64":
                ret = 1;
                if (encode) restxt = crypto.base64("encode", msg);
                else restxt = crypto.base64("decode", msg);
                msb.at(fromQQ).newLine()
                        .append("您的明文为[").append(msg).append("]").newLine()
                        .append("BASE64编码结果为：").append(restxt).sendGroupMsg(fromGroup);
                break;
            case "des":
                ret = 1;
                if (key.length()!=8){
                    CQ.sendGroupMsg(fromGroup,"<Error> DES要求密钥具有8位长度");
                    return ret;
                }
                if (encode) {
                    restxt = crypto.encryptDES(msg, key);
                } else {
                    restxt = crypto.decryptDES(msg, key);
                }
                msb.at(fromQQ).newLine()
                        .append("您的明文为[").append(msg).append("]").newLine()
                        .append("DES").append(a).append("结果为 ").append(restxt).sendGroupMsg(fromGroup);
                break;
            case "3des":
                ret = 1;
                if (encode) {
                    restxt = crypto.encode3Des(key, msg);
                } else {
                    restxt = crypto.decode3Des(key, msg);
                }
                msb.at(fromQQ).newLine()
                        .append("您的明文为[").append(msg).append("]").newLine()
                        .append("3DES").append(a).append("结果为").append(restxt).sendGroupMsg(fromGroup);
            default:
                msb.append("暂不支持该算法！").newLine().append("使用方法：").append(helpMsg()).sendGroupMsg(fromGroup);
                break;
        }
        return ret;
    }

    private int md_inner(String command, String msg) {

        int ret = 0;
        String restxt;
        MsgBuffer msb = new MsgBuffer();
        msb.setCoolQ(CQ);
        switch (command) {
            case "md5":
                restxt = Message_Digest.MD5("enc", msg);
                ret = 1;
                msb.at(fromQQ).newLine()
                        .append("您的明文为:[").append(msg).append("]").newLine()
                        .append("MD5摘要后的结果为：").append(restxt).sendGroupMsg(fromGroup);
                break;
            case "sha1":
                restxt = Message_Digest.sha("enc", msg, "sha-1");
                ret = 1;
                msb.at(fromQQ).newLine()
                        .append("您的明文为:[").append(msg).append("]").newLine()
                        .append("sha1摘要后的结果为：").append(restxt).sendGroupMsg(fromGroup);
                break;
            case "sha256":
                restxt = Message_Digest.sha("enc", msg, "sha-256");
                ret = 1;
                msb.at(fromQQ).newLine()
                        .append("您的明文为:[").append(msg).append("]").newLine()
                        .append("sha256摘要后的结果为：").append(restxt).sendGroupMsg(fromGroup);
                break;
            case "sha512":
                restxt = Message_Digest.sha("enc", msg, "sha-512");
                ret = 1;
                msb.at(fromQQ).newLine()
                        .append("您的明文为:[").append(msg).append("]").newLine()
                        .append("sha512摘要后的结果为：").append(restxt).sendGroupMsg(fromGroup);
                break;
            default:
                msb.append("暂不支持该算法！").newLine().append("使用方法：").append(helpMsg()).sendGroupMsg(fromGroup);
                break;
        }
        return ret;
    }

    public String name() {
        return "me.cqp.jrbot.crypto";
    }

    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        List<String> alist = getProvidedCryptoAlgorithms();
        directives.remove("crypto");
        directives.remove("密码学");
        CQ.logDebug("Crypto::Arguments", directives.toString());
        int len = directives.size();
        if (len == 0) {
            CQ.sendGroupMsg(fromGroup, helpMsg());
            return 1;
        }
        DirProcessState state = DirProcessState.PROCESS_DIR;
        CryptoDirState s = null;
        int ret = 0;
        int index = 0;
        while (index < len && state != DirProcessState.END) {
            String current = directives.get(index);
            CQ.logDebug("Crypto", "processing {}", current);
            CQ.logDebug("Crypto::ProcessState", state.name());

            switch (state) {
                case PROCESS_DIR:
                    switch (current) {
                        case "help":
                        case "-h":
                        case "--help":
                        case "帮助":
                        case "/?":
                        case "?":
                        default:
                            state = DirProcessState.END;
                            CQ.sendGroupMsg(fromGroup, helpMsg());
                            break;
                        case "md":
                        case "digest":
                        case "摘要":
                            state = DirProcessState.PROCESS_PARAM;
                            s = CryptoDirState.MESSAGE_DIGEST;
                            index++;
                            break;
                        case "encode":
                        case "decode":
                        case "enc":
                        case "dec":
                        case "加密":
                        case "解密":
                            state = DirProcessState.PROCESS_PARAM;
                            s = CryptoDirState.CRYPT;
                            index++;
                            break;
                    }
                    break;
                case PROCESS_PARAM:
                    switch (s) {
                        case MESSAGE_DIGEST:
                            if (alist.contains(current)) { //current = alg
                                String msg;
                                if (alist.contains("/m") && alist.indexOf("/m") != alist.size() - 1) {
                                    msg = directives.get(directives.indexOf("/m") + 1);
                                    ret = md_inner(current, msg);
                                    state = DirProcessState.END;
                                } else if (alist.indexOf(current) != alist.size() - 1) {
                                    msg = directives.get(directives.indexOf(current) + 1);
                                    ret = md_inner(current, msg);
                                    state = DirProcessState.END;
                                } else {
                                    CQ.sendGroupMsg(fromGroup, "usage: <触发词> crypto digest <alg> <msg>");
                                }
                            } else {
                                CQ.sendGroupMsg(fromGroup, "暂不支持当前算法名！");
                            }
                            break;
                        case CRYPT:
                            if (alist.contains(current)) {
                                String msg, key;
                                boolean encode = false;
                                if (directives.contains("encode") || directives.contains("enc") || directives.contains("加密")) {
                                    encode = true;
                                    directives.remove("encode");
                                    directives.remove("enc");
                                    directives.remove("加密");
                                } else if (directives.contains("decode") || directives.contains("dec") || directives.contains("解密")) {
                                    directives.remove("decode");
                                    directives.remove("dec");
                                    directives.remove("解密");
                                }
                                if (directives.contains("/m") && directives.contains("/k")
                                        && Math.abs(directives.indexOf("/m") - directives.indexOf("/k")) != 1
                                        && (directives.indexOf("/k") != directives.size() - 1) || directives.indexOf("/m") != directives.size() - 1) {
                                    //crypto enc [alg] /m "message" /k "key"
                                    msg = directives.get(directives.indexOf("/m") + 1);
                                    key = directives.get(directives.indexOf("/k") + 1);
                                    msg = msg.substring(1, msg.length() - 1);
                                    key = key.substring(1, key.length() - 1);
                                    ret = crypt_inner(current, encode, msg, key);
                                } else if (directives.indexOf(current) == directives.size() - 3) {
                                    //crypto enc [alg] "message" "key"
                                    msg = directives.get(directives.indexOf(current) + 1);
                                    key = directives.get(directives.indexOf(current) + 2);
                                    msg = msg.substring(1, msg.length() - 1);
                                    key = key.substring(1, key.length() - 1);
                                    ret = crypt_inner(current, encode, msg, key);
                                } else {
                                    CQ.sendGroupMsg(fromGroup, "usage: <触发词> crypto [encode|decode] <alg> <msg> <key>");
                                }
                            } else {
                                CQ.sendGroupMsg(fromGroup, "暂不支持当前算法名！");
                            }
                            break;
                    }
            }
        }
        return ret;
    }

    @Override
    public String apiEndpointName() {
        return BaseModule.super.apiEndpointName() + "crypto/";
    }
}
