package com.imc.getout.fragments.mainFragments.cards;

public class ChatCard {

    private String senderUid,receiverUid,text,dateText,messageId,chatId;
    private boolean isSeen;

    public ChatCard(String senderUid, String receiverUid, String text,String dateText,boolean isSeen,String messageId,String chatId) {
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.text = text;
        this.dateText = dateText;
        this.isSeen = isSeen;
        this.messageId = messageId;
        this.chatId = chatId;
    }

    public String getDateText() {
        return dateText;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public String gettext() {
        return text;
    }

    public void settext(String text) {
        this.text = text;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
