package me.cqp.JRbot.modules;

public interface signer {
    <T> String generate_signature(T... args);
}
