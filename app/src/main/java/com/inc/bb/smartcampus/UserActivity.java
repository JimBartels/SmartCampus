package com.inc.bb.smartcampus;

public class UserActivity {
    int activityType = 4;
    int activityConfidence;

    public void updateUserActivity(int type, int confidence) {
        this.activityType = type;
        this.activityConfidence = confidence;
    }

    public int[] getUserActivity() {
        int confType[] = new int[2];
        confType[0] = activityType;
        confType[1] = activityConfidence;
        return confType;
    }
}
