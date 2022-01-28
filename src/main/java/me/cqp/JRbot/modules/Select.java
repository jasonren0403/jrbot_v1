package me.cqp.JRbot.modules;

import org.meowy.cqp.jcq.message.MsgBuffer;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static me.cqp.Jrbot.CQ;

public class Select implements BaseModule {
    private final long fromGroup;
    private final long fromQQ;

    public Select(long fromGroup, long fromQQ) {
        this.fromGroup = fromGroup;
        this.fromQQ = fromQQ;
    }

    public void select(String... selections) {
        MsgBuffer msb = new MsgBuffer();
        int i = selections.length;
        Random rm = new Random();
        switch (i){
            case 0:
                //if the code ever reach here I will swallow the whole computer screen! now!
                msb.setCoolQ(CQ).setTarget(fromGroup).
                        at(fromQQ).append("bot建议您 不选 呢").sendGroupMsg();
                break;
            case 1:
                msb.setCoolQ(CQ).setTarget(fromGroup).
                        at(fromQQ).append("只有一项，那只能是 ").append(selections[0]).append(" 咯").sendGroupMsg();
                break;
            default:
                if(itemsIdenticalAll(selections)){
                    msb.setCoolQ(CQ).setTarget(fromGroup).
                            at(fromQQ).append("在？所有选项都一样还需要犹豫么？").sendGroupMsg();
                }else{
                    msb.setCoolQ(CQ).setTarget(fromGroup).
                            at(fromQQ).append("bot建议您选择 ").append(selections[rm.nextInt(i)]).append(" 呢").sendGroupMsg();
                }
                break;
        }
    }

    private boolean itemsIdenticalAll(String... arr){
        String keyslot = arr[0];
        return Arrays.stream(arr).allMatch(str->str.equals(keyslot));
    }


    @Override
    public int processDirectives(@Nonnull List<String> directives) {
        directives.remove("select");
        directives.remove("选择");
        if (directives.size() == 0) {
            CQ.sendGroupMsg(fromGroup, helpMsg());
            return 1;
        }
        CQ.logInfo("Module select", "OK with params {}", directives.toString());
        select(directives.toArray(new String[0]));
        return 1;
    }

    @Override
    public double version() {
        return 1.0;
    }

    @Override
    public String name() {
        return "me.cqp.jrbot.select";
    }

    @Override
    public String apiEndpointName() {
        return "";
    }
}
