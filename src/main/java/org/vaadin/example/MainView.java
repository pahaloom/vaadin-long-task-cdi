package org.vaadin.example;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The main view contains a simple label element and a template element.
 */
@Route("")
@Push
@PWA(name = "Project Base for Vaadin Flow with CDI", shortName = "Project Base", enableInstallPrompt = false)
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {
    static final Logger LOG = Logger.getLogger(MainView.class.getName());

    @Inject
    private GreetService greetService;

    @Inject
    private TaskBean taskBean;

    Set<TaskPojo> tasks = new LinkedHashSet<>();

    @PostConstruct
    public void init() {
        TextField nameField = new TextField("Your name");
        nameField.addThemeName("bordered");

        Grid<TaskPojo> taskGrid = new Grid<>(TaskPojo.class);
        taskGrid.setItems(tasks);
        taskGrid.setMinHeight(600F, Unit.PIXELS);
        taskGrid.setHeightFull();
        taskGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        ListDataProvider<TaskPojo> tasksDataProvider = DataProvider.ofCollection(tasks);
        taskGrid.setDataProvider(tasksDataProvider);

        // Button click listeners can be defined as lambda expressions
        Button sayButton = new Button("Say hello",
                e -> Notification.show(greetService.greet(nameField.getValue())));

        sayButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);

        UI ui = UI.getCurrent();
        Button scheduleButton = new Button("Schedule hello", e -> {
            TaskPojo tp = new TaskPojo();
            LOG.info("Created: " + tp);
            tasks.add(tp);
            Notification.show("Scheduled: " + tp.getId());
            updateGrid(taskGrid);

            taskBean.longRunningGreeting((String result) -> {
                tp.setResult(result);
                LOG.info("Received: " + tp);
                ui.access(() -> {
                    Notification.show(result);
                    updateGrid(taskGrid);
                });
            }, nameField.getValue());
        });

        Button removeButton = new Button("Remove", e -> {
            tasks.removeAll(taskGrid.getSelectedItems());
            updateGrid(taskGrid);
        });

        scheduleButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        scheduleButton.addClickShortcut(Key.ENTER);
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(nameField, new HorizontalLayout(sayButton, scheduleButton), taskGrid, new HorizontalLayout(removeButton));
        setMinHeight(800F, Unit.PIXELS);
        setHeightFull();
        setAlignItems(Alignment.CENTER);
    }

    private void updateGrid(Grid<TaskPojo> taskGrid) {
        taskGrid.getDataProvider().refreshAll();
    }
}
