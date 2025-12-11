package Manager;

import java.util.*;
import IO.IORequest;
import IO.IODevice;
import Process.PCB;
import Process.Process;

public class IOManager {
    private Map<String, IODevice> devices = new HashMap<>();
    private Queue<IORequest> pendingRequests = new LinkedList<>();
    private Map<Integer, Process> waitingProcesses = new HashMap<>();

    public IOManager() {
        // Initialize some devices
        devices.put("DISK", new IODevice("DISK"));
        devices.put("PRINTER", new IODevice("PRINTER"));
        devices.put("NETWORK", new IODevice("NETWORK"));
    }

    public void submitRequest(IORequest request, Process process) {
        pendingRequests.add(request);
        waitingProcesses.put(request.getPid(), process);
        System.out.println("PID " + request.getPid() + " submitted I/O request to " + request.getDeviceName());
    }

    public void processIO() {
        // Tick all devices
        for (IODevice device : devices.values()) {
            IORequest completed = device.tick();
            if (completed != null) {
                // Move process back to ready
                Process p = waitingProcesses.remove(completed.getPid());
                if (p != null) {
                    p.getPCB().transitionTo(PCB.ProcessState.READY);
                    // Re-add to scheduler (this would need scheduler reference)
                }
            }
        }

        // Assign pending requests to free devices
        Iterator<IORequest> it = pendingRequests.iterator();
        while (it.hasNext()) {
            IORequest req = it.next();
            IODevice device = devices.get(req.getDeviceName());

            if (device != null && !device.isBusy()) {
                device.startIO(req);
                it.remove();
            }
        }
    }

    public boolean hasPendingIO() {
        for (IODevice device : devices.values()) {
            if (device.isBusy()) return true;
        }
        return !pendingRequests.isEmpty();
    }
}