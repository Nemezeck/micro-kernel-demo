public class PCB {

    public enum ProcessState { NEW, READY, RUNNING, WAITING, TERMINATED }

    private int pid;
    private ProcessState state;

    private int burstTime;
    private int remainingTime;

    private int memoryRequired;
    private int memoryBase = -1;

    public PCB(int pid, int burstTime, int memReq) {
        this.pid = pid;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.memoryRequired = memReq;
        this.state = ProcessState.NEW;
    }

    public int getPid() { return pid; }
    public int getRemainingTime() { return remainingTime; }
    public int getMemoryRequired() { return memoryRequired; }
    public int getMemoryBase() { return memoryBase; }

    public void allocateMemory(int base) { this.memoryBase = base; }
    public void freeMemory() { this.memoryBase = -1; }
    public boolean hasMemory() { return memoryBase != -1; }

    public void reduceTime(int q) { remainingTime = Math.max(0, remainingTime - q); }

    public void transitionTo(ProcessState newState) {
        System.out.println("PID " + pid + ": " + state + " → " + newState);
        state = newState;
    }
}
