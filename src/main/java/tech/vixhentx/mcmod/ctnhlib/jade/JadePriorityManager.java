package tech.vixhentx.mcmod.ctnhlib.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JadePriorityManager {

    private static final List<JadeBlockDataRegistration> BLOCK_DATA = new ArrayList<>();
    private static final List<JadeBlockComponentRegistration> BLOCK_COMPONENT = new ArrayList<>();

    private JadePriorityManager() {}

    // 注册方法（供外部调用）
    public static void registerBlockData(IServerDataProvider<BlockAccessor> provider,
                                         Class<? extends BlockEntity> target, int priority,
                                         String id) {
        synchronized (BLOCK_DATA) {
            BLOCK_DATA.add(new JadeBlockDataRegistration(provider, target, priority, id));
        }
    }

    public static void registerBlockComponent(IBlockComponentProvider provider, Class<? extends Block> target,
                                              int priority,
                                              String id) {
        synchronized (BLOCK_COMPONENT) {
            BLOCK_COMPONENT.add(new JadeBlockComponentRegistration(provider, target, priority, id));
        }
    }

    // 返回排序后的不可变视图（按 priority 升序，数值越小越先执行）
    public static List<JadeBlockDataRegistration> orderedBlockData() {
        synchronized (BLOCK_DATA) {
            List<JadeBlockDataRegistration> copy = new ArrayList<>(BLOCK_DATA);
            Collections.sort(copy);
            return Collections.unmodifiableList(copy);
        }
    }

    public static List<JadeBlockComponentRegistration> orderedBlockComponent() {
        synchronized (BLOCK_COMPONENT) {
            List<JadeBlockComponentRegistration> copy = new ArrayList<>(BLOCK_COMPONENT);
            Collections.sort(copy);
            return Collections.unmodifiableList(copy);
        }
    }

    public static boolean unregisterBlockData(String id) {
        synchronized (BLOCK_DATA) {
            return BLOCK_DATA.removeIf(r -> r.id().equals(id));
        }
    }

    /** 卸载指定 id 的 BlockComponent 注册 */
    public static boolean unregisterBlockComponent(String id) {
        synchronized (BLOCK_COMPONENT) {
            return BLOCK_COMPONENT.removeIf(r -> r.id().equals(id));
        }
    }

    public record JadeBlockDataRegistration(IServerDataProvider<BlockAccessor> provider,
                                            Class<? extends BlockEntity> target, int priority,
                                            String id) implements Comparable<JadeBlockDataRegistration> {

        @Override
        public int compareTo(JadeBlockDataRegistration o) {
            return Integer.compare(this.priority, o.priority);
        }

        public void registerCommon(IWailaCommonRegistration registration) {
            registration.registerBlockDataProvider(provider, target);
        }
    }

    public record JadeBlockComponentRegistration(IBlockComponentProvider provider, Class<? extends Block> target,
                                          int priority,
                                          String id) implements Comparable<JadeBlockComponentRegistration> {

        @Override
        public int compareTo(JadeBlockComponentRegistration o) {
            return Integer.compare(this.priority, o.priority);
        }

        public void registerClient(IWailaClientRegistration registration) {
            registration.registerBlockComponent(provider, target);
        }
    }


}
