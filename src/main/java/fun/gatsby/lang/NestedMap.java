package fun.gatsby.lang;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedMap<K extends NestedMap.NestedKey,V> {

    private final Map<K,V> map = new HashMap<>();






    public static void main(String[] args) {
        var key3 = new NestedKey3<String, Study,Series>();


    }

    interface NestedKey {
        List<Object> keys();
    }
    @Data
    static class NestedKey2<K1, K2> {
        List<String> keys;

    }
    @Data
    static class NestedKey3<K1,K2, K3> {
        List<String> keys;

    }

}
@Data
class Study{
    String id;
    String name;
}
@Data
class Series{
    String id;
    String sutdyUid;
    String name;
}
@Data
class Image{
    String id;
    String seriesUid;
    String name;

}

