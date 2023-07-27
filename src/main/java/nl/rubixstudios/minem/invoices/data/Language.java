package nl.rubixstudios.minem.invoices.data;

import nl.rubixstudios.minem.invoices.MineMInvoices;

import java.util.List;

public class Language {

    public static String getMessage(String path) {
        final ConfigFile langFile = MineMInvoices.getInstance().getLangFile();
        return langFile.getString(path);
    }

    public static List<String> getMessageList(String path) {
        final ConfigFile langFile = MineMInvoices.getInstance().getLangFile();
        return langFile.getStringList(path);
    }
}
