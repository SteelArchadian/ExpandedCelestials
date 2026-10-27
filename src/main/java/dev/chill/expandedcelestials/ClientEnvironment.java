package dev.chill.expandedcelestials;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid=ExpandedCelestials.MODID,value=Dist.CLIENT)
public final class ClientEnvironment {
    private record State(ClientLevel level,EnvironmentPayload payload,long expires) {}
    private static volatile State state;
    private static final ParticleBudget budget=new ParticleBudget();
    private static long lastTick=Long.MIN_VALUE;
    private static boolean irisChecked;
    private static Object irisApi;
    private static java.lang.reflect.Method shaderCheck;
    public static void receive(EnvironmentPayload payload) {
        var level=Minecraft.getInstance().level;
        if(level!=null && level.dimension().location().equals(payload.dimension())) {
            if(state==null||state.level!=level||!state.payload.particles()||!payload.particles())budget.reset();
            state=new State(level,payload,level.getGameTime()+220);
        }
    }
    private static State current(Level level) {
        var saved=state;
        return saved!=null && saved.level==level && level.getGameTime()<=saved.expires?saved:null;
    }
    public static double gravity(Level level){var saved=current(level);return saved==null?1:saved.payload.gravity();}
    private static boolean night(Level level){long time=Math.floorMod(level.getDayTime(),24000);return time>=12500;}
    public static boolean endSky(Level level) {
        var saved=current(level);return saved!=null&&saved.payload.endSky()&&night(level)&&!shaderPackInUse();
    }
    private static boolean shaderPackInUse() {
        try {
            if(!irisChecked) {
                irisChecked=true;var type=Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisApi=type.getMethod("getInstance").invoke(null);shaderCheck=type.getMethod("isShaderPackInUse");
            }
            return shaderCheck!=null && (Boolean)shaderCheck.invoke(irisApi);
        } catch(ReflectiveOperationException|LinkageError error){return false;}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        var minecraft=Minecraft.getInstance();var level=minecraft.level;var player=minecraft.player;
        if(level==null||player==null||minecraft.isPaused())return;
        long tick=level.getGameTime();if(tick==lastTick)return;lastTick=tick;
        var saved=current(level);
        if(saved==null||!saved.payload.particles()||!night(level)){budget.reset();return;}
        int count=budget.tick(saved.payload.particleRate());
        var random=level.random;
        for(int i=0;i<count;i++)for(int attempt=0;attempt<2;attempt++) {
            double angle=random.nextDouble()*Math.PI*2, radius=4+20*Math.sqrt(random.nextDouble());
            double x=player.getX()+Math.cos(angle)*radius,z=player.getZ()+Math.sin(angle)*radius;
            double y=player.getY()-2+random.nextDouble()*12;
            BlockPos pos=BlockPos.containing(x,y,z);
            if(!level.hasChunkAt(pos)||!level.getBlockState(pos).isAir())continue;
            level.addParticle(random.nextInt(4)==0?ParticleTypes.REVERSE_PORTAL:ParticleTypes.PORTAL,
                x,y,z,(random.nextDouble()-.5)*.08,random.nextDouble()*.025,(random.nextDouble()-.5)*.08);
            break;
        }
    }
    @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor event) {
        var level=Minecraft.getInstance().level;var saved=level==null?null:current(level);
        if(saved==null || !saved.payload.endSky() || !night(level)||event.getCamera().getFluidInCamera()!=FogType.NONE)return;
        event.setRed(event.getRed()*.75f+.24f*.25f);
        event.setGreen(event.getGreen()*.75f+.09f*.25f);
        event.setBlue(event.getBlue()*.75f+.31f*.25f);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){state=null;lastTick=Long.MIN_VALUE;budget.reset();}
}
