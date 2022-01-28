package me.cqp.JRbot.modules;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static me.cqp.Jrbot.CQ;

public class Dice implements BaseModule {
    /**
     * JRbot dice:return random number from 1 to maximum (default 6)
     */
    private final long fromQQ;
    private final long fromGroup;

    /**
     * Dice in private
     * @param fromQQ who sends 'dice'
     */
    public Dice(long fromQQ){
        this(-1L,fromQQ);
    }

    /**
     * Dice in group
     * @param fromGroup the personel in group sends 'dice'
     * @param fromQQ who sends 'dice'
     */
    public Dice(long fromGroup, long fromQQ) {
        this.fromGroup = fromGroup;
        this.fromQQ = fromQQ;
    }

    public void sendMesg(String num){
        String msg_tmpl = "您掷出了{num}点";
        if(this.fromGroup<0){
            CQ.sendPrivateMsg(fromQQ,msg_tmpl.replace("{num}",num));
        }else{
            CQ.sendGroupMsg(fromGroup,msg_tmpl.replace("{num}",num));
        }
    }


    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        directives.remove("dice");
        directives.remove("扔色子");
        if (directives.size() == 0) {
            int t = new Random(System.currentTimeMillis()).nextInt(6) + 1;
            sendMesg(String.valueOf(t));
            return 1;
        }
        int index = 0;
        int len = directives.size();
        DirProcessState s = DirProcessState.PROCESS_PARAM;
        while (index < len && s != DirProcessState.END) {
            if (!directives.get(index).matches("^[1-9][0-9]*|0$") ||
                    Long.parseLong(directives.get(index)) <= 0 ||Long.parseLong(directives.get(index))>=Long.MAX_VALUE) {
                index++;
                continue;
            }
            String num = directives.get(index);
            s = DirProcessState.END;
            long l = ThreadLocalRandom.current().nextLong(Long.parseLong(num))+1;
            sendMesg(String.valueOf(l));
        }
        if (s == DirProcessState.PROCESS_PARAM) {
            int t = new Random(System.currentTimeMillis()).nextInt(6) + 1;
            sendMesg(String.valueOf(t));
        }
        return 1;
    }

    public String name(){
        return "me.cqp.jrbot.dice";
    }

    @Override
    public String apiEndpointName() {
        return "";
    }
}
