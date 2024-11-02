package nl.rubixstudios.minem.invoices.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import nl.rubixstudios.minem.invoices.data.Config;
import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.InvoiceController;
import nl.rubixstudios.minem.invoices.invoice.InvoicePermission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("invoice|invoices|fine|fines|bill|bills|payment")
@Description("Manage invoices for players.")
public class InvoiceCommand extends BaseCommand {

    private final InvoiceController invoiceController;

    public InvoiceCommand() {
        this.invoiceController = InvoiceController.getInstance();
    }

    @Default
    @CommandPermission("minem.invoices.open")
    @Description("Opens your invoice menu.")
    public void onOpen(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Language.getMessage("INVOICE.CREATE_COMMAND.PLAYER_ONLY"));
            return;
        }
        Player player = (Player) sender;
        invoiceController.openInvoiceMenu(player, player, true);
    }

    @Subcommand("help")
    @CommandPermission("minem.invoices.command.help")
    @Description("Displays a list of available invoice commands.")
    public void onHelp(CommandSender sender) {
        sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.COMMAND_LIST_HEADER"));
        getHelpCommands().forEach(sender::sendMessage);
    }

    @Subcommand("forcecreate")
    @CommandPermission("minem.invoices.command.forcecreate")
    @CommandCompletion("@players @players @nothing @nothing")
    @Description("Forcefully creates an invoice between two players.")
    public void onForceCreate(CommandSender sender, OfflinePlayer creator, OfflinePlayer payer, double amount, String reason) {
        if (creator == null || payer == null) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.FORCECREATE_COMMAND.INVALID_PLAYER"));
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.FORCECREATE_COMMAND.NEGATIVE_AMOUNT"));
            return;
        }

        if (reason == null || reason.length() > 32) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.FORCECREATE_COMMAND.REASON_TOO_LONG"));
            return;
        }

        invoiceController.getInvoiceManager().addInvoice(creator.getUniqueId(), payer.getUniqueId(), reason, amount);
        sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.FORCECREATE_COMMAND.INVOICE_SENT").replace("%player_name%", payer.getName() != null ? payer.getName() : "Unknown"));

        if (payer.isOnline()) {
            Player payerOnline = payer.getPlayer();
            if (payerOnline != null) {
                payerOnline.sendMessage(Language.getMessage("INVOICE.COMMANDS.FORCECREATE_COMMAND.INVOICE_RECEIVED"));
            }
        }
    }

    @Subcommand("create")
    @CommandPermission("minem.invoices.command.create")
    @CommandCompletion("@players @nothing @nothing")
    @Description("Creates an invoice for a player.")
    public void onCreate(Player sender, OfflinePlayer payer, double amount, String reason) {
        InvoicePermission permission = getInvoicePermission(sender);
        if (permission == null || !permission.isCanCreateInvoice()) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.NO_PERMISSION"));
            return;
        }

        if (payer == null) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVALID_PLAYER"));
            return;
        }

        if (amount <= 0 || reason == null || reason.length() > 32) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVALID_INPUT"));
            return;
        }

        if (invoiceController.getInvoiceManager().exceededLimit(sender, amount)) {
            double limit = invoiceController.getInvoiceManager().getLimit(sender);
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.LIMIT_EXCEEDED").replace("%limit%", String.valueOf(limit)));
            return;
        }

        invoiceController.getInvoiceManager().addInvoice(sender.getUniqueId(), payer.getUniqueId(), reason, amount);
        sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVOICE_SENT").replace("%player_name%", payer.getName() != null ? payer.getName() : "Unknown"));

        if (payer.isOnline()) {
            Player payerOnline = payer.getPlayer();
            if (payerOnline != null) {
                payerOnline.sendMessage(Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVOICE_RECEIVED"));
            }
        }
    }

    @Subcommand("view")
    @CommandPermission("minem.invoices.command.view")
    @CommandCompletion("@players")
    @Description("View invoices of a specified player.")
    public void onView(Player sender, OfflinePlayer targetPlayer) {
        if (targetPlayer == null) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.VIEW_COMMAND.INVALID_PLAYER"));
            return;
        }

        if (Config.getBoolean("RANK_PERMISSIONS.ONLY_RANKS_MAY_VIEW_INVOICES") && !invoiceController.getInvoiceManager().isAllowedToCheckInvoice(sender)) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.VIEW_COMMAND.NO_PERMISSION_TO_SEND"));
            return;
        }

        invoiceController.openInvoiceMenu(sender, targetPlayer, true);
    }

    @Subcommand("cancel")
    @CommandPermission("minem.invoices.command.cancel")
    @CommandCompletion("@players @nothing")
    @Description("Cancels a specified invoice for a player.")
    public void onCancel(Player sender, OfflinePlayer targetPlayer, int invoiceId, String reason) {
        if (targetPlayer == null) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.INVALID_PLAYER"));
            return;
        }

        if (!invoiceController.getInvoiceManager().isAllowedToCancelInvoice(sender)) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.NO_PERMISSION_TO_SEND"));
            return;
        }

        if (reason == null || reason.length() > 32) {
            sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.REASON_TOO_LONG"));
            return;
        }

        invoiceController.getInvoiceManager().cancelInvoice(sender, targetPlayer.getUniqueId(), invoiceId, reason);
        sender.sendMessage(Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.INVOICE_CANCELLED").replace("%player_name%", targetPlayer.getName() != null ? targetPlayer.getName() : "Unknown"));
    }

    private List<String> getHelpCommands() {
        return Language.getMessageList("INVOICE.COMMANDS.HELP_COMMAND.COMMAND_LIST");
    }

    private InvoicePermission getInvoicePermission(CommandSender sender) {
        return invoiceController.getInvoiceManager().getInvoicePermissions().stream()
                .filter(perm -> perm != null && sender.hasPermission(perm.getPermission()))
                .findFirst()
                .orElse(null);
    }
}
