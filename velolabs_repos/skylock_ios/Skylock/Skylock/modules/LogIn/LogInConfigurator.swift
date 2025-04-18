//
//  LogInConfigurator.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 22/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

final class LogInConfigurator {
    private weak var view: LogInInteractorOutput?
    init(_ view: LogInInteractorOutput) {
        self.view = view
        view.interactor = LogInInteractor()
        view.interactor.view = view
        view.interactor.router = LogInRouter(view as! UIViewController)
    }
    
    var configure: ((LogInInteractor) -> ())? {
        didSet {
            guard let interactor = view?.interactor as? LogInInteractor else { return }
            configure?(interactor)
        }
    }
}
