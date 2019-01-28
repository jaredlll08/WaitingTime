package com.blamejared.waitingtime.games.pong;

import com.blamejared.waitingtime.CustomThread;
import com.blamejared.waitingtime.api.Game;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class Pong extends Game {
    
    private double paddleX;
    private double paddleY;
    
    private double paddleAIX;
    private double paddleAIY;
    
    private double paddleWidth;
    private double paddleHeight;
    
    
    private double ballX;
    private double ballY;
    private double ballSize;
    
    private double ballVelX;
    private double ballVelY;
    private double ballVelDefault;
    
    
    private int scorePlayer = 0;
    private int scoreAI = 0;
    
    @Override
    public void start(double canvasLeft, double canvasRight, double canvasTop, double canvasBottom) {
        super.start(canvasLeft, canvasRight, canvasTop, canvasBottom);
        paddleWidth = (height / width) * 20;
        paddleHeight = width / 10;
        paddleX = canvasRight - paddleWidth - 20;
        paddleY = canvasBottom - 40;
        paddleAIX = canvasLeft + 20;
        paddleAIY = canvasBottom - 40;
        
        ballX = canvasLeft + width / 2;
        ballY = canvasBottom - (height / 3);
        ballSize = paddleHeight / 8;
        ballVelDefault = 2;
        ballVelX = ballVelDefault;
        ballVelY = ballVelDefault;
        if(random.nextBoolean()) {
            ballVelX = -ballVelDefault;
        }
        if(random.nextBoolean()) {
            ballVelY = -ballVelDefault;
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
        if(y > Display.getY() && y < Display.getY() + Display.getHeight()) {
            paddleY = canvasTop + (y - Display.getY()) - (paddleHeight);
        }
        if(paddleY < canvasTop) {
            paddleY = canvasTop;
        }
        if(paddleY + paddleHeight > canvasBottom) {
            paddleY = canvasBottom - paddleHeight;
        }
        
        int dir = 0;
        if(paddleAIY + paddleHeight / 2 > ballY + ballSize / 2) {
            dir = -2;
        } else if(paddleAIY + paddleHeight / 2 < ballY + ballSize / 2) {
            dir = 2;
        }
        paddleAIY += dir;
        if(paddleAIY < canvasTop) {
            paddleAIY = canvasTop;
        }
        if(paddleAIY + paddleHeight > canvasBottom) {
            paddleAIY = canvasBottom - paddleHeight;
        }
        
        if(isCollided()) {
            handleCollision();
        }
        
        ballX += ballVelX;
        ballY += ballVelY;
        
        Rectangle paddleAI = new Rectangle((int) paddleAIX, (int) paddleAIY, (int) paddleWidth, (int) paddleHeight);
        Rectangle paddle = new Rectangle((int) paddleX, (int) paddleY, (int) paddleWidth, (int) paddleHeight);
        Rectangle ball = new Rectangle((int) ballX, (int) ballY, (int) ballSize, (int) ballSize);
        
        if(ball.intersects(paddle)) {
            ballVelX = -ballVelDefault - Math.random();
            ballX = paddleX - ballSize;
        }
        if(ball.intersects(paddleAI)) {
            ballVelX = ballVelDefault + Math.random();
            ballX = paddleAIX + paddleWidth;
        }
        
    }
    
    public boolean isCollided() {
        return ballX < canvasLeft || ballX + ballSize > canvasRight || ballY < canvasTop || ballY + ballSize > canvasBottom;
    }
    
    public void handleCollision() {
        if(ballX < canvasLeft) {
            ballX = canvasLeft + width / 2;
            ballY = canvasTop + height / 2;
            ballVelX = ballVelDefault + Math.random();
            scorePlayer++;
        }
        if(ballX + ballSize > canvasRight) {
            ballX = canvasLeft + width / 2;
            ballY = canvasTop + height / 2;
            ballVelX = -ballVelDefault - Math.random();
            scoreAI++;
        }
        
        if(ballY < canvasTop) {
            ballVelY = ballVelDefault + Math.random();
            ballY = canvasTop;
        }
        if(ballY + ballSize > canvasBottom) {
            ballVelY = -ballVelDefault - Math.random();
            ballY = canvasBottom - ballSize;
        }
    }
    
    
    @Override
    public void render() {
        glPushMatrix();
        CustomThread.setColor(0xAAAAAA);
        int counter = 0;
        for(int y = (int) canvasTop + 20; y < height; y += 10) {
            if(counter++ % 2 != 0) {
                glPushMatrix();
                GL11.glTranslated(canvasLeft + width / 2 - 2.5, y, 0);
                CustomThread.drawBox(5, 10);
                glPopMatrix();
            }
        }
        glPopMatrix();
        glPushMatrix();
        CustomThread.setColor(0xFFFFFF);
        glBegin(GL_QUADS);
        CustomThread.drawBoxFast(paddleX, paddleY, paddleWidth, paddleHeight);
        
        CustomThread.drawBoxFast(paddleAIX, paddleAIY, paddleWidth, paddleHeight);
        
        CustomThread.drawBoxFast(ballX, ballY, ballSize, ballSize);
        glEnd();
        glPopMatrix();
        glPushMatrix();
        CustomThread.drawString("AI: " + scoreAI, canvasLeft + this.width / 4 - CustomThread.fontRenderer.getStringWidth("AI: " + scoreAI), canvasBottom - CustomThread.fontRenderer.FONT_HEIGHT * 2, 0xFFFFFF);
        CustomThread.drawString("Player: " + scorePlayer, canvasRight - this.width / 4 - CustomThread.fontRenderer.getStringWidth("Player: " + scoreAI), canvasBottom - CustomThread.fontRenderer.FONT_HEIGHT * 2, 0xFFFFFF);
        
        glPopMatrix();
        
        
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
    
}