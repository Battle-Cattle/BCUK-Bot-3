package com.expiredminotaur.bcukbot.justgiving;

import com.vaadin.flow.shared.Registration;
import org.atmosphere.cpr.Broadcaster;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class JustGivingEventHandler
{
    private final Executor executor = Executors.newSingleThreadExecutor();

    private final LinkedList<Consumer<JustGivingProgressData>> listeners = new LinkedList<>();

    private final JustGivingProgressData data = new JustGivingProgressData();

    public synchronized Registration register(
            Consumer<JustGivingProgressData> listener)
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

    public synchronized void update(double total, double target)
    {
        data.setData(total, target);
        for (Consumer<JustGivingProgressData> listener : listeners)
        {
            executor.execute(() -> listener.accept(data));
        }
    }

    public JustGivingProgressData getLatestData()
    {
        return data;
    }
}

