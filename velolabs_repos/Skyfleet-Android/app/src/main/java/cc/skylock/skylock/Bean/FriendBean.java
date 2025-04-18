package cc.skylock.skylock.Bean;

import android.content.ContentValues;

/**
 * Created by prabhu on 3/1/16.
 */
public class FriendBean {
    private String first_name;
    private String friend_id;
    private String last_name;
    private String type;

    public static String FIRST_NAME ="first_name";
    public static String FIRST_ID ="friend_id";
    public static String LAST_NAME ="last_name";

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }
    public ContentValues getFriendContentValue(){
        ContentValues values = new ContentValues();
        values.put(FIRST_ID,getFriend_id());
        values.put(LAST_NAME,getLast_name());
        values.put(FIRST_NAME,getLast_name());
        return  values;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
