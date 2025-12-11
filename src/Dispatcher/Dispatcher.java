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

        System.out.println("\n⏱️  Iniciando la programación de FCFS...\n");

        while (scheduler.hasProcess() || !waitingForMemory.isEmpty() || ioManager.hasPendingIO()) {

            // Procesar cualquier operación de E/S completada
            ioManager.processIO();
            handleIOCompletions();

            tryUnblockProcesses();

            if (!scheduler.hasProcess()) {
                currentTime++; // CPU inactiva
                continue;
            }

            Process p = scheduler.getNextProcess();
            PCB pcb = p.getPCB();

            // Asignar memoria si es necesario
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("⚠️  PID " + pcb.getPid() + " ESPERANDO POR MEMORIA");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime);

            // Comprobar si el proceso debe solicitar E/S a mitad de camino
            if (p.shouldRequestIO() && p.hasIORequest()) {
                // Ejecutar hasta el punto de solicitud de E/S
                int timeUntilIO = pcb.getRemainingTime() / 2;
                p.runFor(timeUntilIO);
                currentTime += timeUntilIO;

                System.out.println("🔄 PID " + pcb.getPid() + " CORRIO POR " + timeUntilIO +
                        " (TIEMPO AHORA: " + currentTime + ")");

                // Enviar solicitud de E/S
                IORequest ioReq = p.getNextIORequest();
                ioManager.submitRequest(ioReq, p);
                pcb.transitionTo(PCB.ProcessState.WAITING);

                continue; // El proceso se reprogramará después de la E/S
            }

            // Correr hasta el final
            int time = pcb.getRemainingTime();
            p.runFor(time);
            currentTime += time;

            System.out.println("✓ PID " + pcb.getPid() + " CORRIO POR " + time +
                    " (TIEMPO AHORA: " + currentTime + ")");

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

        System.out.println("\n⏱️  INICIANDO PROGRAMACION DE Round Robin (Quantum: " + rr.getQuantum() + ")...\n");

        while (rr.hasProcess() || !waitingForMemory.isEmpty() || ioManager.hasPendingIO()) {

            // Finalizaciones de E/S de proceso
            ioManager.processIO();
            handleIOCompletions();

            tryUnblockProcesses();

            if (!rr.hasProcess()) {
                currentTime++; // CPU inactiva
                continue;
            }

            Process p = rr.getNextProcess();
            PCB pcb = p.getPCB();

            // Memory allocation
            if (!pcb.hasMemory()) {
                boolean ok = memory.allocate(pcb, pcb.getMemoryRequired());
                if (!ok) {
                    System.out.println("⚠️  PID " + pcb.getPid() + " ESPERANDO POR MEMORIA ");
                    pcb.transitionTo(PCB.ProcessState.WAITING);
                    waitingForMemory.add(p);
                    continue;
                }
            }

            pcb.transitionTo(PCB.ProcessState.RUNNING);
            pcb.markFirstRun(currentTime);

            int slice = Math.min(rr.getQuantum(), pcb.getRemainingTime());

            // Comprueba si se debe solicitar E/S durante este segmento
            if (p.shouldRequestIO() && p.hasIORequest()) {
                // Ejecutar una porción parcial antes de la E/S
                int timeBeforeIO = Math.min(slice, 1); // Utilice 1 unidad de tiempo antes de E/S
                p.runFor(timeBeforeIO);
                currentTime += timeBeforeIO;

                System.out.println("🔄 PID " + pcb.getPid() + " CORRIO POR " + timeBeforeIO +
                        " ANTES DE E/S (TIEMPO AHORA: " + currentTime + ")");

                // Enviar solicitud de E/S
                IORequest ioReq = p.getNextIORequest();
                ioManager.submitRequest(ioReq, p);
                pcb.transitionTo(PCB.ProcessState.WAITING);

                continue;
            }

            // Ejecución normal
            p.runFor(slice);
            currentTime += slice;

            System.out.println("✓ PID " + pcb.getPid() + " SE EJECUTO PARA UN SEGEMENTO " + slice +
                    " (TIEMPO AHORA: " + currentTime + ", RESTANTE: " + pcb.getRemainingTime() + ")");

            // Comprobar si ha terminado
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
        System.out.println("                     ESTADISTICAS DE PLANIFICACION");
        System.out.println("=".repeat(70));
        System.out.println("PID\tLlegada\tRáfaga\tFinalización\tTiempo de Retorno\tEspera\tRespuesta");
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
            System.out.printf("Promedio:\t\t\t\t%.2f\t\t%.2f\t%.2f\n", 
                    avgTurnaround / n, avgWaiting / n, avgResponse / n);
        }
        System.out.println("=".repeat(70) + "\n");
    }
}