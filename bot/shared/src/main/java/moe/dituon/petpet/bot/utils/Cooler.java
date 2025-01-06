package moe.dituon.petpet.bot.utils;

import java.util.concurrent.ConcurrentHashMap;

public class Cooler {
    public static final Long DEFAULT_USER_COOLDOWN = 1000L;
    public static final Long DEFAULT_GROUP_COOLDOWN = 0L;
    public static final String DEFAULT_MESSAGE = "技能冷却中...";
    private static final ConcurrentHashMap<Object, Long> coolDownMap = new ConcurrentHashMap<>(128);
    private static final ConcurrentHashMap<Object, Long> lockTimeMap = new ConcurrentHashMap<>(128);

    public static void lock(Object uid, long lockTime) {
        if (lockTime <= 0L) return;
        coolDownMap.put(uid, System.currentTimeMillis());
        lockTimeMap.put(uid, lockTime);
    }

    public static boolean isLocked(Object uid) {
        if (!coolDownMap.containsKey(uid) || !lockTimeMap.containsKey(uid)) return false;
        return (System.currentTimeMillis() - coolDownMap.get(uid)) <= lockTimeMap.get(uid);
    }
}