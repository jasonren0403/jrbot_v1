package me.cqp.JRbot.Utils.misc;

import org.meowy.cqp.jcq.entity.Group;
import org.meowy.cqp.jcq.entity.QQInfo;

import static me.cqp.Jrbot.CQ;

public class info {

    public static Group getGroupInfo(long target){
        return CQ.getGroupInfo(target,false);
    }

    public static QQInfo getStrangerInfo(long target){
        return CQ.getStrangerInfo(target,false);
    }

}
