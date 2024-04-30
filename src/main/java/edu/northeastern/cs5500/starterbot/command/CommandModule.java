package edu.northeastern.cs5500.starterbot.command;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
public class CommandModule {

    @Provides
    @IntoMap
    @StringKey(SayCommand.NAME)
    public SlashCommandHandler provideSayCommand(SayCommand sayCommand) {
        return sayCommand;
    }

    @Provides
    @IntoMap
    @StringKey(PreferredNameCommand.NAME)
    public SlashCommandHandler providePreferredNameCommand(
            PreferredNameCommand preferredNameCommand) {
        return preferredNameCommand;
    }

    @Provides
    @IntoMap
    @StringKey(FailureCommand.NAME)
    public SlashCommandHandler provideFailureCommand(FailureCommand failureCommand) {
        return failureCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ButtonCommand.NAME)
    public SlashCommandHandler provideButtonCommand(ButtonCommand buttonCommand) {
        return buttonCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ButtonCommand.NAME)
    public ButtonHandler provideButtonCommandClickHandler(ButtonCommand buttonCommand) {
        return buttonCommand;
    }

    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public SlashCommandHandler provideDropdownCommand(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }

    @Provides
    @IntoMap
    @StringKey(DropdownCommand.NAME)
    public StringSelectHandler provideDropdownCommandMenuHandler(DropdownCommand dropdownCommand) {
        return dropdownCommand;
    }

    @Provides
    @IntoMap
    @StringKey(HelloWorldCommand.NAME)
    public SlashCommandHandler provideHelloWorldCommand(HelloWorldCommand helloWorldCommand) {
        return helloWorldCommand;
    }

    @Provides
    @IntoMap
    @StringKey(QuestionsCommand.NAME)
    public SlashCommandHandler provideQuestionsCommand(QuestionsCommand questionsCommand) {
        return questionsCommand;
    }

    @Provides
    @IntoMap
    @StringKey(BookCommand.NAME)
    public SlashCommandHandler provideBookCommand(BookCommand bookCommand) {
        return bookCommand;
    }

    @Provides
    @IntoMap
    @StringKey(BookCommand.NAME)
    public ButtonHandler provideBookCommandClickHandler(BookCommand bookCommand) {
        return bookCommand;
    }

    @Provides
    @IntoMap
    @StringKey(CancelCommand.NAME)
    public SlashCommandHandler provideCancelCommand(CancelCommand cancelCommand) {
        return cancelCommand;
    }

    @Provides
    @IntoMap
    @StringKey(CancelCommand.NAME)
    public ButtonHandler provideCancelCommandClickHandler(CancelCommand cancelCommand) {
        return cancelCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ListCommand.NAME)
    public SlashCommandHandler provideListCommand(ListCommand listCommand) {
        return listCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ListCommand.NAME)
    public ButtonHandler provideListCommandClickHandler(ListCommand listCommand) {
        return listCommand;
    }

    @Provides
    @IntoMap
    @StringKey(ProcessCommand.NAME)
    public SlashCommandHandler provideProcessCommand(ProcessCommand processCommand) {
        return processCommand;
    }

    @Provides
    @IntoMap
    @StringKey(QuestionsCommand.NAME)
    public ButtonHandler provideQuestionsButtonCommandClickHandler(
            QuestionsCommand questionsCommand) {
        return questionsCommand;
    }

    @Provides
    @IntoMap
    @StringKey(NotesCommand.NAME)
    public SlashCommandHandler provideStartCommand(NotesCommand notesCommand) {
        return notesCommand;
    }

    @Provides
    @IntoMap
    @StringKey(NotesCommand.NAME)
    public ButtonHandler provideNotesButtonCommandClickHandler(NotesCommand notesCommand) {
        return notesCommand;
    }

    @Provides
    @IntoMap
    @StringKey(NotesCommand.NAME)
    public ModalHandler provideNotesModalHandler(NotesCommand notesCommand) {
        return notesCommand;
    }

    @Provides
    @IntoMap
    @StringKey(SetNotificationCommand.NAME)
    public SlashCommandHandler provideSetNotificationCommand(
            SetNotificationCommand setNotificationCommand) {
        return setNotificationCommand;
    }

    @Provides
    @IntoMap
    @StringKey(SetNotificationCommand.NAME)
    public ButtonHandler provideSetNotificationButtonCommandClickHandler(
            SetNotificationCommand setNotificationCommand) {
        return setNotificationCommand;
    }

    @Provides
    @IntoMap
    @StringKey(CancelNotificationCommand.NAME)
    public SlashCommandHandler provideCancelNotificationCommand(
            CancelNotificationCommand cancelNotificationCommand) {
        return cancelNotificationCommand;
    }

    @Provides
    @IntoMap
    @StringKey(CancelNotificationCommand.NAME)
    public ButtonHandler provideCancelNotificationButtonCommandClickHandler(
            CancelNotificationCommand cancelNotificationCommand) {
        return cancelNotificationCommand;
    }
}
