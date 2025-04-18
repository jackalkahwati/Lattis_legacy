package com.lattis.ellipse.presentation.dagger.qualifier;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by ssd3 on 9/6/17.
 */

@Qualifier
@Retention(RUNTIME)
public @interface DeviceModel {}
