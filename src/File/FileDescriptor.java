package File;

public class FileDescriptor {
    private String fileName;
    private int size;
    private int startBlock;
    private boolean isOpen;

    public FileDescriptor(String fileName, int size, int startBlock) {
        this.fileName = fileName;
        this.size = size;
        this.startBlock = startBlock;
        this.isOpen = false;
    }

    public String getFileName() { return fileName; }
    public int getSize() { return size; }
    public int getStartBlock() { return startBlock; }
    public boolean isOpen() { return isOpen; }
    public void open() { this.isOpen = true; }
    public void close() { this.isOpen = false; }
}