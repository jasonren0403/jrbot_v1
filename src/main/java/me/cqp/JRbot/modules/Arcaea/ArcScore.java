package me.cqp.JRbot.modules.Arcaea;

import me.cqp.JRbot.Utils.misc.DateUtils;
import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.meowy.cqp.jcq.util.StringUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringJoiner;

import static me.cqp.JRbot.modules.Arcaea.Arcaea.intToStr_diff_mapping;
import static me.cqp.Jrbot.CQ;

public class ArcScore {
    ///region fields
    @Nonnegative
    private final long score;
    @Nonnull
    private final Date time_played;
    @Nonnull
    private final String song_id;
    @Nonnegative
    private final int miss_count;
    @Nonnegative
    private final int shiny_perfect_count;
    @Nonnegative
    private final short modifier;
    @Nonnegative
    private final double rating;
    @Nonnegative
    private final int health;
    @Nonnegative
    private final short best_clear_type;
    @Nonnegative
    private final short clear_type;
    @Nonnegative
    private final short difficulty;
    @Nonnegative
    private final int near_count;
    @Nonnegative
    private final int perfect_count;
    @Nonnull
    private final String song_name;

    ///endregion fields
    ///region getters
    public long getScore() {
        return score;
    }

    @Nonnull
    public String getSong_name() {
        return song_name;
    }

    @Nonnull
    public Date getTime_played() {
        return time_played;
    }

    @Nonnull
    public String getSong_id() {
        return song_id;
    }

    public int getMiss_count() {
        return miss_count;
    }

    public int getShiny_perfect_count() {
        return shiny_perfect_count;
    }

    public short getModifier() {
        return modifier;
    }

    public double getRating() {
        return rating;
    }

    public int getHealth() {
        return health;
    }

    public short getBest_clear_type() {
        return best_clear_type;
    }

    public short getClear_type() {
        return clear_type;
    }

    public short getDifficulty() {
        return difficulty;
    }

    public int getNear_count() {
        return near_count;
    }

    public int getPerfect_count() {
        return perfect_count;
    }
    ///endregion setters

    private ArcScore(long score, @Nonnull Date time_played, @Nonnull String song_id, int miss_count, int shiny_perfect_count, short modifier, double rating, int health, short best_clear_type, short clear_type, short difficulty, int near_count, int perfect_count) {
        this.score = score;
        this.time_played = time_played;
        this.song_id = song_id;
        this.miss_count = miss_count;
        this.shiny_perfect_count = shiny_perfect_count;
        this.modifier = modifier;
        this.rating = rating;
        this.health = health;
        this.best_clear_type = best_clear_type;
        this.clear_type = clear_type;
        this.difficulty = difficulty;
        this.near_count = near_count;
        this.perfect_count = perfect_count;
        this.song_name = getSongName(song_id);
    }

    private ArcScore(long score, @Nonnull Date time_played, @Nonnull String song_id, int miss_count, int shiny_perfect_count, short modifier, double rating, int health, short best_clear_type, short clear_type, short difficulty, int near_count, int perfect_count, @Nonnull String song_name) {
        this.score = score;
        this.time_played = time_played;
        this.song_id = song_id;
        this.miss_count = miss_count;
        this.shiny_perfect_count = shiny_perfect_count;
        this.modifier = modifier;
        this.rating = rating;
        this.health = health;
        this.best_clear_type = best_clear_type;
        this.clear_type = clear_type;
        this.difficulty = difficulty;
        this.near_count = near_count;
        this.perfect_count = perfect_count;
        this.song_name = song_name;
    }

    public static ArcScore sampleScore() {
        //Shades of Light in a Transcendent Realm
        return new ArcScore(9980724, new Date(1145141919810L), "shadesoflight", 0,
                1278, (short) 3, 114.514, 100, (short) 3, (short) 3,
                (short) 2, 1, 1279,"Shades of light in a transcendent realm");
    }


    public static ArcScore[] fromJSON(JSONArray ja) {
        if(ja==null) return new ArcScore[0];
        ArrayList<ArcScore> list = new ArrayList<>();
        int len = ja.length();
        for (int i = 0; i < len; i++) {
            JSONObject jbo = ja.getJSONObject(i);
            ArcScore ars = fromJSON(jbo);
            if (ars != null) list.add(ars);
        }
        return list.toArray(new ArcScore[0]);
    }

    public static ArcScore fromJSON(JSONObject jbo) {
        if (!(jbo.has("song_id") && jbo.has("score") && jbo.has("time_played") && jbo.has("miss_count")
                && jbo.has("shiny_perfect_count") && jbo.has("modifier") && jbo.has("rating") && jbo.has("health")
                && jbo.has("best_clear_type") && jbo.has("clear_type") && jbo.has("difficulty")
                && jbo.has("near_count") && jbo.has("perfect_count"))) {
            System.out.println("Cannot parse corrupted object! Current keys: " + jbo.keySet().toString());
            return null;
        }
        try {
            String sid = jbo.getString("song_id");
            long score = jbo.getLong("score");
            long time_play = jbo.getLong("time_played");
            Date d = new Date(time_play);
            int miss_count = jbo.getInt("miss_count");
            int shiny_perfect_count = jbo.getInt("shiny_perfect_count");
            short modifier = (short) jbo.getInt("modifier");
            double rating = jbo.getDouble("rating");
            int health = jbo.getInt("health");
            short best_clear_type = (short) jbo.getInt("best_clear_type");
            short clear_type = (short) jbo.getInt("clear_type");
            short difficulty = (short) jbo.getInt("difficulty");
            int near_count = jbo.getInt("near_count");
            int perfect_count = jbo.getInt("perfect_count");
            return new ArcScore(score, d, sid, miss_count, shiny_perfect_count, modifier, rating, health, best_clear_type, clear_type, difficulty, near_count, perfect_count);
        } catch (JSONException e) {
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,jbo.toString());
            e.printStackTrace();
            return null;
        }
    }

    public static long calScoreForSong(int note_count, int far, int lost, int pure, int big_pure) {
        assert far + lost + pure == note_count;
        assert big_pure <= pure;
        return 1000000000L * (5L * far + 10L * pure) / note_count / 1000 + big_pure;
    }

    public static String RateForScore(long score, int far, int lost) {
        if (score >= 10000000 && far == 0 && lost == 0) return "★ PM ☆";
        else if (9900000 <= score) return "EX+";
        else if (score >= 9800000) return "EX";
        else if (score >= 9500000) return "AA";
        else if (score >= 9200000) return "A";
        else if (score >= 8900000) return "B";
        else if (score >= 8600000) return "C";
        else return "D";
    }

    public static String getSongName(String sid) {
        String songinfoapi = "http://xxxx.com/v2/songinfo";
        try {
            JSONObject jbo = webutils.getJSONObjectResp(songinfoapi, BotConstants.BOT_HEADER,
                    Connection.Method.GET, new HashMap<String, String>() {{
                        put("songname", sid);
                    }},
                    null, BotWebMethods.URLENCODED, new JSONObject());

            if (webutils.checkJSONResp(jbo, "status", 0)) {
                JSONObject content = jbo.getJSONObject("content");
                JSONObject title_localized = content.getJSONObject("title_localized");
                if (title_localized.has("jp")) return title_localized.getString("jp");
                else return title_localized.getString("en");
            } else {
                if (jbo.has("status")) {
                    switch (jbo.getInt("status")) {
                        case -1:     //invalid songname
                            return "<Error> Invalid songname";
                        case -2:     //not recorded
                            return "<Error> Not recorded";
                        case -3:     //too many records
                            return "<Error> Too many records";
                        case -233:
                        default:
                            return "<Error> Unknown error";
                    }
                }
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSongLevel(int lev, String songname) {
        CQ.logInfo("getSongLevel","lev: {} songname: {}",lev,songname);
        String songinfoapi = "http://xxxx.com/v2/songinfo";
        if (lev < 0 || lev > 3) return "";   //wrong value!
        try {
            JSONObject jbo = webutils.getJSONObjectResp(songinfoapi, BotConstants.BOT_HEADER,
                    Connection.Method.GET, new HashMap<String, String>() {{
                        put("songname", songname);
                    }},
                    null, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(jbo, "status", 0)) {
                JSONObject content = jbo.getJSONObject("content");
                JSONArray diffs = content.getJSONArray("difficulties");
                for (int i = 0, length = diffs.length(); i < length; i++) {
                    JSONObject cur = diffs.getJSONObject(i);
                    if (cur.getInt("ratingClass") == lev) {
                        int rating = cur.getInt("rating");
                        if (cur.has("ratingPlus") && cur.getBoolean("ratingPlus")) return rating + "+";
                        return String.valueOf(rating);
                    }
                }
                return "";
            } else {
                CQ.logWarning("BotArcaea", jbo.toString());
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static String formatResultString(String songname, String username, String ucode, ArcScore arcScore, boolean hide) {
        String format = "{username}[{ucode}]的最近记录" + StringUtils.lineSeparator +
                "歌曲：{songname} [{difficulty} {songlevel}]" + StringUtils.lineSeparator +
                "分数：{score}" + StringUtils.lineSeparator +
                "PURE：{total_pure}(+{big_pure}) " + "FAR：{far} " + "LOST：{lost}" + StringUtils.lineSeparator +
                "游玩于{timedelta}之前";
        Date curr = new Date();
        Date played = arcScore.getTime_played();
        long delta = (curr.getTime() - played.getTime()) / 1000;
        String timedelta = DateUtils.formatTime(delta);
        if (hide) {
            username = hide(username);
            ucode = hide(ucode);
        }
        short dif = arcScore.getDifficulty();
        return format.replace("{username}", username).replace("{ucode}", ucode).replace("{songname}", songname)
                .replace("{score}", String.valueOf(arcScore.getScore())).replace("{total_pure}", String.valueOf(arcScore.getPerfect_count()))
                .replace("{big_pure}", String.valueOf(arcScore.getShiny_perfect_count())).replace("{far}", String.valueOf(arcScore.getNear_count()))
                .replace("{lost}", String.valueOf(arcScore.getMiss_count())).replace("{timedelta}", timedelta)
                .replace("{difficulty}", intToStr_diff_mapping.get(dif))
                .replace("{songlevel}", getSongLevel(dif, songname));
    }

    public static String hide(String ori) {
        if (ori.matches("\\d{9}")) return ori.replaceAll("^\\d{3}", "***").replaceAll("\\d{3}$", "***");
        char[] mid = ori.toCharArray();
        int len = mid.length;
        double masked_pos = 0.4;
        int left = (int) Math.floor(len * (1 - masked_pos));
        for (int i = 0, j = len - 1, total_changed = 0; i <= j; i++, j--) {
            mid[i] = '*';
            total_changed++;
            if (total_changed >= len - left) break;
            mid[j] = '*';
            total_changed++;
            if (total_changed >= len - left) break;
        }
        return new String(mid);
    }

    public static String formatScore(long score) {
        DecimalFormat df = new DecimalFormat("00,000,000");
        return df.format(score).replace(',', '\'');
    }

    public static String formatRating(double rating){
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(rating);
    }

    public static String clearTypeStr(short index) {
        switch (index) {
            case 0:
                return "TL";
            case 1:
                return "NC";
            case 2:
                return "FR";
            case 3:
                return "PM";
            case 4:
                return "EC";
            case 5:
                return "HC";
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ArcScore.class.getSimpleName() + "[", "]")
                .add("score=" + score)
                .add("time_played=" + time_played)
                .add("song_id='" + song_id + "'")
                .add("miss_count=" + miss_count)
                .add("shiny_perfect_count=" + shiny_perfect_count)
                .add("modifier=" + modifier)
                .add("rating=" + rating)
                .add("health=" + health)
                .add("best_clear_type=" + best_clear_type)
                .add("clear_type=" + clear_type)
                .add("difficulty=" + difficulty)
                .add("near_count=" + near_count)
                .add("perfect_count=" + perfect_count)
                .toString();
    }
}
