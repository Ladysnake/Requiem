package ladysnake.dissolution.api.v1.entity;

public interface MotionHandler {
    MotionConfig getConfig();

    void setConfig(MotionConfig config);

    void jump();

    void tick();
}
