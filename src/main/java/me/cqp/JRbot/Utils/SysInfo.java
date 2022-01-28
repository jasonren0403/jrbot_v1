package me.cqp.JRbot.Utils;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class SysInfo {
    private static final int CPUTIME = 500;

    private static final int PERCENT = 100;

    private static final int FAULTLENGTH = 10;


    public static String getMemory() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // 总的物理内存+虚拟内存
        long totalvirtualMemory = osmxb.getTotalSwapSpaceSize();
        // 剩余的物理内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();
        double compare = (1 - freePhysicalMemorySize * 1.0 / totalvirtualMemory) * 100;
        return "内存已使用:" + (int) compare + "%" + "(" + (totalvirtualMemory-freePhysicalMemorySize) / 1024 / 1024 + "MB/" + totalvirtualMemory / 1024 / 1024 + "MB)";
    }

    //获取文件系统使用率
    public static List<String> getDisk() {
        // 操作系统
        List<String> list = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            String dirName = c + ":/";
            File win = new File(dirName);
            if (win.exists()) {
                long total = win.getTotalSpace();
                long free = win.getFreeSpace();
                double compare = (1 - free * 1.0 / total) * 100;
                String str = c + ":盘  已使用 " + (int) compare + "%";
                list.add(str);
            }
        }
        return list;
    }

    //获得cpu使用率
    private static double getCpuRatioForWindows() {
        try {
            String procCmd = System.getenv("windir")
                    + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,"
                    + "KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            // 取进程信息
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return (double) (PERCENT * (busytime) / (busytime + idletime));
            } else {
                return 0.0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    //读取cpu相关信息
    private static long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = substring(line, capidx, cmdidx - 1).trim();
                String cmd = substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.contains("wmic.exe")) {
                    continue;
                }
                String s1 = substring(line, kmtidx, rocidx - 1).trim();
                String s2 = substring(line, umtidx, wocidx - 1).trim();
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    if (s1.length() > 0)
                        idletime += Long.parseLong(s1);
                    if (s2.length() > 0)
                        idletime += Long.parseLong(s2);
                    continue;
                }
                if (s1.length() > 0)
                    kneltime += Long.parseLong(s1);
                if (s2.length() > 0)
                    usertime += Long.parseLong(s2);
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static double[] getCpuUsageForLinux() {
        double cpuUsed = 0;
        double idleUsed = 0.0;
        double[] cpuarray = new double[2];
        Runtime rt = Runtime.getRuntime();
        Process p;
        try {
            p = rt.exec("top -b -n 1");
            BufferedReader in = null;
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String str = null;
            int linecount = 0;
            while ((str = in.readLine()) != null) {
                linecount++;
                if (linecount == 3) {
                    String[] s = str.split("%");
                    String idlestr = s[3];
                    String[] idlestr1 = idlestr.split(" ");
                    idleUsed = Double.parseDouble(idlestr1[idlestr1.length - 1]);
                    cpuUsed = 100 - idleUsed;
                    cpuarray[0] = cpuUsed;
                    cpuarray[1] = idleUsed;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
        }// call "top" command in linux

        return cpuarray;

    }

    /**
     * 由于String.subString对汉字处理存在问题（把一个汉字视为一个字节)，因此在包含汉字的字符串时存在隐患，现调整如下
     *
     * @param src       要截取的字符串
     * @param start_idx 开始坐标（包括该坐标)
     * @param end_idx   截止坐标（包括该坐标）
     * @return
     */
    private static String substring(String src, int start_idx, int end_idx) {
        byte[] b = src.getBytes();
        StringBuilder tgt = new StringBuilder();
        for (int i = start_idx; i <= end_idx; i++) {
            tgt.append((char) b[i]);
        }
        return tgt.toString();
    }

    public static String sysDebug(){
        String header = "==Runtime debug(system)==";
        String memoryDesc = SysInfo.getMemory();
        String cpu;
        if(System.getProperty("os.name").toLowerCase().contains("windows")){
            cpu = "CPU占用率："+getCpuRatioForWindows()+"%";
        }else{
            cpu = "CPU占用率："+ Arrays.toString(getCpuUsageForLinux()) +"%";
        }

        String mem = "Java虚拟机用量(Full/Free/Max)："+(int) Runtime.getRuntime().totalMemory() / 1024+"KB/"+(int) Runtime.getRuntime().freeMemory() / 1024+"KB/"
                + Runtime.getRuntime().maxMemory() / 1024 + "KB";
        return new StringJoiner(System.lineSeparator()).add(header).add(memoryDesc).add(cpu).add(mem).toString();
    }

    public static void main(String[] args) {
        System.out.println(sysDebug());
//        System.out.println(SysInfo.getMemory()); //this
//
//        System.out.println(SysInfo.getDisk().toString());
//        System.out.println(getCpuRatioForWindows());
//        int i = (int) Runtime.getRuntime().totalMemory() / 1024;//Java 虚拟机中的内存总量,以字节为单位
//        System.out.println("总的内存量 i is " + i);
//        int j = (int) Runtime.getRuntime().freeMemory() / 1024;//Java 虚拟机中的空闲内存量
//        System.out.println("空闲内存量 j is " + j);
//        System.out.println("最大内存量 is " + Runtime.getRuntime().maxMemory() / 1024);
//        RuntimeMXBean rmb = ManagementFactory.getRuntimeMXBean();
//        System.out.println("getVmVersion " + rmb.getVmVersion());
//        ThreadMXBean tm = (ThreadMXBean) ManagementFactory.getThreadMXBean();
//        System.out.println("getThreadCount " + tm.getThreadCount());
//        System.out.println("getPeakThreadCount " + tm.getPeakThreadCount());
//        System.out.println("getCurrentThreadCpuTime " + tm.getCurrentThreadCpuTime());
//        System.out.println("getDaemonThreadCount " + tm.getDaemonThreadCount());
//        System.out.println("getCurrentThreadUserTime " + tm.getCurrentThreadUserTime());
//        OperatingSystemMXBean osm = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        System.out.println("osm.getAvailableProcessors() " + osm.getAvailableProcessors());
//        System.out.println("osm.getCommittedVirtualMemorySize() " + osm.getCommittedVirtualMemorySize());
//        System.out.println("osm.getName() " + osm.getName()); //this
//        System.out.println("osm.getProcessCpuTime() " + osm.getProcessCpuTime());
//        System.out.println("osm.getVersion() " + osm.getVersion());

    }
}
