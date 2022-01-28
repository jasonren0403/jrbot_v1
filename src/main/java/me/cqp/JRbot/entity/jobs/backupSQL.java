package me.cqp.JRbot.entity.jobs;

import me.cqp.JRbot.Utils.misc.DateUtils;
import me.cqp.JRbot.Utils.misc.dbutils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static me.cqp.Jrbot.CQ;

public class backupSQL implements BaseBotJob{

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        mainJob();
    }

    public static void mainJob(){
        String basePath = "";
        boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        try{
            Connection conn = dbutils.getConnection();
            Statement t = conn.createStatement();
            ResultSet rs = t.executeQuery("select @@basedir as basePath from dual");
            if(rs.isBeforeFirst()){
                rs.next();
            }
            basePath = rs.getString("basePath");
            rs.close();
            t.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            CQ.logWarning("BackupSQL","Cannot get basePath");
            return;
        }
        Path mysqlDump = Paths.get(basePath,"bin",!isLinux?"mysqldump.exe":"mysqldump");
        String username = dbutils.getProperties().getProperty("username");
        String password = dbutils.getProperties().getProperty("password");
        File backupsqls = Paths.get(CQ.getAppDirectory(),"temp","backupsqls", String.format("jrbot_%s.sql", DateUtils.getDateStr())).toFile();
        String command = String.format("\"%s\" -u%s -p%s --opt --lock-tables=false jrbot %s \"%s\"",
                mysqlDump.toString(),username,password,isLinux?">":"-r",backupsqls.toString());

        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(command);//调用控制台执行shell
            br = new BufferedReader(new InputStreamReader(p.getErrorStream()));//获取执行后出现的错误；getInputStream是获取执行后的结果

            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            System.out.println(sb);//打印执行后的结果
        } catch (Exception e) {
            e.printStackTrace();
            CQ.logInfo("backupSQL","Exception in command execution {}",e.toString());
        }
        finally
        {
            CQ.logInfoSuccess("backupSQL","execute success, new backup is at "+backupsqls.toString());
            if (br != null)
            {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
