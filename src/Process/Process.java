package Process;

import IO.IORequest;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private PCB pcb;
    private List<IORequest> ioRequests = new ArrayList<>();

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
        return !ioRequests.isEmpty();
    }

    public IORequest getNextIORequest() {
        if (ioRequests.isEmpty()) return null;
        return ioRequests.remove(0);
    }
}