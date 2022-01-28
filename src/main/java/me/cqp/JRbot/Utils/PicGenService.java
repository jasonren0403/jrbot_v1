package me.cqp.JRbot.Utils;

import me.cqp.JRbot.modules.Arcaea.ArcResultPic;
import me.cqp.JRbot.modules.Arcaea.ArcScore;
import me.cqp.JRbot.modules.Arcaea.ArcUser;

import java.awt.*;
import java.io.File;
import java.util.concurrent.*;

public class PicGenService {

    public static Callable<File> getArcGenTask(final ArcUser u, final ArcScore s, final boolean hide,final long fromQQ) {
        return () -> new ArcResultPic(u, s, hide,fromQQ).mask(Color.WHITE, 0.75f).
                drawUserInfo(u).drawPlayInfo(s, false)
                .build();
    }
}
