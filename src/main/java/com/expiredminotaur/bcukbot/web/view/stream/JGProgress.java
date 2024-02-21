package com.expiredminotaur.bcukbot.web.view.stream;

import com.expiredminotaur.bcukbot.justgiving.JustGivingEventHandler;
import com.expiredminotaur.bcukbot.justgiving.JustGivingProgressData;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;

@Route("/progress")
@StyleSheet("./jgprogress-view.css")
public class JGProgress extends VerticalLayout
{
    private Registration broadcasterRegistration;

    @Autowired
    JustGivingEventHandler eventHandler;

    private final Div progressBarText = new Div();
    private final Div progress = new Div();
    private final Div target = new Div();
    private final HorizontalLayout footer = new HorizontalLayout();

    public JGProgress()
    {
        setWidthFull();
        Div progressBarContainer = new Div();
        progressBarContainer.setWidthFull();

        Div progressBarOuter = new Div();
        progressBarOuter.setWidthFull();
        progressBarOuter.getStyle()
                .setBackgroundColor("rgb(51, 51, 51)").setMargin("10px auto 0px auto");

        setupProgressText();
        setupProgressBar();
        progressBarOuter.add(progressBarText, progress);
        setupFooter();

        progressBarContainer.add(progressBarOuter, footer);
        add(progressBarContainer);
    }

    private void setupProgressText()
    {
        progressBarText.getStyle()
                .setColor("rgb(255, 255, 255)")
                .setFontSize("24px")
                .setLineHeight("50px")
                .setTextAlign(Style.TextAlign.CENTER)
                .setPosition(Style.Position.ABSOLUTE);
        progressBarText.setWidthFull();
        progressBarText.setHeight("50px");
    }

    private void setupProgressBar()
    {
        progress.getStyle()
                .setBackgroundColor("rgb(229, 116, 20)");
        progress.setHeight("50px");
    }

    private void setupFooter()
    {
        Div spacer = new Div();
        target.getStyle()
                .setColor("rgb(210, 16, 16)")
                .setFontSize("18px")
                .set("text-shadow", "rgb(0, 0, 0) 0px 0px 1px");
        footer.add(spacer, target);
        footer.setWidthFull();
        footer.setFlexGrow(1, spacer);
        footer.getStyle().setPadding("5px 20px");
    }

    private void updateProgress(JustGivingProgressData data)
    {
        String totalString = String.format("£%.2f", data.getTotal());
        String targetString = String.format("£%.2f", data.getTarget());
        String progressString;
        if (data.getPercentage() < 100)
        {
            progressString = String.format("%.2f%%", data.getPercentage());
        } else
        {
            progressString = "100%";
        }

        progressBarText.setText(String.format("%s (%s)", totalString, progressString));
        target.setText(targetString);
        progress.setWidth(progressString);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent)
    {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = eventHandler.register(data ->
                ui.access(() -> updateProgress(data)));
        ui.access(() -> updateProgress(eventHandler.getLatestData()));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent)
    {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }
}
