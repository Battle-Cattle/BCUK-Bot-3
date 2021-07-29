package com.expiredminotaur.bcukbot.web.security;

import com.expiredminotaur.bcukbot.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AccessLevel
{
    Role value() default Role.USER;
}
