package com.blamejared.waitingtime.games.breakout;

import java.awt.*;

public class Brick {
    
    private double x;
    private double y;
    private double width;
    private double height;
    private Breakout game;
    
    public Brick(double x, double y, double width, double height, Breakout game) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.game = game;
    }
    
    public boolean isCollided(double x, double y, double width, double height) {
        Rectangle rect1 = new Rectangle((int) this.x, (int) this.y, (int) this.width, (int) this.height);
        Rectangle rect2 = new Rectangle((int) x, (int) y, (int) width, (int) height);
        return rect1.intersects(rect2);
        
    }
    
    public void collide(double x, double y, double width, double height) {
        if(x > this.x && x < this.x + this.width) {
            double newVel = game.getBallVelX();
            double newX = this.x;
            if(newVel > 0) {
                newX += this.width;
                newVel = -game.getBallVelDefault();
            } else {
                newVel = game.getBallVelDefault();
            }
            game.setBallVelX(newVel);
            game.setBallX(newX);
        } else if(y > this.y && y < this.y + this.height) {
            double newVel = game.getBallVelY();
            double newY = this.y;
            if(newVel > 0) {
                newVel = -game.getBallVelDefault();
            } else {
                newY += this.height;
                newVel = game.getBallVelDefault();
            }
            game.setBallVelY(newVel);
            game.setBallY(newY);
            
        }
        game.setScore(game.getScore() + 50);
        game.getBricksToRemove().add(this);
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
    
    public Breakout getGame() {
        return game;
    }
    
    public void setGame(Breakout game) {
        this.game = game;
    }
    
    
}
