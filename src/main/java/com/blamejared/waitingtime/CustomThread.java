package com.blamejared.waitingtime;

import com.blamejared.waitingtime.api.Game;
import com.blamejared.waitingtime.games.pong.Pong;
import com.blamejared.waitingtime.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import static org.lwjgl.opengl.GL11.*;

public class CustomThread {
    
    private static final Semaphore mutex = new Semaphore(1);
    private static final int TIMING_FRAME_COUNT = 200;
    private static final int TIMING_FRAME_THRESHOLD = TIMING_FRAME_COUNT * 5 * 1000000;
    private static boolean isDisplayVSyncForced = false;
    private static int backgroundColor = 0x111111;
    private static int angle = 0;
    private static boolean rotate = false;
    private static int logoOffset;
    
    private static Properties config = new Properties();
    
    private static Texture logoTexture;
    private static Texture forgeTexture;
    public static final IResourcePack mcPack = Minecraft.getMinecraft().mcDefaultResourcePack;
    public static final IResourcePack fmlPack = createResourcePack(FMLSanityChecker.fmlLocation);
    
    private static final ResourceLocation logoLoc = new ResourceLocation("textures/gui/title/mojang.png");
    private static final ResourceLocation forgeLoc = new ResourceLocation(getString("forgeTexture", "fml:textures/gui/forge.png"));
    private static final ResourceLocation forgeFallbackLoc = new ResourceLocation("fml:textures/gui/forge.png");
    
    public static SplashFontRenderer fontRenderer;
    private static final int barWidth = 400;
    private static final int barHeight = 20;
    private static final int textHeight2 = 20;
    private static final int barOffset = 55;
    
    private static float memoryColorPercent;
    private static long memoryColorChangeTime;
    private static int memoryGoodColor = 0x00FF00;
    private static int memoryWarnColor = 0xFFFF00;
    private static int memoryLowColor = 0xFF0000;
    private static int barBackgroundColor = 0;
    private static int barBorderColor = 0;
    
    private static int barColor;
    
    /**
     * Get the accurate system time
     *
     * @return The system time in milliseconds
     */
    public static long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
    
    /**
     * frames per second
     */
    public static int fps;
    public static int currentFPS;
    /**
     * last fps time
     */
    public static long lastFPS;
    
    private static int prevLeft, prevRight, prevBottom, prevTop;
    public static Game game;
    
    public static Thread createNewThread() {
        File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/splash.properties");
        
        File parent = configFile.getParentFile();
        if(!parent.exists())
            parent.mkdirs();
        
        try(FileReader r = new FileReader(configFile)) {
            config.load(r);
        } catch(IOException e) {
            FMLLog.log.info("Could not load splash.properties, will create a default one");
        }
        //Some systems do not support this and have weird effects, so we need to detect and disable them by default.
        //The user can always force enable it if they want to take the responsibility for bugs.
        boolean defaultEnabled = true;
        
        // Enable if we have the flag, and there's either no optifine, or optifine has added a key to the blackboard ("optifine.ForgeSplashCompatible")
        // Optifine authors - add this key to the blackboard if you feel your modifications are now compatible with this code.
        rotate = getBool("rotate", false);
        logoOffset = getInt("logoOffset", 0);
        barBorderColor = getHex("barBorder", 0xC0C0C0);
        barColor = getHex("bar", 0xCB3D35);
        barBackgroundColor = getHex("barBackground", 0xFFFFFF);
        memoryGoodColor = getHex("memoryGood", 0x78CB34);
        memoryWarnColor = getHex("memoryWarn", 0xE6E84A);
        memoryLowColor = getHex("memoryLow", 0xE42F2F);
        
        initReflection();
        return new Thread(new Runnable() {
            private long updateTiming;
            private long framecount;
            
            @Override
            public void run() {
                setGL();
                
                logoTexture = new Texture(logoLoc, null, false);
                forgeTexture = new Texture(forgeLoc, forgeFallbackLoc);
                glEnable(GL_TEXTURE_2D);
                fontRenderer = new SplashFontRenderer();
                glDisable(GL_TEXTURE_2D);
                int w = Display.getWidth();
                int h = Display.getHeight();
                int left = 320 - w / 2;
                int right = 320 + w / 2;
                int bottom = 240 + h / 2;
                int top = 240 - h / 2;
                game = new Pong(left, right, top, bottom);
                game.start();
                lastFPS = getTime();
                boolean repeatEventsEnabled = Keyboard.areRepeatEventsEnabled();
                Keyboard.enableRepeatEvents(true);
                while(!isDone()) {
                    
                    updateFPS();
                    framecount++;
                    ProgressManager.ProgressBar first = null, penult = null, last = null;
                    Iterator<ProgressManager.ProgressBar> i = ProgressManager.barIterator();
                    while(i.hasNext()) {
                        if(first == null)
                            first = i.next();
                        else {
                            penult = last;
                            last = i.next();
                        }
                    }
                    glClear(GL_COLOR_BUFFER_BIT);
                    w = Display.getWidth();
                    h = Display.getHeight();
                    
                    glViewport(0, 0, w, h);
                    glMatrixMode(GL_PROJECTION);
                    glLoadIdentity();
                    
                    
                    left = 320 - w / 2;
                    right = 320 + w / 2;
                    bottom = 240 + h / 2;
                    top = 240 - h / 2;
                    
                    if(prevLeft != left || prevRight != right || prevBottom != bottom || prevTop != top) {
                        game.resize(left, right, top, bottom);
                    }
                    
                    prevLeft = left;
                    prevRight = right;
                    prevBottom = bottom;
                    prevTop = top;
                    glOrtho(left, right, bottom, top, -1, 1);
                    glMatrixMode(GL_MODELVIEW);
                    glLoadIdentity();
                    setColor(backgroundColor);
                    
                    
                    glEnable(GL_TEXTURE_2D);
                    logoTexture.bind();
                    glBegin(GL_QUADS);
                    logoTexture.texCoord(0, 0, 0);
                    glVertex2f(320 - 256, 240 - 256);
                    logoTexture.texCoord(0, 0, 1);
                    glVertex2f(320 - 256, 240 + 256);
                    logoTexture.texCoord(0, 1, 1);
                    glVertex2f(320 + 256, 240 + 256);
                    logoTexture.texCoord(0, 1, 0);
                    glVertex2f(320 + 256, 240 - 256);
                    glEnd();
                    glDisable(GL_TEXTURE_2D);
                    
                    glPushMatrix();
                    glColor4f(1, 1, 1, 0.6f);
                    glTranslatef(320 - (float) barWidth / 2, top + 10, 0);
                    drawMemoryBar(255);
                    glPopMatrix();
                    
                    glPushMatrix();
                    glPushAttrib(GL_ALL_ATTRIB_BITS);
                    // bars
                    if(first != null) {
                        glPushMatrix();
                        glTranslatef(320 - (float) barWidth / 2, 310, 0);
                        drawBar(first);
                        if(penult != null) {
                            glTranslatef(0, barOffset, 0);
                            drawBar(penult);
                        }
                        if(last != null) {
                            glTranslatef(0, barOffset, 0);
                            drawBar(last);
                        }
                        glPopMatrix();
                    }
                    
                    
                    glPopAttrib();
                    glPopMatrix();
                    glPushMatrix();
                    angle += 1;
                    
                    // forge logo
                    glColor4f(1, 1, 1, 1);
                    float fw = (float) forgeTexture.getWidth() / 2;
                    float fh = (float) forgeTexture.getHeight() / 2;
                    if(rotate) {
                        float sh = Math.max(fw, fh);
                        glTranslatef(320 + w / 2 - sh - logoOffset, 240 + h / 2 - sh - logoOffset, 0);
                        glRotatef(angle, 0, 0, 1);
                    } else {
                        glTranslatef(320 + w / 2 - fw - logoOffset, 240 + h / 2 - fh - logoOffset, 0);
                    }
                    int f = (angle / 5) % forgeTexture.getFrames();
                    glEnable(GL_TEXTURE_2D);
                    forgeTexture.bind();
                    glBegin(GL_QUADS);
                    forgeTexture.texCoord(f, 0, 0);
                    glVertex2f(-fw, -fh);
                    forgeTexture.texCoord(f, 0, 1);
                    glVertex2f(-fw, fh);
                    forgeTexture.texCoord(f, 1, 1);
                    glVertex2f(fw, fh);
                    forgeTexture.texCoord(f, 1, 0);
                    glVertex2f(fw, -fh);
                    glEnd();
                    glDisable(GL_TEXTURE_2D);
                    glPopMatrix();
                    
                    game.update();
                    game.render();
                    
                    drawString("FPS: " + currentFPS, left + 5, top + 5, 0xFFFFFF, 180);
                    mutex.acquireUninterruptibly();
                    long updateStart = System.nanoTime();
                    
                    Display.update();
                    long dur = System.nanoTime() - updateStart;
                    if(framecount < TIMING_FRAME_COUNT) {
                        updateTiming += dur;
                    }
                    mutex.release();
                    if(pause()) {
                        clearGL();
                        setGL();
                    }
                    if(framecount >= TIMING_FRAME_COUNT && updateTiming > TIMING_FRAME_THRESHOLD) {
                        if(!isDisplayVSyncForced) {
                            isDisplayVSyncForced = true;
                            FMLLog.log.info("Using alternative sync timing : {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                    } else {
                        if(framecount == TIMING_FRAME_COUNT) {
                            FMLLog.log.info("Using sync timing. {} frames of Display.update took {} nanos", TIMING_FRAME_COUNT, updateTiming);
                        }
                        Display.sync(100);
                    }
                }
                Keyboard.enableRepeatEvents(repeatEventsEnabled);
                clearGL();
            }
            
            private void setGL() {
                getLock().lock();
                try {
                    Display.getDrawable().makeCurrent();
                } catch(LWJGLException e) {
                    FMLLog.log.error("Error setting GL context:", e);
                    throw new RuntimeException(e);
                }
                glClearColor((float) ((backgroundColor >> 16) & 0xFF) / 0xFF, (float) ((backgroundColor >> 8) & 0xFF) / 0xFF, (float) (backgroundColor & 0xFF) / 0xFF, 1);
                glDisable(GL_LIGHTING);
                glDisable(GL_DEPTH_TEST);
                glEnable(GL_BLEND);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }
            
            private void clearGL() {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayWidth = Display.getWidth();
                mc.displayHeight = Display.getHeight();
                mc.resize(mc.displayWidth, mc.displayHeight);
                glClearColor(1, 1, 1, 1);
                glEnable(GL_DEPTH_TEST);
                glDepthFunc(GL_LEQUAL);
                glEnable(GL_ALPHA_TEST);
                glAlphaFunc(GL_GREATER, .1f);
                try {
                    Display.getDrawable().releaseContext();
                } catch(LWJGLException e) {
                    FMLLog.log.error("Error releasing GL context:", e);
                    throw new RuntimeException(e);
                } finally {
                    getLock().unlock();
                }
            }
        });
    }
    
    public static void drawLine(double x, double y, double x2, double y2, float red, float green, float blue, float alpha, float lineWidth) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buff = tess.getBuffer();
        
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS | GL11.GL_LIGHTING_BIT);
        GL11.glLineWidth(lineWidth);
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 1);
        buff.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buff.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
        buff.pos(x2, y2, 0).color(red, green, blue, alpha).endVertex();
        tess.draw();
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(32826);
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public static void drawCircle(double radius) {
        glPushMatrix();
        glBegin(GL_LINE_LOOP);
        
        for(int i = 0; i < 360; i++) {
            double degInRad = (i * 3.14159 / 180);
            glVertex2d(0, 0);
            glVertex2d(Math.cos(degInRad) * radius, Math.sin(degInRad) * radius);
        }
        
        glEnd();
        glPopMatrix();
    }
    
    private static void drawBar(ProgressManager.ProgressBar b) {
        glPushMatrix();
        // title - message
        setColor(0xFFFFFF);
        glScalef(2, 2, 1);
        glEnable(GL_TEXTURE_2D);
        fontRenderer.drawString(b.getTitle() + " - " + b.getMessage(), 0, 0, 0xFFFFFF);
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
        // border
        glPushMatrix();
        glTranslatef(0, textHeight2, 0);
        setColor(barBorderColor);
        drawBox(barWidth, barHeight);
        // interior
        setColor(barBackgroundColor);
        glTranslatef(1, 1, 0);
        drawBox(barWidth - 2, barHeight - 2);
        // slidy part
        setColor(barColor);
        drawBox((barWidth - 2) * (b.getStep() + 1) / (b.getSteps() + 1), barHeight - 2); // Step can sometimes be 0.
        // progress text
        String progress = "" + b.getStep() + "/" + b.getSteps();
        glTranslatef(((float) barWidth - 2) / 2 - fontRenderer.getStringWidth(progress), 2, 0);
        setColor(0x0);
        glScalef(2, 2, 1);
        glEnable(GL_TEXTURE_2D);
        fontRenderer.drawString(progress, 0, 0, 0x000000);
        glPopMatrix();
    }
    
    public static void updateFPS() {
        if(getTime() - lastFPS > 1000) {
            currentFPS = fps;
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }
    
    public static int bytesToMb(long bytes) {
        return (int) (bytes / 1024L / 1024L);
    }
    
    public static void drawString(String text, double x, double y, int color) {
        drawString(text, x, y, color, 255);
    }
    
    public static void drawString(String text, double x, double y, int color, int alpha) {
        glPushMatrix();
        setColor(color, alpha);
        glTranslated(x, y, 0);
        glScaled(2, 2, 1);
        glEnable(GL_TEXTURE_2D);
        fontRenderer.drawString(text, 0, 0, 0);
        glDisable(GL_TEXTURE_2D);
        glScaled(1, 1, 1);
        glTranslated(-x, -y, 0);
        glPopMatrix();
    }
    
    public static void setColor(int color, int alpha) {
        glColor4ub((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF), (byte) (color & 0xFF), (byte) alpha);
    }
    
    public static void setColor(int color) {
        glColor3ub((byte) ((color >> 16) & 0xFF), (byte) ((color >> 8) & 0xFF), (byte) (color & 0xFF));
    }
    
    public static void drawMemoryBar(int alpha) {
        int maxMemory = bytesToMb(Runtime.getRuntime().maxMemory());
        int totalMemory = bytesToMb(Runtime.getRuntime().totalMemory());
        int freeMemory = bytesToMb(Runtime.getRuntime().freeMemory());
        int usedMemory = totalMemory - freeMemory;
        float usedMemoryPercent = usedMemory / (float) maxMemory;
        
        // title - message
        drawString("Memory Used / Total", 0, 0, 0xFFFFFF, 180);
        // border
        glPushMatrix();
        glTranslatef(0, textHeight2, 0);
        setColor(barBorderColor, alpha);
        drawBox(barWidth, barHeight);
        // interior
        setColor(barBackgroundColor, alpha);
        glTranslatef(1, 1, 0);
        drawBox(barWidth - 2, barHeight - 2);
        // slidy part
        
        long time = System.currentTimeMillis();
        if(usedMemoryPercent > memoryColorPercent || (time - memoryColorChangeTime > 1000)) {
            memoryColorChangeTime = time;
            memoryColorPercent = usedMemoryPercent;
        }
        
        int memoryBarColor;
        if(memoryColorPercent < 0.75f) {
            memoryBarColor = memoryGoodColor;
        } else if(memoryColorPercent < 0.85f) {
            memoryBarColor = memoryWarnColor;
        } else {
            memoryBarColor = memoryLowColor;
        }
        setColor(memoryLowColor, alpha);
        glPushMatrix();
        glTranslatef((barWidth - 2) * (totalMemory) / (maxMemory) - 2, 0, 0);
        drawBox(2, barHeight - 2);
        glPopMatrix();
        
        setColor(memoryBarColor, alpha);
        drawBox((barWidth - 2) * (usedMemory) / (maxMemory), barHeight - 2);
        
        // progress text
        String progress = getMemoryString(usedMemory) + " / " + getMemoryString(maxMemory);
        drawString(progress, ((float) barWidth - 2) / 2 - fontRenderer.getStringWidth(progress), 2, 0, alpha);
        glPopMatrix();
    }
    
    public static String getMemoryString(int memory) {
        return StringUtils.leftPad(Integer.toString(memory), 4, ' ') + " MB";
    }
    
    public static void drawBox(double w, double h) {
        glBegin(GL_QUADS);
        glVertex2d(0, 0);
        glVertex2d(0, h);
        glVertex2d(w, h);
        glVertex2d(w, 0);
        glEnd();
    }
    
    public static String getString(String name, String def) {
        String value = config.getProperty(name, def);
        config.setProperty(name, value);
        return value;
    }
    
    public static boolean getBool(String name, boolean def) {
        return Boolean.parseBoolean(getString(name, Boolean.toString(def)));
    }
    
    public static int getInt(String name, int def) {
        return Integer.decode(getString(name, Integer.toString(def)));
    }
    
    public static int getHex(String name, int def) {
        return Integer.decode(getString(name, "0x" + Integer.toString(def, 16).toUpperCase()));
    }
    //Not taken from forge
    
    private static boolean loadedReflection = false;
    private static Field lock;
    private static Field pause;
    private static Field done;
    
    private static void initReflection() {
        if(!loadedReflection) {
            Class<SplashProgress> progressClass = SplashProgress.class;
            try {
                lock = progressClass.getDeclaredField("lock");
                pause = progressClass.getDeclaredField("pause");
                done = progressClass.getDeclaredField("done");
                
                lock.setAccessible(true);
                pause.setAccessible(true);
                done.setAccessible(true);
                
                getProperties();
                setFakeTextures();
            } catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to setup custom launch screen", e);
            }
            
            loadedReflection = true;
        }
    }
    
    private static void setFakeTextures() throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<SplashProgress> progressClass = SplashProgress.class;
        setNullTexture(progressClass.getDeclaredField("fontTexture"));
        setNullTexture(progressClass.getDeclaredField("logoTexture"));
        setNullTexture(progressClass.getDeclaredField("forgeTexture"));
        
        
    }
    
    private static void setNullTexture(Field field) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        field.setAccessible(true);
        field.set(null, getNullTexture());
    }
    
    private static Object getNullTexture() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> textureClass = Class.forName("net.minecraftforge.fml.client.SplashProgress$Texture");
        Constructor<?> constructor = textureClass.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
        constructor.setAccessible(true);
        return constructor.newInstance(new ResourceLocation("fml:textures/gui/forge.png"), new ResourceLocation("fml:textures/gui/forge.png"));
    }
    
    private static void getProperties() throws NoSuchFieldException, IllegalAccessException {
        Class<SplashProgress> progressClass = SplashProgress.class;
        
        
    }
    
    private static Lock getLock() {
        try {
            return (Lock) lock.get(null);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Failed to get field", e);
        }
    }
    
    private static boolean isDone() {
        try {
            return (boolean) done.get(null);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Failed to get field", e);
        }
    }
    
    private static boolean pause() {
        try {
            return (boolean) pause.get(null);
        } catch(IllegalAccessException e) {
            throw new RuntimeException("Failed to get field", e);
        }
    }
    
    
    private static IResourcePack createResourcePack(File file) {
        if(file.isDirectory()) {
            return new FolderResourcePack(file);
        } else {
            return new FileResourcePack(file);
        }
    }
}