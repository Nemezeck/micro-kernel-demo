package Process;

public class PCB {

    public enum ProcessState { NEW, READY, RUNNING, WAITING, TERMINATED }

    private int pid;
    private ProcessState state;

    private int arrivalTime;      // NEW: When process arrives
    private int burstTime;        // Total CPU time needed
    private int remainingTime;    // Time left to execute

    private int memoryRequired;
    private int memoryBase = -1;

    // Metrics tracking
    private int completionTime = -1;
    private int firstRunTime = -1;  // For response time

    public PCB(int pid, int arrivalTime, int burstTime, int memReq) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.memoryRequired = memReq;
        this.state = ProcessState.NEW;
    }

    // Getters
    public int getPid() { return pid; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getMemoryRequired() { return memoryRequired; }
    public int getMemoryBase() { return memoryBase; }
    public int getArrivalTime() { return arrivalTime; }
    public int getCompletionTime() { return completionTime; }
    public ProcessState getState() { return state; }

    // Memory.Memory management
    public void allocateMemory(int base) { this.memoryBase = base; }
    public void freeMemory() { this.memoryBase = -1; }
    public boolean hasMemory() { return memoryBase != -1; }

    // Execution
    public void reduceTime(int q) {
        remainingTime = Math.max(0, remainingTime - q);
    }

    // Track first run for response time
    public void markFirstRun(int currentTime) {
        if (firstRunTime == -1) {
            firstRunTime = currentTime;
        }
    }

    // Mark completion
    public void markCompletion(int currentTime) {
        this.completionTime = currentTime;
    }

    // Calculate metrics (using burstTime!)
    public int getTurnaroundTime() {
        if (completionTime == -1) return -1;
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        if (completionTime == -1) return -1;
        return getTurnaroundTime() - burstTime;  // HERE'S WHERE burstTime IS USED!
    }

    public int getResponseTime() {
        if (firstRunTime == -1) return -1;
        return firstRunTime - arrivalTime;
    }

    public void transitionTo(ProcessState newState) {
        System.out.println("PID " + pid + ": " + state + " → " + newState);
        state = newState;
    }

    public void printMetrics() {
        System.out.println("PID " + pid + " Metrics:");
        System.out.println("  Burst Time: " + burstTime);
        System.out.println("  Arrival Time: " + arrivalTime);
        System.out.println("  Completion Time: " + completionTime);
        System.out.println("  Turnaround Time: " + getTurnaroundTime());
        System.out.println("  Waiting Time: " + getWaitingTime());
        System.out.println("  Response Time: " + getResponseTime());
    }
}