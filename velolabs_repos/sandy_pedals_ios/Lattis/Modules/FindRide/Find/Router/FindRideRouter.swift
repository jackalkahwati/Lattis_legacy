//
//  FindRideRouter.swift
//  Lattis
//
//  Created by Ravil Khusainov on 07/03/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import LGSideMenuController
import QRCodeReader
import Oval
import SafariServices

final class FindRideRouter: BaseRouter {
    fileprivate let delegate = MenuDelegate()
    class func instantiate(network: BikeNetwork = Session.shared, configure: (FindRideInteractor) -> () = {_ in}) -> FindRideViewController {
        let controller = FindRideViewController()
        let interactor = inject(controller: controller, network: network)
        interactor.router.delegate.controller = controller
        configure(interactor)
        return controller
    }
    
    class func menu(configure: (MapViewController) -> () = {_ in}) -> LGSideMenuController {
        var router: FindRideRouter!
        let findRide = FindRideRouter.instantiate() { router = $0.router }
        let map = MapViewController(findRide)
        router.mapNavigation = map
        configure(map)
        let navigation = UINavigationController(rootViewController: map)
        navigation.isNavigationBarHidden = true
        
        let menu = LGSideMenuController(rootViewController: navigation, leftViewController: MenuRouter.instantiate() { $0.router.homeController = map }, rightViewController: nil)
        menu.isLeftViewSwipeGestureEnabled = false
        menu.leftViewWidth = 213
        menu.delegate = router.delegate
        return menu
    }
    
    class func push(in navigation: MapRepresenting, replace: Bool, configure: (FindRideInteractor) -> () = {_ in}) {
        let controller = FindRideViewController()
        let interactor = inject(controller: controller, network: Session.shared)
        interactor.router.mapNavigation = navigation
        configure(interactor)
        navigation.push(controller, animated: true, replace: replace)
    }
    
    weak var mapNavigation: MapRepresenting!
    
    func openInfo(for bike: Bike, configure: (BikeInfoInteractor) -> ()) {
        let controller = BikeInfoRouter.navigation() { $0.bike = bike; configure($0) }
        let cc = self.controller.presentedViewController ?? self.controller
        cc?.present(controller, animated: true, completion: nil)
    }
    
    func openRoute(to bike: Bike) {
        RouteToBikeRouter.push(in: mapNavigation) { $0.bike = bike }
    }
    
    func openTems(with link: URL) {
        let terms = SFSafariViewController(url: link)
        controller.present(terms, animated: true, completion: nil)
    }
    
//    func openTems(with closure:(UIWebView) -> ()) {
//        let terms = WebViewController.navigation(with: "find_rite_terms_title".localized(), load: closure)
//        controller.present(terms, animated: true, completion: nil)
//    }
    
    func openPayments(onClose: @escaping () -> () = {}) {
        let billing = PaymentMethodRouter.navigation(accessory: .select, onClose: onClose)
        let presenting = controller.presentedViewController ?? controller
        presenting?.present(billing, animated: true, completion: nil)
    }
    
    func openQRScanner(with delegate: FindRideInteractorDelegate) -> Bool {
        guard QRCodeReader.isAvailable() else { return false }
        let view = FindQRView.nib() as! FindQRView
        view.textView.delegate = self.delegate
        let builder = QRCodeReaderViewControllerBuilder {
            $0.readerView = QRCodeReaderContainer(displayable: view)
        }
        view.delegate = delegate
        let scanner = QRCodeReaderViewController(builder: builder)
        scanner.delegate = delegate
        scanner.codeReader.stopScanningWhenCodeIsFound = false
        scanner.completionBlock = view.display(result:)
        controller.present(scanner, animated: true, completion: nil)
        return true
    }
    
    func openDirectons(with delegate: DirectionsInteractorDelegate, title: String) {
        let directions = DirectionsRouter.navigation(with: delegate, title: title)
        controller.present(directions, animated: true, completion: nil)
    }
    
    func openRide(tripService: TripService, lock: Lock) {
        RideRouter.push(in: mapNavigation, replace: true) { interactor in
            interactor.tripService = tripService
            interactor.lock = lock
            interactor.isQr = true
        }
    }
    
    func dismiss(completion: (() -> ())?) {
        controller.dismiss(animated: true, completion: completion)
    }
}

private func inject(controller: FindRideViewController, network: BikeNetwork) -> FindRideInteractor {
    let interactor = FindRideInteractor(network: network)
    controller.interactor = interactor
    interactor.view = controller
    interactor.router = FindRideRouter(controller)
    return interactor
}

class MenuDelegate: NSObject {
    weak var controller: ViewController?
}

extension MenuDelegate: LGSideMenuDelegate {
    func willShowLeftView(_ leftView: UIView, sideMenuController: LGSideMenuController) {
        controller?.isStatusBarHidden = true
    }
    
    func willHideLeftView(_ leftView: UIView, sideMenuController: LGSideMenuController) {
        controller?.isStatusBarHidden = false
    }
}

extension MenuDelegate: UITextViewDelegate {
    func textView(_ textView: UITextView, shouldInteractWith URL: URL, in characterRange: NSRange) -> Bool {
        let terms = SFSafariViewController(url: URL)
        controller?.presentedViewController?.present(terms, animated: true, completion: nil)
        return false
    }
}
