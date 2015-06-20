package me.lukas81298.bungeecloud;

import java.util.UUID;

public class UUIDCounter {

    private static long most = 0L;
    private static long least = 0L;
    
    public static UUID nextUUID() {
	most++;
	return new UUID(most, least);
    }
}
