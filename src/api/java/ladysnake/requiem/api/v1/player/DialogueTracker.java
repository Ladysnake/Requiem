package ladysnake.requiem.api.v1.player;

public interface DialogueTracker {
    void checkFirstConnection();

    void updateDialogue(int choice);

    void resetProgress();
}
