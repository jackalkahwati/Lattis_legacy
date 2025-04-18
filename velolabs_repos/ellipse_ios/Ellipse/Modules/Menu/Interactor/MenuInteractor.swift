//
//  MenuMenuInteractor.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 11/10/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Oval

class MenuInteractor {
    weak var view: MenuInteractorOutput!
    var router: MenuRouter!
    weak var terms: TermsInteractorOutput? {
        didSet {
            guard let view = terms else { return }
            errorHandler = ErrorHandler(view)
        }
    }
    
    fileprivate let source: [MenuItem] = [.home, .ellipses, .find, .sharing, .emergency, .help, .order]
    fileprivate var network: UserNetwork = Session.shared
    fileprivate var errorHandler: ErrorHandler?
}

extension MenuInteractor: MenuInteractorInput {
    var numberOfSections: Int {
        return 1
    }
    
    func numberOfRows(in section: Int) -> Int {
        return source.count
    }
    
    func item(for indexPath: IndexPath) -> MenuItem {
        return source[indexPath.row]
    }
    
    func didSelect(item: MenuItem) {
        router.open(item)
    }
    
    func logOut() {
        AppDelegate.shared.logOut()
    }
}

extension MenuInteractor: TermsAndConditionsDelegate {
    func getTermsAndConditions() {
        guard terms != nil else { return }
        terms?.startLoading(text: "loading_terms".localized())
        network.getTermsAndConditions { [weak self] result in
            switch result {
            case .success(let version, let body):
                self?.terms?.stopLoading(completion: nil)
                self?.terms?.showTermsAndConditions(header: version, body: body)
            case .failure(let error):
                self?.errorHandler?.handle(error: error)
            }
        }
    }
    
    func acceptTermsAndConditions(_ isAccepted: Bool) {
        
    }
}
