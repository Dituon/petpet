package moe.dituon.petpet.bot.qq.permission;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeParser {
    public static final Map<String, Long> TIME_UNIT_MAP = Map.of(
            "ms", 1L,
            "s", TimeUnit.SECONDS.toMillis(1),
            "sec", TimeUnit.SECONDS.toMillis(1),
            "m", TimeUnit.MINUTES.toMillis(1),
            "min", TimeUnit.MINUTES.toMillis(1),
            "h", TimeUnit.HOURS.toMillis(1),
            "hr", TimeUnit.HOURS.toMillis(1),
            "hour", TimeUnit.HOURS.toMillis(1),
            "d", TimeUnit.DAYS.toMillis(1),
            "day", TimeUnit.DAYS.toMillis(1)
    );

    protected final Map<String, Long> timeUnitMap;

    public TimeParser(@Nullable Map<String, Long> timeUnitMap) {
        if (timeUnitMap == null || timeUnitMap.isEmpty() || timeUnitMap == TIME_UNIT_MAP) {
            this.timeUnitMap = TIME_UNIT_MAP;
            return;
        }
        this.timeUnitMap = new HashMap<>(timeUnitMap.size() + TIME_UNIT_MAP.size());
        this.timeUnitMap.putAll(timeUnitMap);
        this.timeUnitMap.putAll(TIME_UNIT_MAP);
    }

    public long parse(String time) {
        return parseTimeToMillis(time, timeUnitMap);
    }

    public static long parseTimeToMillis(String time) {
        return parseTimeToMillis(time, TIME_UNIT_MAP);
    }

    public static long parseTimeToMillis(String time, Map<String, Long> timeUnitMap) {
        if (time == null || time.isEmpty()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        time = time.trim().toLowerCase();
        int index = 0;
        while (index < time.length() && Character.isDigit(time.charAt(index))) {
            index++;
        }

        if (index == 0 || index == time.length()) {
            throw new IllegalArgumentException("Invalid time format: " + time);
        }

        long value = Long.parseLong(time.substring(0, index));
        String unit = time.substring(index);

        Long multiplier = timeUnitMap.get(unit);
        if (multiplier == null) {
            throw new IllegalArgumentException("Unsupported time unit: " + unit);
        }
        return value * multiplier;
    }
}
