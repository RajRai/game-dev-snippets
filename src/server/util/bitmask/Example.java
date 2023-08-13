package server.util.bitmask;

import java.util.Arrays;

public class Example {

    static class UserData {
        @BitmaskField(blockSize = 8)
        public int id;

        @BitmaskField(blockSize = 6)
        public int age;

        @BitmaskField
        public boolean isDev = true;

        public int thisIsntIncludedInTheBitmask = 12345;

        @BitmaskField
        public boolean[] flags = new boolean[]{true, true, false};

        @BitmaskField(blockSize = 3)
        public int[] nums = new int[]{7, 3, 1};

        public UserData(int id, int age) {
            this.id = id;
            this.age = age;
        }
    }

    public static void main(String[] args) {
        Bitmask<UserData> bitmask = new Bitmask<>(UserData.class);

        UserData user = new UserData(42, 25);

        // Making a new mask from an existing object
        int mask = bitmask.createMask(user);
        printInfo(bitmask, user, mask);

        System.out.println();
        System.out.println();

        // Applying an existing mask onto an object
        mask = (int) (Math.random() * Integer.MAX_VALUE);
        bitmask.applyMask(user, mask);

        printInfo(bitmask, user, mask);
    }

    private static void printInfo(Bitmask<UserData> bitmask, UserData user, int mask) {
        System.out.println("Mask: " + String.format("%32s", Integer.toBinaryString(mask)).replace(' ', '0'));
        System.out.println("User ID: " + user.id);
        System.out.println("User Age: " + user.age);
        System.out.println("Is dev: " + user.isDev);
        System.out.println("Flags: " + Arrays.toString(user.flags));
        System.out.println("Nums: " + Arrays.toString(user.nums));
        System.out.println("Unincluded field: " + user.thisIsntIncludedInTheBitmask);

        System.out.println("==========================");

        System.out.println(bitmask.displayBlocks(user, mask));
    }
}
