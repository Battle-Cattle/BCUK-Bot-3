package com.expiredminotaur.bcukbot.web.view.commands;

import com.expiredminotaur.bcukbot.sql.sfx.SFXCategory;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategoryRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTrigger;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTriggerRepository;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "sfx")
public class SFXView extends VerticalLayout
{
    private final ComboBox<String> category = new ComboBox<>("Category");
    private final TextField search = new TextField("Search");
    private final Grid<SFXTrigger> sfx = new Grid<>(SFXTrigger.class);
    private final SFXTriggerRepository sfxTriggerRepository;
    private final SFXCategoryRepository sfxCategoryRepository;

    public SFXView(@Autowired SFXCategoryRepository sfxCategoryRepository, @Autowired SFXTriggerRepository sfxTriggerRepository)
    {
        this.sfxCategoryRepository = sfxCategoryRepository;
        this.sfxTriggerRepository = sfxTriggerRepository;
        addClassName("sfx-view");
        setSizeFull();
        H1 header = new H1("BCUK BOT Sound Effects");
        header.addClassName("header");

        FlexLayout filters = new FlexLayout();
        filters.setWidthFull();
        filters.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        filters.add(category, search);
        filters.getChildren().map(HasStyle.class::cast).forEach(child -> child.addClassName("filter"));

        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("General");
        categories.addAll(sfxCategoryRepository.findAll().stream().map(SFXCategory::getName).toList());
        category.setItems(categories);
        category.setValue("All");
        category.addValueChangeListener(e -> updateList());
        search.addValueChangeListener(e -> updateList());
        search.setValueChangeMode(ValueChangeMode.EAGER);
        sfx.setColumns("trigger", "description");
        Grid.Column<SFXTrigger> trigCol = sfx.getColumnByKey("trigger");
        trigCol.setComparator(Comparator.comparing(SFXTrigger::getTrigger, String.CASE_INSENSITIVE_ORDER));
        sfx.getColumnByKey("description")
                .setComparator(Comparator.comparing(SFXTrigger::getDescription, String.CASE_INSENSITIVE_ORDER));
        sfx.sort(Collections.singletonList(new GridSortOrder<>(trigCol, SortDirection.ASCENDING)));

        updateList();

        add(header, filters, sfx);
    }

    private void updateList()
    {
        String catName = category.getValue();
        Collection<SFXTrigger> sfxList = switch (catName)
        {
            case "All" -> sfxTriggerRepository.findAll();
            case "General" -> sfxTriggerRepository.findByCategoryIsNull();
            default -> sfxCategoryRepository.getSFXCategoryByNameIgnoreCase(catName).getTriggers();
        };

        Set<SFXTrigger> triggers = sfxList.stream().filter(s -> !s.isHidden()) //Hide hidden SFX//Convert to trigger strings
                .filter(t -> search.isEmpty() || t.getTrigger().toLowerCase().contains(search.getValue().toLowerCase())) //Filter by search
                .collect(Collectors.toSet());

        sfx.setItems(triggers);
    }
}