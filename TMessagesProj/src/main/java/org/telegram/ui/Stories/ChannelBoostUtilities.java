package org.telegram.ui.Stories;

import android.text.TextUtils;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

public class ChannelBoostUtilities {
    public static String createLink(int currentAccount, long dialogId) {
        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-dialogId);
        String username = ChatObject.getPublicUsername(chat);
        // AlightGram: build the link on the cosmetic display domain (alight.cc).
        final String prefix = MessagesController.getInstance(currentAccount).getDisplayLinkPrefix();
        if (!TextUtils.isEmpty(username)) {
            return "https://" + prefix + "/boost/" + ChatObject.getPublicUsername(chat);
        } else {
            return "https://" + prefix + "/boost/?c=" + -dialogId;
        }
    }
}
