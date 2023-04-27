package com.expiredminotaur.bcukbot.web.component;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public abstract class Form<T>
{
    final FormLayout layout = new FormLayout();
    final Binder<T> binder;

    final List<Component> components = new ArrayList<>();

    public Form(Class<T> type)
    {
        binder = new Binder<>(type);
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
    }

    protected <F extends AbstractField<?, S>, S> F addField(String label, F component, String field)
    {
        return addField(label, component, field, null);
    }

    protected <F extends AbstractField<?, S>, S> F addField(String label, F component, String field, Converter<S, ?> converter)
    {
        layout.addFormItem(component, label);
        Binder.BindingBuilder<T, S> b = binder.forField(component);
        if (converter != null)
            b.withConverter(converter);
        b.bind(field);
        return component;
    }

    protected <C extends Component> C addExtraComponent(C component)
    {
        components.add(component);
        return component;
    }

    public void open(T data)
    {
        binder.readBean(data);
        Dialog dialog = new Dialog();
        dialog.setWidth("60%");
        dialog.setCloseOnOutsideClick(false);
        dialog.add(layout);
        dialog.add(components);
        dialog.add(createButtons(data, dialog));
        dialog.open();
    }

    private HorizontalLayout createButtons(T data, Dialog dialog)
    {
        HorizontalLayout buttons = new HorizontalLayout();
        Button save = new Button("Save", e -> save(data, dialog));
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", e -> dialog.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        buttons.add(save, cancel);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return buttons;
    }

    private void save(T data, Dialog dialog)
    {
        try
        {
            binder.writeBean(data);
            saveData(data);
            dialog.close();

        } catch (ValidationException ex)
        {
            ex.printStackTrace();
        }
    }

    abstract protected void saveData(T data);
}
