package com.expiredminotaur.bcukbot;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@Push
@EnableCaching
@EnableScheduling
@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@Theme(value = "bcuk-bot", variant = Lumo.DARK)
public class Application extends SpringBootServletInitializer implements AppShellConfigurator
{
    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }
}
