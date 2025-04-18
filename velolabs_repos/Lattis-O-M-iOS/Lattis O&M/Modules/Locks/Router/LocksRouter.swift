//
//  LocksLocksRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Localize_Swift
import QRCodeReader
import AVFoundation

final class LocksRouter: BaseRouter {
    class func instantiate(with delegate: LocksInteractorDelegate, dashboard: UIViewController) -> LocksViewController {
        let controller = UIStoryboard.dashboard.instantiateViewController(withIdentifier: "locks") as! LocksViewController
        controller.title = "locks_title".localized()
        let interactor = inject(controller: controller)
        interactor.dashboard = dashboard
        interactor.delegate = delegate
        return controller
    }
    
    func openQRScanner(with delegate: QRCodeReaderViewControllerDelegate, in controller: UIViewController, completion: @escaping () -> ()) {
        let view = QRReaderView(label: "qr_scanner_connect_button".localized())
        let builder = QRCodeReaderViewControllerBuilder {
            view.translatesAutoresizingMaskIntoConstraints = false
            $0.readerView = QRCodeReaderContainer(displayable: view)
        }
        view.useBlock = {
            self.controller.dismiss(animated: true, completion: completion)
        }
        let scanner = QRCodeReaderViewController(builder: builder)
        scanner.delegate = delegate
        scanner.codeReader.stopScanningWhenCodeIsFound = false
        scanner.completionBlock = view.display(result:)
        controller.present(scanner, animated: true, completion: nil)
    }
    
    func openFilter(with delegate: LocksFilterSectionDelegate, and filter: Lock.Filter) {
        let filterVC = LocksFilterViewController(delegate: delegate, filter: filter, vendor: .ellipse)
        let navigation = UINavigationController(rootViewController: filterVC, style: .blue)
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func openSearch(locks: [Lock], delegate: LocksSearchDelegate) {
        let search = LocksSearchViewController(locks: locks, delegate: delegate)
        let navigation = UINavigationController(rootViewController: search, style: .blue)
        controller.present(navigation, animated: true, completion: nil)
    }
    
    func openOnboarding(locks: [Ellipse], callback: @escaping (Peripheral) -> ()) {
        let onboarding = LockOnboardingViewController(.init(locks, onboard: callback))
        let navigation = UINavigationController(rootViewController: onboarding, style: .blue)
        controller.present(navigation, animated: true, completion: nil)
    }
}

private func inject(controller: LocksViewController) -> LocksInteractor {
    let interactor = LocksInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = LocksRouter(controller)
    return interactor
}
