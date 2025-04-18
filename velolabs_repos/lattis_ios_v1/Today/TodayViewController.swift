//
//  TodayViewController.swift
//  Today
//
//  Created by Ravil Khusainov on 13.01.2020.
//  Copyright © 2020 Velo Labs. All rights reserved.
//

import UIKit
import NotificationCenter
import Cartography
import Localize_Swift

#if RELEASE
fileprivate let urlScheme = "lattisapp://"
#elseif BETA
fileprivate let urlScheme = "lattisapp.beta://"
#else
fileprivate let urlScheme = "lattisapp.dev://"
#endif


class TodayViewController: UIViewController, NCWidgetProviding {
    
    fileprivate weak var rideView: RideView?
    fileprivate weak var warningView: WarningView?
    fileprivate let mainApp = WidgetConnection()
    fileprivate var updateTimer: Timer?
        
    override func viewDidLoad() {
        super.viewDidLoad()
        
        mainApp.receive = { [unowned self] message in
            switch message {
            case .lock(let isLocked):
                DispatchQueue.main.async {
                    self.rideView?.lockControl.status = isLocked ? .locked : .unlocked
                }
            }
        }
    }
        
    func widgetPerformUpdate(completionHandler: (@escaping (NCUpdateResult) -> Void)) {
        completionHandler(fetchData())
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        
        stopTimer()
    }
    
    fileprivate func startTimer() {
        updateTimer?.invalidate()
        updateTimer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true, block: { [weak self] (_) in
            DispatchQueue.main.async {
                self?.fetchData()
            }
        })
    }
    
    @discardableResult fileprivate func fetchData() -> NCUpdateResult {
        if let trip = mainApp.currentTrip {
            show(trip: trip)
            if updateTimer == nil {
                startTimer()
            }
            return .newData
        } else if mainApp.loggedIn {
            stopTimer()
            show(warning: "Ride information will be shown here when you start one".localized())
        } else {
            stopTimer()
            show(warning: "Login and take your firs ride!".localized())
        }
        return .noData
    }
    
    fileprivate func stopTimer() {
        updateTimer?.invalidate()
        updateTimer = nil
    }
    
    fileprivate func show(warning: String) {
        rideView?.removeFromSuperview()
        if warningView == nil {
            let w = WarningView()
            view.addSubview(w)
            
            constrain(w, view) { w, view in
                w.edges == view.edges.inseted(by: .margin/2)
            }
            warningView = w
            w.addTarget(self, action: #selector(openMainApp), for: .touchUpInside)
        }
        warningView?.textLabel.text = warning
    }
    
    fileprivate func show(trip: SharedTrip) {
        warningView?.removeFromSuperview()
        if rideView == nil {
            let ride = RideView(frame: .zero)
            view.addSubview(ride)
            
            constrain(ride, view) { ride, view in
                ride.edges == view.edges.inseted(by: .margin/2)
            }
            ride.lockControl.addTarget(self, action: #selector(handleLock(_:)), for: .touchUpInside)
            ride.button.addTarget(self, action: #selector(openMainApp), for: .touchUpInside)
            rideView = ride
        }
        rideView?.update(trip: trip)
    }
    
    @objc fileprivate func handleLock(_ sender: LockSwitch) {
        guard sender.status != .progress else { return }
        mainApp.send(message: .lock(sender.status == .unlocked))
        sender.status = .progress
    }
    
    @objc fileprivate func openMainApp() {
        extensionContext?.open(URL(string: urlScheme)!, completionHandler: nil)
    }
}

class WarningView: UIControl {
    fileprivate let textLabel = UILabel()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        addSubview(textLabel)
        
        textLabel.numberOfLines = 0
        textLabel.textAlignment = .center
        constrain(textLabel, self) { text, view in
            text.edges == view.edges
        }
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

class RideView: UIView {
    fileprivate let timeLabel = UILabel()
    fileprivate let fareLabel = UILabel()
    fileprivate let timeTitleLabel = UILabel()
    fileprivate let fareTitleLabel = UILabel()
    fileprivate let lockControl = LockSwitch()
    fileprivate let bikeLabel = UILabel()
    fileprivate let button = UIButton(type: .custom)
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        addSubview(timeLabel)
        addSubview(fareLabel)
        addSubview(timeTitleLabel)
        addSubview(fareTitleLabel)
        addSubview(bikeLabel)
        addSubview(button)
        addSubview(lockControl)
        
        bikeLabel.font = .title
        
        timeTitleLabel.font = .smallBold
        
        fareTitleLabel.font = .smallBold
        fareTitleLabel.textAlignment = .right
        
        fareLabel.font = .giant
        fareLabel.textAlignment = .right
        fareLabel.minimumScaleFactor = 0.5
        fareLabel.adjustsFontSizeToFitWidth = true
        
        timeLabel.font = .giant
        timeLabel.minimumScaleFactor = 0.5
        timeLabel.adjustsFontSizeToFitWidth = true
        
        timeTitleLabel.text = "active_ride_time".localized()
        fareTitleLabel.text = "active_ride_fare".localized()
        
        constrain(bikeLabel, lockControl, timeTitleLabel, timeLabel, fareTitleLabel, fareLabel, button, self) { bike, lock, timeTitle, tme, fareTitle, fare, button, view in
            bike.top == view.top
            bike.left == view.left
            bike.right == view.right
            bike.height == 20
            
            timeTitle.left == view.left
            timeTitle.top == bike.bottom + .margin/2
            
            fareTitle.right == view.right
            fareTitle.top == bike.bottom + .margin/2
            
            lock.centerX == view.centerX
            lock.bottom == tme.bottom
            
            timeTitle.right == lock.left - .margin/4
            tme.right == timeTitle.right
            
            fareTitle.left == lock.right + .margin/4
            fare.left == fareTitle.left
            fare.right == view.right
            
            tme.left == view.left
            tme.top == timeTitle.bottom
            fare.top == fareTitle.bottom
            
            button.edges == view.edges
        }
    }
    
    func update(trip: SharedTrip) {
        timeLabel.text = trip.duration
        fareLabel.text = trip.fare
        bikeLabel.text = trip.bikeName
        lockControl.status = trip.isLocked ? .locked : .unlocked
        fareTitleLabel.isHidden = trip.fare == nil
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
