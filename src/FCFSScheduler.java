import java.util.LinkedList;
import java.util.Queue;

public class FCFSScheduler implements Scheduler {

    private Queue<Process> queue = new LinkedList<>();

    @Override
    public void addProcess(Process p) {
        p.getPCB().transitionTo(PCB.ProcessState.READY);
        queue.add(p);
    }

    @Override
    public Process getNextProcess() {
        return queue.poll();
    }

    @Override
    public boolean hasProcess() {
        return !queue.isEmpty();
    }

    @Override
    public void requeue(Process p) {
        queue.add(p);
    }
}
