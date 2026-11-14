package org.telegram.ui.Alight;

import org.telegram.messenger.MessageObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Keeps previous text of edited messages so it can be shown via the
 * message context menu ("View edit history").
 */
public class AlightEdits {

    public static void record(MessageObject oldObj, MessageObject newObj) {
        if (!AlightConfig.editHistory || oldObj == null || newObj == null) {
            return;
        }
        try {
            String oldText = textOf(oldObj);
            if (oldText.length() == 0) {
                return;
            }
            String newText = textOf(newObj);
            if (newText.length() > 0 && oldText.equals(newText)) {
                return;
            }
            ArrayList<String> list = new ArrayList<>();
            if (oldObj.alightEditHistory != null) {
                list.addAll(oldObj.alightEditHistory);
            }
            long when;
            if (oldObj.messageOwner != null && oldObj.messageOwner.edit_date > 0) {
                when = ((long) oldObj.messageOwner.edit_date) * 1000L;
            } else {
                when = System.currentTimeMillis();
            }
            String stamp = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US).format(new Date(when));
            list.add(0, stamp + " — " + oldText);
            if (list.size() > 30) {
                list.subList(30, list.size()).clear();
            }
            newObj.alightEditHistory = list;
        } catch (Throwable t) {
            org.telegram.messenger.FileLog.e(t);
        }
    }

    private static String textOf(MessageObject obj) {
        if (obj.messageText != null && obj.messageText.length() > 0) {
            return obj.messageText.toString();
        }
        if (obj.caption != null && obj.caption.length() > 0) {
            return obj.caption.toString();
        }
        return "";
    }
}
