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

    // Seguimiento de métricas
    private int completionTime = -1;
    private int firstRunTime = -1;  // Para el tiempo de respuesta

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

    // Memoria.Gestión de la memoria
    public void allocateMemory(int base) { this.memoryBase = base; }
    public void freeMemory() { this.memoryBase = -1; }
    public boolean hasMemory() { return memoryBase != -1; }

    // Ejecución
    public void reduceTime(int q) {
        remainingTime = Math.max(0, remainingTime - q);
    }

    // Realizar un seguimiento de la primera ejecución para el tiempo de respuesta
    public void markFirstRun(int currentTime) {
        if (firstRunTime == -1) {
            firstRunTime = currentTime;
        }
    }

    // Marcar finalización
    public void markCompletion(int currentTime) {
        this.completionTime = currentTime;
    }

    // Calcular métricas (¡usando burstTime!)
    public int getTurnaroundTime() {
        if (completionTime == -1) return -1;
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        if (completionTime == -1) return -1;
        return getTurnaroundTime() - burstTime;  // ¡AQUÍ ES DONDE SE UTILIZA burstTime!
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
        System.out.println("PID " + pid + " Métricas:");
        System.out.println("  Tiempo de Ráfaga: " + burstTime);
        System.out.println("  Tiempo de Llegada: " + arrivalTime);
        System.out.println("  Tiempo de Finalización: " + completionTime);
        System.out.println("  Tiempo de Retorno: " + getTurnaroundTime());
        System.out.println("  Tiempo de Espera: " + getWaitingTime());
        System.out.println("  Tiempo de Respuesta: " + getResponseTime());
    }
}