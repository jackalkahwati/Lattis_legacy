//
//  RideViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 27/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import CoreLocation
import JTMaterialSpinner
import Model


class RideViewController: MapTopViewController {
    
    weak var delegate: DashboardDelegate?
    fileprivate let bikeControl: BikeControl
    fileprivate let timeLabel = UILabel.label(font: .theme(weight: .bold, size: .body), color: .white)
    fileprivate let priceLabel = UILabel.label(font: .theme(weight: .bold, size: .title), color: .white)
    fileprivate let lockSwitch = SecuritySwitch()
    fileprivate let manualLockButton = UIButton(type: .custom)
    fileprivate let tapkeyUnlockButton = UIButton(type: .custom)
    fileprivate let parkingButton = UIButton(type: .custom)
    fileprivate let parkingView = ParkingView()
    fileprivate let statusView = UIView()
    fileprivate var parkingBottomToView: NSLayoutConstraint!
    fileprivate var parkingBottomToSafeArea: NSLayoutConstraint!
    fileprivate var parkingLeft: NSLayoutConstraint!
    fileprivate var parkingRight: NSLayoutConstraint!
    fileprivate var homeToParking: NSLayoutConstraint!
    fileprivate var homeToCard: NSLayoutConstraint!
    fileprivate let parkingLoader = JTMaterialSpinner()
    fileprivate let tapkeySpinner = JTMaterialSpinner()
    fileprivate let logic: RideLogicController
    fileprivate weak var axaAlert: AlertContentController?
    fileprivate weak var jammingAlert: AlertController?
    fileprivate weak var sentinelAlert: AlertController?
    fileprivate weak var summaryScreen: TripSummaryViewController?
    
    fileprivate var parkings: [MapPoint] = []
    fileprivate var selectedParking: MapPoint?
    
    init(_ tripService: TripManager) {
        self.bikeControl = .init(bike: tripService.bike)
        self.logic = .init(tripService)
        super.init(nibName: nil, bundle: nil)
        tripService.stripeContext = self
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let emptyView = UIView()
        footerView.addSubview(emptyView)
        constrain(emptyView, footerView, actionContainer) { empty, footer, action in
            empty.left == footer.left
            empty.right == footer.right
            empty.top == footer.top + .containerCornerRadius
            empty.bottom == action.top - .margin
            empty.height == .margin
        }
            
        let containerView = UIControl()
        containerView.addSubview(bikeControl)
        bikeControl.isUserInteractionEnabled = false
        
        let infoButton = UIButton(type: .custom)
        infoButton.setImage(.named("icon_info"), for: .normal)
        infoButton.tintColor = .white
        infoButton.setContentHuggingPriority(.required, for: .horizontal)
        infoButton.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        containerView.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        
        contentView.addSubview(containerView)
        constrain(bikeControl, containerView, contentView) { bike, container, content in
            container.edges == content.edges.inseted(horizontally: .margin)
            
            bike.top == container.top + .margin/2
            bike.bottom == container.bottom - .margin/2
            bike.left == container.left
            bike.right == container.right
        }
        cardView.isHidden = false
        cardView.alpha = 1
        dragView.isHidden = true
        
        statusView.backgroundColor = .accent
        statusView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        statusView.layer.cornerRadius = .containerCornerRadius
        view.addSubview(statusView)
        
        let stackView = UIStackView(arrangedSubviews: [UIImageView(image: .named("icon_duration")), timeLabel])
        stackView.axis = .horizontal
        stackView.spacing = .margin/2
        stackView.alignment = .center
        statusView.addSubview(stackView)
        
        let lineView = UIView.verticalLine(2)
        lineView.backgroundColor = UIColor(white: 1, alpha: 0.5)
        statusView.addSubview(lineView)
        
        statusView.addSubview(priceLabel)
        statusView.addSubview(infoButton)
        
        priceLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        timeLabel.setContentHuggingPriority(.defaultHigh, for: .horizontal)
        stackView.setContentCompressionResistancePriority(.defaultHigh, for: .horizontal)
        
        view.insertSubview(lockSwitch, belowSubview: hintView)
        
        constrain(statusView, lockSwitch, hintLabel, stackView, lineView, priceLabel, cardView, infoButton, mapHomeButton, view) { status, lock, hint, stack, line, price, card, info, home, view in
            status.bottom == card.top + .containerCornerRadius
            status.left == view.left
            status.right == view.right
            status.height == 58
            
            stack.left == status.left + .margin
            stack.right >= line.left - .margin ~ .defaultLow
            stack.top == status.top
            stack.bottom == status.bottom
            
            line.right == price.left - .margin
            line.height == 18
            line.centerY == status.centerY
            
            info.right == status.right - .margin
            info.centerY == status.centerY
            
            price.right == info.left - .margin/2
            price.left == line.right + .margin
            price.centerY == status.centerY
            
            lock.left == view.left + .margin/2
            lock.bottom == status.top - .margin/2
            
            hint.bottom == status.top - .margin
            self.homeToCard = home.bottom == status.top - .margin/2 ~ .defaultHigh
        }
        
        manualLockButton.backgroundColor = .black
        manualLockButton.setTitle("code_manual_lock".localized(), for: .normal)
        manualLockButton.setTitleColor(.white, for: .normal)
        manualLockButton.setImage(.named("icon_manual_lock"), for: .normal)
        manualLockButton.addTarget(self, action: #selector(showManualCode), for: .touchUpInside)
        manualLockButton.contentEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: 20)
        manualLockButton.titleEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: -8)
        manualLockButton.layer.cornerRadius = 24
        manualLockButton.titleLabel?.font = .theme(weight: .medium, size: .body)
        view.insertSubview(manualLockButton, aboveSubview: lockSwitch)
        manualLockButton.isHidden = logic.manager.deviceManager.state != .manualLock
        lockSwitch.isHidden = !manualLockButton.isHidden
        
        constrain(manualLockButton, lockSwitch) { manual, lock in
            manual.leading == lock.leading
            manual.bottom == lock.bottom
            manual.height == 48
        }
        
        view.insertSubview(parkingButton, belowSubview: hintView)
        parkingButton.backgroundColor = .white
        parkingButton.addTarget(self, action: #selector(handleParking), for: .touchUpInside)
        parkingButton.layer.cornerRadius = 24
        parkingButton.addShadow()
        parkingButton.setTitle("P", for: .normal)
        parkingButton.setTitleColor(.white, for: .selected)
        parkingButton.setTitleColor(.accent, for: .normal)
        parkingButton.titleLabel?.font = .theme(weight: .bold, size: .giant)
        
        parkingLoader.circleLayer.strokeColor = UIColor.accent.cgColor
        parkingLoader.circleLayer.lineWidth = 2
        parkingLoader.animationDuration = 1.8
        
        parkingButton.addSubview(parkingLoader)
        parkingLoader.isUserInteractionEnabled = false
        constrain(parkingButton, mapHomeButton, parkingLoader) { parking, home, loader in
            parking.right == home.left - .margin/2
            parking.centerY == home.centerY
            parking.height == 48
            parking.width == parking.height
            
            loader.edges == parking.edges.inseted(by: 3)
        }
        
        view.addSubview(parkingView)
        parkingView.alpha = 0
        parkingView.isHidden = true
        parkingView.closeButton.addTarget(self, action: #selector(unselectParking(_ :)), for: .touchUpInside)
        parkingView.directionsButton.addTarget(self, action: #selector(getDirections), for: .touchUpInside)
        
        constrain(parkingView, mapHomeButton, view) { parking, home, view in
            self.parkingLeft = parking.left == view.left + .margin/2
            self.parkingRight = parking.right == view.right - .margin/2
            self.parkingBottomToSafeArea = parking.bottom == view.safeAreaLayoutGuide.bottom - .margin
                ~ .defaultHigh
            self.parkingBottomToView = parking.bottom == view.bottom ~ .defaultLow
            self.homeToParking = home.bottom == parking.top - .margin/2 ~ .defaultLow
        }

        actionContainer.update(left: nil, right: .plain(title: "connecting_loader".localized()))
        logic.fetchState { [unowned self] (state) in
            DispatchQueue.main.async {
                self.render(state)
            }
        }
                
        lockSwitch.addTarget(self, action: #selector(switchLock), for: .touchUpInside)
        if logic.manager.deviceManager.state == .hub {
            lockSwitch.isHidden = true
        }
        
        if logic.manager.shouldShowManualUnlockPopUp {
            logic.manager.shouldShowManualUnlockPopUp = false
            showManualCode()
        }
        
        if logic.manager.deviceManager.state == .tapkey {
            addTapkeyButton()
        }
                
        NotificationCenter.default.addObserver(self, selector: #selector(endRide(_:)), name: .endRide, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(showSentinelAlert), name: .sentinelOffline, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(sentinelOnline), name: .sentinelOnline, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
    
    func render(_ state: RideState) {
        switch state {
        case .price(let price):
            priceLabel.text = price
        case .duration(let duration):
            timeLabel.text = duration
        case .actions(let left, let right):
            actionContainer.update(left: left, right: right, priority: .right)
        case .security(let status):
            update(tapkey: status)
            lockSwitch.security = status
            lockSwitch.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state == .manualLock || logic.manager.deviceManager.state == .tapkey
            manualLockButton.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state != .manualLock
            if logic.manager.deviceManager.state != .iot, logic.manager.trip.isStarted {
                AppRouter.shared.lockInfo = lockSwitch.isHidden ? nil : .init(target: self, selector: #selector(switchLock), security: status)
                AppRouter.shared.macId = logic.bike.macId
            }
        case .iotAlert(let message):
            showIoTAlert(message: message)
        case .hint(let message, let color):
            view.insertSubview(lockSwitch, belowSubview: hintView)
            render(hint: message, color: color)
        case .processing(let message, let completion):
            if let t = message {
                mapController?.startLoading(t)
            } else {
                mapController?.stopLoading(completion: completion)
            }
        case .loading(let shouldStart):
            if shouldStart {
                actionContainer.right.beginAnimation()
            } else {
                actionContainer.right.endAnimation()
            }
        case .parkingAlert(let fee):
            handle(fee: fee)
        case .summary(let trip):
            tripSummary(trip: trip)
        case .endTrip(let info, let service):
            let end = EndRideViewController(info, tripService: service) { [unowned self] in
                self.dismiss(animated: true, completion: nil)
                self.back()
            }
            logic.manager.stripeContext = end
            self.present(.navigation(end), animated: true, completion: nil)
        case .parking(let parking):
            show(parking: parking)
        case .hideHint:
            hideHint()
        case .ccRequired:
            mapController?.stopLoading {
                self.present(AlertController.cardRequired(completion: self.openPaymentMethods), animated: true, completion: nil)
            }
        case .geofences(let gf):
            mapController?.add(shapes: gf)
        case .axaAlert(let show):
            if show {
                guard axaAlert == nil else { return }
                let alert: AlertContentController = .init(title: "lock".localized()) {
                    let image = UIImageView(image: .named("image_axa_help"))
                    image.contentMode = .scaleAspectFit
                    let stack = UIStackView(arrangedSubviews: [
                        UILabel.label(text: "axa_lock_hint".localized(), font: .theme(weight: .medium, size: .body), allignment: .center, lines: 0),
                        image
                    ])
                    stack.axis = .vertical
                    stack.spacing = .margin
                    return stack
                }
                if logic.manager.deviceManager.state == .iot {
                    alert.closeButton.isHidden = false
                }
                present(alert, animated: true, completion: nil)
                axaAlert = alert
            } else {
                axaAlert?.dismiss(animated: true)
            }
        case .linkaAlert(let locking, let show):
            if show {
                let alert: AlertContentController = .init {
                    let label = UILabel.label(text: locking ? "locking".localized() : "unlocking".localized() , font: .theme(weight: .book, size: .giant), allignment: .center)
                    return label
                }
                present(alert, animated: true, completion: nil)
                axaAlert = alert
            } else {
                axaAlert?.dismiss(animated: true, completion: nil)
            }
        case .segwayHint(let message):
            render(hint: message, autoHide: 10, extraSpace: 88)
            view.insertSubview(lockSwitch, aboveSubview: hintView)
        case .jamming(let condition):
            handleJamming(condition)
        case .failure(let error):
            stopLoading { [unowned self] in
                self.handle(error)
            }
        case .scanQrCode(let completion):
            let scan = ScanToStartViewController(logic.bike) { [unowned self] in
                self.dismiss(animated: true, completion: completion)
            }
            mapController?.present(scan, animated: true)
        case .vehicleDocked:
            showDockingDialog()
        case .showParkings:
            handleParking()
        case .batteryLevel(let level):
            bikeControl.update(batteryLevel: level)
        case .showReservationAlert:
            let alert = AlertController(title: "general_error_title".localized(), body: "reservation_ending_soon".localized())
            present(alert, animated: true)
        }
    }
    
    fileprivate func addTapkeyButton() {
        tapkeySpinner.circleLayer.lineWidth = 2
        tapkeySpinner.circleLayer.strokeColor = UIColor.black.cgColor
        tapkeyUnlockButton.backgroundColor = .black
        tapkeyUnlockButton.setTitle("unlock".localized(), for: .normal)
        tapkeyUnlockButton.setTitleColor(.white, for: .normal)
        tapkeyUnlockButton.setImage(.named("icon_manual_lock"), for: .normal)
        tapkeyUnlockButton.addTarget(self, action: #selector(unlockTapkey), for: .touchUpInside)
        tapkeyUnlockButton.contentEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: 20)
        tapkeyUnlockButton.titleEdgeInsets = .init(top: 0, left: 8, bottom: 0, right: -8)
        tapkeyUnlockButton.layer.cornerRadius = 24
        tapkeyUnlockButton.titleLabel?.font = .theme(weight: .medium, size: .body)
        view.insertSubview(tapkeyUnlockButton, aboveSubview: lockSwitch)
        tapkeyUnlockButton.isHidden = false
        lockSwitch.isHidden = true
        
        view.insertSubview(tapkeySpinner, aboveSubview: tapkeyUnlockButton)
        
        constrain(tapkeyUnlockButton, tapkeySpinner, lockSwitch) { tapkey, spinner, lock in
            tapkey.leading == lock.leading
            tapkey.bottom == lock.bottom
            tapkey.height == 48
            
            spinner.width == 30
            spinner.height == 30
            
            spinner.leading == tapkey.leading + 9
            spinner.centerY == tapkey.centerY
        }
    }
    
    @objc
    fileprivate func showSentinelAlert(_ notification: Notification) {
        let alert = AlertController(title: "unlocking".localized(), body: "sentinel_tap_lock_label".localized())
        alert.actions = [.plain(title: "ok".localized(), style: .active)]
        present(alert, animated: true)
        sentinelAlert = alert
    }
    
    @objc
    fileprivate func sentinelOnline(_ notification: Notification) {
        sentinelAlert?.dismiss(animated: true)
    }
    
    @objc
    fileprivate func showManualCode() {
        logic.manager.shouldShowManualUnlockPopUp = false
        guard let code = logic.manualCode else { return }
        let alert: AlertContentController = .init {
            let container = UIView()
            container.backgroundColor = .init(white: 0.9, alpha: 0.2)
            container.layer.cornerRadius = 5
            
            let codeLabel = UILabel.label(text: code, font: .theme(weight: .book, size: .mighty), allignment: .center)
            container.addSubview(codeLabel)
            
            constrain(codeLabel, container) { code, con in
                code.leading == con.leading + .margin
                code.trailing == con.trailing - .margin
                code.top == con.top + .margin*2
                code.bottom == con.bottom - .margin*2
            }
            
            let okButton = ActionButton(action: .ok { [unowned self] in
                self.dismiss(animated: true)
            })
            
            let stack = UIStackView(arrangedSubviews: [
                UILabel.label(text: "enter_code_on_lock".localized(), font: .theme(weight: .book, size: .giant), allignment: .center, lines: 0),
                container,
                okButton
            ])
            stack.axis = .vertical
            stack.spacing = .margin
            return stack
        }
        present(alert, animated: true, completion: nil)
    }
    
    @objc
    fileprivate func unlockTapkey() {
        logic.manager.deviceManager.unlock()
    }
    
    fileprivate func update(tapkey security: Device.Security) {
        guard logic.manager.deviceManager.state == .tapkey else { return }
        tapkeyUnlockButton.backgroundColor = security == .progress || security == .undefined ? .lightGray : .black
        tapkeyUnlockButton.isEnabled = security != .progress && security != .undefined
        tapkeySpinner.circleLayer.strokeColor = tapkeyUnlockButton.backgroundColor?.cgColor
        if security == .progress {
            tapkeySpinner.beginRefreshing()
        } else {
            tapkeySpinner.endRefreshing()
        }
        if security == .unlocked {
            let alert: AlertContentController? = .init {
                UILabel.label(text: "success".localized(), font: .theme(weight: .book, size: .giant), allignment: .center)
            }
            alert?.closeButton.isHidden = false
            alert?.closeTitle = "ok".localized()
            present(alert!, animated: true)
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) { [weak alert] in
                if let a = alert {
                    a.dismiss(animated: true)
                }
            }
        }
    }
    
    fileprivate func showDockingDialog() {
        let alert = AlertController(title: "end_your_ride".localized(), body: "your_vehicle_is_docked".localized())
        alert.actions = [
            .plain(title: "unlock".localized()) { [unowned self] in
                self.logic.undock()
            },
            .plain(title: "end_ride".localized()) { [unowned self] in
                self.logic.endRide(parking: false, force: false)
            }
        ]
        AppRouter.shared.root?.present(alert, animated: true)
    }
    
    fileprivate func handleJamming(_ condition: Bool) {
        guard condition else {
            jammingAlert?.dismiss(animated: true)
            return
        }
        let alert = AlertController(title: "general_error_title".localized(), body: "shackle_jam".localized())
        present(alert, animated: true)
        jammingAlert = alert
    }
    
    fileprivate func showIoTAlert(message: String) {
        let alert = AlertController(title: "turn_your_vehicle_off".localized(), body: message)
        alert.actions = [
            .plain(title: "yes".localized()) { [unowned self] in
                self.logic.manager.deviceManager.strongLock()
            }
            // remove for hooba issue
            // .plain(title: "no".localized())
        ]
        present(alert, animated: true)
    }
        
    @objc
    fileprivate func endRide(_ notification: Notification) {
        let damage = notification.userInfo?[Notification.UserInfoKey.damageReported] as? Bool
        logic.endRide(damageReported: damage ?? false)
    }
    
    override var mapController: MapRepresentable? {
        didSet {
            mapController?.removeAllPoints()
        }
    }
    
    override func back() {
        AppRouter.shared.lockInfo = nil
        AppRouter.shared.macId = nil
        mapController?.removeAllPoints()
        delegate?.didChange(status: .search, info: nil, animated: false)
    }
    
    @objc
    fileprivate func openBikeDetails() {
        let details = BikeDetailsViewController(logic.bike)
        details.closeButton.addTarget(self, action: #selector(dismissModal), for: .touchUpInside)
        present(details, animated: true, completion: nil)
    }
    
    func openPaymentMethods() {
        let payment = PaymentMethodsViewController(logic: .init(bike: logic.bike))
        present(.navigation(payment), animated: true, completion: nil)
    }
    
    @objc
    fileprivate func dismissModal() {
        dismiss(animated: true, completion: nil)
    }
    
    @objc
    fileprivate func switchLock(_ sender: SecuritySwitch) {
        logic.toggleLock()
    }
    
    @objc fileprivate func handleParking() {
        guard !parkingLoader.isAnimating else { return }
        if parkingButton.isSelected {
            hidePardings()
        } else {
            hideHint()
            parkingLoader.beginRefreshing()
            logic.fetchParkings { [weak self] state in
                self?.render(state)
            }
        }
    }
    
    fileprivate func openParkingDetails() {
        guard let parking = selectedParking as? Parking.Spot else { return }
        let details = ParkingDetailsViewController(parking)
        present(.navigation(details), animated: true, completion: nil)
    }
    
    fileprivate func handle(fee: Parking.Fee) {
        var message = "parking_restricted_warning_message".localized()
        var actions: [ActionButton.Action] = [
            .plain(title: "find_nearby_zones".localized()) { [unowned self] in self.handleParking() },
            .cancel
        ]
        let endRide: ActionButton.Action = .plain(title: "end_ride".localized()) { [unowned self] in
            self.logic.endRide(parking: false)
        }
        switch fee {
        case .allowed:
            logic.endRide(parking: false)
            return
        case .outside:
            message = "active_ride_out_of_zones_text".localized()
            actions.insert(endRide, at: 0)
        case .fee(let fee):
            message = "active_ride_parking_out_of_bounds_text".localizedFormat(fee)
            actions.insert(endRide, at: 0)
        case .notAllowed:
            break
        }
        let alert = AlertController(title: "general_error_title".localized(), message: .plain(message))
        alert.actions = actions
        mapController?.stopLoading { [unowned self] in
            self.navigationController?.parent?.present(alert, animated: true, completion: nil)
        }
    }
    
    fileprivate func tripSummary(trip: Trip) {
        guard summaryScreen == nil else { return }
        let sum = TripSummaryViewController(trip) { [unowned self] in
            self.dismiss(animated: true)
            self.back()
        }
        summaryScreen = sum
        AppRouter.shared.root?.present(.navigation(sum), animated: true, completion: nil)
    }
    
    fileprivate func show(parking: Parking) {
        Analytics.log(.parkingView())
        parkingView.hasPakings = !parking.spots.isEmpty || !parking.zones.isEmpty
        logic.isParkingPresent = true
        lockSwitch.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state == .manualLock || logic.manager.deviceManager.state == .tapkey
        manualLockButton.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state != .manualLock
        parkingLoader.endRefreshing()
        parkings = parking.spots + parking.hubs
        mapController?.add(points: parkings, selected: nil)
        mapController?.add(shapes: parking.zones)
        
        func calculate() -> [CLLocationCoordinate2D] {
            var coordinates: [CLLocationCoordinate2D] = []
            for zone in parking.zones {
                switch zone.shape {
                case .polygon(let coord), .rectangle(let coord):
                    coordinates += coord
                case .circle(let circle):
                    coordinates += circle.coordinates
                }
            }
            coordinates += parking.spots.map(\.coordinate)
            coordinates += parking.hubs.map(\.coordinate)
            if let user = mapController?.location {
                coordinates.append(user.coordinate)
            }
            return coordinates
        }
        
        mapController?.focus(on: calculate())
        
        parkingButton.isSelected = true
        parkingButton.backgroundColor = .accent
        parkingView.isHidden = false
        homeToCard.priority = .defaultLow
        homeToParking.priority = .defaultHigh
        
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
            self.parkingView.alpha = 1
            self.cardView.alpha = 0
            self.footerView.alpha = 0
            self.contentView.alpha = 0
            self.statusView.alpha = 0
            self.actionContainer.alpha = 0
        })
    }
    
    fileprivate func hidePardings() {
        guard parkingButton.isSelected else { return }
        logic.isParkingPresent = false
        lockSwitch.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state == .manualLock || logic.manager.deviceManager.state == .tapkey
        manualLockButton.isHidden = logic.shouldHideLockControll || logic.manager.deviceManager.state != .manualLock
        mapController?.removeAllPoints()
        parkingButton.isSelected = false
        parkingButton.backgroundColor = .white
        parkingView.parking = nil
        parkingBottomToView.priority = .defaultLow
        parkingBottomToSafeArea.priority = .defaultHigh
        homeToCard.priority = .defaultHigh
        homeToParking.priority = .defaultLow
        parkingLeft.constant = .margin/2
        parkingRight.constant = -.margin/2
        
        logic.fetchGeofences { [weak self] (state) in
            self?.render(state)
        }

        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
            self.parkingView.alpha = 0
            self.cardView.alpha = 1
            self.footerView.alpha = 1
            self.contentView.alpha = 1
            self.statusView.alpha = 1
            self.actionContainer.alpha = 1
        }, completion: { _ in
            self.parkingView.isHidden = true
        })
    }
    
    @objc
    fileprivate func unselectParking(_ sender: UIResponder?) {
        parkingView.parking = nil
        parkingBottomToView.priority = .defaultLow
        parkingBottomToSafeArea.priority = .defaultHigh
        parkingLeft.constant = .margin/2
        parkingRight.constant = -.margin/2
        if sender != mapController {
            mapController?.deselectPoint()
        }
        UIView.animate(withDuration: 0.3, animations: view.layoutIfNeeded)
    }
    
    @objc
    fileprivate func getDirections() {
        guard let spot = selectedParking, let name = spot.title else { return }
        
        let action = UIAlertController(title: name, message: nil, preferredStyle: .actionSheet)
        action.addAction(.init(title: "Apple Maps", style: .default, handler: { (_) in
            var urlString = "http://maps.apple.com/?daddr=\(spot.coordinate.latitude),\(spot.coordinate.longitude)"
            urlString += "&q=\(name)"
            let url = URL(string: urlString.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!)!
            UIApplication.shared.open(url)
        }))
        if UIApplication.shared.canOpenURL(URL(string: "https://www.google.com/maps")!) {
            action.addAction(.init(title: "Google Maps", style: .default, handler: { (_) in
                var urlString = "https://www.google.com/maps/?daddr=\(spot.coordinate.latitude),\(spot.coordinate.longitude)"
                urlString += "&q=\(name)"
                let url = URL(string: urlString.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!)!
                UIApplication.shared.open(url)
            }))
        }
        action.addAction(.init(title: "cancel".localized(), style: .cancel, handler: nil))
        present(action, animated: true, completion: nil)
    }
    
    override func mapDidSelect(point: MapPoint) {
        if let parking = point as? Parking.Spot {
            parkingView.parking = parking
        } else if let hub = point as? ParkingHub {
            parkingView.hub = hub
        } else if let selected = selectedParking, point.isEqual(to: selected) {
            return
        }
        selectedParking = point
        parkingBottomToView.priority = .defaultHigh
        parkingBottomToSafeArea.priority = .defaultLow
        homeToCard.priority = .defaultLow
        homeToParking.priority = .defaultHigh
        parkingLeft.constant = 0
        parkingRight.constant = 0
        UIView.animate(withDuration: 0.3, animations: view.layoutIfNeeded)
    }
    
    override func didTapOnMap() {
        unselectParking(mapController)
    }
}

