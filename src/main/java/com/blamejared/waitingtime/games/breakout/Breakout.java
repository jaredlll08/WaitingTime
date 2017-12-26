package com.blamejared.waitingtime.games.breakout;

import com.blamejared.waitingtime.CustomThread;
import com.blamejared.waitingtime.api.Game;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * unused, X brick collision is messed
 */
public class Breakout extends Game {
    
    public Breakout() {
    }
    
    private double paddleX;
    private double paddleY;
    
    private double paddleWidth;
    private double paddleHeight;
    
    
    private double ballX;
    private double ballY;
    private double ballDiam;
    
    private double ballVelX;
    private double ballVelY;
    private double ballVelDefault;
    
    
    private boolean running;
    
    private List<Brick> bricks = new LinkedList<>();
    
    private List<Brick> bricksToRemove = new LinkedList<>();
    
    
    private double score;
    
    @Override
    public void start(double canvasLeft, double canvasRight, double canvasTop, double canvasBottom) {
        random = new Random();
        paddleWidth = width / 4;
        paddleHeight = (height / width) * 20;
        paddleX = canvasLeft + width / 2 + paddleWidth;
        paddleY = canvasBottom - 40;
        ballX = canvasLeft + width / 2;
        ballY = canvasBottom - (height / 3);
        ballDiam = 40;
        ballVelDefault = 2;
        ballVelX = ballVelDefault;
        ballVelY = ballVelDefault;
        if(random.nextBoolean()) {
            ballVelX = -ballVelDefault;
        }
        if(random.nextBoolean()) {
            ballVelY = -ballVelDefault;
        }
        running = true;
        
        bricksToRemove.clear();
        bricks.clear();
        score = 0;
        
        for(int x = 0; x < width; x += width / 10) {
            for(int y = 20; y < 5 * (height / 10); y += height / 10) {
                bricks.add(new Brick(canvasLeft + x, canvasTop + 20 + y, width / 10, height / 10, this));
            }
        }
    }
    
    @Override
    public void resize(double canvasLeft, double canvasRight, double canvasTop, double canvasBottom) {
        super.resize(canvasLeft, canvasRight, canvasTop, canvasBottom);
        start(canvasLeft, canvasRight, canvasTop, canvasBottom);
        ballVelDefault *= width / prevWidth;
    }
    
    @Override
    public void update() {
        int x = MouseInfo.getPointerInfo().getLocation().x;
        int y = MouseInfo.getPointerInfo().getLocation().y;
        if(running) {
            if(x > Display.getX() && x < Display.getX() + Display.getWidth()) {
                paddleX = canvasLeft + (x - Display.getX()) - (paddleWidth / 2);
            }
            if(paddleX < canvasLeft) {
                paddleX = canvasLeft;
            }
            if(paddleX + paddleWidth > canvasRight) {
                paddleX = canvasRight - paddleWidth;
            }
            if(isCollided()) {
                handleCollision();
            }
            
            ballX += ballVelX;
            ballY += ballVelY;
            
            
            if(ballY + ballDiam > paddleY && ballY + ballDiam < paddleY + paddleHeight) {
                if(ballX + ballDiam > paddleX && ballX + ballDiam < paddleX + paddleWidth) {
                    ballVelY = -ballVelDefault;
                    ballY = paddleY - ballDiam;
                }
            }
            if(!bricksToRemove.isEmpty()) {
                bricks.removeAll(bricksToRemove);
                bricksToRemove.clear();
            }
            if(bricks.isEmpty()) {
                for(int x1 = 0; x1 < width; x1 += width / 10) {
                    for(int y1 = 20; y1 < 5 * (height / 10); y1 += height / 10) {
                        bricks.add(new Brick(canvasLeft + x1, canvasTop + 20 + y1, width / 10, height / 10, this));
                    }
                }
            }
        } else {
            if(canvasLeft + x - Display.getX() > canvasLeft && canvasLeft + x - Display.getX() < canvasLeft + 160) {
                if(canvasTop + y - Display.getY() > (canvasTop + 80 + height / 2) - ((height / 6) / 2) && canvasTop + y - Display.getY() < (canvasTop + 80 + height / 2) - ((height / 6) / 2) + 160) {
                    start(canvasLeft, canvasRight, canvasTop, canvasBottom);
                }
            }
        }
        
        
    }
    
    public boolean isCollided() {
        boolean coll = false;
        for(Brick brick : bricks) {
            if(brick.isCollided(ballX, ballY, ballDiam, ballDiam)) {
                coll = true;
            }
        }
        if(ballX < canvasLeft || ballX + ballDiam > canvasRight || ballY < canvasTop || ballY + ballDiam > canvasBottom) {
            return true;
        }
        return coll;
    }
    
    public void handleCollision() {
        if(ballX < canvasLeft) {
            ballVelX = ballVelDefault;
            ballX = canvasLeft;
        }
        if(ballX + ballDiam > canvasRight) {
            ballVelX = -ballVelDefault;
            ballX = canvasRight - ballDiam;
        }
        
        if(ballY < canvasTop) {
            ballVelY = ballVelDefault;
            ballY = canvasTop;
        }
        if(ballY + ballDiam > canvasBottom) {
            running = false;
        }
        for(Brick brick : bricks) {
            if(brick.isCollided(ballX, ballY, ballDiam, ballDiam))
                brick.collide(ballX, ballY, ballDiam, ballDiam);
        }
    }
    
    public void drawCircle(double radius) {
        glBegin(GL_LINE_LOOP);
        
        for(int i = 0; i < 360; i++) {
            double degInRad = (i * 3.14159 / 180);
            glVertex2d(0, 0);
            glVertex2d(Math.cos(degInRad) * radius, Math.sin(degInRad) * radius);
        }
        
        glEnd();
    }
    
    @Override
    public void render() {
        glPushMatrix();
        GL11.glColor4d(1, 0, 0, 1);
        GL11.glTranslated(paddleX, paddleY, 0);
        CustomThread.drawBox(paddleWidth, paddleHeight);
        glPopMatrix();
        
        glPushMatrix();
        CustomThread.drawString("Score: " + getScore(), canvasLeft + this.width / 2, canvasTop, 0x0);
        glPopMatrix();
        
        for(Brick brick : bricks) {
            glPushMatrix();
            glColor4d(0, 0, 0, 1);
            glTranslated(brick.getX(), brick.getY(), 0);
            CustomThread.drawBox(brick.getWidth(), brick.getHeight());
            glPopMatrix();
            glPushMatrix();
            glColor4d(1, 1, 1, 1);
            glTranslated(brick.getX() + 1, brick.getY() + 1, 0);
            CustomThread.drawBox(brick.getWidth() - 2, brick.getHeight() - 2);
            glPopMatrix();
        }
        
        glPushMatrix();
        GL11.glColor4d(1, 0, 0, 1);
        glTranslated(ballX + ballDiam / 2, ballY + ballDiam / 2, 0);
        drawCircle((ballDiam / 2));
        glPopMatrix();
        
        if(!running) {
            glPushMatrix();
            CustomThread.drawString("GAME OVER", (float) (canvasLeft + (width / 2.0f)) - (CustomThread.fontRenderer.getStringWidth("GAME OVER") / 2), (float) (canvasTop + height / 2.0f), 0xFF0000);
            CustomThread.drawString("MOVE MOUSE TO INSIDE SQUARE TO RESTART", (float) (canvasLeft + (width / 2.0f)) - (CustomThread.fontRenderer.getStringWidth("MOVE MOUSE TO INSIDE SQUARE TO RESTART") / 2), (float) (canvasTop + height / 2.0f + CustomThread.fontRenderer.FONT_HEIGHT + 10), 0xFF0000);
            CustomThread.setColor(0x0);
            glTranslated(canvasLeft, (canvasTop + 80 + height / 2) - ((height / 6) / 2), 0);
            CustomThread.drawBox(160, 160);
            glPopMatrix();
        }
        
        
    }
    
    public double getPaddleX() {
        return paddleX;
    }
    
    public void setPaddleX(double paddleX) {
        this.paddleX = paddleX;
    }
    
    public double getPaddleY() {
        return paddleY;
    }
    
    public void setPaddleY(double paddleY) {
        this.paddleY = paddleY;
    }
    
    public double getPaddleWidth() {
        return paddleWidth;
    }
    
    public void setPaddleWidth(double paddleWidth) {
        this.paddleWidth = paddleWidth;
    }
    
    public double getPaddleHeight() {
        return paddleHeight;
    }
    
    public void setPaddleHeight(double paddleHeight) {
        this.paddleHeight = paddleHeight;
    }
    
    public double getBallX() {
        return ballX;
    }
    
    public void setBallX(double ballX) {
        this.ballX = ballX;
    }
    
    public double getBallY() {
        return ballY;
    }
    
    public void setBallY(double ballY) {
        this.ballY = ballY;
    }
    
    public double getBallDiam() {
        return ballDiam;
    }
    
    public void setBallDiam(double ballDiam) {
        this.ballDiam = ballDiam;
    }
    
    public double getBallVelX() {
        return ballVelX;
    }
    
    public void setBallVelX(double ballVelX) {
        this.ballVelX = ballVelX;
    }
    
    public double getBallVelY() {
        return ballVelY;
    }
    
    public void setBallVelY(double ballVelY) {
        this.ballVelY = ballVelY;
    }
    
    public double getBallVelDefault() {
        return ballVelDefault;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    public List<Brick> getBricks() {
        return bricks;
    }
    
    public void setBricks(List<Brick> bricks) {
        this.bricks = bricks;
    }
    
    public List<Brick> getBricksToRemove() {
        return bricksToRemove;
    }
    
    public void setBricksToRemove(List<Brick> bricksToRemove) {
        this.bricksToRemove = bricksToRemove;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
}
