package cn.sandtripper.minecraft.autostopserver;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class StopServerTimer {

    public StopServerTimer(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        // 获取当前时区
        TimeZone timeZone = TimeZone.getDefault();
        this.timestampOffset = (int) (timeZone.getRawOffset() / 1000);
    }

    public void start() {
        stopTimes = parseConfig();
        task = new BukkitRunnable() {
            private int lastCheckedTime = getLocalCurrentSecond(); // 记录上一次检查的时间

            @Override
            public void run() {
                // 如果需要重新加载配置
                if (isReload) {
                    stopTimes = parseConfig();
                    isReload = false;
                }

                // 获取当前时间（秒）
                int localCurrentSeconds = getLocalCurrentSecond();
                int secondsOfDay = (int) (localCurrentSeconds % (86400)); // 从当天的开始计算

                if (lastCheckedTime > secondsOfDay) {
                    // 如果 lastCheckedTime 大于 secondsOfDay，这意味着新的一天已经开始
                    lastCheckedTime = -1;
                }

                for (int stopTime : stopTimes) {
                    if (lastCheckedTime < stopTime && secondsOfDay >= stopTime) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "autostopserver stop");
                        break;
                    }
                }

                lastCheckedTime = secondsOfDay; // 更新最后检查时间
            }
        };
        task.runTaskTimer(plugin, 0, 10 * 20L); // 每10*20 ticks检查一次
    }

    public void reload() {
        isReload = true;
        config = plugin.getConfig();
    }

    public void cancel() {
        task.cancel();
    }


    private int getLocalCurrentSecond() {
        // 获取当前时间的秒数
        long currentTimeMillis = System.currentTimeMillis();
        long currentSeconds = currentTimeMillis / 1000;
        return (int) ((currentSeconds + timestampOffset) % 86400);
    }

    private List<Integer> parseConfig() {
        List<String> StrStopTimes = config.getStringList("stop-times");
        List<Integer> stopTimes = new ArrayList<Integer>();
        for (int i = 0; i < StrStopTimes.size(); i++) {
            String str = StrStopTimes.get(i);
            int seconds = parseSecond(str);
            if (seconds != -1) {
                stopTimes.add(Math.floorMod(seconds - config.getInt("countdown"), 86400));
            } else {
                plugin.getLogger().warning(str + " is a error time format");
            }
        }
        Collections.sort(stopTimes);
        return stopTimes;
    }

    private int parseSecond(String s) {
        if (s.length() != 5) {
            return -1;
        }

        try {
            int hours = Integer.parseInt(s.substring(0, 2)); // 提取小时部分并转换为整数
            int minutes = Integer.parseInt(s.substring(3));    // 提取分钟部分并转换为整数

            if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
                return hours * 60 * 60 + minutes * 60; // 返回秒数
            } else {
                return -1; // 无效的时间范围，返回-1表示无效输入
            }
        } catch (NumberFormatException e) {
            return -1; // 格式错误，返回-1表示无效输入
        }
    }

    private int timestampOffset;
    private boolean isReload;
    private FileConfiguration config;
    private final JavaPlugin plugin;

    private BukkitRunnable task;
    List<Integer> stopTimes;
}

