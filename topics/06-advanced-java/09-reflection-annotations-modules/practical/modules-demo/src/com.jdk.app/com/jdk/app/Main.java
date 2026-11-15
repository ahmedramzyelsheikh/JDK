package com.jdk.app;

import com.jdk.greeting.api.Greeter;
import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) {
        // import com.jdk.greeting.internal.EnglishGreeter;   -> compile error: package is not exported
        System.out.println("Module of Main: " + Main.class.getModule().getName());
        System.out.println("Declared requires: " + Main.class.getModule().getDescriptor().requires().stream()
                .map(r -> r.name()).sorted().toList());
        System.out.println("Can Main see com.jdk.greeting.internal? "
                + Greeter.class.getModule().isExported("com.jdk.greeting.internal", Main.class.getModule()));

        for (Greeter greeter : ServiceLoader.load(Greeter.class)) {
            System.out.println(greeter.language() + " -> " + greeter.greet("Java")
                    + "   (implementation " + greeter.getClass().getName() + ")");
        }
    }
}
