package com.expiredminotaur.bcukbot.web.view.stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.atmosphere.cpr.Broadcaster;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Route("/stream")
public class StreamView extends HorizontalLayout
{
    private final Paragraph message = new Paragraph();
    private Registration broadcasterRegistration;

    public StreamView()
    {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        message.getStyle().set("font-size", "10em")
                .set("text-align", "center");
        message.setWidthFull();
        add(message);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent)
    {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Service.register(newMessage ->
                ui.access(() -> message.setText(newMessage)));
        ui.access(() -> message.setText(Service.getLastMessage()));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent)
    {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    public static class Service
    {
        private static final Executor executor = Executors.newSingleThreadExecutor();

        private static final LinkedList<Consumer<String>> listeners = new LinkedList<>();

        private static String lastMessage;

        public static synchronized Registration register(
                Consumer<String> listener)
        {
            listeners.add(listener);

            return () ->
            {
                synchronized (Broadcaster.class)
                {
                    listeners.remove(listener);
                }
            };
        }

        public static synchronized void broadcast(String message)
        {
            try
            {
                for (int i = 0; i <= message.length(); i++)
                {
                    String sendMessage = message.substring(0, i);
                    lastMessage = sendMessage;
                    for (Consumer<String> listener : listeners)
                    {
                        executor.execute(() -> listener.accept(sendMessage));
                    }
                    Thread.sleep(100);
                }
                Thread.sleep(400);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        public static String getLastMessage()
        {
            return lastMessage;
        }
    }
}