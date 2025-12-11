import Dispatcher.Dispatcher;
import Manager.IOManager;
import Manager.MemoryManager;
import Scheduler.FCFSScheduler;
import Scheduler.RoundRobinScheduler;
import Scheduler.Scheduler;
import Process.Process;
import IO.IORequest;

public class Main {
    public static void main(String[] args) {

        System.out.println("=" .repeat(50));
        System.out.println("        MICROKERNEL SIMULATION");
        System.out.println("=" .repeat(50));

        // ---------------- FCFS with I/O ----------------
        System.out.println("\n📋 TEST 1: FCFS Scheduling with I/O");
        System.out.println("-".repeat(50));

        MemoryManager memory = new MemoryManager(200);
        IOManager ioManager = new IOManager();

        Scheduler fcfs = new FCFSScheduler();

        // Process 1: Needs disk I/O
        Process p1 = new Process(1, 0, 10, 30);
        p1.addIORequest(new IORequest(1, "DISK", 3, "READ"));
        fcfs.addProcess(p1);

        // Process 2: Needs printer I/O
        Process p2 = new Process(2, 1, 8, 20);
        p2.addIORequest(new IORequest(2, "PRINTER", 2, "WRITE"));
        fcfs.addProcess(p2);

        // Process 3: No I/O
        Process p3 = new Process(3, 2, 6, 40);
        fcfs.addProcess(p3);

        Dispatcher d1 = new Dispatcher(fcfs, memory, ioManager);
        d1.runFCFS();
        memory.printMemoryMap();

        // ---------------- Round Robin with I/O ----------------
        System.out.println("\n📋 TEST 2: Round Robin Scheduling with I/O");
        System.out.println("-".repeat(50));

        MemoryManager memory2 = new MemoryManager(200);
        IOManager ioManager2 = new IOManager();

        RoundRobinScheduler rr = new RoundRobinScheduler(3);

        // Process 1: Network I/O
        Process p4 = new Process(1, 0, 10, 30);
        p4.addIORequest(new IORequest(1, "NETWORK", 4, "READ"));
        rr.addProcess(p4);

        // Process 2: Disk I/O
        Process p5 = new Process(2, 0, 8, 20);
        p5.addIORequest(new IORequest(2, "DISK", 2, "WRITE"));
        rr.addProcess(p5);

        // Process 3: No I/O
        Process p6 = new Process(3, 0, 6, 40);
        rr.addProcess(p6);

        Dispatcher d2 = new Dispatcher(rr, memory2, ioManager2);
        d2.runRoundRobin(rr);
        memory2.printMemoryMap();

        // ---------------- Stress Test: Multiple I/O ----------------
        System.out.println("\n📋 TEST 3: Heavy I/O Workload");
        System.out.println("-".repeat(50));

        MemoryManager memory3 = new MemoryManager(300);
        IOManager ioManager3 = new IOManager();

        RoundRobinScheduler rr2 = new RoundRobinScheduler(2);

        for (int i = 1; i <= 5; i++) {
            Process p = new Process(i, 0, 8, 20);
            // Every process needs I/O
            String[] devices = {"DISK", "PRINTER", "NETWORK"};
            String device = devices[i % 3];
            p.addIORequest(new IORequest(i, device, 2 + (i % 3), "READ"));
            rr2.addProcess(p);
        }

        Dispatcher d3 = new Dispatcher(rr2, memory3, ioManager3);
        d3.runRoundRobin(rr2);
        memory3.printMemoryMap();
    }
}