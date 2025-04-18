//
//  SettingsSettingsRouter.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 18/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import QRCodeReader
import AVFoundation

final class SettingsRouter: BaseRouter {
    class func instantiate(with lock: Lock) -> SettingsViewController {
        let settings = UIStoryboard.settings.instantiateViewController(withIdentifier: "settings") as! SettingsViewController
        let interactor = inject(controller: settings)
        interactor.lock = lock
        return settings
    }
    
    func openQRScanner(with delegate: QRCodeReaderViewControllerDelegate, completion: @escaping () -> ()) {
        let view = QRReaderView()
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
    
    func dispatch(for lock: Lock) {
        let dispatch = DispatchRouter.instantiate(with: lock)
        controller.navigationController?.pushViewController(dispatch, animated: true)
    }
}

private func inject(controller: SettingsViewController) -> SettingsInteractor {
    let interactor = SettingsInteractor()
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = SettingsRouter(controller)
    return interactor
}
