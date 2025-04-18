//
//  RideSearchViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation
import Wrappers
import Model

enum RideSearchState {
    case unselected
    case select(Bike)
    case show(Hub)
    case details(Bike)
    case confirmation(Bike)
    case map([MapPoint])
}

class RideSearchViewController: MapTopViewController {
    
    weak var delegate: DashboardDelegate?
    override var mapController: MapRepresentable? {
        didSet {
            mapController?.update(contentInset: .init(top: 72, left: 0, bottom: 192, right: 0))
        }
    }
    fileprivate var location: CLLocation?
    fileprivate let searchControl = UIControl()
    fileprivate let searchLabel = UILabel.label(text: "label_enter_location".localized(), font: .theme(weight: .medium, size: .body), allignment: .center)
    fileprivate let mapVisibleView = UIView()
    fileprivate let logic = RideSearchLogicController()
    fileprivate var searchFlag = false
    fileprivate let membershipView = UIView()
    fileprivate let membershipPerkLabel = UILabel.label(font: .theme(weight: .bold, size: .text), color: .white)
    let membershipLabel = UILabel.label(text: "membership".localized(), font: .theme(weight: .medium, size: .small), color: .white)
    fileprivate var membershipLeft: NSLayoutConstraint!
    fileprivate var membershipLeftFleet: NSLayoutConstraint!
    fileprivate weak var priceButton: DisclosureButton?
    fileprivate weak var priceLabelCard: UILabel?
    
    @UserDefaultsBacked(key: "isTripCanceled", defaultValue: false)
    fileprivate var isTripCanceled: Bool
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let searchImage = UIImageView(image: .named("icon_search"))
        searchImage.isUserInteractionEnabled = false
        searchLabel.isUserInteractionEnabled = false
        searchControl.addSubview(searchImage)
        searchControl.addSubview(searchLabel)
        searchControl.addTarget(self, action: #selector(changeLocation), for: .touchUpInside)
        searchControl.addShadow()
        searchControl.layer.cornerRadius = 24
        searchControl.backgroundColor = .white
        view.insertSubview(searchControl, belowSubview: cardBackground)
        
        view.addSubview(mapVisibleView)
        mapVisibleView.isUserInteractionEnabled = false
        constrain(mapVisibleView, searchControl, searchImage, searchLabel, footerView, cardView, mapHomeButton, view) {container, search, image, label, footer, card, home, view in
            search.top == view.safeAreaLayoutGuide.top + .margin/4
            search.left == view.left + 70
            search.right == view.right - .margin/2
            search.height == 48
            
            image.left == search.left + 24
            image.centerY == search.centerY
            
            label.left == search.left + 54
            label.centerY == search.centerY
            label.right == search.right - 54
            
            container.left == view.left
            container.right == view.right
            container.top == search.bottom
            container.bottom == footer.top
            
            home.bottom == card.top - .margin/2 ~ .defaultLow
        }
        
        let emptyView = UIView()
        footerView.addSubview(emptyView)
        
        let greetingLabel = UILabel.label(text: .greetings(), font: .theme(weight: .medium, size: .small))
        let noteLabel = UILabel.label(text: "label_find_new_ride".localized(), font: .theme(weight: .bold, size: .title))
        let stackView = UIStackView(arrangedSubviews: [greetingLabel, noteLabel])
        stackView.axis = .vertical
        emptyView.addSubview(stackView)
        
        let logoView = UIImageView(image: UITheme.theme.searchLogo ?? .named("logo_find_ride"))
        logoView.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        emptyView.addSubview(logoView)
        
        constrain(footerView, actionContainer, emptyView, stackView, logoView) { footer, action, empty, stack, logo in
            empty.left == footer.left
            empty.right == footer.right
            empty.top == footer.top + .margin
            empty.bottom == action.top - .margin
            
            stack.left == empty.left + .margin
            stack.top == empty.top
            stack.bottom == empty.bottom
            stack.right == logo.left - .margin/2
            
            logo.top == empty.top - .margin/2
            logo.right == empty.right
        }
        
        membershipView.addSubview(membershipLabel)
        membershipView.addSubview(membershipPerkLabel)
        membershipView.backgroundColor = UIColor(white: 0, alpha: 0.65)
        membershipView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        membershipView.layer.cornerRadius = .containerCornerRadius
        membershipPerkLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        
        view.insertSubview(membershipView, belowSubview: cardView)
        membershipView.alpha = 0
        
        constrain(membershipView, cardView, membershipLabel, membershipPerkLabel) { container, card, title, value in
            container.bottom == card.top + .containerCornerRadius
            container.left == card.left
            container.right == card.right
            
            title.bottom == card.top - .margin/2
            self.membershipLeftFleet = title.left == container.left + 100 ~ .defaultHigh
            self.membershipLeft = title.left == container.left + .margin ~ .defaultLow
            title.top == container.top + .margin/2
            title.right == value.left - .margin/2
            
            value.right == container.right - .margin
            value.centerY == title.centerY
        }
        
        render(.unselected)
        logic.fetchUser { [unowned noteLabel] (user) in
            noteLabel.text = user?.firstName
        }
        if isTripCanceled {
            isTripCanceled = false
            render(hint: "booking_timer_expired_label_free".localized(), color: .warning)
        }
        logic.checkForSummary { [weak self] (trip) in
            self?.showSummary(trip: trip)
        }
    }
    
    override func didLayoutSubviews() {
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
            self?.fetchBikes()
        }
    }
    
    fileprivate func showSummary(trip: Trip) {
        let summary = TripSummaryViewController(trip, callback: { [unowned self] in
            self.dismiss(animated: true, completion: nil)
        })
        present(summary, animated: true, completion: nil)
    }
    
    fileprivate func fetchBikes() {
        let frame = mapVisibleView.frame.inset(by: .init(top: .margin*2, left: 0, bottom: .margin, right: 0))
        guard let ne = mapController?.coordinate(for: .init(x: frame.maxX, y: frame.minY), in: view),
            let sw = mapController?.coordinate(for: .init(x: frame.minX, y: frame.maxY), in: view) else { return }
        logic.fetchRentals(for: .init(ne: ne, sw: sw)) { [weak self] (rentals) in
            self?.render(.map(rentals))
        }
    }
    
    fileprivate func scanQrCode(vehicle: Bool = false, name: String = "") {
        if vehicle {
            Analytics.log(.qrCodeVehicle())
        } else {
            Analytics.log(.qrCodeMain())
        }
        if let hint = AppRouter.shared.hintMessage {
            return render(hint: hint)
        }
        guard let location = location else { return }
        let scanner = QRScannerViewController(location.coordinate, subscriptions: logic.subscriptions) { [unowned self] trip in
            self.delegate?.didChange(status: .trip(trip), info: nil, animated: true)
        } rentalHandler: { trip, asset in
            self.delegate?.update(state: .rental(trip), asset: asset)
        }
        scanner.assetName = name
        navigationController?.parent?.present(scanner, animated: true, completion: nil)
    }
    
    @objc
    fileprivate func changeLocation() {
        let loc = SearchViewController(address: logic.address, location: mapController?.location) { [unowned self] result in
            self.show(result: result)
        }
        loc.modalPresentationStyle = .overCurrentContext
        loc.modalTransitionStyle = .crossDissolve
        navigationController?.parent?.present(loc, animated: true, completion: nil)
    }
    
    fileprivate func backToSearch() {
        logic.address = nil
        searchLabel.text = "label_enter_location".localized()
    }
    
    fileprivate func show(result: SearchResult) {
        switch result {
        case .current:
            mapController?.centerOnUserLocation()
            backToSearch()
        case .address(let address):
            searchFlag = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 1) { [weak self] in
                self?.searchFlag = false
            }
            logic.address = address
            mapController?.focus(on: address.coordinate)
            searchLabel.text = address.name
        case .vehicle(let bike):
            mapController?.add(points: [bike], selected: bike)
            render(.select(bike))
            mapController?.focus(on: bike.coordinate)
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                self.mapController?.select(point: bike)
            }
        }
    }
    
    @objc
    fileprivate func openBikeDetails() {
        guard let bike = logic.selected else { return }
        membershipLeft.priority = .defaultHigh
        membershipLeftFleet.priority = .defaultLow
        membershipView.alpha = 0
        render(.details(bike))
    }
    
    fileprivate func confirmReservation(bike: Bike) {
        membershipLeft.priority = .defaultHigh
        membershipLeftFleet.priority = .defaultLow
        let confirm = RideConfirmationViewController(bike, disconut: logic.perk(for: bike.fleet.fleetId), pricing: logic.pricing)
        confirm.payPerUse = logic.payPerUse
        priceButton = confirm.priceButton
        confirm.priceButton.addTarget(self, action: #selector(priceOptions), for: .touchUpInside)
        confirm.bikeControl.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        confirm.willMove(toParent: self)
        addChild(confirm)
        show(content: confirm.view)
        confirm.didMove(toParent: self)
    }
    
    fileprivate func render(_ state: RideSearchState) {
        switch state {
        case .unselected:
            mapController?.deselectPoint()
            hideContent()
            actionContainer.update(left: .plain(title: "label_scan".localized(), icon: .named("icon_scan"), handler: { [weak self] in
                self?.scanQrCode()
            }))
        case .select(let bike):
            logic.selected = bike
            show(bike: bike)
            actionContainer.update(
                left: .plain(title: nil, icon: .named("icon_scan"), style: .activeSecondary, handler: { [unowned self] in
                    self.scanQrCode(vehicle: true, name: bike.name)
                }),
                right: .plain(title: "reserve".localized(), handler: { [unowned self] in
                    self.render(.confirmation(bike))
                }))
        case .show(let hub):
            show(hub: hub) { [unowned self] bike in
                if let phone = bike.requirePhoneNumber, phone, !logic.hasPhonePumber {
                    let alert = AlertController(title: "label_note".localized(), message: .plain("mandatory_phone_text".localized()))
                    alert.actions = [
                        .plain(title: "mandatory_phone_action".localized()) { [unowned self] in
                            self.present(.navigation(ProfileViewController(true)), animated: true, completion: nil)
                        },
                        .cancel
                    ]
                    return present(alert, animated: true, completion: nil)
                }
                self.book(selected: bike)
            }
        case .details(let bike):
            showExtended()
            self.actionContainer.update(
                left: .plain(title: nil, icon: .named("icon_scan"), style: .activeSecondary, handler: { [unowned self] in
                    self.scanQrCode(vehicle: true, name: bike.name)
                }),
                right: .plain(title: "reserve".localized(), handler: { [unowned self] in
                    self.render(.confirmation(bike))
                })
            )
        case .map(let rentals):
            mapController?.add(points: rentals, selected: nil)
        case .confirmation(let bike):
            if let phone = bike.requirePhoneNumber, phone, !logic.hasPhonePumber {
                let alert = AlertController(title: "label_note".localized(), message: .plain("mandatory_phone_text".localized()))
                alert.actions = [
                    .plain(title: "mandatory_phone_action".localized()) { [unowned self] in
                        self.present(.navigation(ProfileViewController(true)), animated: true, completion: nil)
                    },
                    .cancel
                ]
                return present(alert, animated: true, completion: nil)
            }
            if bike.reservationSettings != nil {
                let alert = AlertController(title: "reserve_now_title".localized(), message: .plain("reserve_now_message".localized()))
                alert.actions = [
                    .plain(title: "reserve_now_button".localized()) { [unowned self] in
                        self.showConfimation(bike: bike)
                    },
                    .plain(title: "schedule_reservation".localized()) { [unowned self] in
                        self.showReservation(bike: bike)
                    },
                    .cancel
                ]
                present(alert, animated: true, completion: nil)
            } else {
                showConfimation(bike: bike)
            }
        }
    }
    
    fileprivate func showConfimation(bike: Bike) {
        Analytics.log(.reserve())
        confirmReservation(bike: bike)
        membershipView.alpha = 0
        actionContainer.update(
            left: .plain(title: "cancel".localized(), style: .plain, handler: { [unowned self] in
                self.hideHint()
                self.render(.select(bike))
            }),
            right: .plain(title: "confirm".localized(), handler: { [unowned self] in
                self.book(selected: bike)
            }),
            priority: .right
        )
    }
    
    fileprivate func setButton(isEnable: Bool) {
        self.actionContainer.right.isEnabled = isEnable
        self.actionContainer.right.isActive = isEnable
    }
    
    fileprivate func showReservation(bike: Bike) {
        guard let settings = bike.reservationSettings else { return }
        mapController?.deselectPoint()
        didTapOnMap()
        let reservation = NewReservationViewController(fleet: bike.fleet, settings: settings)
        present(.navigation(reservation), animated: true, completion: nil)
    }
    
    fileprivate func book(selected: Bike) {
        self.setButton(isEnable: false)
        Analytics.log(.confirmed(vehicle: selected.bikeId))
        if let gateway = selected.paymentGateway, UITheme.theme.paymentGateway != gateway {
            self.setButton(isEnable: true)
            return warning()
        }
        if let hint = AppRouter.shared.hintMessage {
            self.setButton(isEnable: true)
            return render(hint: hint)
        }
        if TutorialManager.shared.shouldPresent {
            self.setButton(isEnable: true)
            return TutorialManager.shared.present(from: self) { [unowned self] in
                self.book(selected: selected)
            }
        }
        if logic.shouldSelectPricing {
            self.setButton(isEnable: true)
            return priceOptions()
        }
        guard logic.canRent else {
            self.setButton(isEnable: true)
            present(AlertController.cardRequired { [unowned self] in
                self.present(.navigation(PaymentMethodsViewController(logic: .init(bike: selected))), animated: true, completion: nil)
            }, animated: true, completion: nil)
            return
        }
        if !logic.consentText.isEmpty {
            self.setButton(isEnable: true)
            let terms = StrictTermsViewController(logic.consentText) { [unowned self] in
                self.dismiss(animated: true) { [unowned self] in
                    self.logic.explicitConsent = true
                    self.book(selected: selected)
                }
            }
            present(terms, animated: true)
            return
        }
        logic.book(the: selected) { [weak self] result in
            switch result {
            case .failure(let error):
                self?.setButton(isEnable: true)
                self?.handle(error)
            case .success(let booking):
                self?.render(.unselected)
                self?.delegate?.didChange(status: .booking(booking, selected), info: .init(trip: nil, booking: nil, operatorPhone: booking.onCallOperator, supportPhone: booking.supportPhone, vehicle: nil, rating: nil, port: nil, hub: nil), animated: true)
            }
        }
    }
    
    fileprivate func show(bike: Bike) {
        if let perk = logic.perk(for: bike.fleetId) {
            membershipPerkLabel.text = "perk_template_bike".localizedFormat(perk.string())
            if let name = bike.fleetName {
                membershipLabel.text = name + " " + "member".localized()
            } else {
                membershipLabel.text = "membership".localized()
            }
            
            membershipView.alpha = 1
            membershipLeftFleet.priority = .defaultHigh
            membershipLeft.priority = .defaultLow
        }
        let title = logic.payPerUse ? bike.fullPrice : logic.pricing?.title
        let card = SearchBikeCard(bike: bike, pricingTitle: title)
        priceLabelCard = card.priceLabel
        card.priceButton.addTarget(self, action: #selector(priceOptions), for: .touchUpInside)
        card.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        let details = BikeDetailsViewController(bike)
        details.closeButton.addTarget(self, action: #selector(closeDetails), for: .touchUpInside)
        details.willMove(toParent: self)
        addChild(details)
        show(content: card, expanded: details.view)
        details.didMove(toParent: self)
    }
    
    fileprivate func show(hub: Hub, completion: @escaping (Bike) -> Void) {
        var controller: UIViewController!
        switch hub.integration {
        case .custom:
            if hub.type == "parking_station" {
                controller = HubDetailsView(viewModel: .init(hub, discount: self.logic.perk(for: hub.fleet.fleetId)) { [unowned self] (port, booking) in
                    self.delegate?.update(state: .booking(booking), asset: .port(port, hub))
                }) { [unowned self] in
                    controller.dismiss(animated: true)
                    self.mapController?.deselectPoint()
                }.ui
            } else {
                controller = HubViewController(hub, logic: logic, completion: completion)
            }
            controller.isModalInPresentation = true
        default:
            controller = HubViewController(hub, logic: logic, completion: completion)
        }
        present(.navigation(controller), animated: true)
    }
    
    override func hideContent() {
        super.hideContent()
        membershipView.alpha = 0
    }
    
    @objc
    fileprivate func closeDetails() {
        guard let bike = logic.selected else { return }
        render(.select(bike))
    }
    
    @objc
    fileprivate func priceOptions() {
        guard let options = logic.selected?.pricingOptions else { return }
        let controller = PricingOptionsController(options, selected: logic.pricing?.pricingOptionId, perUseValue: logic.selected?.fullPrice)
        let alert = AlertContentController(title: "select_pricing".localized()) { [unowned self] in
            let pricing = PricingOptionsView(controller)
            pricing.confirmButton.action = .plain(title: "confirm".localized()) { [unowned self] in
                self.logic.pricing = options.first(where: {$0.pricingOptionId == controller.selected})
                if self.logic.pricing == nil {
                    self.logic.payPerUse = true
                }
                self.priceButton?.title = self.logic.pricing?.title ?? self.logic.selected?.fullPrice
                self.priceLabelCard?.text = self.logic.pricing?.title ?? self.logic.selected?.fullPrice
                self.dismiss(animated: true)
            }
            return pricing
        }
        alert.closeButton.isHidden = false
        present(alert, animated: true)
    }
    
    override func mapDidSelect(point: MapPoint) {
        if let bike = point as? Bike {
            render(.select(bike))
            mapController?.focus(on: point.coordinate)
        } else if let hub = point as? Hub {
            render(.show(hub))
        }
    }
    
    override func mapWillMove(byGesture: Bool) {
        guard byGesture else { return }
        
    }
    
    override func didTapOnMap() {
        logic.selected = nil
        logic.pricing = nil
        hideContent()
        render(.unselected)
    }
    
    override func mapDidMove(byGesture: Bool) {
        if logic.selected == nil {
            fetchBikes()
        }
        if !searchFlag {
            backToSearch()
        }
    }
    
    override func didUpdateUseer(location: CLLocation) {
        self.location = location
    }
}


extension String {
    static func greetings() -> String? {
        guard let hour = Calendar.current.dateComponents([.hour], from: Date()).hour else { return nil }
        let template: String
        switch hour {
        case 0..<12:
            template = "label_good_morning"
        case 12..<17:
            template = "label_good_afternoon"
        default:
            template = "label_good_evening"
        }
        return template.localized() + ","
    }
}
