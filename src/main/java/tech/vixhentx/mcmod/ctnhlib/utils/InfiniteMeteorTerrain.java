package tech.vixhentx.mcmod.ctnhlib.utils;
import java.util.HashMap;
import java.util.Map;

public class InfiniteMeteorTerrain {

    // 材质 ID（严格对应你的需求）
    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRAVEL = 2;
    public static final int METEOR = 3;
    public static final int GLASS = 4;

    // 网格大小（可调，影响撞击密度）
    private static final double CELL_SIZE = 64.0;
    // 每个网格单元内发生撞击的概率（0~1，密度）
    private static final double DENSITY = 0.30;

    private final long seed;
    // 缓存：key = cellX + cellZ 编码，value = 该网格的撞击事件（null 表示无撞击）
    private final Map<Long, ImpactEvent> cache = new HashMap<>();

    public InfiniteMeteorTerrain(long seed) {
        this.seed = seed;
    }

    // ================= 核心接口 =================
    /**
     * 输入种子、位置、原始材质剖面、材质偏移参数，输出高度偏移和撞击后材质剖面。
     *
     * @param seed       世界种子
     * @param x, z       水平坐标（z 对应原代码的 y）
     * @param type       输入：原材质剖面（索引 0 为地表 -> 向下），输出：撞击后材质剖面（索引 0 为新地表）
     * @param typeOffset 0.0~1.0，控制玻璃/砂砾生成的物理倾向（高值=高硅沙层，低值=硬岩）
     * @param offset     输出：高度偏移（正=升高，负=降低）
     */
    public void calculate(long seed, double x, double z, int[] type, double typeOffset, double[] offset) {
        // 找到影响该位置的所有撞击（最多 3x3 网格）
        ImpactEvent impact = findNearestImpact(x, z);
        if (impact == null || distanceToImpact(x, z, impact) > impact.radius * 3.0) {
            // 无撞击影响
            offset[0] = 0.0;
            // 但仍需要根据 shape 规则调整 type？此处保留原始材质
            return;
        }

        // 1. 计算垂直偏移（撞击碗 + 溅射毯）+ 角向噪声使坑缘不规则
        double dx = x - impact.cx;
        double dz = z - impact.cz;
        double r = Math.hypot(dx, dz);
        double angle = Math.atan2(dz, dx);

        // 角向噪声：哈希量化噪声使坑缘形状不规则（真实陨石坑非完美圆形）
        double rimNoise = 1.0 + 0.25 * hashAngularNoise(angle, seed);
        double effectiveR = r / Math.max(rimNoise, 0.5);
        offset[0] = craterOffset(effectiveR, impact.radius, impact.depth);

        // 2. 变换材质剖面
        transformColumn(seed, x, z, impact, typeOffset, offset[0], type);
    }

    // ================= 撞击事件生成（确定性密度） =================
    private ImpactEvent getImpact(int cellX, int cellZ) {
        long key = ((long) cellX << 32) | (cellZ & 0xFFFFFFFFL);
        if (cache.containsKey(key)) return cache.get(key);

        ImpactEvent event = generateImpact(cellX, cellZ);
        cache.put(key, event);
        return event;
    }

    private ImpactEvent generateImpact(int cellX, int cellZ) {
        long h = hash(cellX, cellZ, seed);
        double prob = (h & 0xFFFFFFFFL) / 4294967296.0;
        if (prob > DENSITY) return null;

        // 单元内随机偏移
        double ox = rand01(h, 0x9E3779B9L) * CELL_SIZE;
        double oz = rand01(h, 0x85EBCA6BL) * CELL_SIZE;

        double cx = cellX * CELL_SIZE + ox;
        double cz = cellZ * CELL_SIZE + oz;

        double radius = 5.0 + rand01(h, 0xC2B2AE35L) * 15.0;   // 5~20
        double depth  = radius * (0.15 + rand01(h, 0x27D4EB2FL) * 0.25); // 深度约为半径的 15%~40%

        return new ImpactEvent(cx, cz, radius, depth);
    }

    private ImpactEvent findNearestImpact(double x, double z) {
        int centerX = (int) Math.floor(x / CELL_SIZE);
        int centerZ = (int) Math.floor(z / CELL_SIZE);

        ImpactEvent best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ImpactEvent e = getImpact(centerX + dx, centerZ + dz);
                if (e == null) continue;
                double d = Math.hypot(x - e.cx, z - e.cz);
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }
        return best;
    }

    private static double distanceToImpact(double x, double z, ImpactEvent e) {
        return Math.hypot(x - e.cx, z - e.cz);
    }

    // ================= 陨石坑形状 =================
    private static double craterOffset(double r, double R, double D) {
        if (r <= R) {
            // 坑内：碗状凹陷，中心最深（-D）、坑缘归零，坑壁外侧较陡
            double t = r / R;
            return -D * Math.pow(1.0 - t, 1.5);
        } else if (r <= R * 2.5) {
            // 溅射毯：凸起
            double t = 1.0 - (r - R) / (R * 1.5);
            return D * 0.4 * t * t * t;
        } else {
            return 0.0;
        }
    }

    // ================= 材质剖面变换 =================
    private static void transformColumn(long seed, double x, double z, ImpactEvent impact,
                                        double typeOffset, double offset,
                                        int[] type) {
        int len = type.length;
        if (len == 0) return;

        // 1. 计算地表移动量（四舍五入为方块数）
        int shift = (int) Math.floor(offset + 0.5);

        // 2. 构建新材质数组（从新地表开始）
        int[] newType = new int[len];
        java.util.Arrays.fill(newType, AIR);

        // 3. 复制原材质到新数组（移动 shift 格）
        if (shift >= 0) {
            // 地表上升：原地表向下移动，新地表上方需要填充新材质
            for (int i = 0; i + shift < len; i++) {
                newType[i + shift] = type[i];
            }
        } else {
            // 地表下降：原地表被挖掉，新地表从原地下方开始
            int remove = -shift;
            if (remove >= len) return; // 全部被挖空
            for (int i = remove; i < len; i++) {
                newType[i - remove] = type[i];
            }
        }

        // 4. 根据撞击影响修改材质
        double dist = Math.hypot(x - impact.cx, z - impact.cz);
        double R = impact.radius;
        double depth = impact.depth;
        double dxm = x - impact.cx;
        double dzm = z - impact.cz;
        double angle = Math.atan2(dzm, dxm);

        // 与 calculate() 一致的哈希角向噪声，使材料边界也跟随不规则坑缘
        double rimNoise = 1.0 + 0.25 * hashAngularNoise(angle, seed);
        double effectiveR = dist / Math.max(rimNoise, 0.5);

        // 坑内挖空量（shift<0 时地表下降，挖空区为 AIR 开放空间；shift>=0 时为 0）
        double baseDepth = Math.max(0, -shift);

        // 中心陨石核心：大致为扁球体（水平半径 0.35R，垂直厚度随水平距离收缩）
        // 陨石核心从坑底（i >= baseDepth）开始向下，坑内开放空间保持 AIR 空白
        double coreR = R * 0.35;
        double coreDepth = Math.max(1.0, coreR * 0.9);

        for (int i = 0; i < len; i++) {
            double depthFrac = (double) i / Math.max(1, len - 1); // 0 地表 -> 1 深处
            int origMat = newType[i];
            int bx = (int) Math.floor(x);
            int bz = (int) Math.floor(z);

            // 计算撞击影响强度（水平距离 + 深度衰减）
            double influence = 0.0;
            if (effectiveR <= R) {
                influence = 1.0;
            } else if (effectiveR <= R * 2.5) {
                influence = 1.0 - (effectiveR - R) / (R * 1.5);
            }
            influence *= (1.0 - depthFrac * 0.7);

            // 哈希角向噪声：打碎同心环状分布，使材料边界不规则
            double angularInfluence = hashAngularNoise(angle + i * 0.17, seed + 1000);
            influence = Math.max(0, Math.min(1, influence + 0.15 * angularInfluence * (1.0 - depthFrac)));

            // 冲击强度
            double shock = influence * (1.0 - depthFrac * 0.5);

            // 中心陨石核心：椭圆体判断（水平 + 垂直归一化距离平方和 <= 1）
            // 深度基准为坑底（baseDepth），坑内开放空间（i < baseDepth）保持 AIR 空白
            if (i >= baseDepth) {
                double nx = dist / coreR;
                double ny = (i - baseDepth) / coreDepth;
                double spheroid = nx * nx + ny * ny;

                if (spheroid <= 1.0) {
                    newType[i] = METEOR;
                    continue;
                }
                // 紧贴陨石表面的冲击熔融玻璃外壳：陨石周围一圈熔融岩壳
                if (spheroid <= 2.0 && random01(seed, bx, bz, i) < 0.5) {
                    newType[i] = GLASS;
                    continue;
                }
            }

            // 边缘破碎陨石块：在坑缘外侧到溅射毯范围内稀疏散布
            // 使用基于种子与整数坐标的哈希，避免 Double 位模式导致的网格伪影
            // 只嵌在地面以下（i>=1），不替换地表月沙
            if (effectiveR >= R * 0.8 && effectiveR <= R * 2.2 && i >= 1 && i < 4) {
                double edgeT = (effectiveR - R * 0.8) / (R * 1.4);
                double p = 0.08 * (1.0 - edgeT * edgeT);
                if (random01(seed, bx, bz, i) < p) {
                    newType[i] = METEOR;
                    continue;
                }
            }

            // 冲击熔融玻璃透镜：贴合坑碗，坑中心厚、向坑缘减薄，边缘带零星碎渣
            if (effectiveR <= R) {
                // 径向权重：1=坑中心，0=坑缘
                double radialFrac = 1.0 - effectiveR / R;
                // 哈希角向噪声：打破完美环形，使玻璃分布更自然
                radialFrac += hashAngularNoise(angle + i * 0.23, seed + 2000) * 0.2 * radialFrac;
                radialFrac = Math.max(0, Math.min(1, radialFrac));
                // 透镜厚度：中心最厚，随径向收缩（二次衰减）
                double lensDepth = radialFrac * radialFrac * (3.0 + 4.0 * radialFrac);
                // 熔融概率：中心连续成片，越靠边缘越稀疏
                double meltProb = 0.35 + 0.45 * radialFrac;

                if (i < lensDepth && random01(seed, bx, bz, i) < meltProb) {
                    newType[i] = GLASS;
                    continue;
                }
                // 透镜边缘与坑缘之间的玻璃碎块
                if (i < lensDepth + 2 && random01(seed, bx, bz, i) < 0.10) {
                    newType[i] = GLASS;
                    continue;
                }
            }

            // 高压破碎 -> 砂砾
            double gravelThreshold = 0.4 - typeOffset * 0.2;
            double gravelAngularNoise = hashAngularNoise(angle + i * 0.23, seed + 3000) * 0.12;
            if (shock > gravelThreshold + gravelAngularNoise) {
                newType[i] = GRAVEL;
                continue;
            }

            // 溅射毯：不成完美圆环，而是随角度呈稀疏花瓣状泼溅的碎屑层，
            // 相邻扇区强弱不均并留有裸露地表，越远越稀薄
            if (i < shift && effectiveR > R) {
                double ang = Math.atan2(z - impact.cz, x - impact.cx);
                int icx = (int) Math.floor(impact.cx * 3.7);
                int icz = (int) Math.floor(impact.cz * 3.7);
                // 每个陨石坑随机瓣数与旋转相位，避免所有坑呈现相同圆环
                double lobes = 1.0 + 1.5 * random01(seed, icx, icz, 0);
                double phase = Math.PI * random01(seed, icx, icz, 1);
                double lobe = 0.5 + 0.5 * Math.sin(ang * lobes + phase);
                // 距坑缘越远越稀薄
                double spread = 1.0 - (effectiveR - R) / (R * 1.5);
                double coverage = (0.30 + 0.70 * lobe) * (0.5 + 0.5 * spread);
                if (random01(seed, bx, bz, i) < coverage) {
                    int mat = (typeOffset > 0.5 && random01(seed, bx, bz, i + 31) < 0.25) ? GLASS : GRAVEL;
                    newType[i] = mat;
                }
                continue;
            }

            // 其余保持原样（但原样可能已被平移）
            // 轻微角砾化：若 influence > 0.2 且原材质为 STONE，改为 GRAVEL
            if (influence > 0.2 && origMat == STONE) {
                newType[i] = GRAVEL;
            }
        }

        // 5. 写回原数组
        System.arraycopy(newType, 0, type, 0, len);
    }

    // ================= 确定性哈希工具 =================
    private static long hash(long x, long z, long s) {
        long h = s;
        h ^= (x * 0x9E3779B97F4A7C15L);
        h ^= (z * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 29) ^ (h >>> 11);
        h *= 0x27D4EB2F165667C5L;
        h ^= (h >>> 19);
        return h;
    }

    private static double rand01(long h, long salt) {
        long x = (hash(h, salt, 0x9E3779B97F4A7C15L) ^ 0x5BF03635L) & 0xFFFFFFFFL;
        return x / 4294967296.0;
    }

    // 确定性伪随机：根据种子、整数世界坐标与剖面深度，用于边缘陨石块散布
    // 使用 MurmurHash3 finalizer 混合，避免 Double 位模式产生的网格伪影
    private static double random01(long seed, int x, int z, int i) {
        long h = seed;
        h ^= (long) x * 0x9E3779B97F4A7C15L;
        h ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        h ^= (long) i * 0x27D4EB2F165667C5L;
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h & 0xFFFFFFFFL) / 4294967296.0;
    }

    // 角向哈希噪声：将圆周量化为若干扇区，每个扇区用哈希生成随机半径，
    // 多八度叠加产生锯齿状不规则边缘（比正弦波更接近真实陨石坑）
    private static double hashAngularNoise(double angle, long seed) {
        double result = 0.0;
        double amp = 1.0;
        double freq = 1.0;
        for (int o = 0; o < 3; o++) {
            int segments = (int)(12 * freq);
            double aStep = angle * segments / (2.0 * Math.PI);
            int aIdx = (int) Math.floor(aStep);
            double aFrac = aStep - aIdx;
            // 哈希生成扇区边界的随机值
            double n1 = random01(seed, aIdx, o, 77);
            double n2 = random01(seed, aIdx + 1, o, 77);
            // smoothstep 插值
            double t = aFrac * aFrac * (3.0 - 2.0 * aFrac);
            result += amp * ((n1 + (n2 - n1) * t) - 0.5);
            amp *= 0.5;
            freq *= 2.0;
        }
        return result; // 范围约 [-0.875, +0.875]
    }

    // ================= 内部数据结构 =================
    private static class ImpactEvent {
        final double cx, cz;
        final double radius, depth;
        ImpactEvent(double cx, double cz, double radius, double depth) {
            this.cx = cx; this.cz = cz; this.radius = radius; this.depth = depth;
        }
    }
}