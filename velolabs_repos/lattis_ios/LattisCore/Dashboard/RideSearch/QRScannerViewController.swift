//
//  QRScannerViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/08/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import Kingfisher
import AVFoundation
import JTMaterialSpinner
import QRCodeView
import CoreLocation
import OvalAPI
import Model

extension Bike {
    struct QRCode: Codable, Equatable {
        let qr_id: Int
        let bike_name: String
    }
}

class QRScannerViewController: UIViewController {
    
    typealias Handler = (TripManager) -> ()
    fileprivate let handler: Handler
    fileprivate let rentalHandler: (Trip, Asset) -> Void
    fileprivate let subscriptions: [Subscription]

    fileprivate let logic: QRScannerLogicController
    fileprivate let titleLabel = UILabel.label(text: "scan_qr_code_title".localized(), font: .theme(weight: .medium, size: .giant))
    fileprivate let closeButton = UIButton(type: .custom)
    fileprivate var subtitleLabel = UILabel.label(text: "scan_qr_code_subtitle".localized(), font: .theme(weight: .book, size: .title))
    fileprivate let torchButton = UIButton(type: .custom)
    fileprivate let readerView = QRCodeView()
    fileprivate let focusView = UIView()
    fileprivate let loaderContainer = UIView()
    fileprivate let spinner = JTMaterialSpinner()
    fileprivate let coverView = UIView()
    fileprivate let cardView = UIView()
    fileprivate let actionContainer = ActionContainer(left: .ok)
    fileprivate let contentView = UIView()
    fileprivate var showLayout: NSLayoutConstraint!
    fileprivate var hideLayout: NSLayoutConstraint!
    fileprivate weak var priceButton: DisclosureButton?
    
    var assetName: String = ""
    
    init(_ coordinate: CLLocationCoordinate2D, subscriptions: [Subscription], handler: @escaping Handler, rentalHandler: @escaping (Trip, Asset) -> Void) {
        self.logic = QRScannerLogicController(coordinate: coordinate)
        self.handler = handler
        self.rentalHandler = rentalHandler
        self.subscriptions = subscriptions
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = .white
        
        if assetName != "" {
            subtitleLabel = UILabel.label(text: "find_qr_code_on".localizedFormat(assetName),
                                          font: .theme(weight: .book, size: .title))
        }
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        closeButton.tintColor = .black
        view.addSubview(titleLabel)
        view.addSubview(closeButton)
        
        readerView.layer.cornerRadius = .containerCornerRadius
        torchButton.layer.cornerRadius = 24
        torchButton.backgroundColor = .secondaryBackground
        torchButton.setImage(.named("icon_torch"), for: .normal)
        torchButton.tintColor = .lightGray
        torchButton.addTarget(self, action: #selector(handleTorch), for: .touchUpInside)
        subtitleLabel.numberOfLines = 0
        
        view.addSubview(subtitleLabel)
        view.addSubview(readerView)
        view.addSubview(torchButton)
        
        constrain(titleLabel, closeButton, subtitleLabel, readerView, torchButton, view) { title, close, subtitle, scanner, torch, view in
            title.top == view.safeAreaLayoutGuide.top + .margin
            title.left == view.left + .margin
            title.right == close.left - .margin/2
            
            close.right == view.right - .margin
            close.centerY == title.centerY
            
            subtitle.top == title.bottom + .margin/2
            subtitle.left == view.left + .margin
            subtitle.right == view.right - .margin
            
            scanner.left == subtitle.left
            scanner.right == subtitle.right
            scanner.top == subtitle.bottom + .margin
            scanner.bottom == torch.top - .margin
            
            torch.left == subtitle.left
            torch.right == subtitle.right
            torch.bottom == view.safeAreaLayoutGuide.bottom - .margin/2
            torch.height == 80
        }
        
        coverView.backgroundColor = UIColor(white: 0, alpha: 0.5)
        coverView.isHidden = true
        coverView.alpha = 0
        view.addSubview(coverView)
        
        cardView.backgroundColor = .white
        cardView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        cardView.layer.cornerRadius = .containerCornerRadius
        view.addSubview(cardView)
        
        cardView.addSubview(contentView)
        cardView.addSubview(actionContainer)
        actionContainer.update(
            left: .plain(title: "cancel".localized(), style: .plain, handler: { [unowned self] in
                self.cancelConfirmation()
            }),
            right: .plain(title: "confirm".localized(), handler: { [unowned self] in
                self.confirm()
            }),
            priority: .right
        )
        
        constrain(coverView, cardView, actionContainer, contentView, view) { cover, card, action, content, view in
            cover.edges == view.edges
            
            card.left == view.left
            card.right == view.right
            
            content.top == card.top + .margin
            content.left == card.left
            content.right == card.right
            content.bottom == action.top - .margin
            
            action.bottom == card.bottom - .margin - 64
            action.left == card.left + .margin
            action.right == card.right - .margin
            
            self.hideLayout = card.top == view.bottom ~ .defaultHigh
            self.showLayout = card.bottom == view.safeAreaLayoutGuide.bottom + 64 ~ .defaultLow
        }
        
        readerView.addSubview(focusView)
        readerView.addSubview(loaderContainer)
        loaderContainer.addSubview(spinner)
        loaderContainer.alpha = 0
        focusView.alpha = 1
        loaderContainer.backgroundColor = UIColor(white: 0, alpha: 0.5)
        focusView.isUserInteractionEnabled = false
        loaderContainer.isUserInteractionEnabled = false
        
        constrain(focusView, loaderContainer, spinner, readerView) { focus, container, spinner, view in
            focus.edges == view.edges.inseted(by: .margin)
            container.edges == view.edges
            
            spinner.width == 100
            spinner.height == spinner.width
            
            spinner.center == container.center
        }
        
        let topLeftView = UIImageView(image: .named("icon_scanner_corner"))
        let topRightView = UIImageView(image: .named("icon_scanner_corner"))
        topRightView.transform = topRightView.transform.rotated(by: .pi/2)
        let bottomLeftView = UIImageView(image: .named("icon_scanner_corner"))
        bottomLeftView.transform = bottomLeftView.transform.rotated(by: -.pi/2)
        let bottomRightView = UIImageView(image: .named("icon_scanner_corner"))
        bottomRightView.transform = bottomRightView.transform.rotated(by: .pi)
        focusView.addSubview(topLeftView)
        focusView.addSubview(topRightView)
        focusView.addSubview(bottomRightView)
        focusView.addSubview(bottomLeftView)
        
        constrain(topLeftView, topRightView, bottomLeftView, bottomRightView, focusView) { topLeft, topRight, bottomLeft, bottomRight, view in
            topLeft.left == view.left
            topLeft.top == view.top
            
            topRight.right == view.right
            topRight.top == view.top
            
            bottomLeft.left == view.left
            bottomLeft.bottom == view.bottom
            
            bottomRight.right == view.right
            bottomRight.bottom == view.bottom
        }
        
        readerView.found = { [unowned self] code in
            self.handle(code)
        }
        readerView.startScan()
        
        #if targetEnvironment(simulator)
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            //http://segway.com/UCLWUO
            //http://segway.com/000665 - PHILIP
            // {\"bike_name\":\"GB8\",\"qr_id\":10001149}
//            self.logic.handle(code: "{\"bike_name\":\"GB8\",\"qr_id\":10001652}", completion: self.render(_:))
            self.logic.handle(code: "http://segway.com/U0RPJ8", completion: self.render(_:))
        }
        #endif
    }
    
//    override func viewWillDisappear(_ animated: Bool) {
//        super.viewWillDisappear(animated)
//        
//        readerView.stopScan()
//    }
    
    fileprivate func handle(_ code: String) -> Bool {
        func startLoading() {
            torchButton.isUserInteractionEnabled = false
            spinner.beginRefreshing()
            UIView.animate(withDuration: 0.3) {
                self.focusView.alpha = 0
                self.loaderContainer.alpha = 1
            }
        }
        
        func stopLoading() {
            torchButton.isUserInteractionEnabled = true
            spinner.endRefreshing()
            UIView.animate(withDuration: 0.3) {
                self.focusView.alpha = 1
                self.loaderContainer.alpha = 0
            }
        }
        
        if logic.handle(code: code, completion: { [weak self] (state) in
            stopLoading()
            self?.render(state)
        }) {
            startLoading()
            readerView.stopScan()
            torchButton.tintColor = .lightGray
            return true
        }
        return false
    }

    fileprivate func render(_ state: QRScannerState) {
        switch state {
        case .bike(let bike):
            showConfirmation(for: bike)
        case .port(let port, let hub):
            showConfirmation(for: port, hub: hub)
        case .hub(let hub):
            showConfirmation(for: hub)
        case .rental(let trip, let asset):
            dismiss(animated: false) {
                self.dismiss(animated: true) {
                    self.rentalHandler(trip, asset)
                }
            }
        case .trip(let service):
            dismiss(animated: false) {
                self.dismiss(animated: true) {
                    self.handler(service)
                }
            }
        case .failed(let error):
            if error.isHTTP(code: 404) {
                warning(title: nil, message: "qr_code_no_fleet_label".localized()) { [unowned self] in
                    self.readerView.startScan()
                }
                return
            }
            if error.isHTTP(code: 409) {
                warning(title: nil, message: "preauthorization_warning".localized()) { [unowned self] in
                    self.readerView.startScan()
                }
                return
            }
            handle(error, retryHandler: { [unowned self] in
                self.readerView.startScan()
            })
        }
    }
        
    @objc
    fileprivate func handleTorch() {
        guard let device = AVCaptureDevice.default(for: AVMediaType.video)
            else {return}
        
        if device.hasTorch {
            do {
                try device.lockForConfiguration()
                
                if device.torchMode != .on {
                    device.torchMode = .on
                    torchButton.tintColor = .black
                } else {
                    device.torchMode = .off
                    torchButton.tintColor = .lightGray
                }
                
                device.unlockForConfiguration()
            } catch {
                print("Torch could not be used")
            }
        } else {
            print("Torch is not available")
        }
    }
    
    fileprivate func showConfirmation(for bike: Bike) {
        let sub = subscriptions.first(where: {$0.membership.fleet.fleetId == bike.fleet.fleetId})
        let confirm = RideConfirmationViewController(bike, disconut: sub?.membership.incentive, pricing: logic.pricing)
        priceButton = confirm.priceButton
        confirm.bikeControl.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        confirm.priceButton.addTarget(self, action: #selector(priceOptions), for: .touchUpInside)
        confirm.willMove(toParent: self)
        addChild(confirm)
        show(content: confirm.view)
        confirm.didMove(toParent: self)
    }
    
    fileprivate func showConfirmation(for port: Hub.Port, hub: Hub) {
        let sub = subscriptions.first(where: {$0.membership.fleet.fleetId == hub.fleet.fleetId})
        let confirmation = PortConfirmationView(viewModel: .init(port, hub: hub, discount: sub?.membership.incentive, confirm: { [weak self] booking in
            self?.dismiss(animated: true) {
                self?.startLoading("loading".localized())
            }
            self?.logic.start(asset: .port(port, hub), booking: booking) { state in
                self?.render(state)
            }
        }), deselect: {
            self.readerView.startScan()
            self.dismiss(animated: true)
        }).ui
        confirmation.modalPresentationStyle = .overCurrentContext
        confirmation.modalTransitionStyle = .crossDissolve
        present(confirmation, animated: true)
    }
    
    fileprivate func showConfirmation(for hub: Hub) {
        let sub = subscriptions.first(where: {$0.membership.fleet.fleetId == hub.fleet.fleetId})
        let hubs = HubDetailsView(viewModel: .init(hub, discount: sub?.membership.incentive, booked: { [weak self] port, bookin in
            self?.startLoading("loading".localized())
            self?.logic.start(asset: .port(port, hub), booking: bookin) { state in
                self?.render(state)
            }
        }), dismiss: {
            self.readerView.startScan()
            self.dismiss(animated: true)
        }).ui
        present(hubs, animated: true)
    }
    
    fileprivate func show(content: UIView) {
        contentView.addSubview(content)
        coverView.alpha = 0
        coverView.isHidden = false
        constrain(content) {$0.edges == $0.superview!.edges}
        cardView.layoutIfNeeded()
        hideLayout.priority = .defaultLow
        showLayout.priority = .defaultHigh
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
            self.coverView.alpha = 1
        })
    }
    
    fileprivate func hideContent() {
        hideLayout.priority = .defaultHigh
        showLayout.priority = .defaultLow
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
            self.coverView.alpha = 0
        }, completion: { _ in
            self.coverView.isHidden = true
            self.contentView.subviews.forEach{$0.removeFromSuperview()}
        })
    }
    
    fileprivate func cancelConfirmation() {
        readerView.startScan()
        hideContent()
    }
    
    fileprivate func confirm() {
        if TutorialManager.shared.shouldPresent {
            return TutorialManager.shared.present(from: self) { [unowned self] in
                self.confirm()
            }
        }
        guard logic.canRent else {
            let payment = PaymentMethodsViewController(logic: .init(bike: logic.bike!))
            present(.navigation(payment), animated: true, completion: nil)
            return
        }
        if logic.heedPhoneNumber {
            let alert = AlertController(title: "label_note".localized(), message: .plain("mandatory_phone_text".localized()))
            alert.actions = [
                .plain(title: "mandatory_phone_action".localized()) {
                    self.present(.navigation(ProfileViewController(true)), animated: true, completion: nil)
                },
                .cancel
            ]
            return present(alert, animated: true, completion: nil)
        }
        if !logic.consentText.isEmpty {
            let terms = StrictTermsViewController(logic.consentText) { [unowned self] in
                self.dismiss(animated: true) { [unowned self] in
                    self.logic.explicitConsent = true
                    self.confirm()
                }
            }
            present(terms, animated: true)
            return
        }
        if logic.shouldSelectPricing {
            return priceOptions()
        }
        startLoading("starting_ride_loader".localized())
        logic.startTrip { [weak self] (state) in
            self?.render(state)
        }
    }
    
    @objc
    fileprivate func openBikeDetails() {
        guard let bike = logic.bike else { return }
        let details = BikeDetailsViewController(bike)
        details.closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        present(details, animated: true, completion: nil)
    }
    
    @objc
    fileprivate func priceOptions() {
        guard let options = logic.bike?.pricingOptions, !options.isEmpty else { return }
        let controller = PricingOptionsController(options, selected: logic.pricing?.pricingOptionId, perUseValue: logic.bike?.fullPrice)
        let alert = AlertContentController(title: "select_pricing".localized()) {
            let pricing = PricingOptionsView(controller)
            pricing.confirmButton.action = .plain(title: "confirm".localized()) { [unowned self] in
                self.logic.pricing = options.first(where: {$0.pricingOptionId == controller.selected})
                if self.logic.pricing == nil {
                    self.logic.payPerUse = true
                }
                let pricing = logic.pricing?.title ?? logic.bike?.price
                self.priceButton?.title = pricing ?? "bike_detail_bike_cost_free".localized()
                self.dismiss(animated: true)
            }
            return pricing
        }
        alert.closeButton.isHidden = false
        present(alert, animated: true)
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let e = error as? SessionError, e.code == .conflict {
            let alert = AlertController(title: "general_error_title".localized(), body: "bike_already_rented_label".localized())
            stopLoading {
                viewController.present(alert, animated: true)
            }
            Analytics.report(error)
            return
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

