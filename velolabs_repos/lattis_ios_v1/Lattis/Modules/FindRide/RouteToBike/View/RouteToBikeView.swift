//
//  RouteToBikeRouteToBikeView.swift
//  Lattis
//
//  Created by Ravil Khusainov on 02/03/2017.
//  Copyright © 2017 Lattis .inc. All rights reserved.
//

import UIKit
import GTProgressBar
import JTMaterialSpinner

class RouteToBikeView: MapContainer {
    @IBOutlet weak var fareLabel: UILabel!
    @IBOutlet weak var activeTimeLabel: UILabel!
    @IBOutlet weak var activeView: UIView!
    @IBOutlet weak var startButton: ProgressButton!
    @IBOutlet weak var bikeNameLabel: UILabel!
    @IBOutlet weak var bottomBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var topBottomLayout: NSLayoutConstraint!
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var spinner: JTMaterialSpinner!
    @IBOutlet weak var topInfoView: UIView!
    @IBOutlet weak var cancelButton: UIButton!
    @IBOutlet weak var timeTextView: UITextView!
    @IBOutlet weak var separatorView: SeparatorView!
    @IBOutlet weak var infoButton: UIButton!
    @IBOutlet weak var textHeight: NSLayoutConstraint!
    
    fileprivate var timerUpdater: TimerUpdater?
    
    override func awakeFromNib() {
        super.awakeFromNib()

        spinner.circleLayer.lineWidth = 3
        spinner.circleLayer.strokeColor = UIColor.lsTurquoiseBlue.cgColor
        spinner.animationDuration = 1.5
        
        timeTextView.linkTextAttributes = [.underlineStyle: NSUnderlineStyle.single.rawValue,
                                       .foregroundColor: timeTextView.textColor!,
                                       .underlineColor: timeTextView.textColor!]
    }
    
    func showSpinner() {
        spinner.beginRefreshing()
        UIView.animate(withDuration: .defaultAnimation) { 
            self.bottomBottomLayout.priority = UILayoutPriority(rawValue: 800)
            self.topBottomLayout.priority = UILayoutPriority(rawValue: 900)
            self.layoutIfNeeded()
        }
    }
    
    func hideSpinner() {
        spinner.endRefreshing()
        UIView.animate(withDuration: .defaultAnimation, delay: 0, options: .curveEaseIn, animations: {
            self.spinner.alpha = 0
            self.bottomBottomLayout.priority = UILayoutPriority(rawValue: 900)
            self.topBottomLayout.priority = UILayoutPriority(rawValue: 800)
            self.layoutIfNeeded()
        }, completion: { _ in
            self.spinner.isHidden = true
        })
    }
    
    func update(time: String, for bike: Bike) {
        if timerUpdater == nil {
            timerUpdater = TimerUpdater(bike)
        }
        timeTextView.attributedText = timerUpdater?.update(time: time)
        var size = timeTextView.frame.size
        size.height = CGFloat.greatestFiniteMagnitude
        size = timeTextView.sizeThatFits(size)
        textHeight.constant = size.height + 20
        layoutIfNeeded()
        if timeTextView.alpha == 0 {
            UIView.animate(withDuration: .defaultAnimation, animations: {
                self.timeTextView.alpha = 1
            })
        }
    }
}

extension RouteToBikeView {
    final class TimerUpdater {
        private let baseText: String
        private var result: NSMutableAttributedString
        private var timeRange: NSRange
        init(_ bike: Bike) {
            let bikeName = bike.name ?? "No name"
            let free = bike.fleetType == .privateFree || bike.fleetType == .publicFree
            let text = free ? "bike_booking_timer_text_free".localized() : "bike_booking_timer_text_payment".localized()
            var range = (text as NSString).range(of: "#BIKE#")
            timeRange = (text as NSString).range(of: "#TIME#")
            self.baseText = (text as NSString).replacingCharacters(in: range, with: bikeName)
            range.length = bikeName.count
            self.result = NSMutableAttributedString(string: baseText, attributes: [.font: UIFont.systemFont(ofSize: 14), .foregroundColor: UIColor.lsWarmGrey])
            result.addAttributes([.link: "http://bike.info"], range: range)
        }
        
        func update(time: String) -> NSAttributedString {
            result.replaceCharacters(in: timeRange, with: time)
            timeRange.length = time.count
            result.addAttribute(.foregroundColor, value: UIColor.lsRed, range: timeRange)
            return result
        }
    }
}
