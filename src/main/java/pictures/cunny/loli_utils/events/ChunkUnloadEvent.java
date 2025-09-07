package pictures.cunny.loli_utils.events;

public class ChunkUnloadEvent {
    public static final ChunkUnloadEvent INSTANCE = new ChunkUnloadEvent();
    public int x, z;

    public ChunkUnloadEvent get(int x, int z) {
        this.x = x;
        this.z = z;
        return this;
    }
}
