package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

public class CTNHEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {
    protected CTNHEntityBuilder(AbstractRegistrate owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory factory, MobCategory classification) {
        super(owner, parent, name, callback, factory, classification);
    }

    public static <T extends Entity, P> CTNHEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (CTNHEntityBuilder<T, P>) (new CTNHEntityBuilder<T, P>(owner, parent, name, callback, factory, classification)).defaultLang();
    }

    public CTNHEntityBuilder<T, P> cnlang(String name) {
        this.setData(CNLANG, (ctx, prov) ->
                prov.add(ctx.getEntry().getDescriptionId(), name)
        );
        return this;
    }
}
