package IO;

public class IODevice {
    private String name;
    private boolean isBusy;
    private IORequest currentRequest;
    private int remainingTime;

    public IODevice(String name) {
        this.name = name;
        this.isBusy = false;
    }

    public String getName() { return name; }
    public boolean isBusy() { return isBusy; }

    public void startIO(IORequest request) {
        this.currentRequest = request;
        this.remainingTime = request.getDuration();
        this.isBusy = true;
        System.out.println("  → Device [" + name + "] started " + request.getOperation() +
                " for PID " + request.getPid() + " (duration: " + request.getDuration() + ")");
    }

    public IORequest tick() {
        if (!isBusy) return null;

        remainingTime--;

        if (remainingTime <= 0) {
            isBusy = false;
            System.out.println("  → Device [" + name + "] completed " + currentRequest.getOperation() +
                    " for PID " + currentRequest.getPid());
            IORequest completed = currentRequest;
            currentRequest = null;
            return completed;
        }
        return null;
    }
}