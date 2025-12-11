package Dispatcher;

import IO.IORequest;
import Manager.IOManager;
import Manager.MemoryManager;
import Process.Process;
import Process.PCB;
import Scheduler.RoundRobinScheduler;
import Scheduler.Scheduler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Dispatcher {

    private Scheduler scheduler;
    private MemoryManager memory;
    private IOManager ioManager;
    private int currentTime = 0;

    private Queue<Process> waitingForMemory = new LinkedList<>();
    private List<Process> completedProcesses = new ArrayList<>();

    public Dispatcher(Scheduler scheduler, MemoryManager memory, IOManager ioManager) {
        this.scheduler = scheduler;
        this.memory = memory;
        this.ioManager = ioManager;
    }

    private void tryUnblockProcesses() {
        int size = waitingForMemory.size();

        for (int i = 0; i < size; i++) {
            Process p = waitingForMemory.poll();
            PCB pcb = p.getPCB();

            boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());

            if (ok) {
                pcb.transitionTo(PCB.ProcessState.READY);
                scheduler.requeue(p);
            } else {
                waitingForMemory.add(p);
            }
        }
    }

    private void handleIOCompletions() {
        List<Process> completed = ioManager.getCompletedIO();
        for (Process p : completed) {
            p.getPCB().transitionTo(PCB.ProcessState.READY);
            scheduler.requeue(p);
        }
    }

    public void runFCFS() {
        currentTime = 0;

        System.out.println("\n⏱️  Starting FCFS Scheduling...\n");

        while (scheduler.hasProcess() || !waitingForMemory.isEmpty() || ioManager.hasPendingIO()) {

            // Process any completed I/O operations
            ioManager.processIO();
            handleIOCompletions();

            tryUnblockProcesses();

            if (!scheduler.hasProcess()) {
                currentTime++; // CPU idle
                continue;
            }

            Process p = scheduler.getNextProcess();
            PCB pcb = p.getPCB();

            // Allocate memory if needed
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("⚠️  PID " + pcb.getPid() + " waiting for memory");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime);

            // Check if process should request I/O midway
            if (p.shouldRequestIO() && p.hasIORequest()) {
                // Run until I/O request point
                int timeUntilIO = pcb.getRemainingTime() / 2;
                p.runFor(timeUntilIO);
                currentTime += timeUntilIO;

                System.out.println("🔄 PID " + pcb.getPid() + " ran for " + timeUntilIO +
                        " (time now: " + currentTime + ")");

                // Submit I/O request
                IORequest ioReq = p.getNextIORequest();
                ioManager.submitRequest(ioReq, p);
                pcb.transitionTo(PCB.ProcessState.WAITING);

                continue; // Process will be rescheduled after I/O
            }

            // Run to completion
            int time = pcb.getRemainingTime();
            p.runFor(time);
            currentTime += time;

            System.out.println("✓ PID " + pcb.getPid() + " ran for " + time +
                    " (time now: " + currentTime + ")");

            pcb.transitionTo(PCB.ProcessState.TERMINATED);
            pcb.markCompletion(currentTime);
            memory.free(pcb);

            completedProcesses.add(p);
            tryUnblockProcesses();
        }

        printStatistics();
    }

    public void runRoundRobin(RoundRobinScheduler rr) {
        currentTime = 0;

        System.out.println("\n⏱️  Starting Round Robin Scheduling (Quantum: " + rr.getQuantum() + ")...\n");

        while (rr.hasProcess() || !waitingForMemory.isEmpty() || ioManager.hasPendingIO()) {

            // Process I/O completions
            ioManager.processIO();
            handleIOCompletions();

            tryUnblockProcesses();

            if (!rr.hasProcess()) {
                currentTime++; // CPU idle
                continue;
            }

            Process p = rr.getNextProcess();
            PCB pcb = p.getPCB();

            // Memory allocation
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("⚠️  PID " + pcb.getPid() + " waiting for memory");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime);

            int slice = Math.min(rr.getQuantum(), pcb.getRemainingTime());

            // Check if should request I/O during this slice
            if (p.shouldRequestIO() && p.hasIORequest()) {
                // Run partial slice before I/O
                int timeBeforeIO = Math.min(slice, 1); // Use 1 time unit before I/O
                p.runFor(timeBeforeIO);
                currentTime += timeBeforeIO;

                System.out.println("🔄 PID " + pcb.getPid() + " ran for " + timeBeforeIO +
                        " before I/O (time now: " + currentTime + ")");

                // Submit I/O request
                IORequest ioReq = p.getNextIORequest();
                ioManager.submitRequest(ioReq, p);
                pcb.transitionTo(PCB.ProcessState.WAITING);

                continue;
            }

            // Normal execution
            p.runFor(slice);
            currentTime += slice;

            System.out.println("✓ PID " + pcb.getPid() + " ran for slice " + slice +
                    " (time now: " + currentTime + ", remaining: " + pcb.getRemainingTime() + ")");

            // Check if finished
            if (pcb.getRemainingTime() == 0) {
                pcb.transitionTo(PCB.ProcessState.TERMINATED);
                pcb.markCompletion(currentTime);
                memory.free(pcb);
                completedProcesses.add(p);
                tryUnblockProcesses();
            } else {
                // Not finished, requeue
                pcb.transitionTo(PCB.ProcessState.READY);
                rr.requeue(p);
            }
        }

        printStatistics();
    }

    private void printStatistics() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                     SCHEDULING STATISTICS");
        System.out.println("=".repeat(70));
        System.out.println("PID\tArrival\tBurst\tCompletion\tTurnaround\tWaiting\tResponse");
        System.out.println("-".repeat(70));

        double avgTurnaround = 0;
        double avgWaiting = 0;
        double avgResponse = 0;

        for (Process p : completedProcesses) {
            PCB pcb = p.getPCB();
            System.out.printf("%d\t%d\t%d\t%d\t\t%d\t\t%d\t%d\n",
                    pcb.getPid(),
                    pcb.getArrivalTime(),
                    pcb.getBurstTime(),
                    pcb.getCompletionTime(),
                    pcb.getTurnaroundTime(),
                    pcb.getWaitingTime(),
                    pcb.getResponseTime()
            );

            avgTurnaround += pcb.getTurnaroundTime();
            avgWaiting += pcb.getWaitingTime();
            avgResponse += pcb.getResponseTime();
        }

        int n = completedProcesses.size();
        if (n > 0) {
            System.out.println("-".repeat(70));
            System.out.printf("Average:\t\t\t\t%.2f\t\t%.2f\t%.2f\n",
                    avgTurnaround / n, avgWaiting / n, avgResponse / n);
        }
        System.out.println("=".repeat(70) + "\n");
    }
}