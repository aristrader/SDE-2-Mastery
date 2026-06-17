package org.example.backend_fundamentals.design_patterns.foundations.oop_pillars.abstraction;

import java.util.HashMap;
import java.util.Map;

/**
 * The cast test for leaky vs clean abstraction.
 * <p>
 * {@link Leaky#get(String)} returns {@code Object}, so every caller must cast
 * and "know" what type was stored. The implementation has leaked — callers are
 * coupled to the storage decision. {@link Clean#get(String)} returns the typed
 * value directly; the underlying storage is invisible.
 */
public class LeakyVsCleanAbstraction {

  static class Leaky {
    private final Map<String, Object> store = new HashMap<>();

    public void put(String key, Object value) { store.put(key, value); }

    /** Caller must cast and know the actual stored type — that's the leak. */
    public Object get(String key) { return store.get(key); }
  }

  static class Clean {
    private final Map<String, String> store = new HashMap<>();

    public void put(String key, String value) { store.put(key, value); }

    /** Typed return — no cast, no implementation knowledge required. */
    public String get(String key) { return store.get(key); }
  }

  public static void main(String[] args) {
    Leaky leaky = new Leaky();
    leaky.put("greeting", "hello");
    String l = (String) leaky.get("greeting"); // cast required = leak
    System.out.println("Leaky: " + l + "  (cast was needed; caller must know it stored a String)");

    Clean clean = new Clean();
    clean.put("greeting", "hello");
    String c = clean.get("greeting"); // no cast
    System.out.println("Clean: " + c + "  (no cast; storage is invisible to caller)");
  }
}
