package me.cqp.JRbot.entity;


public class BofSong {
    private String team_name;
    private String song_name;
    private String composer;
    private String genre;
    private int stars;
    private int total_pts;
    private float median_pts;
    private int rating;
    private String type;
    private String last_update;
    private boolean disqualified;
    private float reserved;

    public float getReserved() {
        return reserved;
    }

    public void setReserved(float reserved) {
        this.reserved = reserved;
    }

    public boolean isDisqualified() {
        return disqualified;
    }

    public void setDisqualified(boolean disqualified) {
        this.disqualified = disqualified;
    }

    public BofSong(String team, String song, String composer, String genre, int stars, int total, float median, int rating, String type, String last_update, boolean disqualified) {
        this.team_name = team;
        this.composer = composer;
        this.song_name = song;
        this.genre = genre;
        this.stars = stars;
        this.total_pts = total;
        this.median_pts = median;
        this.rating = rating;
        this.type = type;
        this.last_update = last_update;
        this.disqualified = disqualified;
    }


    public String getTeam_name() {
        return team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getTotal_pts() {
        return total_pts;
    }

    public void setTotal_pts(int total_pts) {
        this.total_pts = total_pts;
    }

    public float getMedian_pts() {
        return median_pts;
    }

    public void setMedian_pts(float median_pts) {
        this.median_pts = median_pts;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String toString() {
        return "[Team name:" + team_name + ",song name:" + song_name +
                ",composer:" + composer + ",genre:" + genre + ",stars:" +
                stars + ",total_pts:" + total_pts + ",median_pts:" + median_pts +
                ",rating:" + rating + "]";
    }
}
