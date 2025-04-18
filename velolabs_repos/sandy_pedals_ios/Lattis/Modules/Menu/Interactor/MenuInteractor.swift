//
//  MenuMenuInteractor.swift
//  Lattis
//
//  Created by Ravil Khusainov on 27/02/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import Foundation

class MenuInteractor {
    weak var view: MenuInteractorOutput!
    var router: MenuRouter!
    var bike: Bike?
    
    init() {
        AppRouter.shared.privateNetwork = {
            self.router.openProfile().action = { interactor in
                interactor.addPrivateNetwork()
            }
            self.view.reload(selected: 1)
        }
        
        AppRouter.shared.addPhoneNumber = {
            self.router.openProfile().action = { interactor in
                interactor.addPhoneNumber()
            }
            self.view.reload(selected: 1)
        }
    }
}

extension MenuInteractor: MenuInteractorInput {
    func viewLoaded() {
        view.updateMenu(withDamage: bike != nil)
    }
    
    func home() {
        router.openHome()
    }
    
    func damage() {
        guard let bike = bike else { return view.show(error: MenuError.emptyBike, file: #file, line: #line) }
        router.openDamage(with: bike)
    }
    
    func theft() {
        guard let bike = bike else { return view.show(error: MenuError.emptyBike, file: #file, line: #line) }
        router.openTheft(with: bike)
    }
    
    func profile() {
        router.openProfile()
    }
    
    func billing() {
        router.openBilling()
    }
    
    func history() {
        router.openHistory()
    }
    
    func help() {
        router.openHelp()
    }
}

enum MenuError: Error {
    case emptyBike
}
