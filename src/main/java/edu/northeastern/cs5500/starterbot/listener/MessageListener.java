package edu.northeastern.cs5500.starterbot.listener;

import edu.northeastern.cs5500.starterbot.command.ButtonHandler;
import edu.northeastern.cs5500.starterbot.command.ModalHandler;
import edu.northeastern.cs5500.starterbot.command.SlashCommandHandler;
import edu.northeastern.cs5500.starterbot.command.StringSelectHandler;
import edu.northeastern.cs5500.starterbot.exception.ButtonNotFoundException;
import edu.northeastern.cs5500.starterbot.exception.CommandNotFoundException;
import edu.northeastern.cs5500.starterbot.exception.StringSelectNotFoundException;
import edu.northeastern.cs5500.starterbot.service.OpenTelemetryService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

@Slf4j
public class MessageListener extends ListenerAdapter {

    @Inject Map<String, Provider<SlashCommandHandler>> commands;
    @Inject Map<String, Provider<ButtonHandler>> buttons;
    @Inject Map<String, Provider<StringSelectHandler>> stringSelects;
    @Inject Map<String, Provider<ModalHandler>> modals;

    @Inject OpenTelemetryService openTelemetryService;

    @Inject
    public MessageListener() {
        super();
    }

    @Override
    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        var name = event.getName();
        Span span = openTelemetryService.span(name);

        try (Scope scope = span.makeCurrent()) {
            for (Entry<String, Provider<SlashCommandHandler>> entry : commands.entrySet()) {
                if (entry.getKey().equals(name)) {
                    entry.getValue().get().onSlashCommandInteraction(event);
                    span.end();
                    return;
                }
            }

            throw new CommandNotFoundException(name);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            log.error("onSlashCommandInteraction failed", e);
        } finally {
            span.end();
        }
    }

    public @Nonnull Collection<CommandData> allCommandData() {
        Collection<CommandData> commandData =
                commands.values().stream()
                        .map(Provider<SlashCommandHandler>::get)
                        .map(SlashCommandHandler::getCommandData)
                        .collect(Collectors.toList());
        if (commandData == null) {
            return new ArrayList<>();
        }
        return commandData;
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        log.info("onButtonInteraction: {}", event.getButton().getId());
        String id = event.getButton().getId();
        Objects.requireNonNull(id);
        String handlerName = id.split(":")[0];
        log.info("id {}", id);

        Span span = openTelemetryService.span(handlerName);

        try (Scope scope = span.makeCurrent()) {
            for (Entry<String, Provider<ButtonHandler>> entry : buttons.entrySet()) {
                if (entry.getKey().equals(handlerName)) {
                    log.info(" {} is picked", handlerName);

                    entry.getValue().get().onButtonInteraction(event);
                    span.end();
                    return;
                }
            }

            throw new ButtonNotFoundException(handlerName);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            log.error("onButtonInteraction failed", e);
        } finally {
            span.end();
        }
    }

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        log.info("onStringSelectInteraction: {}", event.getComponent().getId());
        String handlerName = event.getComponent().getId();

        Span span = openTelemetryService.span(handlerName);

        try (Scope scope = span.makeCurrent()) {
            for (Entry<String, Provider<StringSelectHandler>> entry : stringSelects.entrySet()) {
                if (entry.getKey().equals(handlerName)) {
                    entry.getValue().get().onStringSelectInteraction(event);
                    return;
                }
            }

            throw new StringSelectNotFoundException(handlerName);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            log.error("onStringSelectInteraction failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * This method is called when a modal interaction event is received. It finds the appropriate
     * handler for the modal and calls the handler's onModalInteraction method.
     *
     * @param event ModalInteractionEvent
     * @return void
     */
    @Override
    public void onModalInteraction(@Nonnull ModalInteractionEvent event) {
        // Log the interaction
        log.info("onModalInteraction: {}", event.getModalId());
        String modelId = event.getModalId();
        String handlerName = modelId.split(":", 3)[0];

        // Begin a span for telemetry if necessary
        Span span = openTelemetryService.span("onModalInteraction");

        try (Scope scope = span.makeCurrent()) {
            // Find the handler for the modal
            for (Entry<String, Provider<ModalHandler>> entry : modals.entrySet()) {
                if (entry.getKey().equals(handlerName)) {
                    // Call the handler
                    entry.getValue().get().onModalInteraction(event);
                    return;
                }
            }

            // If no handler is found, throw an exception
            throw new ModalInteractionNotFoundException(event.getModalId());
        } catch (Exception e) {
            // Record the exception in telemetry and log it
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            log.error("onModalInteraction failed", e);

            // Provide a reply in case of failure
            event.reply("An error occurred while processing your note.").setEphemeral(true).queue();
        } finally {
            // End the span
            span.end();
        }
    }

    // An exception class for not found modal interactions
    public class ModalInteractionNotFoundException extends Exception {
        public ModalInteractionNotFoundException(String modalId) {
            super("Modal interaction not found: " + modalId);
        }
    }
}
