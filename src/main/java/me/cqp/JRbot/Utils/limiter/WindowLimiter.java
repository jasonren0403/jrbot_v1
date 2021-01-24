package me.cqp.JRbot.Utils.limiter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WindowLimiter {
    int timeBetween();
    String desc() default "[None]";
    int maxWin();
}
