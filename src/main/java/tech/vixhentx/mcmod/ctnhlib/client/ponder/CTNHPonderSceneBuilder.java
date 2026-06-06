package tech.vixhentx.mcmod.ctnhlib.client.ponder;

import net.createmod.ponder.api.element.TextElementBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
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
