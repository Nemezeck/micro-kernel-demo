package Manager;

import Memory.Memory;
import Memory.MemoryBlock;
import Process.PCB;


import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MemoryManager {

    private int totalMemory;
    private TreeMap<Integer, MemoryBlock> freeBlocks = new TreeMap<>();
    private Map<Integer, MemoryBlock> allocated = new HashMap<>();

    public MemoryManager(int total) {
        this.totalMemory = total;
        // Inicializar con un bloque grande libre
        freeBlocks.put(0, new MemoryBlock(0, total));
    }

    public boolean allocate(PCB pcb, int size) {
        MemoryBlock bestFit = null;
        int bestBase = -1;

        // Best-fit: encuentra el bloque más pequeño que encaja
        for (var entry : freeBlocks.entrySet()) {
            MemoryBlock block = entry.getValue();

            if (block.size >= size) {
                if (bestFit == null || block.size < bestFit.size) {
                    bestFit = block;
                    bestBase = entry.getKey();
                }
            }
        }

        if (bestFit == null) {
            System.out.println("Fallo la asignación de memoria para el PID " + pcb.getPid());
            return false;
        }

        // Quitar el bloque libre
        freeBlocks.remove(bestBase);

        // Crear bloque asignado
        MemoryBlock allocatedBlock = new MemoryBlock(bestBase, size);
        allocated.put(pcb.getPid(), allocatedBlock);

        // Actualizar Process.Process.Process.PCB con la dirección base de memoria
        pcb.allocateMemory(bestBase);

        // Si hay espacio sobrante, crea un nuevo bloque libre
        if (bestFit.size > size) {
            int newStart = bestBase + size;
            int newSize = bestFit.size - size;
            freeBlocks.put(newStart, new MemoryBlock(newStart, newSize));
        }

        // Simular escritura en memoria real
        for (int i = bestBase; i < bestBase + size; i++) {
            Memory.mem[i] = pcb.getPid(); // Marcar la memoria como perteneciente a este proceso
        }

        System.out.println("Asignado PID " + pcb.getPid() +
        " → [" + bestBase + "-" + (bestBase + size - 1) +
        "] (" + size + " unidades)");
        return true;
    }

    public void free(PCB pcb) {
        int pid = pcb.getPid();
        if (!allocated.containsKey(pid)) return;

        MemoryBlock block = allocated.remove(pid);

        // Borrar la memoria actual
        for (int i = block.start; i < block.start + block.size; i++) {
            Memory.mem[i] = 0;
        }

        // Proceso de actualización.Proceso.Proceso.PCB
        pcb.freeMemory();

        // Agregar de nuevo a bloques gratuitos
        freeBlocks.put(block.start, block);

        // Fusionar bloques libres adyacentes
        coalesce();

        System.out.println("Liberado PID " + pid +
        " → [" + block.start + "-" + (block.start + block.size - 1) + "]");
    }

    private void coalesce() {
        TreeMap<Integer, MemoryBlock> merged = new TreeMap<>();

        MemoryBlock current = null;
        for (var entry : freeBlocks.entrySet()) {
            MemoryBlock block = entry.getValue();

            if (current == null) {
                current = new MemoryBlock(block.start, block.size);
            } else {
                // Verificar si es adyacente
                if (current.start + current.size == block.start) {
                    current.size += block.size; // une
                } else {
                    merged.put(current.start, current);
                    current = new MemoryBlock(block.start, block.size);
                }
            }
        }

        if (current != null) {
            merged.put(current.start, current);
        }

        freeBlocks = merged;
    }

    public void printMemoryMap() {
        System.out.println("\n--- Mapa de Memoria ---");
        System.out.println("Asignado:");
        for (var entry : allocated.entrySet()) {
            MemoryBlock b = entry.getValue();
            System.out.println("  PID " + entry.getKey() + ": [" + b.start + "-" + (b.start + b.size - 1) + "]");
        }
        System.out.println("Libre:");
        for (var entry : freeBlocks.entrySet()) {
            MemoryBlock b = entry.getValue();
            System.out.println("  [" + b.start + "-" + (b.start + b.size - 1) + "] (" + b.size + " unidades)");
        }
        System.out.println("------------------\n");
    }
}