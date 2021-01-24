package me.cqp.JRbot.entity.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface BaseBotJob extends Job {
    @Override
    void execute(JobExecutionContext context) throws JobExecutionException;
}
