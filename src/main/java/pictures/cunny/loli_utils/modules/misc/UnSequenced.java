package pictures.cunny.loli_utils.modules.misc;

import meteordevelopment.meteorclient.systems.modules.Module;
import pictures.cunny.loli_utils.LoliUtilsMeteor;

public class UnSequenced extends Module {
    public UnSequenced() {
        super(LoliUtilsMeteor.CATEGORY, "un-sequenced", "Sends packets without sequences.");
        this.runInMainMenu = true;
    }
}
