package com.blamejared.waitingtime.api;

import java.util.Random;

public abstract class Game {
    
    protected double canvasLeft;
    protected double canvasRight;
    protected double canvasTop;
    protected double canvasBottom;
    
    protected double width;
    protected double height;
    
    protected double prevWidth;
    
    protected Random random;
    
    public Game() {
    }
    
    public void start(double canvasLeft, double canvasRight, double canvasTop, double canvasBottom){
        random = new Random();
        this.canvasLeft = canvasLeft;
        this.canvasRight = canvasRight;
        this.canvasTop = canvasTop;
        this.canvasBottom = canvasBottom;
        width = canvasRight - canvasLeft;
        height = canvasBottom - canvasTop;
        prevWidth = width;
    }
    
    public abstract void update();
    
    public abstract void render();
    
    
    public void resize(double canvasLeft, double canvasRight, double canvasTop, double canvasBottom) {
        this.canvasLeft = canvasLeft;
        this.canvasRight = canvasRight;
        this.canvasTop = canvasTop;
        this.canvasBottom = canvasBottom;
        if(prevWidth != canvasRight - canvasLeft)
            prevWidth = canvasRight - canvasLeft;
        width = canvasRight - canvasLeft;
        height = canvasBottom - canvasTop;
    }
    
    public double getCanvasLeft() {
        return canvasLeft;
    }
    
    public void setCanvasLeft(double canvasLeft) {
        this.canvasLeft = canvasLeft;
    }
    
    public double getCanvasRight() {
        return canvasRight;
    }
    
    public void setCanvasRight(double canvasRight) {
        this.canvasRight = canvasRight;
    }
    
    public double getCanvasTop() {
        return canvasTop;
    }
    
    public void setCanvasTop(double canvasTop) {
        this.canvasTop = canvasTop;
    }
    
    public double getCanvasBottom() {
        return canvasBottom;
    }
    
    public void setCanvasBottom(double canvasBottom) {
        this.canvasBottom = canvasBottom;
    }
    
    public Random getRandom() {
        return random;
    }
}
