package cc.skylock.skylock.pojo;

/**
 * Created by Velo Labs Android on 13-01-2016.
 */
public class PojoforContacts {
    String name;
    String number;
    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PojoforContacts(String name,String number,String id)
    {
         this.name = name;
        this.number = number;
        this.id = id;
    }
    public PojoforContacts()
    {}

}
