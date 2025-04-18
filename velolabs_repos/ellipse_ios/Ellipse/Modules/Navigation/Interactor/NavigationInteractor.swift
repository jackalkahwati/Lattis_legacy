//
//  NavigationNavigationInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 15/11/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import CoreLocation
import Oval

class NavigationInteractor {
    weak var view: NavigationInteractorOutput! {
        didSet {
            errorHandler = NavigationErrorHandler(view)
        }
    }
    var router: NavigationRouter!
    var userLocation: CLLocationCoordinate2D?
    var selected: Ellipse?
    
    fileprivate let storage: EllipseStorage = CoreDataStack.shared
    fileprivate var storageHandler: StorageHandler?
    fileprivate var errorHandler: NavigationErrorHandler!
}

extension NavigationInteractor: NavigationInteractorInput {
    func start() {
        if let ellipse = selected {
            view.addCloseButton()
            view.show(ellipse: ellipse)
            view.show(locks: [ellipse])
        } else {
            storageHandler = storage.ellipses() { [unowned self] locks in
                self.view.show(locks: locks)
                if var selected = self.selected, let idx = locks.firstIndex(where: {$0.lockId == selected.lockId}) {
                    selected = locks[idx]
                    self.view.show(ellipse: selected)
                    self.selected = selected
                }
            }
        }
    }
    
    func select(ellipse: Ellipse) {
        selected = ellipse
        view.show(ellipse: ellipse)
    }
    
    func unselect() {
        selected = nil
    }
    
    func getDirection() {
        guard let ellipse = selected, let start = userLocation else { return }
        let url = "http://maps.apple.com/maps?saddr=\(start.latitude),\(start.longitude)&daddr=\(ellipse.coordinate.latitude),\(ellipse.coordinate.longitude)"
        UIApplication.shared.open(URL(string:url)!, options: [:], completionHandler: nil)
    }
}


