package tech.vixhentx.mcmod.ctnhlib.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import org.joml.Quaternionf;

public class ExtendNbtUtils {

    public static CompoundTag writeVec3(Vec3 vec3) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putDouble("X", vec3.x);
        compoundtag.putDouble("Y", vec3.y);
        compoundtag.putDouble("Z", vec3.z);
        return compoundtag;
    }

    public static Vec3 readVec3(CompoundTag nbt) {
        return new Vec3(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
    }

    public static CompoundTag writeQuaternionf(Quaternionf quaternionf) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putFloat("RotationX", quaternionf.x());
        compoundtag.putFloat("RotationY", quaternionf.y());
        compoundtag.putFloat("RotationZ", quaternionf.z());
        compoundtag.putFloat("RotationW", quaternionf.w());
        return compoundtag;
    }

    public static Quaternionf readQuaternionf(CompoundTag nbt) {
        return new Quaternionf(nbt.getFloat("RotationX"), nbt.getFloat("RotationY"), nbt.getFloat("RotationZ"),
                nbt.getFloat("RotationW"));
    }
}
