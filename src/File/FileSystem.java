package File;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileSystem {
    private static final int BLOCK_SIZE = 64; // bytes por bloque
    private static final int TOTAL_BLOCKS = 16;

    private byte[][] disk = new byte[TOTAL_BLOCKS][BLOCK_SIZE];
    private boolean[] freeBlocks = new boolean[TOTAL_BLOCKS];
    private Map<String, FileDescriptor> fileTable = new HashMap<>();

    public FileSystem() {
        // Inicializar todos los bloques como libres
        Arrays.fill(freeBlocks, true);
    }

    public boolean createFile(String fileName, int size) {
        if (fileTable.containsKey(fileName)) {
            System.out.println("El archivo " + fileName + " ya existe");
            return false;
        }

        int blocksNeeded = (size + BLOCK_SIZE - 1) / BLOCK_SIZE;
        int startBlock = findContiguousBlocks(blocksNeeded);

        if (startBlock == -1) {
            System.out.println("Espacio insuficiente para el archivo " + fileName);
            return false;
        }

        // Asignar bloques
        for (int i = startBlock; i < startBlock + blocksNeeded; i++) {
            freeBlocks[i] = false;
        }

        FileDescriptor fd = new FileDescriptor(fileName, size, startBlock);
        fileTable.put(fileName, fd);

        System.out.println("Archivo creado " + fileName + " (" + size + " bytes) en el bloque " + startBlock);
        return true;
    }

    private int findContiguousBlocks(int count) {
        for (int i = 0; i <= TOTAL_BLOCKS - count; i++) {
            boolean found = true;
            for (int j = i; j < i + count; j++) {
                if (!freeBlocks[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    public boolean writeFile(String fileName, byte[] data) {
        FileDescriptor fd = fileTable.get(fileName);
        if (fd == null) {
            System.out.println("Archivo " + fileName + " no encontrado");
            return false;
        }

        if (!fd.isOpen()) {
            System.out.println("El archivo " + fileName + " no está abierto");
            return false;
        }

        int blocksNeeded = (data.length + BLOCK_SIZE - 1) / BLOCK_SIZE;
        int startBlock = fd.getStartBlock();

        for (int i = 0; i < blocksNeeded; i++) {
            int offset = i * BLOCK_SIZE;
            int length = Math.min(BLOCK_SIZE, data.length - offset);
            System.arraycopy(data, offset, disk[startBlock + i], 0, length);
        }

        System.out.println("Escritos " + data.length + " bytes en " + fileName);
        return true;
    }

    public byte[] readFile(String fileName) {
        FileDescriptor fd = fileTable.get(fileName);
        if (fd == null) {
            System.out.println("Archivo " + fileName + " no encontrado");
            return null;
        }

        if (!fd.isOpen()) {
            System.out.println("El archivo " + fileName + " no está abierto");
            return null;
        }

        byte[] data = new byte[fd.getSize()];
        int blocksNeeded = (fd.getSize() + BLOCK_SIZE - 1) / BLOCK_SIZE;
        int startBlock = fd.getStartBlock();

        for (int i = 0; i < blocksNeeded; i++) {
            int offset = i * BLOCK_SIZE;
            int length = Math.min(BLOCK_SIZE, fd.getSize() - offset);
            System.arraycopy(disk[startBlock + i], 0, data, offset, length);
        }

        System.out.println("Leídos " + data.length + " bytes de " + fileName);
        return data;
    }

    public boolean openFile(String fileName) {
        FileDescriptor fd = fileTable.get(fileName);
        if (fd == null) return false;
        fd.open();
        System.out.println("Archivo abierto " + fileName);
        return true;
    }

    public boolean closeFile(String fileName) {
        FileDescriptor fd = fileTable.get(fileName);
        if (fd == null) return false;
        fd.close();
        System.out.println("Archivo cerrado " + fileName);
        return true;
    }

    public void listFiles() {
        System.out.println("\n--- Sistema de Archivos ---");
        for (FileDescriptor fd : fileTable.values()) {
            System.out.println("  " + fd.getFileName() + ": " + fd.getSize() +
                    " bytes, bloque " + fd.getStartBlock() +
                    (fd.isOpen() ? " [ABIERTO]" : " [CERRADO]"));
        }
        System.out.println("-------------------\n");
    }
}