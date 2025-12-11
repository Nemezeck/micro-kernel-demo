package Scheduler;
import Process.Process;

public interface Scheduler {
    void addProcess(Process p);
    Process getNextProcess();
    boolean hasProcess();
    void requeue(Process p);
}
