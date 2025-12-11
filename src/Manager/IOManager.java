package Manager;

import java.util.*;
import IO.IORequest;
import IO.IODevice;
import Process.PCB;
import Process.Process;

public class IOManager {
    private Map<String, IODevice> devices = new HashMap<>();
    private Queue<IORequestWrapper> pendingRequests = new LinkedList<>();
    private Map<Integer, Process> waitingProcesses = new HashMap<>();
    private List<Process> completedIO = new ArrayList<>();

    public IOManager() {
        // Initialize devices
        devices.put("DISK", new IODevice("DISK"));
        devices.put("PRINTER", new IODevice("PRINTER"));
        devices.put("NETWORK", new IODevice("NETWORK"));
    }

    // Wrapper to keep track of both request and process
    private static class IORequestWrapper {
        IORequest request;
        Process process;

        IORequestWrapper(IORequest req, Process proc) {
            this.request = req;
            this.process = proc;
        }
    }

    public void submitRequest(IORequest request, Process process) {
        pendingRequests.add(new IORequestWrapper(request, process));
        waitingProcesses.put(request.getPid(), process);
        System.out.println("✓ PID " + request.getPid() + " submitted I/O request: " +
                request.getOperation() + " on " + request.getDeviceName());
    }

    public void processIO() {
        // Tick all devices (advance I/O operations)
        for (IODevice device : devices.values()) {
            IORequest completed = device.tick();
            if (completed != null) {
                // I/O completed, move process back to ready
                Process p = waitingProcesses.remove(completed.getPid());
                if (p != null) {
                    completedIO.add(p);
                    System.out.println("✓ I/O completed for PID " + completed.getPid());
                }
            }
        }

        // Assign pending requests to free devices
        Iterator<IORequestWrapper> it = pendingRequests.iterator();
        while (it.hasNext()) {
            IORequestWrapper wrapper = it.next();
            IODevice device = devices.get(wrapper.request.getDeviceName());

            if (device != null && !device.isBusy()) {
                device.startIO(wrapper.request);
                it.remove();
            }
        }
    }

    public List<Process> getCompletedIO() {
        List<Process> result = new ArrayList<>(completedIO);
        completedIO.clear();
        return result;
    }

    public boolean hasPendingIO() {
        for (IODevice device : devices.values()) {
            if (device.isBusy()) return true;
        }
        return !pendingRequests.isEmpty() || !waitingProcesses.isEmpty();
    }

    public boolean isProcessWaitingForIO(int pid) {
        return waitingProcesses.containsKey(pid);
    }
}