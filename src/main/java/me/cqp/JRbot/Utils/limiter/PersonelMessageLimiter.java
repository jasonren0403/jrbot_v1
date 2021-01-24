package me.cqp.JRbot.Utils.limiter;


public class PersonelMessageLimiter {

    @WindowLimiter(timeBetween = 30,desc="Private message",maxWin = 3)
    public void LimitPrivateMsg(){
        
    }
}
