package cn.sandtripper.minecraft.autostopserver;

import org.bukkit.plugin.java.JavaPlugin;

public final class AutoStopServer extends JavaPlugin {
    private static final String[] enableTexts = {
            "\033[36m                _        _____ _              _____                          \033[0m",
            "\033[36m      /\\        | |      / ____| |            / ____|                         \033[0m",
            "\033[36m     /  \\  _   _| |_ ___| (___ | |_ ___  _ __| (___   ___ _ ____   _____ _ __ \033[0m",
            "\033[36m    / /\\ \\| | | | __/ _ \\\\___ \\| __/ _ \\| '_ \\\\___ \\ / _ \\ '__\\ \\ / / _ \\ '__|\033[0m",
            "\033[36m   / ____ \\ |_| | || (_) |___) | || (_) | |_) |___) |  __/ |   \\ V /  __/ |   \033[0m",
            "\033[36m  /_/    \\_\\__,_|\\__\\___/_____/ \\__\\___/| .__/_____/ \\___|_|    \\_/ \\___|_|   \033[0m",
            "\033[36m                                        | |                                   \033[0m",
            "\033[36m                                        |_|                                   \033[0m",
            "\033[36m自动关服机 --by SandTripper\033[0m",
            "\033[36m启动成功!\033[0m"
    };

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        this.timer = new StopServerTimer(this);
        getCommand("AutoStopServer").setExecutor(new CommandHandler(this));
        timer.start();
        //显示加载文字
        for (int i = 0; i < enableTexts.length; i++) {
            getLogger().info(enableTexts[i]);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        timer.cancel();
    }

    public StopServerTimer timer;
}
