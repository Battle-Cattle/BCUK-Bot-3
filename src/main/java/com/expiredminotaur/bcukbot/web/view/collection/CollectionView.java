package com.expiredminotaur.bcukbot.web.view.collection;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.collection.CollectionService;
import com.expiredminotaur.bcukbot.web.component.Form;
import com.expiredminotaur.bcukbot.web.security.UserTools;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;

public abstract class CollectionView<T> extends VerticalLayout
{
    private final Grid<T> grid;
    private final UserTools userTools;
    private final CollectionService<T> service;
    private final Class<T> type;
    private EditForm editForm;

    public CollectionView(UserTools userTools, CollectionService<T> service, Class<T> type)
    {
        this.userTools = userTools;
        this.service = service;
        this.grid = new Grid<>(type);
        this.type = type;
    }

    protected void setup(String title, String dataField, String label)
    {
        this.editForm = new EditForm(type, dataField, label);
        setSizeFull();
        H2 header = new H2(title);
        grid.setColumns("id", dataField, "source", "date");
        grid.setSizeFull();

        if (userTools.hasAccess(Role.MOD))
        {
            grid.addColumn(new ComponentRenderer<>(data -> new Button("Edit", e -> editForm.open(data))))
                    .setHeader("Edit")
                    .setFlexGrow(0);
        }

        grid.getColumns().forEach(c -> c.setAutoWidth(true));
        grid.recalculateColumnWidths();

        grid.setDataProvider(DataProvider.fromCallbacks(
                query -> service.findAll(
                        query.getOffset(), query.getLimit()),
                query -> (int) service.count()));

        add(header, grid);
    }

    private class EditForm extends Form<T>
    {
        public EditForm(Class<T> type, String dataField, String label)
        {
            super(type);
            addField(label, new TextField(), dataField).setWidthFull();
        }

        @Override
        protected void saveData(T data)
        {
            service.save(data);
            grid.getDataProvider().refreshItem(data);
        }
    }
}
