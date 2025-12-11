package Process;

import IO.IORequest;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private PCB pcb;
    private List<IORequest> ioRequests = new ArrayList<>();
    private int ioRequestIndex = 0;

    public Process(int pid, int arrival, int burst, int memReq) {
        this.pcb = new PCB(pid, arrival, burst, memReq);
    }

    public PCB getPCB() { return pcb; }

    public void runFor(int time) {
        pcb.reduceTime(time);
    }

    public void addIORequest(IORequest req) {
        ioRequests.add(req);
    }

    public boolean hasIORequest() {
        return ioRequestIndex < ioRequests.size();
    }

    public IORequest getNextIORequest() {
        if (!hasIORequest()) return null;
        return ioRequests.get(ioRequestIndex++);
    }

    public boolean shouldRequestIO() {
        // Solicitar E/S después de que se haya consumido la mitad del tiempo de ráfaga
        if (!hasIORequest()) return false;

        int consumed = pcb.getBurstTime() - pcb.getRemainingTime();
        int halfBurst = pcb.getBurstTime() / 2;

        return consumed >= halfBurst && ioRequestIndex == 0;
    }
}