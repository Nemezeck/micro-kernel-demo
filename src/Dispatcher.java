import java.util.LinkedList;
import java.util.Queue;

public class Dispatcher {

    private Scheduler scheduler;
    private MemoryManager memory;

    // Processes waiting for memory
    private Queue<Process> waitingForMemory = new LinkedList<>();

    public Dispatcher(Scheduler scheduler, MemoryManager memory) {
        this.scheduler = scheduler;
        this.memory = memory;
    }

    private void tryUnblockProcesses() {
        int size = waitingForMemory.size();

        for (int i = 0; i < size; i++) {
            Process p = waitingForMemory.poll();
            PCB pcb = p.getPCB();

            boolean ok = memory.allocate(pcb.getPid(), pcb.getMemoryRequired());

            if (ok) {
                pcb.transitionTo(PCB.ProcessState.READY);
                scheduler.requeue(p);
            } else {
                waitingForMemory.add(p);
            }
        }
    }

    public void runFCFS() {
        while (scheduler.hasProcess() || !waitingForMemory.isEmpty()) {

            tryUnblockProcesses();

            if (!scheduler.hasProcess()) continue;

            Process p = scheduler.getNextProcess();
            PCB pcb = p.getPCB();

            // Allocate memory if needed
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb.getPid(), pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("PID " + pcb.getPid()
                            + " waiting for memory. Moving to WAITING state.");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);

            int time = pcb.getRemainingTime();
            p.runFor(time);
            System.out.println("PID " + pcb.getPid() + " ran for " + time);

            pcb.transitionTo(PCB.ProcessState.TERMINATED);
            memory.free(pcb.getPid());

            tryUnblockProcesses();
        }
    }
    public void runRoundRobin(RoundRobinScheduler rr) {

        while (rr.hasProcess() || !waitingForMemory.isEmpty()) {

            // Try to give memory to blocked processes
            tryUnblockProcesses();

            if (!rr.hasProcess())
                continue;

            Process p = rr.getNextProcess();
            PCB pcb = p.getPCB();

            // Memory allocation
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb.getPid(), pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("PID " + pcb.getPid()
                            + " waiting for memory. Moving to WAITING.");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            // Run the process for a time slice
            pcb.transitionTo(PCB.ProcessState.RUNNING);

            int slice = Math.min(rr.getQuantum(), pcb.getRemainingTime());
            p.runFor(slice);

            System.out.println("PID " + pcb.getPid() + " ran for slice " + slice);

            // Finished?
            if (pcb.getRemainingTime() == 0) {
                pcb.transitionTo(PCB.ProcessState.TERMINATED);
                memory.free(pcb.getPid());
                tryUnblockProcesses();
            }
            else {
                // Not finished → requeue
                pcb.transitionTo(PCB.ProcessState.READY);
                rr.requeue(p);
            }
        }
    }

}
