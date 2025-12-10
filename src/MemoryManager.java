import java.util.*;

public class MemoryManager {

    private int totalMemory;
    private TreeMap<Integer, Integer> freeBlocks = new TreeMap<>();
    private Map<Integer, Integer> allocated = new HashMap<>();

    public MemoryManager(int total) {
        this.totalMemory = total;
        freeBlocks.put(0, total);
    }

    public boolean allocate(int pid, int size) {
        Integer bestBase = null;

        for (var entry : freeBlocks.entrySet()) {
            int base = entry.getKey();
            int length = entry.getValue();

            if (length >= size) {
                if (bestBase == null || length < freeBlocks.get(bestBase)) {
                    bestBase = base;
                }
            }
        }

        if (bestBase == null) return false;

        int blockLen = freeBlocks.get(bestBase);
        freeBlocks.remove(bestBase);

        allocated.put(pid, bestBase);

        if (blockLen > size) {
            freeBlocks.put(bestBase + size, blockLen - size);
        }

        System.out.println("Allocated PID " + pid + " memory at base " + bestBase);
        return true;
    }

    public void free(int pid) {
        if (!allocated.containsKey(pid)) return;

        int base = allocated.remove(pid);
        freeBlocks.put(base, 10); // assume fixed memory requirement for simplicity
        System.out.println("Memory freed for PID " + pid);
    }
}
