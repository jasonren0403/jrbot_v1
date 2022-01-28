package me.cqp.JRbot.entity;

public enum BotWebMethods {
    URLENCODED(1),  //application/x-www-form-urlencoded
    JSON(2),    //application/json
    PLAIN_TEXT(4),  //text/plain
    FORM_DATA(8),//multipart/form-data
    XML(16); //text/xml

    private int value;

    private BotWebMethods(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static BotWebMethods valueOf(int value) {
        switch (value) {
            case 1:
            default:
                return URLENCODED;
            case 2:
                return JSON;
            case 4:
                return PLAIN_TEXT;
            case 8:
                return FORM_DATA;
            case 16:
                return XML;
        }
    }

}
