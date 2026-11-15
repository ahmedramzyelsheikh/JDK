package com.jdk.shop.app;

import com.jdk.shop.core.Cart;

/**
 * Application module using the core module.
 * Run: ./mvnw -q -f topics/12-build-tools/practical/maven-multimodule/pom.xml install -DskipTests
 *      ./mvnw -q -f topics/12-build-tools/practical/maven-multimodule/shop-app/pom.xml exec:java
 */
public class ShopApp {

    public static void main(String[] args) {
        Cart cart = new Cart()
                .add("effective java", "45.00", 1)
                .add("coffee", "3.20", 4);
        cart.lines().forEach(l -> System.out.printf("%-16s %2d x %6s = %7s%n", l.product(), l.quantity(), l.unitPrice(), l.total()));
        System.out.println("Total: " + cart.total());
        System.out.println("Module shop-app is using the Cart class from module shop-core.");
    }
}
