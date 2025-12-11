package IO;

public class IORequest {
    private int pid;
    private String deviceName;
    private int duration;
    private String operation; // "LEER" o "ESCRIBIR"

    public IORequest(int pid, String deviceName, int duration, String operation) {
        this.pid = pid;
        this.deviceName = deviceName;
        this.duration = duration;
        this.operation = operation;
    }

    public int getPid() { return pid; }
    public String getDeviceName() { return deviceName; }
    public int getDuration() { return duration; }
    public String getOperation() { return operation; }
}
