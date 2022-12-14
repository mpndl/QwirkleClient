package za.nmu.wrpv.qwirkle.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class Message implements Serializable {
    private static final long serialVersionUID = 0L;
    protected final Map<String, Object> data = new HashMap<>();
    public void apply() {}
    public void put(String key, Object value) {
        data.put(key, value);
    }
    public Object get(String key) {
        return data.get(key);
    }
    public void remove(String key) {
        data.remove(key);
    }
}
