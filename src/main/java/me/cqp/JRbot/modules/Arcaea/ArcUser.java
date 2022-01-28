package me.cqp.JRbot.modules.Arcaea;

import me.cqp.JRbot.Utils.webLogging;
import me.cqp.JRbot.entity.BotConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;

import static me.cqp.Jrbot.CQ;

public class ArcUser {
    ///region fields
    @Nonnull
    private final String name;
    @Nonnull
    private Date join_date;
    @Nonnegative
    private final long user_id;
    @Nonnegative
    private int character_id;
    @Nonnegative
    private double rating;

    private boolean is_char_uncapped;
    private boolean is_skill_sealed;
    private boolean is_char_uncapped_override;

    @Override
    public String toString() {
        return new StringJoiner(", ", ArcUser.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("join_date=" + join_date)
                .add("user_id=" + user_id)
                .add("character_id=" + character_id)
                .add("is_char_uncapped=" + is_char_uncapped)
                .add("is_skill_sealed=" + is_skill_sealed)
                .add("rating=" + rating)
                .add("is_char_uncapped_override=" + is_char_uncapped_override)
                .add("recent_scores=" + Arrays.toString(recent_scores))
                .toString();
    }

    private ArcScore[] recent_scores;
    ///endregion

    ///region getters
    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Date getJoin_date() {
        return join_date;
    }

    public long getUser_id() {
        return user_id;
    }

    public int getCharacter_id() {
        return character_id;
    }

    public boolean is_char_uncapped() {
        return is_char_uncapped;
    }

    public boolean is_skill_sealed() {
        return is_skill_sealed;
    }

    public boolean is_char_uncapped_override() {
        return is_char_uncapped_override;
    }

    public ArcScore[] getRecent_scores() {
        return recent_scores;
    }

    public double getRating() {
        return rating;
    }
    ///endregion getters


    private ArcUser(@Nonnull String name, @Nonnull Date join_date, int character_id, long user_id, boolean is_char_uncapped, boolean is_skill_sealed, boolean is_char_uncapped_override, ArcScore[] recent_scores, double rating) {
        this.name = name;
        this.join_date = join_date;
        this.user_id = user_id;
        this.character_id = character_id;
        this.is_char_uncapped = is_char_uncapped;
        this.is_skill_sealed = is_skill_sealed;
        this.is_char_uncapped_override = is_char_uncapped_override;
        this.recent_scores = recent_scores;
        this.rating = rating;
    }

    private ArcUser(@Nonnull String name, long uid) {
        this.name = name;
        this.user_id = uid;
    }

    public static ArcUser sampleUser(){
        return new ArcUser("Hikari",new Date(1145141919810L),1,114514,false,false,false,null,1250);
    }

    public static ArcUser[] fromJSON(JSONArray ja) {
        ArrayList<ArcUser> list = new ArrayList<>();
        int len = ja.length();
        for (int i = 0; i < len; i++) {
            JSONObject jbo = ja.getJSONObject(i);
            ArcUser aru = fromJSON(jbo);
            if (aru != null) list.add(aru);
        }
        return list.toArray(new ArcUser[0]);
    }

    public static ArcUser getUserFromList(ArrayList<ArcUser> arcusers, long uid) {
        for (ArcUser arcuser : arcusers) {
            if (arcuser.getUser_id() == uid) {
                return arcuser;
            }
        }
        return null;
    }

    public static ArcUser getUserFromArray(ArcUser[] arcusers, long uid) {
        for (ArcUser arcuser : arcusers) {
            if (arcuser.getUser_id() == uid) {
                return arcuser;
            }
        }
        return null;
    }

    public static ArcUser fromJSON(JSONObject jbo) {
        try {
            String name = jbo.getString("name");
            double rating = jbo.getDouble("rating");
            Date join_date = new Date(jbo.getLong("join_date"));
            int character_id = jbo.getInt("character");
            long user_id = jbo.getInt("user_id");
            boolean is_char_uncapped = jbo.getBoolean("is_char_uncapped");
            boolean is_skill_sealed = jbo.getBoolean("is_skill_sealed");
            boolean is_char_uncapped_override = jbo.getBoolean("is_char_uncapped_override");

            ArcScore[] arcScores = new ArcScore[1];
            if(!jbo.has("recent_score")) arcScores = null;
            else {
                JSONArray ja = jbo.optJSONArray("recent_score");
                if(ja == null) arcScores[0] = ArcScore.fromJSON(jbo.getJSONObject("recent_score"));
                else arcScores = ArcScore.fromJSON(ja);
            }
            return new ArcUser(name, join_date, character_id, user_id, is_char_uncapped,
                    is_skill_sealed, is_char_uncapped_override, arcScores, rating);
        } catch (JSONException e) {
            webLogging.addLog("warning","BotArcaea::user",e.toString());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, join_date, user_id, character_id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArcUser arcUser = (ArcUser) o;
        return user_id == arcUser.user_id &&
                name.equals(arcUser.name);
    }

    public String ratingStar(){
        double rating = this.rating;
        return ratingStar(rating);
    }


    public static String ratingStar(double rating) {
        if (rating >= 12 && rating < 12.5) return "☆";
        else if (rating >= 12.5) return "☆☆";
        else return "";
    }
}
