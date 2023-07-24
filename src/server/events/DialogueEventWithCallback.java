package server.events;

/**
 * Useful for doing some constant undifferentiated thing after your DialogueEvents execute
 */
public record DialogueEventWithCallback(DialogueEvent event, DialogueEvent callback
) implements DialogueEvent {
    @Override
    public void toExecute() {
        if (event != null) event.toExecute();
        if (callback != null) callback.toExecute();
    }
}
