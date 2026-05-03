package com.ctnhlang;

public interface Lang {

    <T> T translate(Object... args);

    String key();
}
