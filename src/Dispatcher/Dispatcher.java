package Dispatcher;

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

    public void runFCFS() {
        currentTime = 0;

        while (scheduler.hasProcess() || !waitingForMemory.isEmpty()) {

            tryUnblockProcesses();

            if (!scheduler.hasProcess()) {
                currentTime++; // Idle CPU
                continue;
            }

            Process p = scheduler.getNextProcess();
            PCB pcb = p.getPCB();

            // Allocate memory if needed
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("PID " + pcb.getPid()
                            + " waiting for memory. Moving to WAITING state.");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime); // Track first run time

            int time = pcb.getRemainingTime();
            p.runFor(time);
            currentTime += time; // Advance clock

            System.out.println("PID " + pcb.getPid() + " ran for " + time +
                    " (time now: " + currentTime + ")");

            pcb.transitionTo(PCB.ProcessState.TERMINATED);
            pcb.markCompletion(currentTime); // Track completion time
            memory.free(pcb);

            completedProcesses.add(p);
            tryUnblockProcesses();
        }

        printStatistics();
    }

    public void runRoundRobin(RoundRobinScheduler rr) {
        currentTime = 0;

        while (rr.hasProcess() || !waitingForMemory.isEmpty()) {

            tryUnblockProcesses();

            if (!rr.hasProcess()) {
                currentTime++; // Idle CPU
                continue;
            }

            Process p = rr.getNextProcess();
            PCB pcb = p.getPCB();

            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("PID " + pcb.getPid()
                            + " waiting for memory. Moving to WAITING.");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime); // Track first run

            int slice = Math.min(rr.getQuantum(), pcb.getRemainingTime());
            p.runFor(slice);
            currentTime += slice; // Advance clock

            System.out.println("PID " + pcb.getPid() + " ran for slice " + slice +
                    " (time now: " + currentTime + ")");

            if (pcb.getRemainingTime() == 0) {
                pcb.transitionTo(PCB.ProcessState.TERMINATED);
                pcb.markCompletion(currentTime); // Track completion
                memory.free(pcb);
                completedProcesses.add(p);
                tryUnblockProcesses();
            } else {
                pcb.transitionTo(PCB.ProcessState.READY);
                rr.requeue(p);
            }
        }

        printStatistics();
    }

    private void printStatistics() {
        System.out.println("\n========== SCHEDULING STATISTICS ==========");
        System.out.println("PID\tArrival\tBurst\tCompletion\tTurnaround\tWaiting\tResponse");
        System.out.println("---\t-------\t-----\t----------\t----------\t-------\t--------");

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
            System.out.println("-------------------------------------------");
            System.out.printf("Average:\t\t\t\t%.2f\t\t%.2f\t%.2f\n",
                    avgTurnaround / n, avgWaiting / n, avgResponse / n);
        }
        System.out.println("===========================================\n");
    }
}