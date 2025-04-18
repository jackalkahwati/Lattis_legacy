//
//  CurrentStateViewController.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 11.06.2020.
//  Copyright © 2020 Lattis inc. All rights reserved.
//

import UIKit
import Cartography
import OvalAPI
import Reachability
import EllipseLock
import Wrappers
import Model

class LoaderView: UIView {
    
    let titleLabel = UILabel.label(text: "find_ride_search".localized(), font: .theme(weight: .book, size: .giant), allignment: .center, lines: 0)
    let progressIndicator = ProgressIindicator()
    fileprivate let contentView = UIView()
    fileprivate let bottomImageView = UIImageView(image: UITheme.theme.loadingImage)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        backgroundColor = .white
        contentView.addSubview(titleLabel)
        contentView.addSubview(progressIndicator)
        addSubview(contentView)
        addSubview(bottomImageView)
        
        constrain(contentView, titleLabel, progressIndicator, bottomImageView, self) { content, title, progress, image, view in
            title.top == content.top + .margin
            title.left == content.left + .margin
            title.right == content.right - .margin
            
            progress.top == title.bottom + .margin
            progress.left == title.left
            progress.right == title.right
            
            content.centerX == view.centerX
            content.centerY == view.centerY - .margin*3
            
            content.left >= view.left + .margin*2
            content.right <= view.right - .margin*2
            content.top >= view.safeAreaLayoutGuide.top + .margin*2
            content.bottom <= view.safeAreaLayoutGuide.bottom - .margin*2
            
            image.bottom == view.bottom + .margin/2
            image.centerX == view.centerX
        }
        progressIndicator.startLoading()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}


class CurrentStateViewController: UIViewController, OverMap {
    var mapController: MapRepresentable?
    weak var delegate: DashboardDelegate?
    
    fileprivate let loadingView = LoaderView()
    
    fileprivate let network: UserAPI & TripAPI & BikeAPI = AppRouter.shared.api()
    fileprivate var reachability: Reachability?
    fileprivate var ellipse: Ellipse?
    @UserDefaultsBacked(key: "tripIdToHandleDocking")
    fileprivate var tripIdToHandleDocking: Int?
    @UserDefaultsBacked(key: "tripIdToShowSummary")
    public var tripIdToShowSummary: Int?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.addSubview(loadingView)
        
        constrain(loadingView, view) { loading, view in
            loading.top == view.top
            loading.bottom == view.bottom
            loading.left == view.left
            loading.right == view.right
        }
        
        reachability = try? Reachability()
        reachability?.whenReachable = checkStatus(reachability:)
        reachability?.whenUnreachable = restoreTrip(reachability:)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        try? reachability?.startNotifier()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        reachability?.stopNotifier()
        reachability = nil
    }
    
    fileprivate func checkStatus(reachability: Reachability) {
        network.checkStatus { [weak self] (result) in
            switch result {
            case .success(let info):
                self?.handle(info: info)
            case .failure(let error):
                self?.handle(error, retryHandler: {
                    AppRouter.shared.logOut()
                })
//                if let e = error as? SessionError,
//                    e.api.url == API.refreshToken.url {
//                    AppRouter.shared.logOut()
//                } else {
//                    self?.handle(error)
//                }
            }
        }
    }
    
    fileprivate func restoreTrip(reachability: Reachability) {
        guard AppRouter.shared.macId != nil else { return }
        AppRouter.shared.lockInfo = .init(target: self, selector: #selector(triggerLock), security: .progress)
        EllipseManager.shared.scan(with: self)
    }
    
    @objc
    fileprivate func triggerLock() {
        guard AppRouter.shared.macId != nil else { return }
        guard let e = ellipse, e.isPaired else { return }
        switch e.security {
        case .locked:
            e.unlock()
        default:
            e.lock()
        }
        AppRouter.shared.lockInfo = .init(target: self, selector: #selector(triggerLock), security: .progress)
    }
    
    fileprivate func handle(info: Status.Info) {
        if let tripId = info.rating?.tripId {
            tripIdToShowSummary = tripId
        }
        if let tr = info.trip {
            if let docked = info.vehicle?.isDocked, docked {
                tripIdToHandleDocking = tr.tripId
            }
            network.getTrip(by: tr.tripId) { [weak self] (result) in
                switch result {
                case .success(let details):
                    if let bike = details.trip.bike {
                        let service = TripManager(details.trip, bike: bike)
                        self?.delegate?.didChange(status: .trip(service), info: info, animated: true)
                    } else if let hub = details.hub {
                        if let portId = details.trip.portId, let port = hub.ports?.first(where: (\.portId == portId)) {
                            self?.delegate?.update(state: .rental(details.trip), asset: .port(port, hub))
                        } else {
                            self?.delegate?.update(state: .rental(details.trip), asset: .hub(hub))
                        }
                    }
                case .failure(let error):
                    self?.handle(error)
                }
            }
        } else if let booking = info.booking {
            if let bikeId = booking.bikeId {
                network.getBike(by: bikeId, qrCodeId: nil, iotCode: nil) { [weak self] (result) in
                    switch result {
                    case .success(let bike):
                        let b = Bike.Booking(supportPhone: info.supportPhone ?? UITheme.theme.support.phoneNumber, onCallOperator: info.operatorPhone, bookedOn: booking.bookedOn, expiresIn: booking.till.timeIntervalSince(booking.bookedOn))
                        self?.delegate?.didChange(status: .booking(b, bike), info: info, animated: true)
                    case .failure(let error):
                        self?.handle(error)
                    }
                }
            } else if let hub = info.hub, let id = booking.portId, let port = hub.ports?.first(where: {$0.portId == id}) {
                self.delegate?.update(state: .booking(.init(booking, info: info)), asset: .port(port, hub))
            }
        } else {
            delegate?.didChange(status: .search, info: nil, animated: true)
        }
    }
    
    func didLayoutSubviews() {}
}

extension CurrentStateViewController: EllipseManagerDelegate, EllipseDelegate {
    func manager(_ lockManager: EllipseManager, didUpdateLocks insert: [Ellipse], delete: [Ellipse]) {
        guard let mac = AppRouter.shared.macId,
            let ellipse = lockManager.locks.first(where: {$0.macId == mac}) else { return }
        ellipse.connect(handler: self)
        self.ellipse = ellipse
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate security: Ellipse.Security) {
        let sec: Device.Security
        switch security {
        case .unlocked:
            sec = .unlocked
        default:
            sec = .locked
        }
        guard AppRouter.shared.macId != nil else { return }
        AppRouter.shared.lockInfo = .init(target: self, selector: #selector(triggerLock), security: sec)
    }
    
    func ellipse(_ ellipse: Ellipse, didUpdate connection: Ellipse.Connection) {
        
    }
    
    
}


extension Booking {
    init(_ status: Status.Booking, info: Status.Info) {
        self.init(bookingId: status.bookingId, supportPhone: info.supportPhone ?? "", onCallOperator: info.operatorPhone ?? "", bookedOn: status.bookedOn, expiresIn: status.till.timeIntervalSince(status.bookedOn))
    }
}
