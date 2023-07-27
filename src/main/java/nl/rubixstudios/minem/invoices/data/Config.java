package nl.rubixstudios.minem.invoices.data;

import nl.rubixstudios.minem.invoices.MineMInvoices;

import java.io.File;
import java.util.List;

public class Config {

    public static File INVOICE_DIR;

    public static String LICENSE_KEY;

    public Config() {
        final ConfigFile config = MineMInvoices.getInstance().getConfigFile();

        INVOICE_DIR = new File(MineMInvoices.getInstance().getDataFolder(), "invoices");
        if (!INVOICE_DIR.exists()) INVOICE_DIR.mkdirs();

        LICENSE_KEY = config.getString("LICENSE_KEY");
    }

    public static String getMessage(String path) {
        final ConfigFile configFile = MineMInvoices.getInstance().getConfigFile();
        return configFile.getString(path);
    }

    public static int getInteger(String path) {
        final ConfigFile configFile = MineMInvoices.getInstance().getConfigFile();
        return configFile.getInt(path);
    }

    public static List<String> getMessageList(String path) {
        final ConfigFile configFile = MineMInvoices.getInstance().getConfigFile();
        return configFile.getStringList(path);
    }
}
