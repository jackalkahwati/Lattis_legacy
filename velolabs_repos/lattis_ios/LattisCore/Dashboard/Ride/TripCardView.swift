//
//  TripCardView.swift
//  LattisCore
//
//  Created by Ravil Khusainov on 30/05/2019.
//  Copyright © 2019 Lattis inc. All rights reserved.
//

import Cartography
import JTMaterialSpinner

class TripCardView: UIView {
    
    let connectButton = UIButton.rounded(height: 44, width: .nan)
    let spinner = JTMaterialSpinner()
    let infoButton = UIButton(type: .custom)
    
    fileprivate let bikeNameLabel = UILabel()
    fileprivate let timeTopLabel = UILabel()
    fileprivate let timeLabel = UILabel()
    fileprivate let fareTopLabel = UILabel()
    fileprivate let fareLabel = UILabel()
    
    init() {
        super.init(frame: .zero)
        
        backgroundColor = .white
        addShadow()
        layer.cornerRadius = 5
        
        addSubview(bikeNameLabel)
        bikeNameLabel.font = .theme(weight: .bold, size: .title)
        bikeNameLabel.textColor = .gray
        
        addSubview(timeTopLabel)
        addSubview(timeLabel)
        addSubview(fareLabel)
        addSubview(fareTopLabel)
        
        timeTopLabel.font = .theme(weight: .medium, size: .small)
        timeTopLabel.textColor = .lightGray
        timeTopLabel.text = "TIME"
        
        fareTopLabel.font = .theme(weight: .medium, size: .tiny)
        fareTopLabel.textColor = .lightGray
        fareTopLabel.text = "FARE"
        fareTopLabel.textAlignment = .right
        
        timeLabel.font = .theme(weight: .bold, size: .giant)
        timeLabel.textColor = .gray
        timeLabel.minimumScaleFactor = 0.5
        
        fareLabel.font = .theme(weight: .bold, size: .giant)
        fareLabel.textColor = .gray
        fareLabel.textAlignment = .right
        
        addSubview(infoButton)
        infoButton.setContentHuggingPriority(.required, for: .horizontal)
        infoButton.setImage(.named("icon_info"), for: .normal)
        
        addSubview(connectButton)
        connectButton.addShadow()
        connectButton.isHidden = true
        connectButton.backgroundColor = .neonBlue
        connectButton.contentEdgeInsets = .init(top: 0, left: .margin, bottom: 0, right: .margin)
        connectButton.titleLabel?.font = .theme(weight: .book, size: .body)
        connectButton.setTitleColor(.white, for: .normal)
        connectButton.setTitle("Connect", for: .normal)
        
        addSubview(spinner)
        spinner.circleLayer.strokeColor = UIColor.neonBlue.cgColor
        
        let lineView = UIView()
        lineView.backgroundColor = UIColor(white: 0.7, alpha: 0.3)
        addSubview(lineView)
        
        addSubview(lineView)
        
        constrain(bikeNameLabel, infoButton, timeTopLabel, timeLabel, lineView, connectButton, spinner, fareTopLabel, fareLabel, self) { bikeName, info, timeTop, time, line, connect, spin, fareTop, fare, card in
            
            info.top == card.top + .margin/2
            info.right == card.right - .margin/2
            
            bikeName.left == card.left + .margin/2
            bikeName.top == card.top + .margin/2
            bikeName.right == info.left - .margin/2
            
            line.centerX == card.centerX
            line.bottom == card.bottom - .margin
            line.top == card.top + .margin
            line.width == 1
            
            time.bottom == line.bottom
            time.left == bikeName.left
            time.right == line.left - .margin/2
            
            timeTop.left == time.left
            timeTop.right == time.right
            timeTop.bottom == time.top
            
            fare.left == line.right + .margin/2
            fare.right == card.right - .margin/2
            fare.bottom == time.bottom
            
            fareTop.bottom == fare.top
            fareTop.left == fare.left
            fareTop.right == fare.right
            
            connect.center == line.center
            spin.center == line.center
            spin.width == 35
            spin.height == spin.width
        }
    }
    
    func update(bike: Bike?) {
        bikeNameLabel.text = bike?.name
    }
    
    func update(time: String?) {
        timeLabel.text = time
    }
    
    func update(fare: String?) {
        fareLabel.text = fare
        fareTopLabel.isHidden = fare == nil
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func showConnect(animated: Bool = true) {
        guard connectButton.isHidden else { return }
        connectButton.alpha = 0
        connectButton.isHidden = false
        func perform() {
//            lockSwitch.alpha = 0
            connectButton.alpha = 1
        }
        if animated {
            UIView.animate(withDuration: 0.3, animations: perform) { (_) in
//                self.lockSwitch.isHidden = true
            }
        } else {
            perform()
//            lockSwitch.isHidden = true
        }
    }
    
    func hideConnect(animated: Bool = true) {
        guard !connectButton.isHidden else { return }
//        lockSwitch.alpha = 0
//        lockSwitch.isHidden = false
        func perform() {
//            lockSwitch.alpha = 1
            connectButton.alpha = 0
        }
        if animated {
            UIView.animate(withDuration: 0.3, animations: perform) { (_) in
                self.connectButton.isHidden = true
            }
        } else {
            perform()
            connectButton.isHidden = true
        }
    }
    
    func startLoading() {
        timeLabel.isHidden = true
        timeTopLabel.isHidden = true
        fareLabel.isHidden = true
        fareTopLabel.isHidden = true
        spinner.beginRefreshing()
        connectButton.isHidden = true
//        lockSwitch.isHidden = true
    }
    
    func stopLoading(connected: Bool) {
        timeLabel.isHidden = false
        timeTopLabel.isHidden = false
        fareLabel.isHidden = false
        fareTopLabel.isHidden = false
        spinner.endRefreshing()
        connectButton.isHidden = connected
//        lockSwitch.isHidden = !connected
    }
}
