package me.cqp.JRbot.modules.Arcaea;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ArcRuntimeException extends RuntimeException {
    private String url;
    private final int errorCode;
    private final String errorMsg;
    private static final Map<Integer, String> errMap = new HashMap<Integer, String>() {{
        put(3, "You have been logged out by another device. Please restart Arcaea.");
        put(4, "Could not connect to online server");
        put(5, "Incorrect app version");
        put(9, "The Arcaea network is currently under maintenance.");
        put(12, "Please update Arcaea to the latest version.");
        put(104, "Incorrect username or password");
        put(105, "You've logged into over 2 devices in 24 hours. Please wait before using this new device.");
        put(106, "This account is locked.");
        put(121, "This account is locked.");
        put(122, "A temporary hold has been placed on your account. Please visit the official website to resolve the issue.");
        put(150, "This feature has been restricted for your account.");
        put(401, "This user does not exist.");
        put(601, "Your friends list is full.");
        put(602, "This user is already your friend.");
        put(604, "You can't be friends with yourself ;-;");
        put(905, "Please wait 24 hours before using this feature again.");
    }};

    public ArcRuntimeException(String msg) {
        super(msg);
        this.errorMsg = msg;
        this.errorCode = -1;
    }

    public ArcRuntimeException(int errCode) {
        this.errorCode = errCode;
        this.errorMsg = errMap.getOrDefault(errCode, "No available mapping found!");
    }

    public ArcRuntimeException(JSONObject errjsonObject) {
        if (errjsonObject.has("code")) {
            this.errorMsg = errjsonObject.getString("code") + ":" + errjsonObject.getString("message");
            this.errorCode = -1;
        } else {
            this.errorCode = errjsonObject.getInt("error_code");
            this.errorMsg = errMap.getOrDefault(errorCode,"No available mapping found!");
        }
    }
}
