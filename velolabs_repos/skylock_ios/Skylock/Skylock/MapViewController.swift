//
//  MapViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 20/12/2016.
//  Copyright © 2016 Andre Green. All rights reserved.
//

import UIKit
import MessageUI
import GoogleMaps
import Localize_Swift

class MapViewController: SLBaseViewController {
    private static let CalloutScaler: CGFloat = 4.0
    private static let CalloutOffsetScaler: CGFloat = 0.65
    private static let CalloutYOffset = 40.0
    
    private var initialLocation = false
    var userPosition: CLLocationCoordinate2D = kCLLocationCoordinate2DInvalid {
        didSet {
            if !initialLocation {
                centerOnUser()
                initialLocation = true
            }
        }
    }
    
    fileprivate var selectedLock: SLLock?
    private var directions: [SLDirection] = []
    private var directionEndAddress: String = ""
    fileprivate var lockMarkers: [String: GMSMarker] = [:]
    fileprivate var locks: [SLLock] = []
    private var notificationViewController: SLNotificationViewController?
    fileprivate var lockInfoViewController: SLLockInfoViewController?
    fileprivate var directionDrawingHelper: SLDirectionDrawingHelper?
    
    fileprivate lazy var mapView: GMSMapView = {
        let camera = GMSCameraPosition.camera(withTarget: self.userPosition, zoom: 5)
        let view = GMSMapView.map(withFrame: self.view.bounds, camera: camera)
        view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.isMyLocationEnabled = true
        view.delegate = self
        return view
    }()
    
    private lazy var locationButton: UIButton = {
        let image = #imageLiteral(resourceName: "show_current location_button.png")
        let button = UIButton(type: .custom)
        button.setImage(image, for: .normal)
        button.frame = CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height)
        button.addTarget(self, action: #selector(locationButtonPressed), for: .touchUpInside)
        self.view.addSubview(button)
        return button
    }()
    
    fileprivate let directionsBackgroundView: UIView = {
        let view = UIView()
        view.backgroundColor = .black
        view.alpha = 0
        return view
    }()
    
    private lazy var directionsViewController: SLDirectionsViewController = {
        let controller = SLDirectionsViewController()
        controller.directions = self.directions
        controller.endAddress = self.directionEndAddress
        controller.delegate = self
        return controller
    }()
    
    private lazy var noEllipseConnectedView: SLNoEllipseConnectedView? = {
        let text = "You have not yet conneced to an Ellipse. We can only show the location of locks that you have connected to.".localized()
        let frame = CGRect(x: 0, y: 0, width: self.view.bounds.width, height: 156)
        return SLNoEllipseConnectedView(frame: frame, text: text)
    }()
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        SLDatabaseManager.shared().setCurrentUser()
        
        locks = SLLockManager.sharedManager.allLocksForCurrentUser()

        registerNotifications()
        
        view.addSubview(mapView)
        
        let menuButton = UIBarButtonItem(image: #imageLiteral(resourceName: "lock_screen_hamburger_menu.png"), style: .plain, target: self, action: #selector(menuButtonPressed))
        
        navigationItem.leftBarButtonItem = menuButton
        navigationItem.title = "FIND MY ELLIPSE".localized()
        
        locationButton.frame = {
            var frame = self.locationButton.frame
            frame.origin.x = self.view.bounds.width - frame.width - 15
            frame.origin.y = self.view.bounds.height - frame.height - 66
            return frame
        }()
        
        if let location = SLDatabaseManager.shared().getCurrentUser()?.location {
            userPosition = location
        }
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        if locks.isEmpty {
            noEllipseConnectedView?.frame = {
                var frame = noEllipseConnectedView!.frame
                frame.origin.x = 0
                frame.origin.y = -frame.height
                return frame
            }()
            view.addSubview(noEllipseConnectedView!)
            UIView.animate(withDuration: 0.35, animations: {
                self.noEllipseConnectedView!.frame = {
                    var frame = self.noEllipseConnectedView!.frame
                    frame.origin.x = 0
                    frame.origin.y = self.navigationController!.navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height
                    return frame
                }()
            })
        } else {
            locks.forEach({ self.addToMap(lock: $0) })
        }
    }

    private func registerNotifications() {
        NotificationCenter.default.addObserver(self, selector: #selector(handleCrashAndTheftAlerts(notification:)), name: NSNotification.Name(rawValue: kSLNotificationAlertOccured), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(lockPaired(notification:)), name: NSNotification.Name(rawValue: kSLNotificationLockPaired), object: nil)
    }
    
    @objc private func menuButtonPressed() {
        if noEllipseConnectedView?.superview != nil {
            noEllipseConnectedView?.removeFromSuperview()
        }
        noEllipseConnectedView = nil
        dismiss(animated: true, completion: nil)
    }
    
    @objc private func showMoreButtonPressed() {
        if lockInfoViewController == nil {
            presentLockInfoViewController()
        } else {
            removeLockInfoViewController()
        }
    }
    
    func removeCrashAndTheftViewController() {
        if notificationViewController != nil {
            notificationViewController?.dismiss(animated: true, completion: { 
                self.notificationViewController = nil
            })
        }
    }
    
    fileprivate func presentLockInfoViewController() {
        guard let lock = selectedLock else { return }
        let controller = SLLockInfoViewController(lock: lock)
        lockInfoViewController = controller
        lockInfoViewController?.delegate = self
        lockInfoViewController?.view.frame = {
            var frame = self.view.bounds
            frame.size.height = 175
            frame.origin.y = -frame.height
            return frame
        }()
        
        addChildViewController(controller)
        view.addSubview(controller.view)
        view.bringSubview(toFront: controller.view)
        controller.didMove(toParentViewController: self)
        
        UIView.animate(withDuration: 0.35) {
            self.lockInfoViewController?.view.frame = {
                var frame = controller.view.frame
                frame.origin.y = self.navigationController!.navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height
                return frame
            }()
        }
    }
    
    fileprivate func removeLockInfoViewController() {
        guard let controller = lockInfoViewController else { return }
        UIView.animate(withDuration: 0.35, animations: { 
            controller.view.frame = {
                var frame = controller.view.frame
                frame.origin.y = self.navigationController!.navigationBar.bounds.height + UIApplication.shared.statusBarFrame.height - frame.height
                return frame
            }()
        }) { (_) in
            controller.view.removeFromSuperview()
            controller.removeFromParentViewController()
            self.lockInfoViewController = nil
        }
    }
    
    @objc private func lockPaired(notification: Notification) {
        if let emptyView = noEllipseConnectedView {
            UIView.animate(withDuration: 0.35, animations: { 
                emptyView.frame = {
                    var frame = emptyView.frame
                    frame.origin.x = 0
                    frame.origin.y = -emptyView.frame.height
                    return frame
                }()
            }, completion: { (_) in
                emptyView.removeFromSuperview()
                self.noEllipseConnectedView = nil
            })
        }
        
        if let lock = SLLockManager.sharedManager.getCurrentLock() {
            addToMap(lock: lock)
        }
    }
    
    @objc private func handleCrashAndTheftAlerts(notification: Notification) {
        
    }
    
    private func addToMap(lock: SLLock) {
        guard CLLocationCoordinate2DIsValid(lock.location) else { return }
        guard lockMarkers[lock.macId!] == nil else { return }
        let marker = lock.marker
        marker.map = mapView
        lockMarkers[lock.macId!] = marker
    }
    
    fileprivate func getDirections() {
        guard let lock = selectedLock else { return print("Can't present directions") }
        guard directions.isEmpty else { return }
        
        let helper = SLDirectionAPIHelper(start: userPosition, end: lock.location, isBiking: false)
        presentLoadingViewWithMessage(message: "Building directions...".localized())
        helper.getDirections { [weak self] (directions, endAddress) in
            guard let dir = directions as? [SLDirection], let address = endAddress else {
                DispatchQueue.main.async {
                    self?.dismissLoadingViewWithCompletion(completion: { 
                        let texts: [SLWarningViewControllerTextProperty : String?] = [.Header: "Direction is not found!".localized(), .Info: "Sorry. We cand build route to this bike." ]
                        self?.presentWarningViewControllerWithTexts(texts: texts, cancelClosure: nil)
                    })
                }
                print("Error: could not retrieve directions")
                return
            }
            self?.directions = dir
            self?.directionEndAddress = address
            
            self?.enterDirectionsMode()
            
            DispatchQueue.main.async {
                self?.dismissLoadingViewWithCompletion(completion: { 
                    self?.presentDirectionsViewController(directions: dir)
                })
            }
        }
    }
    
    @objc private func locationButtonPressed() {
        centerOnUser()
    }
    
    private func centerOnUser() {
        let camera = GMSCameraPosition.camera(withTarget: userPosition, zoom: 16)
        mapView.animate(to: camera)
    }
    
    private func enterDirectionsMode() {
        guard directions.isEmpty == false && directionEndAddress.isEmpty == false else { return print("Error: direcions and/or directionEndAddress not defined") }
        guard directionDrawingHelper == nil else { return }
        
        directionDrawingHelper = SLDirectionDrawingHelper(mapView: mapView, directions: directions)
        directionDrawingHelper?.drawDirections {}
        
    }
    
    private func presentDirectionsViewController(directions: [AnyObject]) {
        guard let parent = navigationController?.parent else { return }
        directionsViewController.directions = directions
        directionsViewController.view.frame = {
            var frame = parent.view.frame
            frame.size.width = parent.view.bounds.width*0.9
            frame.origin.x = -frame.width
            return frame
        }()
        
        directionsBackgroundView.frame = parent.view.bounds
        parent.view.addSubview(directionsBackgroundView)
        directionsViewController.view.backgroundColor = .white
        parent.addChildViewController(directionsViewController)
        parent.view.addSubview(directionsViewController.view)
        parent.view.bringSubview(toFront: directionsViewController.view)
        directionsViewController.didMove(toParentViewController: parent)
        
        UIView.animate(withDuration: 0.35) { 
            self.directionsViewController.view.frame = {
                var frame = self.directionsViewController.view.frame
                frame.origin.x = 0
                return frame
            }()
            self.directionsBackgroundView.alpha = 0.38
        }
    }
    
    private func dismissNotificationViewController(completion:(() -> ())?) {
        guard let controller = notificationViewController else { return }
        UIView.animate(withDuration: 0.35, animations: { 
            controller.view.alpha = 0
            self.directionsBackgroundView.alpha = 0
        }) { (_) in
            self.directionsBackgroundView.removeFromSuperview()
            controller.view.removeFromSuperview()
            controller.removeFromParentViewController()
            self.notificationViewController = nil
        }
        completion?()
    }
    
    fileprivate func exitDirectionMode() {
        directions.removeAll()
    }
}


// MARK: - GMSMapViewDelegate
extension  MapViewController: GMSMapViewDelegate {
    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        guard let macAddress = marker.userData as? String, lockInfoViewController == nil else { return true }
        if let lock = locks.filter({ $0.macId == macAddress }).first {
            selectedLock = lock
            mapView.animate(toLocation: marker.position)
            presentLockInfoViewController()
        }
        return true
    }
    
    func mapView(_ mapView: GMSMapView, markerInfoWindow marker: GMSMarker) -> UIView? {
        let view = UIView()
        return view
    }
    
    func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
        if directionDrawingHelper != nil {
            directionDrawingHelper?.removeDirections()
            directionDrawingHelper = nil
        }
        
        if lockInfoViewController != nil {
            removeLockInfoViewController()
        }
        
        selectedLock = nil
    }
}


// MARK: - MFMessageComposeViewControllerDelegate
extension MapViewController: MFMessageComposeViewControllerDelegate {
    func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        controller.dismiss(animated: true, completion: nil)
    }
}

// MARK: - SLDirectionsViewController
extension MapViewController: SLDirectionsViewControllerDelegate {
    func directionsViewControllerWantsExit(_ directionsController: SLDirectionsViewController!) {
        UIView.animate(withDuration: 0.35, animations: { 
            directionsController.view.frame = {
                var frame = directionsController.view.frame
                frame.origin.x = -frame.width
                return frame
            }()
            self.directionsBackgroundView.alpha = 0
        }) { (_) in
            self.directionsBackgroundView.removeFromSuperview()
            directionsController.view.removeFromSuperview()
            directionsController.removeFromParentViewController()
            self.exitDirectionMode()
        }
    }
}

// MARK: - SLLockInfoViewControllerDelegate
extension MapViewController: SLLockInfoViewControllerDelegate {
    func directionsButtonPressed(livc: SLLockInfoViewController) {
        guard let lock = selectedLock else { return }
        let camera = GMSCameraPosition.camera(withTarget: lock.location, zoom: 16)
        mapView.animate(to: camera)
        getDirections()
    }
}

private extension SLLock {
    var marker: GMSMarker {
        let mark = GMSMarker(position: location)
        mark.icon = #imageLiteral(resourceName: "map_shared_to_me_bike_icon_large.png")
        mark.title = displayName
        mark.infoWindowAnchor = CGPoint.zero
        mark.userData = macId
        return mark
    }
}
