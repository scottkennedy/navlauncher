package com.navlauncher.app;

public class Destination {
    private final int mId;
    private final String mAddress;

    public Destination(final int id, final String address) {
        super();

        mId = id;
        mAddress = address;
    }

    public int getId() {
        return mId;
    }

    public String getAddress() {
        return mAddress;
    }

    @Override
    public String toString() {
        return mAddress;
    }
}
