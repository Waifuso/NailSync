package phong.demo.DTO;

public class Json_object_withID {

    private String message;

    private long ID;

    public Json_object_withID(String message, long ID) {
        this.message = message;
        this.ID = ID;
    }

    public String getMessage() {
        return message;
    }

    public long getID() {
        return ID;
    }
}
