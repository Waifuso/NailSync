package phong.demo.DTO;

public class JSon_Object <T>{

    private T object;

    public JSon_Object(T object) {
        this.object = object;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
