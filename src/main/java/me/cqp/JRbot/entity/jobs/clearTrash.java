package me.cqp.JRbot.entity.jobs;

import me.cqp.JRbot.entity.BotConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static me.cqp.JRbot.Utils.misc.DateUtils.getDateStr;
import static me.cqp.Jrbot.CQ;
import static me.cqp.Jrbot.tempfilestr;

public class clearTrash implements BaseBotJob{
    private static final File data_folder = Paths.get(CQ.getAppDirectory())
            .getParent().getParent().getParent().getParent().toFile();
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int imgs,records,bface,show;
        String datestr = getDateStr();
        File f = Paths.get(CQ.getAppDirectory(),"temp",datestr).toFile();
        tempfilestr = f.getAbsolutePath();
        CQ.logInfo("Daily task","update tempfile to {}",tempfilestr);
        CQ.sendGroupMsg(BotConstants.ALARM_GROUP,"Executed daliy task deleting temp file.");
        try {
            FileUtils.deleteDirectory(f);
            imgs = delete_imgs(7);
            records = delete_records(7);
            bface = delete_bface(7);
            show = delete_show(7);
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP,String.format("Successfully delete %d images, %d records, %d bface" +
                    ", %d shows.",imgs,records,bface,show));
        } catch (IOException e) {
            e.printStackTrace();
            CQ.sendGroupMsg(BotConstants.ALARM_GROUP, String.format("failed to delete directory, reason: %s", e.getMessage()));
        }

    }

    private static void delete_imgs(){
        delete_imgs(1);
    }

    private static void delete_records(){
        delete_records(1);
    }

    private static void delete_bface(){
        delete_bface(1);
    }

    private static void delete_show(){
        delete_show(1);
    }

    private static int delete_imgs(int threshold_days){
        long now = System.currentTimeMillis();
        Path p = Paths.get(data_folder.toString(),"image");

        Collection<File> c = FileUtils.listFiles(p.toFile(),new String[]{"cqimg"},false);
        return do_delete_files(threshold_days, now, c);
    }

    private static int do_delete_files(int threshold_days, long now, Collection<File> c) {
        List<File> a = c.stream().filter(file -> now - file.lastModified()>= (long) threshold_days *24*60*60*1000).collect(Collectors.toList());
        for (File f:a
        ) {
            FileUtils.deleteQuietly(f);
        }
        return a.size();
    }

    private static int delete_records(int threshold_days){
        long now = System.currentTimeMillis();
        Path p = Paths.get(data_folder.toString(),"record");
        Collection<File> c = FileUtils.listFiles(p.toFile(),new String[]{"silk"},false);
        return do_delete_files(threshold_days, now, c);
    }

    private static int delete_bface(int threshold_days){
        Path p = Paths.get(data_folder.toString(),"bface");
        long now = System.currentTimeMillis();
        Collection<File> c = FileUtils.listFilesAndDirs(p.toFile(),
                FileFilterUtils.and(FileFilterUtils.ageFileFilter(now- (long) threshold_days *24*60*60*1000),FileFilterUtils.suffixFileFilter("cqbf")),
                FileFilterUtils.trueFileFilter());
        for (File f:c
        ) {
            if(p.equals(f.toPath())) continue;
            FileUtils.deleteQuietly(f);
        }
        return c.isEmpty()?0:c.size() - 1;
    }

    private static int delete_show(int threshold_days){
        Path p = Paths.get(data_folder.toString(),"show");
        long now = System.currentTimeMillis();
        Collection<File> c = FileUtils.listFiles(p.toFile(),
                FileFilterUtils.and(FileFilterUtils.ageFileFilter(now- (long) threshold_days *24*60*60*1000),FileFilterUtils.suffixFileFilter("cqshow")),
                FileFilterUtils.trueFileFilter());
        for (File f:c
        ) {
            if(p.equals(f.toPath())) continue;
            FileUtils.deleteQuietly(f);
        }
        return c.isEmpty()?0:c.size() - 1;
    }
}
