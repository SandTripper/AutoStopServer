package cn.sandtripper.minecraft.autostopserver;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class CommandHandler implements CommandExecutor {
    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageConfigReader = new ConfigReader(plugin, "message.yml");
        this.messageConfig = this.messageConfigReader.getConfig();
        this.config = plugin.getConfig();
        this.isCancel = false;
        this.isStopping = false;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1 && strings[0].equals("stop")) {
            stop(commandSender);
        } else if (strings.length == 2 && strings[0].equals("stop")) {
            try {
                int countdown = Integer.parseInt(strings[1]);
                stopWithCountdown(commandSender, countdown);
            } catch (Exception e) {
                commandSender.sendMessage(colorFormat(messageConfig.getString("plugin-prefix") + " " + messageConfig.getString("invalid-arguments-message")));
            }
        } else if (strings.length == 1 && strings[0].equals("cancel")) {
            cancel(commandSender);
        } else if (strings.length == 1 && strings[0].equals("reload")) {
            reload(commandSender);
        } else {
            help(commandSender);
        }
        return true;
    }

    private void stop(CommandSender commandSender) {
        stopWithCountdown(commandSender, config.getInt("countdown"));
    }

    private void stopWithCountdown(CommandSender commandSender, int countdown) {
        if (isStopping) {
            commandSender.sendMessage(colorFormat(messageConfig.getString("plugin-prefix") + " " + messageConfig.getString("stopping-message")));
            return;
        }
        isStopping = true;
        new BukkitRunnable() {
            int _countdown = countdown;
            List<Integer> remindTimes = config.getIntegerList("remind-times");

            @Override
            public void run() {
                if (!isCancel) {

                    for (int i = 0; i < remindTimes.size(); i++) {
                        if (remindTimes.get(i) == _countdown) {
                            broadcastStopTitle(_countdown);
                            broadcastStopMessage(_countdown);
                        }
                    }
                    if (_countdown == 0) {
                        Bukkit.getServer().shutdown();
                    }
                    _countdown -= 1;
                } else {
                    broadcastCancelTitle();
                    broadcastCancelMessage();
                    isCancel = false;
                    isStopping = false;
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancel(CommandSender commandSender) {
        if (!isStopping) {
            commandSender.sendMessage(colorFormat(messageConfig.getString("plugin-prefix") + " " + messageConfig.getString("nothing-to-cancel")));
        } else {
            isCancel = true;
        }
    }

    private void reload(CommandSender commandSender) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        messageConfigReader.saveDefaultConfig();
        messageConfigReader.reloadConfig();
        config = plugin.getConfig();
        messageConfig = messageConfigReader.getConfig();
        ((AutoStopServer) plugin).timer.reload();
        commandSender.sendMessage(colorFormat(messageConfig.getString("plugin-prefix") + " " + messageConfig.getString("plugin-reloaded-message")));
    }

    private void help(CommandSender commandSender) {
        List<String> helpCommandMessages = messageConfig.getStringList("help-command-message");
        for (int i = 0; i < helpCommandMessages.size(); i++) {
            commandSender.sendMessage(colorFormat(helpCommandMessages.get(i)));
        }
    }

    private void broadcastCancelTitle() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(colorFormat(messageConfig.getString("message-prefix")), colorFormat(messageConfig.getString("shutdown-cancelled-message")), 10, 70, 20);
        }
    }

    private void broadcastCancelMessage() {
        Bukkit.broadcastMessage(colorFormat(messageConfig.getString("message-prefix")) + " " + colorFormat(messageConfig.getString("shutdown-cancelled-message")));
    }

    private void broadcastStopTitle(int countdown) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(colorFormat(messageConfig.getString("message-prefix")), colorFormat(messageConfig.getString("shutdown-message")).replace("{TIME}", String.valueOf(countdown)), 10, 70, 20);
        }
    }

    private void broadcastStopMessage(int countdown) {
        Bukkit.broadcastMessage(colorFormat(messageConfig.getString("message-prefix")) + " " + colorFormat(messageConfig.getString("shutdown-message")).replace("{TIME}", String.valueOf(countdown)));
    }

    private String colorFormat(String message) {
        if (message == null) {
            return "";
        }
        return message.replace("&", "§");
    }

    private boolean isCancel;
    private boolean isStopping;
    private JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messageConfig;
    private ConfigReader messageConfigReader;

}
