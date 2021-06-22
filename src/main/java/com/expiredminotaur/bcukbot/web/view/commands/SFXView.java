package com.expiredminotaur.bcukbot.web.view.commands;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.sfx.SFX;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategory;
import com.expiredminotaur.bcukbot.sql.sfx.SFXCategoryRepository;
import com.expiredminotaur.bcukbot.sql.sfx.SFXRepository;
import com.expiredminotaur.bcukbot.web.component.Form;
import com.expiredminotaur.bcukbot.web.layout.MainLayout;
import com.expiredminotaur.bcukbot.web.security.AccessLevel;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
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
import java.util.Comparator;
import java.util.List;

@Route(value = "sfx", layout = MainLayout.class)
@AccessLevel(Role.MOD)
public class SFXView extends HorizontalLayout
{
    private static final SFXCategory NO_CATEGORY = new SFXCategory("Uncategorised");
    private final Logger log = LoggerFactory.getLogger(SFXView.class);
    private final File folder = new File("sfx");
    private final ComboBox<SFXCategory> categoryFilter = new ComboBox<>("Filter by Category");
    private final Grid<SFX> sfxCommandGrid = new Grid<>(SFX.class);
    private final SFXRepository sfxCommands;
    private final SFXCategoryRepository sfxCategories;

    public SFXView(@Autowired SFXRepository sfxCommands, @Autowired SFXCategoryRepository sfxCategories)
    {
        this.sfxCommands = sfxCommands;
        this.sfxCategories = sfxCategories;
        setSizeFull();
        VerticalLayout fileManagerLayout = new VerticalLayout();
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        Text message = new Text("");
        Grid<String> fileList = new Grid<>();
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
        upload.addSucceededListener(event ->
        {
            try
            {
                File targetFile = new File("sfx/" + event.getFileName());
                OutputStream outStream = new FileOutputStream(targetFile);
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
        fileManagerLayout.add(upload, message, fileList);

        VerticalLayout commandManagerLayout = new VerticalLayout();

        SfxForm sfxForm = new SfxForm();
        Button addTriggerButton = new Button("Add Trigger", e -> sfxForm.open(new SFX()));

        sfxCommandGrid.setColumns("triggerCommand", "file", "weight", "hidden");
        sfxCommandGrid.addColumn(this::getCategory).setHeader("Category")
                .setComparator(Comparator.comparing(this::getCategory, String.CASE_INSENSITIVE_ORDER));
        sfxCommandGrid.addColumn(new ComponentRenderer<>(sfx -> new Button("Edit", e -> sfxForm.open(sfx))))
                .setHeader("Edit")
                .setFlexGrow(0);
        sfxCommandGrid.getColumns().forEach(c -> c.setAutoWidth(true));

        updateCategoryFilter();
        categoryFilter.setItemLabelGenerator(SFXCategory::getName);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> updateGrid());

        updateGrid();

        SFXCategoryForm categoryForm = new SFXCategoryForm();
        Button addCategoryButton = new Button("Add Category", e -> categoryForm.open(new SFXCategory()));

        HorizontalLayout buttons = new HorizontalLayout(addTriggerButton, addCategoryButton);
        commandManagerLayout.add(buttons, categoryFilter, sfxCommandGrid);

        add(fileManagerLayout, commandManagerLayout);
        setFlexGrow(0, fileManagerLayout);
        setFlexGrow(1, commandManagerLayout);
    }

    private void updateGrid()
    {
        SFXCategory sfxCategory = categoryFilter.getValue();
        List<SFX> items;
        if (sfxCategory == null)
            items = sfxCommands.findAll();
        else if (sfxCategory == NO_CATEGORY)
            items = sfxCommands.findByCategoryIsNull();
        else
            items = sfxCommands.findByCategory(categoryFilter.getValue());
        sfxCommandGrid.setItems(items);
        sfxCommandGrid.recalculateColumnWidths();
    }

    private void updateCategoryFilter()
    {
        List<SFXCategory> items = new ArrayList<>();
        items.add(NO_CATEGORY);
        items.addAll(sfxCategories.findAll());

        categoryFilter.setItems(items);
    }

    private String getCategory(SFX sfx)
    {
        SFXCategory category = sfx.getCategory();
        if (category == null)
            return NO_CATEGORY.getName();
        return category.getName();
    }

    private class SfxForm extends Form<SFX>
    {
        public SfxForm()
        {
            super(SFX.class);
            addField("Trigger Command", new TextField(), "triggerCommand");
            addField("SFX File", new ComboBox<String>(), "file").setItems(folder.list());
            addField("Weight", new TextField(), "weight", new StringToIntegerConverter("Invalid number"));
            addField("Hidden", new Checkbox(), "hidden");
            ComboBox<SFXCategory> category = addField("Category", new ComboBox<>(), "category");
            category.setItemLabelGenerator(SFXCategory::getName);
            category.setItems(sfxCategories.findAll());
            category.setClearButtonVisible(true);
        }

        @Override
        protected void saveData(SFX data)
        {
            sfxCommands.save(data);
            updateGrid();
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
