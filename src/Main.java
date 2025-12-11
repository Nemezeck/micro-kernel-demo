import Dispatcher.Dispatcher;
import Manager.IOManager;
import Manager.MemoryManager;
import Scheduler.FCFSScheduler;
import Scheduler.RoundRobinScheduler;
import Scheduler.Scheduler;
import Process.Process;

public class Main {
    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   MICROKERNEL SIMULATION");
        System.out.println("========================================\n");

        // ---------------- FCFS ----------------
        MemoryManager memory = new MemoryManager(200);
        IOManager ioManager = new IOManager();

        Scheduler fcfs = new FCFSScheduler();
        fcfs.addProcess(new Process(1, 0, 10, 30));   // pid, arrival, burst, memory
        fcfs.addProcess(new Process(2, 2, 4, 20));
        fcfs.addProcess(new Process(3, 4, 6, 40));

        Dispatcher d1 = new Dispatcher(fcfs, memory, ioManager);
        System.out.println("\n=== Running FCFS ===");
        d1.runFCFS();

        // ---------------- Round Robin ----------------
        MemoryManager memory2 = new MemoryManager(200);
        IOManager ioManager2 = new IOManager();

        RoundRobinScheduler rr = new RoundRobinScheduler(3);
        rr.addProcess(new Process(1, 0, 10, 30));
        rr.addProcess(new Process(2, 2, 4, 20));
        rr.addProcess(new Process(3, 4, 6, 40));

        Dispatcher d2 = new Dispatcher(rr, memory2, ioManager2);
        System.out.println("\n=== Running Round Robin ===");
        d2.runRoundRobin(rr);
    }
}