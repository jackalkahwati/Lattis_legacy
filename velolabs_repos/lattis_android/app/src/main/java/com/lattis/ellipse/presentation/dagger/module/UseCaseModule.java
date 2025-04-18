package com.lattis.ellipse.presentation.dagger.module;

import com.lattis.ellipse.domain.interactor.authentication.SignInUseCase;
import com.lattis.ellipse.domain.interactor.authentication.SignUpUseCase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class UseCaseModule {

    @Provides
    @Singleton
    SignInUseCase provideSignInUseCase(SignInUseCase useCase){
        return useCase;
    }

    @Provides
    @Singleton
    SignUpUseCase provideSignUpUseCase(SignUpUseCase useCase){
        return useCase;
    }
}
