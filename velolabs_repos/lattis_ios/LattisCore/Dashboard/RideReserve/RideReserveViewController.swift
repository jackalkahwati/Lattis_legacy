//
//  RideReserveViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 17/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import JTMaterialSpinner
import CoreLocation
import Model

class RideReserveViewController: MapTopViewController {
    
    weak var delegate: DashboardDelegate?

    fileprivate let bikeControl: BikeControl
    fileprivate let statusLabel = UILabel.label(font: .theme(weight: .medium, size: .text), color: .white)
    fileprivate let timeLabel = UILabel.label(font: .theme(weight: .medium, size: .text), color: .white)
    fileprivate let logic: RideReserveLogicController
    
    init(_ booking: Bike.Booking, bike: Bike) {
        self.logic = .init(booking, bike: bike, manager: bike.deviceManager())
        self.bikeControl = .init(bike: bike)
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
//        TutorialManager.shared.presented = false
        
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
        
        let infoView = UIImageView(image: .named("icon_info"))
        infoView.tintColor = .black
        containerView.addSubview(infoView)
        containerView.addTarget(self, action: #selector(openBikeDetails), for: .touchUpInside)
        
        contentView.addSubview(containerView)
        constrain(infoView, bikeControl, containerView, contentView) { info, bike, container, content in
            container.edges == content.edges.inseted(horizontally: .margin)
            info.top == container.top
            info.right == container.right
            
            bike.top == info.bottom + .margin/2
            bike.bottom == container.bottom - .margin/2
            bike.left == container.left
            bike.right == container.right
        }
        cardView.isHidden = false
        cardView.alpha = 1
        dragView.isHidden = true
        
        let timeView = UIControl()
        timeView.addTarget(self, action: #selector(showHint), for: .touchUpInside)
        let stackView = UIStackView(arrangedSubviews: [statusLabel, .circle(color: .white), timeLabel])
        stackView.axis = .horizontal
        stackView.spacing = .margin/2
        stackView.alignment = .center
        stackView.isUserInteractionEnabled = false
        timeView.addSubview(stackView)
        timeView.backgroundColor = .accent
        timeView.layer.maskedCorners = [.layerMaxXMaxYCorner, .layerMaxXMinYCorner]
        timeView.layer.cornerRadius = 10
        view.addSubview(timeView)
                
        constrain(timeView, hintLabel, statusLabel, stackView, containerView, mapHomeButton, view) { time, hint, status, stack, container, home, view in
            stack.left == time.left + .margin
            stack.top == time.top + .margin/2
            stack.bottom == time.bottom - .margin/2
            time.right == status.right + 110
            stack.edges == time.edges.inseted(horizontally: .margin, vertically: .margin/2)
            time.left == view.left
            time.bottom == container.top
            
            hint.bottom == time.top - .margin
            
            home.bottom == time.top
        }
        
//        actionContainer.update(left: nil, right: .plain(title: "cancel") {
//            self.cancel()
//        })
        
        showHint()
        logic.fetchState { [unowned self] (state) in
            self.render(state)
        }
    }
    
    override var mapController: MapRepresentable? {
        didSet {
            logic.location = mapController?.location
            mapController?.removeAllPoints()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.mapController?.add(points: [self.logic.bike], selected: nil)
            }
        }
    }
    
    override func canSelectPoint() -> Bool {false}
    
    fileprivate func render(_ state: RideReserveState) {
        var rightAction: ActionButton.Action?
        switch state {
        case .label(let status):
            statusLabel.text = status
            return
        case .time(let time):
            timeLabel.text = time
            return
        case .connection(let state):
            switch state {
            case .connected:
                rightAction = .plain(title: "booking_begin_trip".localized(), handler: { [unowned self] in
                    if self.logic.manager.state.in([.iot, .hub, .manualLock, .iotWithAdapter, .tapkey]) {
                        self.scan()
                    } else {
                        self.startRide(auto: false)
                    }
                })
                actionContainer.right.endAnimation()
            case .connecting:
                rightAction = .plain(title: "connecting_loader".localized(), style: .inactiveSecondary)
                actionContainer.right.beginAnimation()
            case .search, .disconnected:
                if logic.isKuhmuteEnabled {
                    rightAction = .plain(title: "booking_begin_trip".localized()) { [unowned self] in
                        self.scan()
                    }
                } else {
                    rightAction = .plain(title: "walk_to_bike_label".localized(), style: .inactiveSecondary) { [unowned self] in
                        #if targetEnvironment(simulator)
                        self.startRide(auto: false)
                        #endif
                    }
                    actionContainer.right.endAnimation()
                }
            }
        case .qrCode:
            rightAction = .plain(title: "booking_begin_trip".localized(), handler: { [unowned self] in
                self.scan()
            })
            actionContainer.right.endAnimation()
        case .trip(let service):
            stopLoading {
                self.delegate?.didChange(status: .trip(service), info: nil, animated: true)
            }
            return
        case .cancel:
            stopLoading {
                self.delegate?.didChange(status: .search, info: nil, animated: true)
            }
            return
        case .loading(let message):
            if let m = message {
                startLoading(m)
            } else {
                stopLoading()
            }
            return
        case .bleState(let enabled):
            if enabled {
                #warning("Implement reverse logic if needed")
            } else {
                actionContainer.right.endAnimation()
                rightAction = .plain(title: "connect_to_lock".localized(), style: .inactiveSecondary) { [unowned self] in
                    #if targetEnvironment(simulator)
                    self.startRide(auto: false)
                    #else
                    self.render(hint: "bluetooth_access_alert_message".localized())
                    #endif
                }
            }
        case .failure(let error):
            stopLoading {
                self.handle(error)
            }
        }
        
        actionContainer.update(
            left: .plain(title: "cancel".localized(), style: .plain, handler: { [unowned self] in
                self.cancel()
            }),
            right: rightAction,
            priority: .right
        )
    }
    
    @objc
    fileprivate func showHint() {
        let text = logic.bike.isPayment ? "reservation_timer_hint" : "reservation_timer_hint_free"
        render(hint: text.localized(), logo: .named("logo_hint_watch"))
    }
    
    @objc
    fileprivate func cancel() {
        let alert = AlertController(title: "bookin_cancel_title".localized(), message: .plain("booking_cancel_message".localized()))
        alert.actions.append(.plain(title: "booking_cancel_confirm".localized()) { [unowned self] in
            self.logic.cancel()
            })
        alert.actions.append(.plain(title: "no_keep_looking".localized()))
        present(alert, animated: true, completion: nil)
    }
    
    @objc
    fileprivate func openBikeDetails() {
        let details = BikeDetailsViewController(logic.bike)
        details.closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
        present(details, animated: true, completion: nil)
    }
    
    fileprivate func startRide(auto: Bool) {
        if let hint = AppRouter.shared.hintMessage {
            return render(hint: hint)
        }
        guard auto == false else {
            delegate?.shouldCheckStatus()
            return
        }
        logic.stopCountdown()
        logic.startTrip()
    }
    
    fileprivate func scan() {
        present(
            ScanToStartViewController(logic.bike, completion: { [unowned self] in
                self.dismiss(animated: true) {
                    self.startRide(auto: false)
                }
            }),
            animated: true
        )
    }
    
    override func didUpdateUseer(location: CLLocation) {
        logic.location = location
    }
    
    override func handle(_ error: Error, from viewController: UIViewController, retryHandler: @escaping () -> Void) {
        if let e = error as? Trip.Fail, e == .noLocation {
            let alert = AlertController(title: "general_error_title".localized(), body: "location_access_hint".localized())
            present(alert, animated: true)
            return
        }
        super.handle(error, from: viewController, retryHandler: retryHandler)
    }
}

extension UIView {
    static func circle(size: CGFloat = 4, color: UIColor = .black) -> UIView {
        let view = UIView(frame: .init(x: 0, y: 0, width: size, height: size))
        constrain(view) {
            $0.height == size
            $0.width == size
        }
        view.backgroundColor = color
        view.layer.cornerRadius = size/2
        return view
    }
}
