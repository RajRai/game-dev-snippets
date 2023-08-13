package server.util.bitmask;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Bitmask<T> {
    private final List<Field> fields = new ArrayList<>();

    public Bitmask(Class<T> clazz){
        for (Field field : clazz.getDeclaredFields()) {
            BitmaskField bitmaskField = field.getAnnotation(BitmaskField.class);
            if (bitmaskField != null) {
                int blockSize = bitmaskField.blockSize();
                int totalBlockSizes = 0;
                totalBlockSizes += blockSize;
                if (totalBlockSizes > 32) {
                    throw new IllegalArgumentException("Total block sizes exceed 32 bits");
                }
                fields.add(field);
            }
        }
    }

    public int createMask(T data) {
        int mask = 0;
        int shift = 0;

        for (Field field : fields) {
            BitmaskField bitmaskField = field.getAnnotation(BitmaskField.class);
            int blockSize = bitmaskField.blockSize();
            int arraySize = 1;

            int value;
            try {
                field.setAccessible(true);
                Object obj = field.get(data);
                if (obj instanceof int[] intArray) {
                    arraySize = intArray.length;
                    value = extractIntArrayBits(intArray, blockSize, arraySize);
                } else if (obj instanceof boolean[] booleanArray) {
                    arraySize = booleanArray.length;
                    value = extractBooleanArrayBits(booleanArray, arraySize);
                }
                else if (obj instanceof Boolean){
                    value = (boolean)obj ? 1 : 0;
                }
                else{
                    value = (int)obj;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error accessing field", e);
            }

            int trueSize = blockSize * Math.max(1, arraySize);
            mask |= (value & ((1 << trueSize) - 1)) << shift;
            shift += trueSize;
        }

        return mask;
    }

    public void applyMask(T data, int mask) {
        int shift = 0;

        for (Field field : fields) {
            BitmaskField bitmaskField = field.getAnnotation(BitmaskField.class);
            int blockSize = bitmaskField.blockSize();
            int arraySize = 1;

            try {
                Object obj = field.get(data);
                if (obj.getClass().isArray()) {
                    arraySize = Array.getLength(obj);
                }

                int trueSize = blockSize * arraySize;
                int value = (mask >> shift) & ((1 << trueSize) - 1);

                field.setAccessible(true);
                if (field.getType() == int[].class || field.getType() == Integer[].class) {
                    int[] intArray = extractIntArrayFromMask(shift, mask, blockSize, arraySize);
                    field.set(data, intArray);
                } else if (field.getType() == boolean[].class || field.getType() == Boolean[].class) {
                    boolean[] booleanArray = extractBooleanArrayFromMask(shift, mask, arraySize);
                    field.set(data, booleanArray);
                } else if (field.getType() == boolean.class || field.getType() == Boolean.class){
                    boolean booleanValue = value != 0;
                    field.set(data, booleanValue);
                } else {
                    field.set(data, value);
                }
                shift += trueSize;
            } catch (Exception e) {
                throw new RuntimeException("Error accessing field", e);
            }
        }
    }

    private int[] extractIntArrayFromMask(int shift, int mask, int blockSize, int arraySize) {
        int[] intArray = new int[arraySize];
        for (int i = 0; i < arraySize; i++) {
            intArray[i] = (mask >> shift) & ((1 << blockSize) - 1);
            shift += blockSize;
        }
        return intArray;
    }

    private boolean[] extractBooleanArrayFromMask(int shift, int mask, int arraySize) {
        boolean[] booleanArray = new boolean[arraySize];
        for (int i = 0; i < arraySize; i++) {
            booleanArray[i] = ((mask >> shift) & 1) == 1;
            shift++;
        }
        return booleanArray;
    }

    private int extractIntArrayBits(int[] array, int blockSize, int arraySize) {
        int result = 0;
        for (int i = 0; i < arraySize; i++) {
            result |= (array[i] & ((1 << blockSize) - 1)) << blockSize * i;
        }
        return result;
    }

    private int extractBooleanArrayBits(boolean[] array, int arraySize) {
        int result = 0;
        for (int i = 0; i < arraySize; i++) {
            result |= (array[i] ? 1 : 0) << i;
        }
        return result;
    }

    public String displayBlocks(T data, int mask) {
        StringBuilder result = new StringBuilder();
        int shift = 0;

        for (Field field : fields) {
            BitmaskField bitmaskField = field.getAnnotation(BitmaskField.class);
            int blockSize = bitmaskField.blockSize();
            int arraySize = 1;

            try {
                Object obj = field.get(data);
                if (obj.getClass().isArray()) {
                    arraySize = Array.getLength(obj);
                }
            } catch (Exception e){
                throw new RuntimeException("Error accessing field", e);
            }


            for (int i = 0; i < arraySize; i++){
                int value = (mask >> shift) & ((1 << blockSize) - 1);
                String padded = String.format("%" + blockSize + "s", Integer.toBinaryString(value)).replace(' ', '0');
                result.append(String.format("%s[%d] (%d: %d-%d): %s\n", field.getName(), i, blockSize, shift, shift+blockSize-1, padded));
                shift += blockSize;
            }
        }

        return result.toString();
    }
}
