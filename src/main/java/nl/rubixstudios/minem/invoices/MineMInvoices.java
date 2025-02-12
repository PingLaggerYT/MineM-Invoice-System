package nl.rubixstudios.minem.invoices;

import co.aikar.commands.PaperCommandManager;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import nl.rubixstudios.minem.invoices.command.InvoiceCommand;
import nl.rubixstudios.minem.invoices.data.Config;
import nl.rubixstudios.minem.invoices.data.ConfigFile;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.InvoiceController;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import nl.rubixstudios.minem.invoices.util.metrics.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

@Getter
public final class MineMInvoices extends JavaPlugin {

    @Getter private static MineMInvoices instance;
    @Setter private boolean fullyEnabled;

    private Metrics metrics;

    @Setter private ConfigFile configFile;
    @Setter private ConfigFile langFile;

    private Economy econ;
    private Gson gson;

    private InvoiceController invoiceController;

    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration file
        if (!loadConfig()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Print plugin information
        printPluginInfo();

        // Check version validity
        if (!checkVersion()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Check Vault availability
        checkVault();

        registerGson();
        setupEconomy();

        invoiceController = new InvoiceController();

        // Initialize ACF and register the command
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new InvoiceCommand());

        int pluginId = 19961; // <-- Replace with the id of your plugin!
        metrics = new Metrics(this, pluginId);

        fullyEnabled = true;

        log("&aEnabled "  + getDescription().getName() + " &a!");
        log("");
        log("&e===&6=============================================&e===");
    }

    @Override
    public void onDisable() {
        if (fullyEnabled) {
            log("- &cDisabling "  + getDescription().getName() + " &c!");
            log("");
            if (invoiceController != null) {
                invoiceController.disable();
            }
            Bukkit.getServicesManager().unregisterAll(this);
            log("");

            metrics.shutdown();
        }
    }

    private boolean loadConfig() {
        try {
            configFile = new ConfigFile("config.yml");
            langFile = new ConfigFile("language_EN.yml");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return false;
        }
        new Config();
        new Language();
        return true;
    }

    private void printPluginInfo() {
        log("&e===&6=============================================&e===");
        log("- &fName&7: &e"  + getDescription().getName());
        log("- &fVersion&7: &e"  + getDescription().getVersion());
        log("- &fAuthor&7: &e"  + getDescription().getAuthors());
        log("- &fSupport&7: &e"  + getDescription().getWebsite());
        log("");
    }

    private boolean checkVersion() {
        log("&eChecking version compatibility:");

        // Get the server version string
        String serverVersion = Bukkit.getServer().getVersion();

        // Extract the major version number
        int majorVersion;
        try {
            String[] versionParts = serverVersion.split("\\.");
            majorVersion = Integer.parseInt(versionParts[1]); // The major version number is the second part
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return false; // Unable to determine version, handle accordingly
        }

        // Check if the major version is between 8 and 20 (inclusive)
        if (majorVersion >= 8 && majorVersion <= 20) {
            log("&aServer version %version% is compatible!".replace("%version%", String.valueOf(majorVersion)));
            return true;
        } else {
            log("&cServer version %version% is not compatible!".replace("%version%", String.valueOf(majorVersion)));
            return false;
        }
    }

    private boolean checkVault() {
        log("&eChecking Vault availability:");
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            log("   &c&lVault integration is not available because it's not installed!");
            return false;
        }
        log("   &aVault is installed and available.");
        log("");
        return true;
    }

    public void registerGson() {
        gson = new Gson();
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public void log(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorUtil.translate(message));
    }
}
