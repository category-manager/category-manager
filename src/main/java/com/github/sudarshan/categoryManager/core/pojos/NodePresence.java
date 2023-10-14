package com.github.sudarshan.categoryManager.core.pojos;

public enum NodePresence {
    BOTH("both"), ONLY_LINKED("onlyLinked"), ONLY_UNLINKED("onlyUnlinked");
    private final String name;

    NodePresence(String name) {
        this.name = name;
    }

    public final String getName(){
        return this.name;
    }
}
