package me.cqp.JRbot.Utils.misc;

import me.cqp.JRbot.Utils.crypto;
import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.debug;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class webutils {

    public static String BasicAuth(String username, String password, boolean debug) {
        if (debug) System.out.println(username + ":" + password);
        return "Basic " + crypto.base64("encode", username + ":" + password);
    }

    public static String BasicAuth(String username, String password) {
        return BasicAuth(username, password, false);
    }

    public static String getToken() {
        JSONObject jbo;
        try {
            if (run_token.isEmpty()) {
                jbo = webutils.getJSONObjectResp("http://xxxx.com/jrbot/activate",
                        BotConstants.BOT_HEADER,
                        Connection.Method.POST,
                        new HashMap<String, String>() {
                            {
                                put("activate_method", "JRBOT");
                            }
                        }, null, BotWebMethods.URLENCODED, new JSONObject());
            } else {
                jbo = webutils.getJSONObjectResp("http://localhost:3001/api/v1/bot/reactivate",
                        BotConstants.BOT_HEADER,
                        Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            }
            if (!webutils.checkJSONResp(jbo)) {
                webLogging.addLog("warning","Startup::GetToken", debug.err_mesg_for_json_resp(jbo));
                return "";
            } else {
                run_token = jbo.getString("token");
                webLogging.addLog("info","Startup::GetToken", "Successfully get runtime token!");
                return jbo.getString("token");
            }
        } catch (IOException ios) {
            webLogging.addLog("warning","Startup::GetToken","Error getting token: "+ios.toString());
            return "";
        }
    }

    public static Document getDocument(String url, String userAgent, Connection.Method method, Map<String,String> data, String auth, BotWebMethods type, String jsonbody, ArrayList<Map<String,String>> addtionalHeaders) throws IOException{
        Connection conn = Jsoup.connect(url).ignoreContentType(true)
                .userAgent(userAgent).maxBodySize(Integer.MAX_VALUE).timeout(10000);
        if (type == BotWebMethods.JSON) {
            conn = conn.header("Content-Type", "application/json").requestBody(jsonbody);
        }
        if (data != null && !data.isEmpty()) {
            conn = conn.data(data);
        }
        if (auth != null && !auth.isEmpty()) {
            conn = conn.header("authorization", auth);
        }
        for (Map<String,String> m:
             addtionalHeaders) {
            conn.headers(m);
        }
        conn = conn.method(method);  //Jsoup only supports GET,POST,PUT,DELETE,PATCH,HEAD,OPTIONS,TRACE
        return conn.execute().parse();
    }

    public static Document getDocument(String url, String userAgent, Connection.Method method, Map<String, String> data, String auth, BotWebMethods type, String jsonbody) throws IOException {
        return getDocument(url,userAgent,method,data,auth,type,jsonbody,new ArrayList<>());
    }

    public static Document getDocument(String url,Connection.Method method,Map<String,String> data) throws IOException{
        return getDocument(url,BotConstants.BOT_HEADER,method,data,null,BotWebMethods.URLENCODED,new JSONObject().toString());
    }

    public static JSONObject getJSONObjectResp(String url, String userAgent, Connection.Method method, Map<String, String> data, String auth, BotWebMethods type, JSONObject jsonbody,ArrayList<Map<String,String>> addtionalHeaders) throws IOException {
        JSONObject jbo = null;
        Document doc = getDocument(url, userAgent, method, data, auth, type, jsonbody.toString(),addtionalHeaders);
        if (doc != null) {
            CQ.logDebug("webutils::path", method.name() + " "+url.replace(BotConstants.BOT_ROOT,""));
            if (data != null)
                CQ.logDebug("webutils::request parameters", data.toString());
            CQ.logDebug("webutils::JSONResponse", doc.body().text());
            try {
                jbo = new JSONObject(doc.body().text());
                return jbo;
            } catch (JSONException e) {
                String mesg = String.format("Warning, Non-JSON response get when accessing %s.", url);
                webLogging.addLog("warning","webutils::JSONResponseError", mesg + "\r\n" + e.getMessage());
            }
        }
        return new JSONObject();
    }

    public static JSONObject getJSONObjectResp(String url, String userAgent, Connection.Method method, Map<String, String> data, String auth, BotWebMethods type, JSONObject jsonbody) throws IOException {
        return getJSONObjectResp(url,userAgent,method,data,auth,type,jsonbody,new ArrayList<>());
    }

    public static Object[][] fetchTableData(String type, String token) {
        String table_name = type.replace("bot_", "");
        String additional = "module".equalsIgnoreCase(table_name) ? "other" : "time";
        String apiEndpoint = String.format("http://localhost:3001/api/v1/bot/%s?%sconfigstr=true", table_name, additional);
        try{
            JSONObject jbo = getJSONObjectResp(apiEndpoint,
                    BotConstants.BOT_HEADER, Connection.Method.GET, null, token,
                    BotWebMethods.URLENCODED, new JSONObject());
            if (checkJSONResp(jbo)) {
                JSONArray ja = jbo.getJSONArray("contents");
                Object[][] obj = new Object[ja.length()][];
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject j = ja.getJSONObject(i);
                    Map<String, Object> map = j.toMap();
                    Object[] os;
                    if ("task".equals(table_name)) {
                        os = new Object[]{map.get("task_id"), map.get("task_name"), map.get("task_description"), map.get("isdaliy"), map.get("isonce"), map.get("istaskcomplete"), map.get("execute_time_config")};
                    } else if ("module".equals(table_name)) {
                        os = new Object[]{map.get("module_inner"), map.get("in_group"), map.get("isManageOnly"), map.get("developer"), map.get("help"), map.get("name"), map.get("introduction"), map.get("other_configs")};
                    } else os = new Object[]{"column name not implemented!"};
                    obj[i] = os;
                }
                return obj;
            }
            throw new IOException("Non-true return value");
        }catch(IOException ioex){
            return new Object[0][];
        }
    }

    public static boolean checkJSONResp(JSONObject jbo) {
        return checkJSONResp(jbo, "success", true);
    }

    public static boolean checkJSONResp(JSONObject jbo, String checkKey, Object required) {
        if (jbo == null) return false;
        if (!jbo.has(checkKey)) return false;
        return jbo.get(checkKey).equals(required);
    }

}
