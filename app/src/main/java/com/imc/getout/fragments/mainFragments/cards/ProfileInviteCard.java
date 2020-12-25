package com.imc.getout.fragments.mainFragments.cards;

import java.util.ArrayList;

public class ProfileInviteCard {

    private String inviteTypeText,inviteTitleText,inviteLocationText,inviteAddressText,inviteExplanationText,id,timeText;
    private ArrayList<String> attendants,requests;
    private int priority;
    private long maxPerson,currentPerson;

    public ProfileInviteCard(String inviteTypeText, String inviteTitleText, String inviteLocationText, String inviteAddressText, String inviteExplanationText, ArrayList<String> attendants,ArrayList<String> requests, String id, int priority,long maxPerson,long currentPerson,String timeText) {
        this.inviteTypeText = inviteTypeText;
        this.inviteTitleText = inviteTitleText;
        this.inviteLocationText = inviteLocationText;
        this.inviteAddressText = inviteAddressText;
        this.inviteExplanationText = inviteExplanationText;
        this.attendants = attendants;
        this.requests = requests;
        this.id = id;
        this.priority = priority;
        this.maxPerson = maxPerson;
        this.currentPerson = currentPerson;
        this.timeText = timeText;
    }

    public ArrayList<String> getRequests() { return requests; }

    public long getCurrentPerson() { return currentPerson; }

    public long getMaxperson() { return maxPerson; }

    public int getPriority() {
        return priority;
    }

    public String getDocumentId() {
        return id;
    }

    public void setDocumentId(String id) {
        this.id = id;
    }

    public String getInviteTypeText() {
        return inviteTypeText;
    }

    public void setInviteTypeText(String inviteTypeText) {
        this.inviteTypeText = inviteTypeText;
    }

    public String getInviteTitleText() {
        return inviteTitleText;
    }

    public void setInviteTitleText(String inviteTitleText) {
        this.inviteTitleText = inviteTitleText;
    }

    public String getInviteLocationText() {
        return inviteLocationText;
    }

    public void setInviteLocationText(String inviteLocationText) {
        this.inviteLocationText = inviteLocationText;
    }

    public String getInviteAddressText() {
        return inviteAddressText;
    }

    public void setInviteAddressText(String inviteAddressText) {
        this.inviteAddressText = inviteAddressText;
    }

    public String getInviteExplanationText() {
        return inviteExplanationText;
    }

    public void setInviteExplanationText(String inviteExplanationText) {
        this.inviteExplanationText = inviteExplanationText;
    }

    public ArrayList<String> getAttendants() {
        return attendants;
    }

    public void setAttendants(ArrayList<String> attendants) {
        this.attendants = attendants;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }
}
