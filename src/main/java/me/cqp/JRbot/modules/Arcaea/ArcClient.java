package me.cqp.JRbot.modules.Arcaea;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.util.StringUtils;

import java.io.IOException;
import java.util.*;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class ArcClient {
    private final RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(15000)
            .setConnectTimeout(15000)
            .setConnectionRequestTimeout(15000)
            .setCookieSpec(CookieSpecs.STANDARD)
            .build();
    public static final String BOT_API_URL = "https://xxxx.com/jrbot/module/arcaea/settings/";
    public static final String ARC_API_BASE = "https://arcapi.lowiro.com/coffee/";
    protected static int ARC_API_VER;
    protected static String ARC_APP_VER;
    protected static boolean useClient;


    static {
        JSONObject jbo = null;
        try {
            jbo = webutils.getJSONObjectResp(BOT_API_URL, BotConstants.BOT_HEADER,
                    Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                ARC_APP_VER = jbo.getJSONObject("content").optString("arc_app_ver","3.0.3");
                ARC_API_VER = jbo.getJSONObject("content").optInt("arc_api_ver",12);
                useClient = jbo.getJSONObject("content").optBoolean("use_account_pool",false);
            } else {
                CQ.logWarning("ArcClient::Init", "Init failed! Try reinit by calling ##arcaea reinit");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CQ.logError("ArcClient::Init","failed to init arcaea client because {}",e.toString());
        }

    }

    public static final int ARC_APP_MINOR = 1;
    public static String UA_IOS = String.format("Arc-mobile/%s.%d CFNetwork/758.5.3 Darwin/15.6.0", ARC_APP_VER, ARC_APP_MINOR);
    public static final String UA_ANDROID = "Dalvik/2.1.0 (Linux; U; Android 8.0.0;)";
    public static final String UA_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";

    public static void reinit() {
        try{
            JSONObject jbo = webutils.getJSONObjectResp(BOT_API_URL, BotConstants.BOT_HEADER,
                    Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                ARC_APP_VER = jbo.getJSONObject("content").optString("arc_app_ver","3.0.3");
                ARC_API_VER = jbo.getJSONObject("content").optInt("arc_api_ver",12);
                useClient = jbo.getJSONObject("content").optBoolean("use_account_pool",false);
            } else {
                CQ.logWarning("ArcClient::ReInit", "ReInit failed! Try reinit by calling ##arcaea reinit");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CQ.logError("ArcClient::Reinit","Init communication error {}",e.toString());
        }

    }

    public static String debug_msg() {
        return "Using pool: " + useClient + StringUtils.lineSeparator +
                "Arcaea Client UA(IOS): " + ArcClient.UA_IOS + StringUtils.lineSeparator
                + "Version:" + ArcClient.ARC_APP_VER + StringUtils.lineSeparator + "ARCAPI VER: " + ArcClient.ARC_API_VER;
    }

    public static boolean toggleHide(long fromQQ,boolean hide){
        try{
            JSONObject jbo1 = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ+"/hide",
                    BotConstants.BOT_HEADER, Connection.Method.GET,
                    new HashMap<String,String>(){{
                        put("hide", String.valueOf(hide));
                    }},run_token,BotWebMethods.URLENCODED,new JSONObject());
            return webutils.checkJSONResp(jbo1);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getUcode(long fromQQ){
        try{
            JSONObject jbo = webutils.getJSONObjectResp("https://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ+"/ucode",
                    BotConstants.BOT_HEADER, Connection.Method.GET,null,run_token,BotWebMethods.URLENCODED,new JSONObject());
            if(webutils.checkJSONResp(jbo)) return jbo.getJSONObject("content").optString("arcucode","error");
            return "error";
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static boolean toggleMode(long fromQQ,int mode){
        try{
            JSONObject jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/module/arcaea/bind/"+fromQQ+"/mode",
                    BotConstants.BOT_HEADER, Connection.Method.POST,new HashMap<String,String>(){{
                        put("mode",String.valueOf(mode));
                    }},
                    run_token,BotWebMethods.URLENCODED,new JSONObject());
            return webutils.checkJSONResp(jbo);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateMAJVER(String newver) {
        String api_endpoint = BOT_API_URL + "arc_app_ver/";
        try{
            JSONObject jbo = webutils.getJSONObjectResp(api_endpoint, BotConstants.BOT_HEADER,
                    Connection.Method.POST, new HashMap<String, String>() {{
                        put("val", newver);
                    }},
                    run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                ARC_APP_VER = jbo.getJSONObject("content").getString("arc_app_ver");
            } else {
                CQ.logWarning("ArcClient::UpdateMajVer", "Update failed! ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CQ.logWarning("ArcClient::UpdateMajVer", "Update failed! ");
        }

    }

    public static void updateAPIVER(int newver) {
        String api_endpoint = BOT_API_URL + "arc_api_ver/";
        try{
            JSONObject jbo = webutils.getJSONObjectResp(api_endpoint, BotConstants.BOT_HEADER,
                    Connection.Method.POST, new HashMap<String, String>() {{
                        put("val", String.valueOf(newver));
                    }},
                    run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo)) {
                ARC_API_VER = jbo.getJSONObject("content").getInt("arc_api_ver");
            } else {
                CQ.logWarning("ArcClient::UpdateAPIVer", "Update failed! ");
            }
        } catch (IOException e) {
            e.printStackTrace();
            CQ.logWarning("ArcClient::UpdateAPIVer", "Update failed! ");
        }

    }

    private static final Map<Integer, String> endpoints = new HashMap<Integer, String>() {
        {
            put(0, "/user/me");
            put(1, "/purchase/bundle/pack");
            put(2, "/serve/download/me/song?url=false");
            put(3, "/game/info");
            put(4, "/present/me?lang=en");
            put(5, "/world/map/me");
        }
    };
    private static final Map<String, String> agent_mapping = new HashMap<String, String>() {
        {
            put("ios", UA_IOS);
            put("android", UA_ANDROID);
            put("web", UA_WEB);
        }
    };
    private String platform;
    private String authorization;
    private int uid = 0;
    private int max_friends;
    private String username;
    private String device_id;
    private Header[] basicHeaders;
    private ArrayList<ArcUser> friends = new ArrayList<>();
    private ArrayList<BasicHeader> list = new ArrayList<BasicHeader>() {{
        add(new BasicHeader("Host", "arcapi.lowiro.com"));
        add(new BasicHeader("AppVersion", ARC_APP_VER));
        add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
        add(new BasicHeader("Accept", "*/*"));
        add(new BasicHeader("Connection", "keep-alive"));
    }};


    private static final String loginurl = ARC_API_BASE + ARC_API_VER + "/auth/login";
    private static final String gameinfourl = ARC_API_BASE + ARC_API_VER + "/game/info";
    private static final String aggregateurl = ARC_API_BASE + ARC_API_VER + "/compose/aggregate";
    private static final String friendaddurl = ARC_API_BASE + ARC_API_VER + "/friend/me/add";
    private static final String frienddelurl = ARC_API_BASE + ARC_API_VER + "/friend/me/delete";

    public ArcClient(String platform, String authorization, String device_id, int uid) {
        this.platform = platform;
        this.authorization = authorization;
        this.device_id = device_id;
        this.uid = uid;
        list.add(new BasicHeader("Authorization", authorization));
        list.add(new BasicHeader("User-Agent", agent_mapping.get(platform)));
        list.add(new BasicHeader("i", String.valueOf(uid)));

        this.basicHeaders = list.toArray(new Header[0]);
        this.aggregateCall();
        CQ.logInfo("ArcClientInit", "uid:" + uid + "\n" + Arrays.toString(basicHeaders));
    }


    public ArcClient(String platform, String username, String password, String device_id, boolean loginAlso) {
        this.platform = platform;
        this.device_id = device_id;
        this.username = username;
        if (loginAlso) {
            login(username, password, device_id, platform);
            list.add(new BasicHeader("i", String.valueOf(uid)));
            this.basicHeaders = list.toArray(new Header[0]);
        } else {
            System.out.println("Warning! Do not try to use other functions while you are not logged in.");
        }

    }

    /**
     * POST /auth/login
     *
     * @param username
     * @param password
     * @param device_id
     * @param platform
     * @return
     */
    public String login(String username, String password, String device_id, String platform) throws ArcRuntimeException {
        if(CQ!=null) CQ.logInfo("ArcClientLogin", "Login using {}:{} {} from {}", username, password, device_id, platform);
        else System.out.printf("Login using %s , from %s",username,platform);
        HttpPost post = new HttpPost(loginurl);
        post.addHeader("Host", "arcapi.lowiro.com");
        post.addHeader("AppVersion", ARC_APP_VER);
        post.addHeader("DeviceId", device_id);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        post.addHeader("Accept", "*/*");
        post.addHeader("Connection", "keep-alive");
        switch (platform.toLowerCase()) {
            case "android":
                post.addHeader("User-Agent", agent_mapping.get("android"));
                break;
            case "ios":
                post.addHeader("User-Agent", agent_mapping.get("ios"));
                break;
            case "web":
                post.addHeader("User-Agent", agent_mapping.get("web"));
                break;
        }
        post.addHeader("Authorization", webutils.BasicAuth(username, password));
        post.setEntity(new StringEntity("grant_type=client_credentials", ContentType.APPLICATION_FORM_URLENCODED));
        post.setConfig(requestConfig);
        HttpClient httpClient = HttpClients.createDefault();
        if(CQ!=null) CQ.logDebug("ArcLoginHeader", Arrays.toString(post.getAllHeaders()));
        else System.out.println(Arrays.toString(post.getAllHeaders()));
        try {
            HttpResponse responseContent = httpClient.execute(post);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
                CQ.logDebug("ArcClientLogin", content);
                CQ.logWarning("ArcClientLogin", new ArcRuntimeException("The content is not a valid jsonobject, login call failed!"));
                throw new ArcRuntimeException("The content is not a valid jsonobject, login failed!");
            }
            JSONObject jbo = new JSONObject(content);
            if (webutils.checkJSONResp(jbo)) {
                this.authorization = jbo.getString("token_type") + " " + jbo.getString("access_token");
                System.out.println("Your authorization key: " + this.authorization);
                list.add(new BasicHeader("Authorization", this.authorization));
                list.add(new BasicHeader("User-Agent", agent_mapping.get(platform)));
                basicHeaders = list.toArray(new Header[0]);
                if(CQ!=null) CQ.logInfo("ArcClientLogin", "headers changed to\n" + Arrays.toString(basicHeaders));
                else System.out.println(Arrays.toString(basicHeaders));
                this.aggregateCall();
                return jbo.getString("token_type") + " " + jbo.getString("access_token");
            } else {
                throw new ArcRuntimeException(jbo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public JSONArray aggregateWithReturn(int[] calllistids) throws ArcRuntimeException {
        JSONArray ja = new JSONArray();
        for (int i : calllistids) {
            JSONObject jbo = new JSONObject();
            jbo.put("id", i);
            jbo.put("endpoint", endpoints.get(i));
            ja.put(jbo);
        }
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("calls", ja.toString()));
        String str = "";
        try {
            str = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpGet get = new HttpGet(aggregateurl + "?" + str);
        get.setConfig(requestConfig);
        get.setHeaders(basicHeaders);
        HttpClient httpClient = HttpClients.createDefault();
        try {
            CQ.logDebug("ArcAggregateHeader:" + this.uid, Arrays.toString(get.getAllHeaders()));
            HttpResponse responseContent = httpClient.execute(get);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
                CQ.logDebug("ArcAggregate" + this.uid, "Call for uid {}", this.uid);
                CQ.logDebug("ArcAggregate" + this.uid, content);
                throw new ArcRuntimeException("The content is not a valid jsonobject, aggregate call failed!");
            }
            JSONObject j = new JSONObject(content);
            if (webutils.checkJSONResp(j)) {
                return j.getJSONArray("value");
            } else {
                throw new ArcRuntimeException(j);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArcRuntimeException arc) {
            CQ.logWarning("ArcAggregate", "Bad Header, try relogin...");
            ArcClientOffline a = ArcDao.getLoginCredential(this.uid);
            String token = a.login(a.getArcuname(), "Renziheng", a.getDeviceId(), a.getPlatform());
            ArcDao.UpdateAccessToken(a.getArcuid(), token);
        }
        return new JSONArray();
    }

    /**
     * GET /compose/aggregate?calls=[]
     */
    public ArcClient aggregateCall() throws ArcRuntimeException {
        JSONArray ja = new JSONArray();
        JSONObject jbo = new JSONObject();
        jbo.put("id", 0);
        jbo.put("endpoint", "/user/me");
        ja.put(jbo);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("calls", ja.toString()));
        String str = "";
        try {
            str = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpGet get = new HttpGet(aggregateurl + "?" + str);
        get.setConfig(requestConfig);
        get.setHeaders(basicHeaders);
        HttpClient httpClient = HttpClients.createDefault();
        try {
            if(CQ!=null) CQ.logDebug("ArcAggregateHeader", Arrays.toString(get.getAllHeaders()));
            HttpResponse responseContent = httpClient.execute(get);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
//                CQ.logDebug("ArcClientInit","Init for uid {}",this.uid);
                CQ.logDebug("ArcAggregate", content);
                CQ.logWarning("ArcAggregate", new ArcRuntimeException("The content is not a valid jsonobject, aggregate call failed!"));
                throw new ArcRuntimeException("The content is not a valid jsonobject, aggregate call failed!");
            }
            JSONObject j = new JSONObject(content);
            if (webutils.checkJSONResp(j)) {
                JSONObject val = j.getJSONArray("value").getJSONObject(0).getJSONObject("value");
                this.uid = val.getInt("user_id");
                this.max_friends = val.getInt("max_friend");
                friends = new ArrayList<>(Arrays.asList(ArcUser.fromJSON(val.getJSONArray("friends"))));
            } else {
                throw new ArcRuntimeException(j);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArcRuntimeException arc) {
            CQ.logWarning("ArcAggregate", "Bad Header, try relogin...");
            ArcClientOffline a = ArcDao.getLoginCredential(this.uid);
            String token = a.login(a.getArcuname(), "Renziheng", a.getDeviceId(), a.getPlatform());
            ArcDao.UpdateAccessToken(a.getArcuid(), token);
        }
        return this;
    }

    /**
     * GET /game/info
     */
    public boolean showGameInfo() throws ArcRuntimeException {
        HttpGet get = new HttpGet(gameinfourl);
        get.setConfig(requestConfig);
        get.setHeaders(basicHeaders);
        HttpClient httpClient = HttpClients.createDefault();
        try {
            if(CQ!=null) CQ.logDebug("ArcHeader", Arrays.toString(get.getAllHeaders()));
            HttpResponse responseContent = httpClient.execute(get);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
                //try relogin
                CQ.logDebug("ArcGameInfo", content);
                CQ.logWarning("ArcGameInfo", new ArcRuntimeException("The content is not a valid jsonobject, game info call failed!"));
                throw new ArcRuntimeException(content);
            }
            JSONObject jbo = new JSONObject(content);
            if (webutils.checkJSONResp(jbo)) {
//                System.out.println(jbo.toString(2));
                return true;
            } else {
                throw new ArcRuntimeException(jbo);
            }
        } catch (ArcRuntimeException arc) {
            CQ.logWarning("ArcGameInfo", "Bad Header, try relogin...");
            ArcClientOffline a = ArcDao.getLoginCredential(this.uid);
            String token = a.login(a.getArcuname(), "Renziheng", a.getDeviceId(), a.getPlatform());
            ArcDao.UpdateAccessToken(a.getArcuid(), token);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArcClient.class.getSimpleName() + "[", "]")
                .add("platform='" + platform + "'")
                .add("authorization='" + authorization + "'")
                .add("uid=" + uid)
                .add("device_id='" + device_id + "'")
                .toString();
    }

    public int getMaxFriends() {
        return max_friends;
    }

    public ArcUser[] getFriends() throws ArcRuntimeException {
        JSONArray ja = aggregateWithReturn(new int[]{0});
        if (ja.length() != 0)
            return ArcUser.fromJSON(aggregateWithReturn(new int[]{0}).getJSONObject(0).getJSONObject("value").getJSONArray("friends"));
        return new ArcUser[0];
    }

    public void clearFriends() throws ArcRuntimeException {
        ArcUser[] _friends = getFriends();
        long[] ids = Arrays.stream(_friends).mapToLong(ArcUser::getUser_id).toArray();
        for (long id : ids) {
            deleteFriend(id);
        }
        friends.clear();
    }

    /**
     * POST /friend/me/add
     *
     * @return map<username, uid>
     */
    public Map<String, Long> addFriend(String ucode) throws ArcRuntimeException {
        Map<String, Long> map = new HashMap<>();
        HttpPost post = new HttpPost(friendaddurl);
        post.setHeaders(basicHeaders);
        post.setConfig(requestConfig);
        post.setEntity(new StringEntity(String.format("friend_code=%s", ucode), ContentType.APPLICATION_FORM_URLENCODED));
        HttpClient httpClient = HttpClients.createDefault();
        try {
            CQ.logDebug("ArcAddFriendHeader", Arrays.toString(post.getAllHeaders()));
            HttpResponse responseContent = httpClient.execute(post);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
                //try relogin
                throw new ArcRuntimeException(content);
            }
            JSONObject jbo = new JSONObject(content);
            if (webutils.checkJSONResp(jbo)) {
                ArrayList<ArcUser> original = friends;  //this.friends
                JSONArray _friends = jbo.getJSONObject("value").getJSONArray("friends");
                friends = new ArrayList<>(Arrays.asList(ArcUser.fromJSON(_friends)));
                friends.stream().filter(u -> !original.contains(u)).findFirst().ifPresent(u -> map.put(u.getName(), u.getUser_id()));
                for (Map.Entry<String, Long> s : map.entrySet()) {
                    System.out.printf("ucode: %s -> uname: %s uid: %d%n", ucode, s.getKey(), s.getValue());
                }
                return map;
            } else {
                System.out.printf("Adding %s fault: error_code %d%n", ucode, jbo.optInt("error_code"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArcRuntimeException arc) {
            CQ.logWarning("ArcAddFriend", "Bad Header, try relogin...");
            ArcClientOffline a = ArcDao.getLoginCredential(this.uid);
            String token = a.login(a.getArcuname(), "Renziheng", a.getDeviceId(), a.getPlatform());
            ArcDao.UpdateAccessToken(a.getArcuid(), token);
        }
        return new HashMap<>();
    }

    /**
     * POST /friend/me/delete
     *
     * @param friend_id
     * @return
     */
    public boolean deleteFriend(long friend_id) throws ArcRuntimeException {
        HttpPost post = new HttpPost(frienddelurl);
        post.setHeaders(basicHeaders);
        post.setConfig(requestConfig);
        post.setEntity(new StringEntity(String.format("friend_id=%d", friend_id), ContentType.APPLICATION_FORM_URLENCODED));
        HttpClient httpClient = HttpClients.createDefault();
        try {
            if(CQ!=null) CQ.logDebug("ArcDelFriendHeader", Arrays.toString(post.getAllHeaders()));
            HttpResponse responseContent = httpClient.execute(post);
            HttpEntity valueEntity = responseContent.getEntity();
            String content = EntityUtils.toString(valueEntity);
            if (!content.startsWith("{") && !content.endsWith("}")) {
                //try relogin
                throw new ArcRuntimeException(content);
            }
            JSONObject jbo = new JSONObject(content);
            if (webutils.checkJSONResp(jbo)) {
                friends.remove(ArcUser.getUserFromList(friends, friend_id));
                return true;
            } else {
                System.out.printf("Removing %d fault: error_code %d%n", friend_id, jbo.optInt("error_code"));
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (ArcRuntimeException arc) {
            CQ.logWarning("ArcFriendDel", "Bad Header, try relogin...");
            ArcClientOffline a = ArcDao.getLoginCredential(this.uid);
            String token = a.login(a.getArcuname(), "xxxx", a.getDeviceId(), a.getPlatform());
            ArcDao.UpdateAccessToken(a.getArcuid(), token);
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArcClient arcClient = (ArcClient) o;
        return uid == arcClient.uid &&
                platform.equals(arcClient.platform) &&
                authorization.equals(arcClient.authorization) &&
                device_id.equals(arcClient.device_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, authorization, uid, device_id);
    }

}

