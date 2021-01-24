package me.cqp.JRbot.entity.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static me.cqp.Jrbot.CQ;

public class keepAlive implements BaseBotJob {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println(jobExecutionContext.getResult());
        CQ.logInfo("Scheduler","Bot scheduler is alive, executed at {}, next execution at {}",
                jobExecutionContext.getFireTime(),jobExecutionContext.getNextFireTime());
    }
}
