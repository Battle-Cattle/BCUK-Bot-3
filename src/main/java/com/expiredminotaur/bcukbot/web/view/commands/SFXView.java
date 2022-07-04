package com.expiredminotaur.bcukbot.web.view.commands;

import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategory;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategoryRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXRepository;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Route(value = "sfx")
public class SFXView extends VerticalLayout
{
    private final ComboBox<String> category = new ComboBox<>("Category");

    private final TextField search = new TextField("Search");
    private final Grid<String> sfx = new Grid<>();
    private final SFXRepository sfxRepository;
    private final SFXCategoryRepository sfxCategoryRepository;

    public SFXView(@Autowired SFXCategoryRepository sfxCategoryRepository, @Autowired SFXRepository sfxRepository)
    {
        this.sfxCategoryRepository = sfxCategoryRepository;
        this.sfxRepository = sfxRepository;
        addClassName("sfx-view");
        setSizeFull();
        H1 header = new H1("BCUK BOT Sound Effects");
        header.addClassName("header");

        FlexLayout filters = new FlexLayout();
        filters.setWidthFull();
        filters.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        filters.add(category, search);
        filters.getChildren().filter(HasStyle.class::isInstance).map(HasStyle.class::cast).forEach(child -> child.addClassName("filter"));

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("General");
        categories.addAll(sfxCategoryRepository.findAll().stream().map(SFXCategory::getName).collect(Collectors.toList()));
        category.setItems(categories);
        category.setValue("All");
        category.addValueChangeListener(e -> updateList());
        search.addValueChangeListener(e -> updateList());
        search.setValueChangeMode(ValueChangeMode.EAGER);
        sfx.addColumn(String::toString);
        updateList();

        add(header, filters, sfx);
    }

    private void updateList()
    {
        String catName = category.getValue();
        Collection<SFX> sfxList;

        switch (catName)
        {
            case "All":
                sfxList = sfxRepository.findAll();
                break;
            case "General":
                sfxList = sfxRepository.findByCategoryIsNull();
                break;
            default:
                sfxList = sfxCategoryRepository.getSFXCategoryByNameIgnoreCase(catName).getSfx();
                break;
        }

        Set<String> triggers = sfxList.stream().filter(s -> !s.isHidden()) //Hide hidden SFX
                .map(SFX::getTriggerCommand) //Convert to trigger strings
                .filter(t -> search.isEmpty() || t.toLowerCase().contains(search.getValue().toLowerCase())) //Filter by search
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        sfx.setItems(triggers);
    }
}