package dev.chill.expandedcelestials;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
            double angle=random.nextDouble()*Math.PI*2, radius;
            double x,z;
            radius=3+21*Math.sqrt(random.nextDouble());
            x=player.getX()+Math.cos(angle)*radius;z=player.getZ()+Math.sin(angle)*radius;
            // Find the ground: walk down from above the player to the first solid block under air.
            BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos(x,player.getY()+6,z);
            if(!level.hasChunkAt(pos))continue;
            int floor=Integer.MIN_VALUE;
            for(int step=0;step<18;step++,pos.move(0,-1,0))
                if(level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender(level,pos.below())){floor=pos.getY();break;}
            if(floor==Integer.MIN_VALUE)continue;
            double y=floor+.1+random.nextDouble()*.5;
            // Purple glowing motes close to the ground, with the odd portal speck drifting up.
            level.addParticle(random.nextInt(3)==0?ParticleTypes.PORTAL:ParticleTypes.DRAGON_BREATH,
                x,y,z,(random.nextDouble()-.5)*.02,random.nextDouble()*.03,(random.nextDouble()-.5)*.02);
            if(random.nextInt(3)==0)level.addParticle(ParticleTypes.REVERSE_PORTAL,
                x,y+.3,z,(random.nextDouble()-.5)*.06,random.nextDouble()*.04,(random.nextDouble()-.5)*.06);
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

    // Eclipse: a black disc over the moon with a glowing corona ring and a flare on one edge. Drawn as plain coloured
    // geometry right after the sky so it needs no texture and also appears under shader packs.
    private static void ring(Matrix4f m,float r0,float a0,float r1,float a1,float red,float green,float blue) {
        var b=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_STRIP,DefaultVertexFormat.POSITION_COLOR);
        for(int i=0;i<=48;i++) {
            float angle=i*Mth.TWO_PI/48,cos=Mth.cos(angle),sin=Mth.sin(angle);
            b.addVertex(m,cos*r0,-100,sin*r0).setColor(red,green,blue,a0);
            b.addVertex(m,cos*r1,-100,sin*r1).setColor(red,green,blue,a1);
        }
        BufferUploader.drawWithShader(b.buildOrThrow());
    }
    @SubscribeEvent public static void eclipse(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_SKY)return;
        var level=Minecraft.getInstance().level;var saved=level==null?null:current(level);
        if(saved==null||!saved.payload.eclipseSky()||!night(level))return;
        float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);Matrix4f m=new Matrix4f(event.getModelViewMatrix());
        float fade=1-level.getRainLevel(partial);
        m.rotateY(-Mth.HALF_PI).rotateX(level.getTimeOfDay(partial)*Mth.TWO_PI);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();RenderSystem.disableCull();RenderSystem.disableDepthTest();RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        {
        var b=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN,DefaultVertexFormat.POSITION_COLOR);
        b.addVertex(m,0,-100,0).setColor(0f,0f,0f,fade);
        for(int i=0;i<=48;i++){float angle=i*Mth.TWO_PI/48;b.addVertex(m,Mth.cos(angle)*20.5f,-100,Mth.sin(angle)*20.5f).setColor(0f,0f,0f,fade);}
        BufferUploader.drawWithShader(b.buildOrThrow());
        }
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        ring(m,19.5f,.9f*fade,21f,.7f*fade,1f,.8f,.45f);
        ring(m,21f,.7f*fade,24f,.28f*fade,1f,.5f,.15f);
        ring(m,24f,.28f*fade,36f,0f,1f,.35f,.08f);
        // Diamond-ring flare on one edge.
        {
        var b=Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN,DefaultVertexFormat.POSITION_COLOR);
        b.addVertex(m,20.8f,-100,7f).setColor(1f,1f,.85f,.95f*fade);
        for(int i=0;i<=16;i++){float angle=i*Mth.TWO_PI/16;b.addVertex(m,20.8f+Mth.cos(angle)*7f,-100,7f+Mth.sin(angle)*7f).setColor(1f,.6f,.2f,0f);}
        BufferUploader.drawWithShader(b.buildOrThrow());
        }
        RenderSystem.defaultBlendFunc();RenderSystem.depthMask(true);RenderSystem.enableDepthTest();RenderSystem.enableCull();RenderSystem.disableBlend();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){state=null;lastTick=Long.MIN_VALUE;budget.reset();}
}
