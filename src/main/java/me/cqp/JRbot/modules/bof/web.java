package me.cqp.JRbot.modules.bof;

import me.cqp.JRbot.Utils.misc.webutils;
import me.cqp.JRbot.entity.BofSong;
import me.cqp.JRbot.entity.BotWebMethods;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;

public class web {
    /**
     * 2019 bofxv 查询 http://manbow.nothing.sh/event/event.cgi?action=List_def&event=127
     * 插入到数据库中并更新
     * 暂时废弃使用
     */
    @Deprecated
    public static List<BofSong> bofxv_contents(boolean get_team_only, String required) {
        List<BofSong> list = new ArrayList<>();
        BofSong bofSong = null;
        int total = 0, rating = 0;
        float median = 0.0f;
        int entryBMS = 0, impresses = 0;
        try{
            Document doc = webutils.getDocument("http://manbow.nothing.sh/event/event.cgi?action=List_def&event=133",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36",
                    Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, "");
            //拿到总BMS谱面数和评论数
            if (doc == null) {
                CQ.logFatal("Error::bofxv2019", "Can't access bofxv2019 page");
                return list;
            }
            Pattern pattern = Pattern.compile("EntryBMS : \\d+ / TOTALImpre : \\d+");
            String text = doc.getElementsMatchingOwnText(pattern).text();
            if (text == null || text.isEmpty()) {
                CQ.logFatal("Error::bofxv2019", "Can't get entryBMS or total impression data");
                return list;
            }
            Pattern nums = Pattern.compile("\\d+");
            //System.out.println(text);
            Matcher matcher = nums.matcher(text);
            if (matcher.find()) {
                entryBMS = Integer.parseInt(matcher.group(0));
                System.out.println(entryBMS);
            }
            if (matcher.find()) {
                impresses = Integer.parseInt(matcher.group(0));
                System.out.println(impresses);
            }
            CQ.logDebug("Debug::bofxv2019", "EntryBMS:" + entryBMS + " Impresses: " + impresses);
            main.overwrite_bof_datas(entryBMS, impresses);
            //拿到队伍信息
            Elements teams = doc.select("#modern_list > div.team_information");
            //System.out.println(teams.size());
            for (Element e : teams) {
//            System.out.println(e);
//            System.out.println("------------------------------");
                String team_name = e.select("div.fancy-title.title-dotted-border.title-center > *").text();
//            System.out.println("Team name:" + team_name);
                if (team_name.isEmpty()) continue;
                if (!get_team_only) {
                    Elements songs = e.getElementsByAttributeValue("class", "pricing-box best-price");

                    for (Element e2 : songs) {
//                    System.out.println(e2);
                        Elements song_contents = e2.getElementsByAttributeValue("class", "textOverflow");
                        if (song_contents.size() < 3) {
                            System.out.println("Detected corrupted song contents.");
                            System.out.println(e2);
                            continue;
                        }
                        String genre = song_contents.get(0).text();
                        String song_name = song_contents.get(1).text();
                        if (song_name.contains("NO ENTRY")) {
//                    System.out.println(e2);
                            continue;
                        }
                        String composer = song_contents.get(2).text();

                        Elements point_elements = e2.getElementsByAttributeValue("class", "pricing-features");
                        List<String> str = point_elements.eachText();
//                System.out.println(str);
                        if (str.size() == 0) {
                            System.out.println("pricing-features lacked");
                            System.out.println(e2);
                            continue;
                        }
                        if (str.get(0).contains("Disqualification")) {
                            bofSong = new BofSong(team_name, song_name, composer, genre, 0, 0, 0.0f, 0, "Disqualified", null, true);
//                    CQ.logDebug("Debug::BOFXV","song with disqualification");
//                    list.add(bofSong);
                            List<BofSong> list2 = new ArrayList<>();
                            list2.add(bofSong);
//                            DBOps.init_or_update_bofxv_contents(list2, true);
                            continue;
                        }
                        int stars = Integer.parseInt(str.get(0));
                        Matcher matcher2 = nums.matcher(str.get(1));

                        if (matcher2.find()) {
                            total = Integer.parseInt(matcher2.group(0));
                            if (matcher2.find()) {
                                median = Float.parseFloat(matcher2.group(0));
                            }
                        }

                        //System.out.println(str);
                        //[16, Total : 14435 Point Median : 925 Points] 第一个是得星数，第二个是总分和中位数分数
                        Element others = e2.getElementsByAttributeValue("class", "pricing-action").first();
                        if (others == null) {
                            System.out.println("pricing-action lacked");
                            System.out.println(e2);
                            continue;
                        }
                        String other_desc = others.text();
                        //System.out.println(other_desc);
                        String[] args = other_desc.split("/");
                        if (args[0].trim().matches("No.\\d+")) {
                            matcher2 = nums.matcher(args[0]);
                            if (matcher2.find()) {
                                rating = Integer.parseInt(matcher2.group());
                            }
                        }
                        String type = args[1].trim();
                        other_desc = other_desc.replaceAll("No.\\d+ / \\w+ /", "").trim();
                        other_desc = other_desc.replace("update :", "").trim();
                        bofSong = new BofSong(team_name, song_name, composer, genre, stars, total, median, rating, type, other_desc, false);
//                    CQ.logDebug("Debug::BOFXV","normal song");
                        list.add(bofSong);

//                System.out.println("genre:" + genre + "\nsong name:" + song_name + "\ncomposer:" + composer);
//                System.out.printf("Stars:%d,total points:%d,median pts:%f\n", stars, total, median);
//                System.out.printf("Rating:%d,type:%s,last update date:%s\n", rating, type, other_desc);
                        //No.6 / Original / update : 2019/10/13 23:29 排名/类型/最后更新
                    }
                } else {
                    if (!team_name.equalsIgnoreCase(required)) continue;
                    int team_imp = Integer.parseInt(e.getElementById("team_imp").text());
                    float team_total = Float.parseFloat(e.getElementById("team_total").text());
                    float team_med = Float.parseFloat(e.getElementById("team_med").text());
//            System.out.printf("%d,%f,%f\n",team_imp,team_total,team_med);
                    bofSong = new BofSong(team_name, "", "", "", 0, 0, team_med, team_imp, "", "", false);
                    bofSong.setReserved(team_total);
                    list.add(bofSong);
                }

            }
            CQ.logInfo("Debug::BOFXV", "bofxv content get");

            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
}
