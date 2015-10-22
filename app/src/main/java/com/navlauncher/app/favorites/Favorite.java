package com.navlauncher.app.favorites;

import com.navlauncher.app.Destination;

public class Favorite extends Destination {
    private final String mName;

    public Favorite(final int id, final String name, final String address) {
        super(id, address);
        mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
