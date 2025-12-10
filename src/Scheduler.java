public interface Scheduler {
    void addProcess(Process p);
    Process getNextProcess();
    boolean hasProcess();
    void requeue(Process p);
}
