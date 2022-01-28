package me.cqp.JRbot.modules.Arcaea;

import java.util.StringJoiner;

public class ArcClientOffline extends ArcClient{

    private long arcuid;
    private String arcucode;
    private String arcuname;
    private String authorization;
    private String deviceId;
    private String platform;
    private boolean islocked;
    private boolean notuse;

    public ArcClientOffline(long arcuid, String arcucode, String arcuname, String authorization, String deviceId, String platform, boolean islocked, boolean notuse) {
        super(platform,arcuname,"xxxx",deviceId,false);
        this.arcuid = arcuid;
        this.arcucode = arcucode;
        this.arcuname = arcuname;
        this.authorization = authorization;
        this.deviceId = deviceId;
        this.platform = platform;
        this.islocked = islocked;
        this.notuse = notuse;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArcClientOffline.class.getSimpleName() + "[", "]")
                .add("arcuid=" + arcuid)
                .add("arcucode='" + arcucode + "'")
                .add("arcuname='" + arcuname + "'")
                .add("authorization='" + authorization + "'")
                .add("deviceId='" + deviceId + "'")
                .add("platform='" + platform + "'")
                .add("islocked=" + islocked)
                .add("notuse=" + notuse)
                .toString();
    }

    public long getArcuid() {
        return arcuid;
    }

    public void setArcuid(long arcuid) {
        this.arcuid = arcuid;
    }

    public String getArcucode() {
        return arcucode;
    }

    public void setArcucode(String arcucode) {
        this.arcucode = arcucode;
    }

    public String getArcuname() {
        return arcuname;
    }

    public void setArcuname(String arcuname) {
        this.arcuname = arcuname;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isIslocked() {
        return islocked;
    }

    public void setIslocked(boolean islocked) {
        this.islocked = islocked;
    }

    public boolean isNotuse() {
        return notuse;
    }

    public void setNotuse(boolean notuse) {
        this.notuse = notuse;
    }
}
