package cc.skylock.skylock.Bean;

/**
 * Created by Velo Labs Android on 31-01-2017.
 */

public class Error {

    /**
     * name : MissingParameter
     * message : There is one or more parameters missing in the supplied request
     */

    private String name;
    private String message;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
