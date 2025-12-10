public class Process {
    private PCB pcb;

    public Process(int pid, int arrival, int burst, int memReq) {
        this.pcb = new PCB(pid, burst, memReq);
    }

    public PCB getPCB() { return pcb; }

    public void runFor(int time) {
        pcb.reduceTime(time);
    }
}
