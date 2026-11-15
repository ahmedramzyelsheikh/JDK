package com.jdk.greeting.internal;

import com.jdk.greeting.api.Greeter;

/** Public class in a NON-exported package: other modules can't use it directly, only via ServiceLoader. */
public class EnglishGreeter implements Greeter {

    @Override
    public String language() {
        return "en";
    }

    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
