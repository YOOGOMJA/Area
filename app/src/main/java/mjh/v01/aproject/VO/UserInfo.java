package mjh.v01.aproject.VO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jihun on 2017-07-18.
 */

public class UserInfo {


    public String email;
    public String name;
    public String nick;
    public String pwd;
    public String phone;
    public ArrayList<String> groupName;

    public ArrayList<String> getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName.add(groupName);
    }

    public String getNick() {
        return nick;
    }

    public UserInfo() {
        this.groupName = new ArrayList<String>();

    }

    public UserInfo(String email, String name, String nick, String pwd, String phone) {
        this.email = email;
        this.name = name;
        this.nick = nick;
        this.pwd = pwd;
        this.phone = phone;
        this.groupName = new ArrayList<String>();
    }

    public Map<String, Object> toMap(String g) {
        if (groupName == null) {
            this.groupName = new ArrayList<String>();
        }
        this.groupName.add(g);
        HashMap<String, Object> result = new HashMap<>();
        result.put("groupName", getGroupName());
        result.put("email", email);
        result.put("name", name);
        result.put("nick", nick);
        result.put("pwd", pwd);
        result.put("phone", phone);
        return result;
    }

    public UserInfo(String email, String name, String nick, String pwd, String phone, ArrayList<String> groupName) {
        if (groupName == null) {
            this.groupName = new ArrayList<String>();
        }
        this.email = email;
        this.name = name;
        this.nick = nick;
        this.pwd = pwd;
        this.phone = phone;
        this.groupName = groupName;
    }
}
