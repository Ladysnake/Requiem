package ladysnake.requiem.api.v1.remnant;

public interface DialogueTracker {
    void checkFirstConnection();

    void updateDialogue(int choice);

    void resetProgress();
}
