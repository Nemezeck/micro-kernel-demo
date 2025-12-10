public class Main {
    public static void main(String[] args) {

        MemoryManager memory = new MemoryManager(200);

        // ---------------- FCFS ----------------
        Scheduler fcfs = new FCFSScheduler();
        fcfs.addProcess(new Process(1, 0, 10, 30));
        fcfs.addProcess(new Process(2, 0, 4, 20));
        fcfs.addProcess(new Process(3, 0, 6, 40));

        Dispatcher d1 = new Dispatcher(fcfs, memory);
        System.out.println("\n=== Running FCFS ===");
        d1.runFCFS();


        // ---------------- Round Robin ----------------
        MemoryManager memory2 = new MemoryManager(200);

        RoundRobinScheduler rr = new RoundRobinScheduler(3);
        rr.addProcess(new Process(1, 0, 10, 30));
        rr.addProcess(new Process(2, 0, 4, 20));
        rr.addProcess(new Process(3, 0, 6, 40));

        Dispatcher d2 = new Dispatcher(rr, memory2);
        System.out.println("\n=== Running Round Robin ===");
        d2.runRoundRobin(rr);
    }
}
