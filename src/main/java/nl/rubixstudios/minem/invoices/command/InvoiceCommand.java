package nl.rubixstudios.minem.invoices.command;

import nl.rubixstudios.minem.invoices.data.Language;
import nl.rubixstudios.minem.invoices.invoice.InvoiceController;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InvoiceCommand implements CommandExecutor, TabCompleter {

    private final InvoiceController invoiceController;
    
    public InvoiceCommand() {
        this.invoiceController = InvoiceController.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.PLAYER_ONLY"));
                return true;
            }
            final Player player = (Player) sender;

            this.invoiceController.openInvoiceMenu(player, player, true);
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.OPEN_MENU"));
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.PLAYER_ONLY"));
                return true;
            }

            if (!(sender instanceof ConsoleCommandSender) && !sender.isOp()&& !this.invoiceController.getInvoiceManager().isAllowedToSentInvoice(((Player) sender))) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.NO_PERMISSION"));
                return true;
            }

            if (args.length < 4) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.USAGE"));
                return true;
            }

            final Player player = (Player) sender;
            final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.INVALID_PLAYER"));
                return true;
            }

            double amount;
            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.INVALID_AMOUNT"));
                return true;
            }

            if (amount <= 0) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.NEGATIVE_AMOUNT"));
                return true;
            }

            if (this.invoiceController.getInvoiceManager().exceededLimit(player, amount)) {
                final double limit = this.invoiceController.getInvoiceManager().getLimit(player);
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.LIMIT_EXCEEDED").replace("%limit%", String.valueOf(limit)));
                return true;
            }

            // Combine remaining arguments as the reason
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.length() > 32) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.REASON_TOO_LONG"));
                return true;
            }

            this.invoiceController.getInvoiceManager().addInvoice(player.getUniqueId(), targetPlayer.getUniqueId(), reason, amount);
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVOICE_SENT").replace("%player_name%", targetPlayer.getName()));

            if (targetPlayer.isOnline()) {
                final Player targetPlayerOnline = (Player) targetPlayer;
                targetPlayerOnline.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.CREATE_COMMAND.INVOICE_RECEIVED"));
            }
        } else if (args[0].equalsIgnoreCase("view")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.PLAYER_ONLY"));
                return true;
            }

            if (!(sender instanceof ConsoleCommandSender) && !sender.isOp() && !this.invoiceController.getInvoiceManager().isAllowedToCheckInvoice(((Player) sender))) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.NO_PERMISSION"));
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.VIEW_COMMAND.USAGE"));
                return true;
            }

            final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.INVALID_PLAYER"));
                return true;
            }

            final Player player = (Player) sender;
            this.invoiceController.openInvoiceMenu(player, targetPlayer, true);
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.VIEW_COMMAND.OPEN_MENU").replace("%player_name%", targetPlayer.getName()));
        } else if (args[0].equalsIgnoreCase("cancel")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.PLAYER_ONLY"));
                return true;
            }

            if (!(sender instanceof ConsoleCommandSender) && !sender.isOp() && !this.invoiceController.getInvoiceManager().isAllowedToCancelInvoice(((Player) sender))) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.NO_PERMISSION"));
                return true;
            }

            final Player player = (Player) sender;

            if (args.length < 4) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.USAGE"));
                return true;
            }

            final OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.INVALID_PLAYER"));
                return true;
            }

            int invoiceId;
            try {
                invoiceId = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.CANCEL_COMMAND.INVALID_INVOICE_ID"));
                return true;
            }

            // Combine remaining arguments as the reason
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                reasonBuilder.append(args[i]).append(" ");
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.length() > 32) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.REASON_TOO_LONG"));
                return true;
            }

            this.invoiceController.getInvoiceManager().cancelInvoice(player, targetPlayer.getUniqueId(), invoiceId, reason);
        } else if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + "\n");
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + Language.getMessage("INVOICE.COMMANDS.COMMAND_LIST_HEADER"));
            for (String helpCommand : this.getHelpCommands()) {
                sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + helpCommand);
            }
            sender.sendMessage(Language.getMessage("INVOICE.PREFIX") + "\n");
        }

        return false;
    }

    private List<String> getHelpCommands() {
        return Language.getMessageList("INVOICE.COMMANDS.HELP_COMMAND.COMMAND_LIST");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("create");
            completions.add("check");
            completions.add("cancel");
            completions.add("help");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("cancel")) {
                // Add player names as completions
                completions.addAll(getPlayerNames());
            }
        }

        return completions;
    }

    private List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        return playerNames;
    }
}
