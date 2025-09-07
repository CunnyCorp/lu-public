package pictures.cunny.loli_utils.events;

public class ChunkLoadEvent {
    public static final ChunkLoadEvent INSTANCE = new ChunkLoadEvent();
    public int x, z;

    public ChunkLoadEvent get(int x, int z) {
        this.x = x;
        this.z = z;
        return this;
    }
}
