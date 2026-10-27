package dev.chill.expandedcelestials;
/** Integer credit avoids burst spawning and depends on game ticks, not render FPS. */
public final class ParticleBudget {
    private int credit;
    public int tick(int rate) {
        if(rate<0||rate>64)throw new IllegalArgumentException("Particle rate must be 0..64");
        credit+=rate;int count=credit/20;credit%=20;return count;
    }
    public void reset(){credit=0;}
}
