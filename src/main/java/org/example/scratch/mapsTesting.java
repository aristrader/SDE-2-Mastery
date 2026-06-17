package org.example.scratch;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.google.common.collect.ImmutableMap;

public class mapsTesting {
  public static void main(String[] args) {

//    Map<String, Object> xyz = ImmutableMap.of("RAVI", "RAJAN", "KAVI", null);
//    System.out.println(xyz);

    Map<String, Object> map = new HashMap<>();
    map.put("a", 1);
    map.put("b", "asjsdsnj");

    Map<String, Object> copyMap = new HashMap<>(null);
    copyMap.put("c", 3.14);
    copyMap.remove("d");
    copyMap.remove("a");
    copyMap.put("b", "Hi value changed");

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
    }

    System.out.println("     ");

    for (Map.Entry<String, Object> entry : copyMap.entrySet()) {
      System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
    }

  }
}
