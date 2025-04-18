//
//  SLTheftNotificationViewController.swift
//  Skylock
//
//  Created by Andre Green on 7/23/16.
//  Copyright © 2016 Andre Green. All rights reserved.
//

class SLTheftNotificationViewController: SLNotificationViewController {
    let interval:Double = 2.3
    
    var circleIndex:Int = 0
    
    let startDiameter:CGFloat = 10.0
    
    var finalDiamter:CGFloat = 0.0
    
    var initialCircleFrame:CGRect = CGRect.zero
    
    let numberOfCircles:Int = 5
    
    let xPadding:CGFloat = 35.0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.initialCircleFrame = CGRect(
            x: 0.5*(self.view.bounds.size.width - self.startDiameter),
            y: 0.5*(self.view.bounds.size.height - self.startDiameter),
            width: self.startDiameter,
            height: self.startDiameter
        )
        
        self.finalDiamter = pow(pow(self.view.bounds.size.width, 2) + pow(self.view.bounds.size.height, 2), 0.5)
        
        self.run()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.infoLabel.frame = CGRect(
            x: self.infoLabel.frame.minX,
            y: self.cancelButton.frame.minY - self.infoLabel.bounds.size.height - 30.0,
            width: self.infoLabel.bounds.size.width,
            height: self.infoLabel.bounds.size.height
        )
        
        self.titleLabel.frame = CGRect(
            x: self.titleLabel.frame.minX,
            y: self.infoLabel.frame.minY - self.titleLabel.bounds.size.height - 10.0,
            width: self.titleLabel.bounds.size.width,
            height: self.titleLabel.bounds.size.height
        )
    }
    
    func run() {
        Timer.scheduledTimer(
            timeInterval: self.interval/Double(self.numberOfCircles),
            target: self,
            selector: #selector(timerFired),
            userInfo: nil,
            repeats: true
        )
    }
    
    func timerFired() {
        let circleView = self.createCircleView()
        self.view.addSubview(circleView)
        self.bringTopViewsToFront()
        self.makeAnimation(circleView: circleView)
    }
    
    func makeAnimation(circleView: UIView) {
        let scale:CGFloat = self.finalDiamter/circleView.frame.size.width
        
        UIView.animate(
            withDuration: self.interval,
            delay: 0.0,
            options: UIViewAnimationOptions.curveLinear,
            animations: {
                circleView.transform = circleView.transform.scaledBy(x: scale, y: scale)
                circleView.alpha = 0.0
        }) { (finished) in
            circleView.removeFromSuperview()
        }
    }
    
    func createCircleView() -> UIView {
        let circle:UIView = UIView(frame: self.initialCircleFrame)
        circle.backgroundColor = UIColor(white: 0.8, alpha: 0.25)
        circle.layer.cornerRadius = 0.5*self.initialCircleFrame.size.width
        
        return circle
    }
    
    func bringTopViewsToFront() {
        self.view.bringSubview(toFront: self.takeActionButton)
        self.view.bringSubview(toFront: self.cancelButton)
        self.view.bringSubview(toFront: self.titleLabel)
        self.view.bringSubview(toFront: self.infoLabel)
    }
}
