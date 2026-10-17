package tech.vixhentx.mcmod.ctnhlib.client.ponder;

import net.createmod.ponder.api.element.TextElementBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import tech.vixhentx.mcmod.ctnhlib.client.ponder.ui.MachineUI;
import tech.vixhentx.mcmod.ctnhlib.client.ponder.ui.MachineUiStart;
import tech.vixhentx.mcmod.ctnhlib.client.ponder.ui.ToolGridPlacement;
import tech.vixhentx.mcmod.ctnhlib.langprovider.Lang;

public class CTNHPonderSceneBuilder extends CreateSceneBuilder {

    private final String modId;
    private final LangRegistrar langRegistrar;
    private String sceneId;
    private int textIndex;

    public CTNHPonderSceneBuilder(SceneBuilder builder) {
        this(builder, null, LangRegistrar.NOOP);
    }

    public CTNHPonderSceneBuilder(SceneBuilder builder, String modId, LangRegistrar langRegistrar) {
        super(builder);
        this.modId = modId;
        this.langRegistrar = langRegistrar == null ? LangRegistrar.NOOP : langRegistrar;
    }

    public void init5x5(SceneBuildingUtil util) {
        this.configureBasePlate(0, 0, 5);
        this.scaleSceneView(0.9f);
        this.world().showSection(util.select().layer(0), Direction.UP);
    }

    public void init7x7(SceneBuildingUtil util) {
        this.configureBasePlate(0, 0, 7);
        this.scaleSceneView(0.75f);
        this.world().showSection(util.select().layer(0), Direction.UP);
    }

    public void init9x9(SceneBuildingUtil util) {
        this.configureBasePlate(0, 0, 9);
        this.scaleSceneView(0.6f);
        this.world().showSection(util.select().layer(0), Direction.UP);
    }

    public void initAll(SceneBuildingUtil util) {
        this.configureBasePlate(0, 0, 32);
        this.scaleSceneView(0.3f);
        this.world().showSection(util.select().layer(0), Direction.UP);
    }

    public void rotateAround(int duration) {
        int time = duration / 4;
        this.rotateCameraY(90);
        this.idle(time);
        this.rotateCameraY(90);
        this.idle(time);
        this.rotateCameraY(90);
        this.idle(time);
        this.rotateCameraY(90);
        this.idle(time);
    }

    /**
     * 摆放一个可复用的机器界面，例如
     * {@code scene.showUI(MY_MACHINE_UI).at(anchor).machinePos(pos).slot(0).withItem(stack).show(200)}。
     *
     * <p>
     * 返回起点态：接着必须用 {@code at(...)} 给出坐标，{@code at(Vec3)} 之后还须补 {@code machinePos(pos)}。
     */
    public MachineUiStart showUI(MachineUI ui) {
        return ui.in(this);
    }

    /**
     * 在方块的某一面上画出 GT 的九宫格工具覆盖层：手持工具对准方块时会浮出来的那块 3x3 格子，
     * 格子里是「点这里会做什么」的图标（扳手转向、软锤启停、硬锤静音……）。
     *
     * <pre>{@code
     * scene.showToolGrid(controllerPos, Direction.NORTH).outline(4).show(120);
     * }</pre>
     *
     * <p>
     * 格子按行优先编号（从面外侧看）：0 左上、1 上、2 右上、3 左、4 中心、5 右、6 左下、7 下、8 右下。
     * 图标由方块自己给，所以机器上的盖子、自动输出口都会照实反映出来；给不出图标的格子留空。
     */
    public ToolGridPlacement showToolGrid(BlockPos pos, Direction face) {
        return new ToolGridPlacement(this, pos, face);
    }

    public CreateSceneBuilder getSceneBuilder() {
        return this;
    }

    public TextElementBuilder showText(int duration, String en, String cn) {
        String key = nextTextKey();
        registerLang(key, en, cn);
        return overlay().showText(duration).text("");
    }

    public TextElementBuilder showText(int duration, Lang lang) {
        return overlay().showText(duration).text(lang.translate().getContents().toString());
    }

    public void title(String sceneId) {
        this.sceneId = sceneId;
        this.textIndex = 0;
        title(sceneId, "");
    }

    public void title(String sceneId, String title) {
        this.sceneId = sceneId;
        this.textIndex = 0;
        super.title(sceneId, title);
    }

    public void title(String sceneId, Lang header) {
        title(sceneId, header.translate().getContents().toString());
    }

    public void title(String sceneId, String en, String cn) {
        title(sceneId, en);
        registerLang(sceneLangKey("title"), en, cn);
        registerLang(sceneLangKey("header"), en, cn);
    }

    public void title(String sceneId, String headerEn, String headerCn, String titleEn, String titleCn) {
        title(sceneId, headerEn);
        registerLang(sceneLangKey("title"), titleEn, titleCn);
        registerLang(sceneLangKey("header"), headerEn, headerCn);
    }

    public void title(String sceneId, Component component) {
        String text = component.getString();
        title(sceneId, text, text);
    }

    private String nextTextKey() {
        return sceneLangKey("text_" + ++textIndex);
    }

    private String sceneLangKey(String entry) {
        if (modId == null || modId.isBlank()) {
            throw new IllegalStateException("Ponder scene lang keys require a mod id");
        }
        return modId + ".ponder." + sceneId + "." + entry;
    }

    private void registerLang(String key, String en, String cn) {
        langRegistrar.register(key, en, cn);
    }

    @FunctionalInterface
    public interface LangRegistrar {

        LangRegistrar NOOP = (key, en, cn) -> {};

        void register(String key, String en, String cn);
    }
}
