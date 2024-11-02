package nl.rubixstudios.minem.invoices.invoice;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import nl.rubixstudios.minem.invoices.MineMInvoices;
import nl.rubixstudios.minem.invoices.data.Config;
import nl.rubixstudios.minem.invoices.data.ConfigFile;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.object.Invoice;
import nl.rubixstudios.minem.invoices.invoice.object.InvoiceStatus;
import nl.rubixstudios.minem.invoices.invoice.task.InvoiceTask;
import nl.rubixstudios.minem.invoices.util.ColorUtil;
import nl.rubixstudios.minem.invoices.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

@Getter
public class InvoiceManager {

    private List<InvoiceUser> invoiceUsers;
    private List<InvoicePermission> invoicePermissions;

    private final File invoiceUsersFile;

    private BukkitRunnable invoiceTask;

    public InvoiceManager() {
        this.invoiceUsersFile = FileUtils.getOrCreateFile(Config.INVOICE_DIR, "invoiceUsers.json");

        this.loadInvoices();
        this.loadInvoicePermissions();

        this.invoiceTask = new InvoiceTask(this);
        this.invoiceTask.runTaskTimerAsynchronously(MineMInvoices.getInstance(), 0L, 5 * (60 * 20L));
    }

    public void disable() {
        this.invoiceTask.cancel();
        this.invoiceTask = null;

        this.saveInvoices(true);

    }

    public void loadInvoicePermissions() {
        if (this.invoicePermissions == null) {
            this.invoicePermissions = new ArrayList<>();
        } else {
            this.invoicePermissions.clear();
        }

        ConfigFile config = MineMInvoices.getInstance().getConfigFile();
        if (config == null) {
            MineMInvoices.getInstance().getLogger().warning("Could not load invoice permissions, config file is null.");
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("RANK_PERMISSIONS");
        if (section == null || section.getKeys(false).isEmpty()) {
            MineMInvoices.getInstance().getLogger().warning("Could not load invoice permissions, please set them up in the config.");
            return;
        }

        section.getKeys(false).forEach(key -> {
            final String permission = section.getString(key + ".PERMISSION");
            final double limit = section.getDouble(key + ".LIMIT_AMOUNT");
            final boolean canCreateInvoice = section.getBoolean(key + ".CAN_CREATE_INVOICE");
            final boolean canCancelInvoice = section.getBoolean(key + ".CAN_CANCEL_INVOICE");
            final boolean canCheckInvoice = section.getBoolean(key + ".CAN_CHECK_INVOICE");

            final InvoicePermission invoicePermission = new InvoicePermission(key, permission, limit, canCreateInvoice, canCancelInvoice, canCheckInvoice);
            invoicePermissions.add(invoicePermission);
        });
    }

    public void loadInvoices() {
        try (FileReader reader = new FileReader(invoiceUsersFile)) {
            Type invoiceUserType = new TypeToken<List<InvoiceUser>>() {}.getType();
            List<InvoiceUser> loadedInvoiceUsers = MineMInvoices.getInstance().getGson().fromJson(reader, invoiceUserType);
            if (loadedInvoiceUsers != null && !loadedInvoiceUsers.isEmpty()) {
                invoiceUsers = loadedInvoiceUsers;
                MineMInvoices.getInstance().log(ColorUtil.translate("&aLoaded " + invoiceUsers.size() + " invoices."));
            } else {
                invoiceUsers = new ArrayList<>();
                MineMInvoices.getInstance().log(ColorUtil.translate("&eNo invoices found. Initialized an empty list."));
            }
        } catch (FileNotFoundException e) {
            invoiceUsers = new ArrayList<>();
            MineMInvoices.getInstance().log(ColorUtil.translate("&eNo invoices file found. Initialized an empty list."));
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveInvoices(boolean log) {
        try (FileWriter writer = new FileWriter(invoiceUsersFile)) {
            Type invoiceUserType = new TypeToken<List<InvoiceUser>>() {}.getType();
            MineMInvoices.getInstance().getGson().toJson(invoiceUsers, invoiceUserType, writer);
            writer.flush();
            if (log) {
                MineMInvoices.getInstance().log(ColorUtil.translate("&aSaved " + invoiceUsers.size() + " invoices."));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addInvoice(UUID billingFrom, UUID billingTo, String description, double amount) {
        final InvoiceUser invoiceUser = this.getOrCreateInvoiceUser(billingTo);

        final int invoiceId = 1 + invoiceUser.getInvoices().size();
        final Date dateToday = new Date();

        final Invoice invoice = new Invoice(invoiceId, InvoiceStatus.OPEN, billingFrom, amount, description, dateToday);
        invoice.setDaysToPay(Config.getInteger("INVOICE_SETTINGS.STANDARD_DAYS_TO_PAY"));
        invoiceUser.getInvoices().add(invoice);
    }

    public String payInvoice(UUID billingUser, int invoiceId, boolean autoPay) {
        InvoiceUser invoiceUser = getOrCreateInvoiceUser(billingUser);
        Invoice invoice = invoiceUser.getInvoices()
                .stream()
                .filter(inv -> inv.getInvoiceId() == invoiceId)
                .findFirst()
                .orElse(null);

        if (invoice == null) {
            return "INVOICE_NOT_FOUND";
        }

        if (invoice.getInvoiceStatus() != InvoiceStatus.OPEN) {
            return "INVOICE_ALREADY_PAID";
        }

        if (autoPay) {
            return "INVOICE_AUTO_PAID";
        }

        Player player = Bukkit.getPlayer(billingUser);
        if (!MineMInvoices.getInstance().getEcon().has(player, invoice.getInvoiceAmount())) {
            return "NOT_ENOUGH_MONEY";
        }

        MineMInvoices.getInstance().getEcon().withdrawPlayer(player, invoice.getInvoiceAmount());

        invoice.setInvoiceStatus(InvoiceStatus.PAID);
        invoice.setDaysToPay(0);
        invoice.setPaymentDateTime(System.currentTimeMillis());
        invoice.setPaidBy(player.getUniqueId());

        // Give money to invoice creator
        Player creator = Bukkit.getPlayer(invoice.getInvoiceAuthor());
        if (creator != null) {
            MineMInvoices.getInstance().getEcon().depositPlayer(creator, invoice.getInvoiceAmount());
        }

        return "INVOICE_PAID";
    }

    public void cancelInvoice(Player player, UUID billingUser, int invoiceId, String reason) {
        InvoiceUser invoiceUser = getOrCreateInvoiceUser(billingUser);
        Invoice invoice = invoiceUser.getInvoices()
                .stream()
                .filter(inv -> inv.getInvoiceId() == invoiceId)
                .findFirst()
                .orElse(null);

        if (invoice == null) {
            return;
        }

        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        invoice.setDaysToPay(0);
        invoice.setCanceledOnDateTime(System.currentTimeMillis());
        invoice.setCancelReason(reason);
        invoice.setCanceledBy(player.getUniqueId());

        player.sendMessage(Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.INVOICE_CANELLED")
                .replace("%player_name%", Bukkit.getOfflinePlayer(billingUser).getName())
                .replace("%invoice_id%", String.valueOf(invoiceId)));
    }


    public InvoiceUser getOrCreateInvoiceUser(UUID playerId) {
        InvoiceUser invoiceUser = getInvoiceUser(playerId);

        if (invoiceUser == null) {
            invoiceUser = new InvoiceUser(playerId);
            invoiceUsers.add(invoiceUser);
        }

        return invoiceUser;
    }

    public InvoiceUser getInvoiceUser(UUID playerId) {
        return invoiceUsers.stream()
                .filter(invoiceUser -> invoiceUser.getPlayerId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean isAlreadyInvoiceUser(UUID playerId) {
        return invoiceUsers.stream()
                .anyMatch(invoiceUser -> invoiceUser.getPlayerId().equals(playerId));
    }

    public boolean isAllowedToSentInvoice(Player player) {
        return invoicePermissions.stream().anyMatch(entry -> player.hasPermission(entry.getPermission()) && entry.isCanCreateInvoice());
    }

    public boolean isAllowedToCheckInvoice(Player player) {
        return invoicePermissions.stream().anyMatch(entry -> player.hasPermission(entry.getPermission()) && entry.isCanCheckInvoices());
    }

    public boolean isAllowedToCancelInvoice(Player player) {
        return invoicePermissions.stream().anyMatch(entry -> player.hasPermission(entry.getPermission()) && entry.isCanCancelInvoice());
    }

    public boolean exceededLimit(Player player, double amount) {
        return invoicePermissions.stream()
                .filter(entry -> player.hasPermission(entry.getPermission()))
                .anyMatch(entry -> entry.getLimit() < amount);
    }

    public double getLimit(Player player) {
        return invoicePermissions.stream()
                .filter(entry -> player.hasPermission(entry.getPermission()))
                .findFirst()
                .map(InvoicePermission::getLimit)
                .orElse(0.0);
    }
}
