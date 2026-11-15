package com.jdk.greeting.internal;

import com.jdk.greeting.api.Greeter;

public class ArabicGreeter implements Greeter {

    @Override
    public String language() {
        return "ar";
    }

    @Override
    public String greet(String name) {
        return "Ahlan, " + name + "!";
    }
}
