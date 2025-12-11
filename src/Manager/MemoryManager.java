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
        // Initialize with one big free block
        freeBlocks.put(0, new MemoryBlock(0, total));
    }

    public boolean allocate(PCB pcb, int size) {
        MemoryBlock bestFit = null;
        int bestBase = -1;

        // Best-fit: find the smallest block that fits
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
            System.out.println("Memory.Memory allocation failed for PID " + pcb.getPid());
            return false;
        }

        // Remove the free block
        freeBlocks.remove(bestBase);

        // Create allocated block
        MemoryBlock allocatedBlock = new MemoryBlock(bestBase, size);
        allocated.put(pcb.getPid(), allocatedBlock);

        // Update Process.Process.Process.PCB with memory base address
        pcb.allocateMemory(bestBase);

        // If there's leftover space, create a new free block
        if (bestFit.size > size) {
            int newStart = bestBase + size;
            int newSize = bestFit.size - size;
            freeBlocks.put(newStart, new MemoryBlock(newStart, newSize));
        }

        // Simulate writing to actual memory
        for (int i = bestBase; i < bestBase + size; i++) {
            Memory.mem[i] = pcb.getPid(); // Mark memory as belonging to this process
        }

        System.out.println("Allocated PID " + pcb.getPid() +
                " → [" + bestBase + "-" + (bestBase + size - 1) +
                "] (" + size + " units)");
        return true;
    }

    public void free(PCB pcb) {
        int pid = pcb.getPid();
        if (!allocated.containsKey(pid)) return;

        MemoryBlock block = allocated.remove(pid);

        // Clear the actual memory
        for (int i = block.start; i < block.start + block.size; i++) {
            Memory.mem[i] = 0;
        }

        // Update Process.Process.Process.PCB
        pcb.freeMemory();

        // Add back to free blocks
        freeBlocks.put(block.start, block);

        // Coalesce adjacent free blocks
        coalesce();

        System.out.println("Freed PID " + pid +
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
                // Check if adjacent
                if (current.start + current.size == block.start) {
                    current.size += block.size; // Merge
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
        System.out.println("\n--- Memory.Memory Map ---");
        System.out.println("Allocated:");
        for (var entry : allocated.entrySet()) {
            MemoryBlock b = entry.getValue();
            System.out.println("  PID " + entry.getKey() + ": [" + b.start + "-" + (b.start + b.size - 1) + "]");
        }
        System.out.println("Free:");
        for (var entry : freeBlocks.entrySet()) {
            MemoryBlock b = entry.getValue();
            System.out.println("  [" + b.start + "-" + (b.start + b.size - 1) + "] (" + b.size + " units)");
        }
        System.out.println("------------------\n");
    }
}