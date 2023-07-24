package server.model.players.dialogue.ddm;

import server.events.DialogueEvent;
import server.events.DialogueEventWithCallback;
import server.model.players.Player;
import server.model.players.dialogue.DialogueBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.*;

/**
 * You can do custom dialogue stuff using this class. But, you should do it in a manualNavOption
 * see {@link #manualNavigationOptions(OptionConfig)}. You can call send() on the current layer, or a previous layer,
 * when the custom stuff is done, to refresh it, if that's what you want to do when it's done.
 *<br>
 * Do not call send() on a layer which hasn't been navigated to yet. Or, if you do, call {@link #withContext(Object) withContext}
 * with the context first.
 *
 * @param <C> The type of the context object corresponding to this menu layer
 */
public class MenuLayer<C> {
    private final ArrayList<Runnable> queue = new ArrayList<>();
    private final ArrayList<DDMOption<C>> options = new ArrayList<>();
    private MenuLayer<?> prev;
    private final String origTitle;
    private String title;
    private final Player c;
    private Predicate<C> pageCheck = ctx -> true;
    public C context;

    private MenuLayer(Player c, String title, boolean isTerminal){
        this.c = c;
        this.origTitle = title;
        this.title = title;
        if (!isTerminal){
            this.prev = makeTerminalLayer(c);
        }
    }

    public MenuLayer(Player c, String title){
        this(c, title, false);
    }

    public MenuLayer(Player c){
        this(c, "");
    }

    /**
     * Sends the layer as a dialogue. All options are generated just before display to ensure up-to-date data
     * This does cause some inconveniences when adding options, but they're minor, once you learn what's going on
     */
    public void send(){
        c.getPA().closeAllWindows();

        options.clear();
        title = origTitle;

        if (context == null) return;

        queue.forEach(Runnable::run);

        List<DDMOption<C>> filtered = options.stream()
            .filter(option -> this.pageCheck.test(context) && (option.shouldShow() == null || option.shouldShow().test(context)))
            .toList();
        c.getDH().build().sendTitledOptions(
            title,
            filtered.stream().map(DDMOption::text).toArray(String[]::new),
            filtered.stream().map(DDMOption::event).toArray(DialogueEvent[]::new),
            () -> prev.send() // set the neverMind event to the previous layer send
        );
    }

    protected void send(MenuLayer<?> prev, C context){
        this.prev = prev;
        this.context = context;
        this.contextTitle(ctx -> title);
        send();
    }

    public MenuLayer<C> withContext(C context){
        this.context = context;
        return this;
    }

    /**
     * Dynamically sets the page title
     */
    public MenuLayer<C> contextTitle(Function<C, String> titleGenerator){
        queue.add(() -> {
            this.title = titleGenerator.apply(context);
        });
        return this;
    }

    public MenuLayer<C> pagePermission(Predicate<C> check){
        if (check == null){
            this.pageCheck = ctx -> true;
            return this;
        }
        queue.add(() -> {
            this.pageCheck = check;
        });
        return this;
    }

    public MenuLayer<C> closeOption(Function<C, String> labelGenerator, Consumer<C> event) {
        return closeOption(labelGenerator, event, ctx -> true);
    }

    public MenuLayer<C> closeOption(Function<C, String> labelGenerator, Consumer<C> event, Predicate<C> shouldShow) {
        return closeOption(labelGenerator, event, shouldShow, ctx -> null);
    }

    /**
     * Adds an option which closes all PA windows after running the event
     *
     * @see #refreshOption(Function, Consumer, Predicate, Function)
     */
    public MenuLayer<C> closeOption(
        Function<C, String> labelGenerator,
        Consumer<C> event,
        Predicate<C> shouldShow,
        Function<C, String> shouldConfirm
    ){
        return closeOption(
            new OptionConfig<C>(this)
                .label(labelGenerator)
                .onClick(event)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public MenuLayer<C> closeOption(OptionConfig<C> config) {
        queue.add(() -> {
            String confirmTitle = config.shouldConfirm.apply(context);
            DialogueEvent toShow = () -> config.event.accept(context);
            if (confirmTitle != null) {
                toShow = new DialogueEventWithConfirmation(toShow, confirmTitle);
            }
            options.add(new DDMOption<>(
                config.labelGenerator.apply(context),
                toShow,
                config.shouldShow
            ));
        });
        return this;
    }

    public MenuLayer<C> backtrackOption(Function<C, String> labelGenerator, Consumer<C> event) {
        return backtrackOption(labelGenerator, event, ctx -> true);
    }

    public MenuLayer<C> backtrackOption(Function<C, String> labelGenerator, Consumer<C> event, Predicate<C> shouldShow){
        return backtrackOption(labelGenerator, event, shouldShow, ctx -> null);
    }

    /**
     * Adds an option which backtracks to the previous layer after the event is run
     *
     * @see #refreshOption(Function, Consumer, Predicate, Function)
     */
    public MenuLayer<C> backtrackOption(
        Function<C, String> labelGenerator,
        Consumer<C> event,
        Predicate<C> shouldShow,
        Function<C, String> shouldConfirm
    ){
        return backtrackOption(
            new OptionConfig<C>(this)
                .label(labelGenerator)
                .onClick(event)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public MenuLayer<C> backtrackOption(OptionConfig<C> config) {
        queue.add(() -> {
            String confirmTitle = config.shouldConfirm.apply(context);
            DialogueEvent toShow = new DialogueEventWithCallback(
                () -> config.event.accept(context), () -> prev.send()
            );
            if (confirmTitle != null) {
                toShow = new DialogueEventWithConfirmation(toShow, confirmTitle);
            }
            options.add(new DDMOption<>(
                config.labelGenerator.apply(context),
                toShow,
                config.shouldShow
            ));
        });
        return this;
    }

    public MenuLayer<C> refreshOption(Function<C, String> text, Consumer<C> event) {
        return refreshOption(text, event, ctx -> true);
    }

    public MenuLayer<C> refreshOption(Function<C, String> text, Consumer<C> event, Predicate<C> shouldShow) {
        return refreshOption(text, event, shouldShow, ctx -> null);
    }

    /**
     * Adds an option which refreshes the current layer after the event is run
     *
     * @param labelGenerator a function to generate the option label, given the context
     * @param event the thing to do when the option is clicked
     * @param shouldShow a predicate indicating whether this option should be displayed
     * @param shouldConfirm returns null if the action doesn't need a confirmation screen, otherwise the title for the confirm screen
     */
    public MenuLayer<C> refreshOption(
        Function<C, String> labelGenerator,
        Consumer<C> event,
        Predicate<C> shouldShow,
        Function<C, String> shouldConfirm
    ){
        return refreshOption(
            new OptionConfig<C>(this)
                .label(labelGenerator)
                .onClick(event)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public MenuLayer<C> refreshOption(OptionConfig<C> config) {
        queue.add(() -> {
            String confirmTitle = config.shouldConfirm.apply(context);
            DialogueEvent toShow = new DialogueEventWithCallback(
                () -> config.event.accept(context), this::send
            );
            if (confirmTitle != null) {
                toShow = new DialogueEventWithConfirmation(toShow, confirmTitle);
            }
            options.add(new DDMOption<>(
                config.labelGenerator.apply(context),
                toShow,
                config.shouldShow
            ));
        });
        return this;
    }

    public MenuLayer<C> forwardOption(Function<C, String> labelGenerator, Function<C, LayerContextPair<?>> next){
        return forwardOption(labelGenerator, next, ctx -> true);
    }

    public MenuLayer<C> forwardOption(Function<C, String> labelGenerator, Function<C, LayerContextPair<?>> next, Predicate<C> shouldShow) {
        return forwardOption(labelGenerator, next, shouldShow, ctx -> null);
    }

    /**
     * @param labelGenerator a function to generate the option label, given the context
     * @param next a function returning the LayerTransfer to invoke
     * @param shouldShow a predicate indicating whether this option should be displayed
     * @param shouldConfirm returns null if the action doesn't need a confirmation screen, otherwise the title for the confirm screen
     */
    public MenuLayer<C> forwardOption(
        Function<C, String> labelGenerator,
        Function<C, LayerContextPair<?>> next,
        Predicate<C> shouldShow,
        Function<C, String> shouldConfirm
    ) {
        return forwardOption(
            new ForwardOptionConfig<C>(this)
                .label(labelGenerator)
                .next(next)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public MenuLayer<C> forwardOption(ForwardOptionConfig<C> config) {
        queue.add(() -> {
            String labelText = config.labelGenerator.apply(context);
            String confirmTitle = config.shouldConfirm.apply(context);
            DialogueEvent event = () -> transfer(this, config.next.apply(context), labelText);
            if (confirmTitle != null) {
                event = new DialogueEventWithConfirmation(event, confirmTitle);
            }
            options.add(new DDMOption<>(
                labelText,
                event,
                config.shouldShow
            ));
        });
        return this;
    }

    public <O> MenuLayer<C> selectionMenu(
        Function<C, Collection<O>> entryGenerator,
        BiFunction<C, O, String> labelGenerator,
        BiFunction<C, O, LayerContextPair<?>> layerMapper
    )
    {
        return selectionMenu(entryGenerator, labelGenerator, layerMapper, (ctx, obj) -> true);
    }

    public <O> MenuLayer<C> selectionMenu(
        Function<C, Collection<O>> entryGenerator,
        BiFunction<C, O, String> labelGenerator,
        BiFunction<C, O, LayerContextPair<?>> layerMapper,
        BiPredicate<C, O> shouldShow
    )
    {
        return selectionMenu(entryGenerator, labelGenerator, layerMapper, shouldShow, (ctx, obj) -> null);
    }

    /**
     * @param <O> the type of object being selected in the list
     * @param entryGenerator outputs a list of objects to select, given a context
     * @param labelGenerator outputs a label given a context and the object being labelled
     * @param layerMapper outputs the LayerTransfer to invoke
     * @param shouldShow returns true if the passed in should be shown
     * @param shouldConfirm returns null if the action doesn't need a confirmation screen, otherwise the title for the confirm screen
     */
    public <O> MenuLayer<C> selectionMenu(
        Function<C, Collection<O>> entryGenerator,
        BiFunction<C, O, String> labelGenerator,
        BiFunction<C, O, LayerContextPair<?>> layerMapper,
        BiPredicate<C, O> shouldShow,
        BiFunction<C, O, String> shouldConfirm
    )
    {
        return selectionMenu(
            new SelectionOptionConfig<>(entryGenerator)
                .labels(labelGenerator)
                .nextLayer(layerMapper)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public <O> MenuLayer<C> selectionMenu(SelectionOptionConfig<C, O> config) {
        queue.add(() -> {
            config.entryGenerator.apply(context).forEach(obj -> {
                String labelText = config.labelGenerator.apply(context, obj);
                String confirmTitle = config.shouldConfirm.apply(context, obj);
                DialogueEvent event = () -> transfer(this, config.layerMapper.apply(context, obj), labelText);
                if (confirmTitle != null) {
                    event = new DialogueEventWithConfirmation(event, confirmTitle);
                }
                options.add(new DDMOption<>(
                    labelText,
                    event,
                    player -> config.shouldShow.test(context, obj)
                ));
            });
        });
        return this;
    }

    public MenuLayer<C> dialogueResponse(Function<C, String> labelGenerator, Function<C, DialogueBuilder> builderGenerator) {
        return dialogueResponse(labelGenerator, builderGenerator, ctx -> true);
    }

    public MenuLayer<C> dialogueResponse(Function<C, String> labelGenerator, Function<C, DialogueBuilder> builderGenerator, Predicate<C> shouldShow){
        return dialogueResponse(
            new DialogueOptionConfig<C>(this)
                .label(labelGenerator)
                .showBuilder(builderGenerator)
                .showIf(shouldShow)
        );
    }

    /**
     * Don't use sendOptions inside the DialogueBuilder. Use a {@link #manualNavigationOptions(OptionConfig)} instead
     */
    public MenuLayer<C> dialogueResponse(DialogueOptionConfig<C> config) {
        queue.add(() -> {
            options.add(new DDMOption<>(
                config.labelGenerator.apply(context),
                () -> {
                    config.builderGenerator
                        .apply(context)
                        .sendStatement("Please wait...", this::send)
                        .run();
                },
                config.shouldShow
            ));
        });
        return this;
    }

    public MenuLayer<C> manualNavigationOption(
        Function<C, String> labelGenerator,
        Consumer<C> event
    ) { return manualNavigationOption(labelGenerator, event, ctx -> true); }

    public MenuLayer<C> manualNavigationOption(
        Function<C, String> labelGenerator,
        Consumer<C> event,
        Predicate<C> shouldShow
    ) { return manualNavigationOption(labelGenerator, event, shouldShow, ctx -> null); }

    public MenuLayer<C> manualNavigationOption(
        Function<C, String> labelGenerator,
        Consumer<C> event,
        Predicate<C> shouldShow,
        Function<C, String> shouldConfirm
    ){
        return manualNavigationOptions(
            new OptionConfig<C>(this)
                .label(labelGenerator)
                .onClick(event)
                .showIf(shouldShow)
                .confirmationTitle(shouldConfirm)
        );
    }

    public MenuLayer<C> manualNavigationOptions(OptionConfig<C> config) {
        Function<C, String> labelGenerator = config.labelGenerator;
        Consumer<C> event = config.event;
        Predicate<C> shouldShow = config.shouldShow;
        Function<C, String> shouldConfirm = config.shouldConfirm;

        queue.add(() -> {
            String confirmTitle = shouldConfirm.apply(context);
            DialogueEvent toShow = () -> event.accept(context);
            if (confirmTitle != null){
                toShow = new DialogueEventWithConfirmation(toShow, confirmTitle);
            }
            options.add(new DDMOption<>(
                labelGenerator.apply(context),
                toShow,
                shouldShow
            ));
        });
        return this;
    }

    /**
     * This only works when the next layer and this layer have the same context type
     */
    public LayerContextPair<C> next(MenuLayer<C> to){
        return next(to, context);
    }

    /**
     * @param redirectTo the MenuLayer to redirect to
     * @param ctx the context to open the MenuLayer with
     * @param <N> the context type for the new layer (the layer being redirected to)
     * @return a LayerTransfer linking this layer with the specified one
     */
    public <N> LayerContextPair<N> next(
        MenuLayer<N> redirectTo,
        N ctx
    ){
        return new LayerContextPair<>(redirectTo, ctx);
    }

    /**
     * @return a LayerTransfer which will display the previous layer, but not link this layer as that one's prev
     */
    public LayerContextPair<?> prev(){
        return prev.layerContextPair();
    }

    private LayerContextPair<C> layerContextPair(){
        return new LayerContextPair<>(this, context);
    }

    public static MenuLayer<Object> makeTerminalLayer(Player c){
        return new MenuLayer<>(c, "", true){
            @Override
            public void send() {
                c.getPA().closeAllWindows();
            }
        };
    }

    public static record LayerContextPair<C>(
        MenuLayer<C> layer,
        C context
    ) { }

    private static record DDMOption<C>(
        String text,
        DialogueEvent event,
        Predicate<C> shouldShow
    ) { }

    private final class DialogueEventWithConfirmation implements DialogueEvent {
        private final DialogueEvent onConfirm;
        private final String title;

        private DialogueEventWithConfirmation(
            DialogueEvent onConfirm,
            String title
        ) {
            this.onConfirm = onConfirm;
            this.title = title;
        }

        @Override
        public void toExecute() {
            c.getPA().closeAllWindows();
            c.getDH().build().sendTitledOptions(
                title,
                "Yes, I'm sure.", onConfirm,
                "Cancel", MenuLayer.this::send

            ).run();
        }
    }
    
    private static <A, B> void transfer(MenuLayer<A> prev, LayerContextPair<B> transferred, String title){
        // We need to check if the layer is already in the stack of previous layers, and if it is, revert to that point in the stack
        // This is to avoid creating an infinite loop by updating the existing layer's prev reference to one of its children
        int limit = 100;
        MenuLayer<?> curr = prev;
        while(limit-- > 0 && curr.prev != null){
            if (transferred.layer == curr.prev){
                transferred.layer.withContext(transferred.context).send(); // Don't link prev if it creates a loop, just revert the stack to there
                return;
            }
            curr = curr.prev;
        }

        // If no loop will be created
        transferred.layer.send(prev, transferred.context);
    }



    public static class OptionConfig<C> {
        private Function<C, String> labelGenerator;
        private Consumer<C> event;
        private Predicate<C> shouldShow;
        private Function<C, String> shouldConfirm;

        public OptionConfig(MenuLayer<C> parent){}

        public OptionConfig<C> label(Function<C, String> labelGenerator) {
            this.labelGenerator = labelGenerator;
            return this;
        }

        public OptionConfig<C> onClick(Consumer<C> event) {
            this.event = event;
            return this;
        }

        public OptionConfig<C> showIf(Predicate<C> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }

        public OptionConfig<C> confirmationTitle(Function<C, String> shouldConfirm) {
            this.shouldConfirm = shouldConfirm;
            return this;
        }
    }

    public static class ForwardOptionConfig<C> {
        private Function<C, String> labelGenerator;
        private Predicate<C> shouldShow;
        private Function<C, String> shouldConfirm;
        private Function<C, LayerContextPair<?>> next;

        public ForwardOptionConfig(MenuLayer<C> parent){}

        public ForwardOptionConfig<C> label(Function<C, String> labelGenerator) {
            this.labelGenerator = labelGenerator;
            return this;
        }

        public ForwardOptionConfig<C> showIf(Predicate<C> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }

        public ForwardOptionConfig<C> confirmationTitle(Function<C, String> shouldConfirm) {
            this.shouldConfirm = shouldConfirm;
            return this;
        }

        public ForwardOptionConfig<C> next(Function<C, LayerContextPair<?>> next){
            this.next = next;
            return this;
        }
    }

    public static class SelectionOptionConfig<C, O> {

        private Function<C, Collection<O>> entryGenerator;
        private BiFunction<C, O, String> labelGenerator;
        private BiFunction<C, O, LayerContextPair<?>> layerMapper;
        private BiPredicate<C, O> shouldShow;
        private BiFunction<C, O, String> shouldConfirm;

        public SelectionOptionConfig(Function<C, Collection<O>> objects){
            this.entryGenerator = objects;
        }

        public SelectionOptionConfig<C, O> objects(Function<C, Collection<O>> entryGenerator) {
            this.entryGenerator = entryGenerator;
            return this;
        }

        public SelectionOptionConfig<C, O> labels(BiFunction<C, O, String> labelGenerator) {
            this.labelGenerator = labelGenerator;
            return this;
        }

        public SelectionOptionConfig<C, O> nextLayer(BiFunction<C, O, LayerContextPair<?>> layerMapper) {
            this.layerMapper = layerMapper;
            return this;
        }

        public SelectionOptionConfig<C, O> showIf(BiPredicate<C, O> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }

        public SelectionOptionConfig<C, O> confirmationTitle(BiFunction<C, O, String> shouldConfirm) {
            this.shouldConfirm = shouldConfirm;
            return this;
        }
    }

    public static class DialogueOptionConfig<C> {

        private Function<C, String> labelGenerator;
        private Function<C, DialogueBuilder> builderGenerator;
        private Predicate<C> shouldShow;


        public DialogueOptionConfig(MenuLayer<C> parent){}

        public DialogueOptionConfig<C> label(Function<C, String> labelGenerator) {
            this.labelGenerator = labelGenerator;
            return this;
        }

        public DialogueOptionConfig<C> showBuilder(Function<C, DialogueBuilder> builderGenerator) {
            this.builderGenerator = builderGenerator;
            return this;
        }

        public DialogueOptionConfig<C> showIf(Predicate<C> shouldShow) {
            this.shouldShow = shouldShow;
            return this;
        }
    }
}
