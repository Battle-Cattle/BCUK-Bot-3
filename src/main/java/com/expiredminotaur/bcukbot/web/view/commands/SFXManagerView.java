package com.expiredminotaur.bcukbot.web.view.commands;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategory;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategoryRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTrigger;
import com.expiredminotaur.bcukbot.sql.sfx.SFXTriggerRepository;
import com.expiredminotaur.bcukbot.web.component.Form;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Route(value = "sfx_manager", layout = MainLayout.class)
@AccessLevel(Role.MOD)
public class SFXManagerView extends HorizontalLayout
{
    private static final SFXCategory NO_CATEGORY = new SFXCategory("Uncategorised");
    private final Logger log = LoggerFactory.getLogger(SFXManagerView.class);
    private final File folder = new File("sfx");
    private final ComboBox<SFXCategory> categoryFilter = new ComboBox<>("Filter by Category");
    private final Grid<SFXTrigger> sfxCommandGrid = new Grid<>(SFXTrigger.class);
    private final SfxTriggerForm sfxTriggerForm = new SfxTriggerForm();
    private final SFXTriggerRepository sfxCommands;
    private final SFXCategoryRepository sfxCategories;
    private  final SFXRepository sfxRepository;

    public SFXManagerView(@Autowired SFXTriggerRepository sfxCommands, @Autowired SFXCategoryRepository sfxCategories, @Autowired SFXRepository sfxRepository)
    {
        this.sfxCommands = sfxCommands;
        this.sfxCategories = sfxCategories;
        this.sfxRepository = sfxRepository;
        setSizeFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        Text message = new Text("");

        if (!folder.exists())
        {
            if (!folder.mkdir())
            {
                add(new H1("SFX Folder is missing, contact Admin"));
                return;
            }
        }

        upload.setAcceptedFileTypes(".mp3", ".flac", ".wav", ".mp4", ".m4a", ".ogg", ".aac", ".opus");
        upload.addStartedListener(event -> message.setText(""));
        upload.addFileRejectedListener(event -> message.setText(event.getErrorMessage()));

        Grid<String> fileList = new Grid<>();

        upload.addSucceededListener(event ->
        {
            File targetFile = new File("sfx/" + event.getFileName());
            try (OutputStream outStream = new FileOutputStream(targetFile))
            {
                InputStream initialStream = buffer.getInputStream();
                byte[] byteBuffer = new byte[initialStream.available()];
                if (initialStream.read(byteBuffer) > 0)
                {
                    outStream.write(byteBuffer);
                    upload.getElement().setPropertyJson("files", Json.createArray());
                    fileList.setItems(folder.list());
                } else
                {
                    message.setText("Error uploading file");
                }
            } catch (IOException e)
            {
                log.error("Error reading SFX upload", e);
                message.setText("Error uploading file");
            }
        });

        Grid.Column<String> column = fileList.addColumn(s -> s);
        column.setHeader("Files");
        fileList.setItems(folder.list());

        VerticalLayout fileManagerLayout = new VerticalLayout();
        fileManagerLayout.add(upload, message, fileList);

        sfxCommandGrid.setColumns("trigger", "description", "hidden");
        sfxCommandGrid.getColumnByKey("trigger")
                .setComparator(Comparator.comparing(SFXTrigger::getTrigger, String.CASE_INSENSITIVE_ORDER));
        sfxCommandGrid.addColumn(this::getCategory).setHeader("Category")
                .setComparator(Comparator.comparing(this::getCategory, String.CASE_INSENSITIVE_ORDER));
        sfxCommandGrid.getColumns().forEach(c -> c.setResizable(true));
        sfxCommandGrid.addColumn(new ComponentRenderer<>(sfx -> new Button("Edit", e -> sfxTriggerForm.open(sfx))))
                .setHeader("Edit")
                .setAutoWidth(true)
                .setFlexGrow(0);
        sfxCommandGrid.setItemDetailsRenderer(createSFXDetailsRenderer());
        Grid.Column<SFXTrigger> trigCol = sfxCommandGrid.getColumnByKey("trigger");
        sfxCommandGrid.sort(Collections.singletonList(new GridSortOrder<>(trigCol, SortDirection.ASCENDING)));

        updateCategoryFilter();
        categoryFilter.setItemLabelGenerator(SFXCategory::getName);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> updateGrid());

        updateGrid();

        SFXCategoryForm categoryForm = new SFXCategoryForm();

        Button addTriggerButton = new Button("Add Trigger", e -> sfxTriggerForm.open(new SFXTrigger()));
        Button addCategoryButton = new Button("Add Category", e -> categoryForm.open(new SFXCategory()));

        HorizontalLayout buttons = new HorizontalLayout(addTriggerButton, addCategoryButton);

        VerticalLayout commandManagerLayout = new VerticalLayout();
        commandManagerLayout.add(buttons, categoryFilter, sfxCommandGrid);

        add(fileManagerLayout, commandManagerLayout);
        setFlexGrow(0, fileManagerLayout);
        setFlexGrow(1, commandManagerLayout);
    }

    private ComponentRenderer<SFXDetailsLayout, SFXTrigger> createSFXDetailsRenderer()
    {
        return new ComponentRenderer<>(SFXDetailsLayout::new, SFXDetailsLayout::setTrigger);
    }

    private void updateGrid()
    {
        SFXCategory sfxCategory = categoryFilter.getValue();
        List<SFXTrigger> items;
        if (sfxCategory == null)
            items = sfxCommands.findAll();
        else if (sfxCategory == NO_CATEGORY)
            items = sfxCommands.findByCategoryIsNull();
        else
            items = sfxCommands.findByCategory(categoryFilter.getValue());
        sfxCommandGrid.setItems(items);
    }

    private void updateCategoryFilter()
    {
        List<SFXCategory> items = new ArrayList<>();
        items.add(NO_CATEGORY);
        items.addAll(sfxCategories.findAll());

        categoryFilter.setItems(items);
    }

    private String getCategory(SFXTrigger sfx)
    {
        SFXCategory category = sfx.getCategory();
        if (category == null)
            return NO_CATEGORY.getName();
        return category.getName();
    }

    private static class SFXDetailsLayout extends VerticalLayout
    {
        private final Grid<SFX> sfxGrid = new Grid<>(SFX.class);

        public SFXDetailsLayout()
        {
            sfxGrid.setColumns("file", "weight");
            add(sfxGrid);
        }

        public void setTrigger(SFXTrigger trigger)
        {
            sfxGrid.setItems(trigger.getSfxSet());
        }
    }

    private class SfxTriggerForm extends Form<SFXTrigger>
    {
        private final ComboBox<SFXCategory> category;
        private final Grid<SFX> sfxGrid = new Grid<>(SFX.class);
        private SFXTrigger trigger;

        public SfxTriggerForm()
        {
            super(SFXTrigger.class);

            addField("Trigger Command", new TextField(), "trigger");
            addField("Description", new TextField(), "description").setWidthFull();
            addField("Hidden", new Checkbox(), "hidden");
            category = addField("Category", new ComboBox<>(), "category");
            category.setItemLabelGenerator(SFXCategory::getName);
            category.setClearButtonVisible(true);
            sfxGrid.setColumns("file", "weight");
            SfxForm sfxFrom = new SfxForm();
            sfxGrid.addColumn(new ComponentRenderer<>(sfx -> new Button("Edit", e -> sfxFrom.open(sfx))))
                    .setHeader("Edit")
                    .setAutoWidth(true)
                    .setFlexGrow(0);
            Grid.Column<SFX> fileCol = sfxGrid.getColumnByKey("file");
            sfxGrid.sort(Collections.singletonList(new GridSortOrder<>(fileCol, SortDirection.ASCENDING)));
            addExtraComponent(new Button("Add SFX", e -> sfxFrom.open(new SFX(trigger))));
            addExtraComponent(sfxGrid);
        }

        @Override
        public void open(SFXTrigger data)
        {
            trigger = data;
            category.setItems(sfxCategories.findAll());
            sfxGrid.setItems(data.getSfxSet());
            sfxGrid.recalculateColumnWidths();
            super.open(data);
        }

        @Override
        protected void saveData(SFXTrigger data)
        {
            sfxCommands.save(data);
            updateGrid();
        }

        public void updateTriggerGrid()
        {
            sfxGrid.setItems(sfxRepository.getByTriggerId(trigger.getId()));
        }
    }

    private class SfxForm extends Form<SFX>
    {
        private final ComboBox<String> files;

        public SfxForm()
        {
            super(SFX.class);
            files = addField("File", new ComboBox<>(), "file");
            addField("Weight", new TextField(), "weight", new StringToIntegerConverter("Invalid number"));
        }

        @Override
        public void open(SFX data)
        {
            files.setItems(folder.list());
            super.open(data);
        }

        @Override
        protected void saveData(SFX data)
        {
            sfxRepository.save(data);
            sfxTriggerForm.updateTriggerGrid();
        }
    }

    private class SFXCategoryForm extends Form<SFXCategory>
    {
        public SFXCategoryForm()
        {
            super(SFXCategory.class);
            addField("Name", new TextField(), "name").setWidthFull();
        }

        @Override
        protected void saveData(SFXCategory data)
        {
            sfxCategories.save(data);
            updateCategoryFilter();
        }
    }
}
