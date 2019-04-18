package ladysnake.requiem.api.v1.internal;

public class UninitializedApiException extends IllegalStateException {
    public UninitializedApiException() {
        this("A feature of Requiem's API has not been initialized. " +
                "Either it has been used too early, or no implementation is available.");
    }

    public UninitializedApiException(String s) {
        super(s);
    }
}
