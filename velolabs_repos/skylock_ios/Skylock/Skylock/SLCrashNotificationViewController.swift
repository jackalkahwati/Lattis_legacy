//
//  SLCrashNotificationViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

protocol SLCrashNotificationViewControllerDelegate:SLNotificationViewControllerDelegate {
    func timerExpired(cnvc: SLCrashNotificationViewController)
}

class SLCrashNotificationViewController: SLNotificationViewController {
    var countDownTime:Int = 30
    
    var timer:Timer?
    
    weak var crashDelegate:SLCrashNotificationViewControllerDelegate?
    
    lazy var thinkerViewController:SLThinkerViewController = {
        let text:[SLThinkerViewControllerLabelTextState:String] = [
            .clockwiseTopMoving: NSLocalizedString("\(self.countDownTime)", comment: ""),
            .clockwiseBottomMoving: NSLocalizedString("seconds", comment: "")
        ]
        
        let tvc:SLThinkerViewController = SLThinkerViewController(
            texts: text,
            firstBackgroundColor: UIColor.white,
            secondBackgroundColor: UIColor(red: 102, green: 177, blue: 227),
            foregroundColor: UIColor(red: 60, green: 83, blue: 119),
            inActiveBackgroundColor: UIColor(red: 130, green: 156, blue: 178),
            textColor: UIColor.white
        )
        
        return tvc
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.titleLabel.frame = CGRect(
            x: self.titleLabel.frame.minX,
            y: 150.0,
            width: self.titleLabel.bounds.size.width,
            height: self.titleLabel.bounds.size.height
        )
        
        self.infoLabel.frame = CGRect(
            x: self.infoLabel.frame.minX,
            y: self.titleLabel.frame.maxY + 10.0,
            width: self.infoLabel.bounds.size.width,
            height: self.infoLabel.bounds.size.height
        )
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        if !self.view.subviews.contains(self.thinkerViewController.view) {
            let diameter:CGFloat = 145.0
            self.thinkerViewController.view.frame = CGRect(
                x: 0.5*(self.view.bounds.size.width - diameter),
                y: self.infoLabel.frame.maxY + 20.0,
                width: diameter,
                height: diameter
            )
            
            self.addChildViewController(self.thinkerViewController)
            self.view.addSubview(self.thinkerViewController.view)
            self.view.bringSubview(toFront: self.thinkerViewController.view)
            self.thinkerViewController.didMove(toParentViewController: self)
            
            self.startCountDown()
        }
        
        self.thinkerViewController.setState(state: .clockwiseMoving)
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        timer?.invalidate()
        timer = nil
    }
    
    private func startCountDown() {
        self.timer =  Timer.scheduledTimer(
            timeInterval: 1.0,
            target: self,
            selector: #selector(tickClock),
            userInfo: nil,
            repeats: true
        )
    }
    
    @objc private func tickClock() {
        self.countDownTime -= 1
        if self.countDownTime >= 0 {
            self.thinkerViewController.updateTextForState(
                state: .clockwiseMoving,
                topText: "\(self.countDownTime)",
                bottomText: nil
            )
        } else {
            self.timer?.invalidate()
            self.timer = nil
            self.crashDelegate?.timerExpired(cnvc: self)
        }
    }
    
    override func cancelButtonPressed() {
        self.timer?.invalidate()
        self.timer = nil
        super.cancelButtonPressed()
    }
}

