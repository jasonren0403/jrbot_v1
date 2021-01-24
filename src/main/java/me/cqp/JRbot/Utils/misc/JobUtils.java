package me.cqp.JRbot.Utils.misc;

import me.cqp.JRbot.entity.BotConstants;
import me.cqp.JRbot.entity.BotWebMethods;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.*;

import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.run_token;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class JobUtils {
    private static Scheduler scheduler;

    public static Scheduler getInstance() throws SchedulerException {
        if (scheduler == null) {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        }
        return scheduler;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Job> forJobClass(String jobclazz) throws ClassNotFoundException {
        System.out.println("forname "+jobclazz);
        return (Class<? extends Job>) Class.forName(jobclazz);
    }

    public static void processJobs() throws SchedulerException {
        JSONArray ja = getJobs();
        scheduler = getInstance();
        for (int i = 0; i < ja.length(); i++) {
            Map<JobDetail, Set<? extends Trigger>> job = parseJob(ja.getJSONObject(i));
            scheduler.scheduleJobs(job, true);
        }
        scheduler.start();
//        CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"after scheduler start,before debug");
        debugJobs(false,true);
    }

    private static JSONArray getJobs() {
        /**
         * "contents":[
         * {},   //job 1
         * {},   //job 2
         * ...
         * ]
         */
        try{
            JSONObject obj = webutils.getJSONObjectResp("http://xxxx.com/jrbot/task", BotConstants.BOT_HEADER,
                    Connection.Method.GET, null, run_token, BotWebMethods.URLENCODED, new JSONObject());
            if (webutils.checkJSONResp(obj)) {
                return obj.optJSONArray("contents");
            } else {
                return new JSONArray();
            }
        } catch (IOException e) {
            return new JSONArray();
        }

    }

    private static Map<JobDetail, Set<? extends Trigger>> parseJob(JSONObject jobobject) {
        /**
         *   JobDetail job = newJob(connectTask.class)
         *                  .withIdentity("connectJob", "group1")
         *                  .withDescription("Access lowest.world/connect from time to time")
         *                  .build();
         *   {
         *       "task_id":"x",
         *       "task_name":"y",
         *       "task_description":"z",
         *       "is_completed":true|false,
         *       "execute_time_config":{
         *          "is_once":true|false,
         *          "trigs":[
         *              {"cron":"cronstr","trig_id":"x1","desc":""},
         *              {"cron":"cronstr2","trig_id":"2x","desc":""},
         *              ...
         *          ]
         *       }
         *   }
         */
        Map<JobDetail, Set<? extends Trigger>> map = new HashMap<>();
        Set<Trigger> s = new HashSet<>();
        String task_id = jobobject.getString("task_id");
        String name = jobobject.getString("task_name");
        String desc = jobobject.optString("task_description", "");
        JSONObject conf = jobobject.getJSONObject("execute_time_config");
        try {
            Class<? extends Job> c = forJobClass("me.cqp.JRbot.entity.jobs." + name);

            JobDetail jd = newJob(c).withIdentity(task_id).withDescription(desc).build();
            JSONArray trigs = conf.optJSONArray("trigs");
            if (trigs != null && trigs.length() > 0) {
                for (int i = 0; i < trigs.length(); i++) {
                    JSONObject cur = trigs.getJSONObject(i);
                    Trigger t = parseTrigger(cur, name, "job-" + name);
                    if (t != null) s.add(t);
                }
            }
            map.put(jd, s);
            CQ.logDebug("JobUtil","Class:{}, task_id:{},desc:{}",c,task_id,desc);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return map;
    }

    private static Trigger parseTrigger(JSONObject jobobject, String jobName, String groupName) {
        /**
         * Trigger trig2 = newTrigger()
         *                     .withIdentity("testTrig","t-g1")
         *                     .withSchedule(cronSchedule("0 0 * * * ? *"))
         *                     .forJob("jobName")
         *                     .startNow()
         *                     .build();
         */
        String id = jobobject.getString("trig_id");
        String cron = jobobject.getString("cron");
        String desc = jobobject.optString("desc", "");
        return newTrigger().withIdentity(id, groupName).withSchedule(cronSchedule(cron))
                .withDescription(desc).forJob(jobName).startNow().build();
    }

    public static void debugJobs(boolean sysout, boolean coolqout) throws SchedulerException {
        assert sysout || coolqout;
        List<JobExecutionContext> ctxs = getInstance().getCurrentlyExecutingJobs();
        for (JobExecutionContext ctx : ctxs) {
            if (sysout)
                System.out.printf("job [%s] %s%n", ctx.getJobDetail().getJobClass(), ctx.getScheduler().isStarted() ? "is started" : "is not started");
            else{
                CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Job started");
                CQ.logInfo("Scheduler", String.format("job [%s] %s", ctx.getJobDetail().getJobClass(), ctx.getScheduler().isStarted() ? "is started" : "is not started"));
            }
        }
    }

}
