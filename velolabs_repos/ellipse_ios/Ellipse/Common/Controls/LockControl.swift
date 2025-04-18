//
//  LockControl.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 10/11/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit

class LockControl: UIControl {
    let topContainer = UIView()
    let wormView = UIView()
    
    let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .elHeader
        label.textColor = .black
        label.textAlignment = .center
        label.numberOfLines = 0
        label.text = "action_notconnected".localized()
        return label
    }()
    
    let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = .elHeaderLight
        label.textColor = .black
        label.textAlignment = .center
        label.minimumScaleFactor = 0.5
        label.adjustsFontSizeToFitWidth = true
        return label
    }()
    
    fileprivate var isAnimating = false
    
    init() {
        super.init(frame: .zero)
        backgroundColor = .elBlueyGrey
        addSubview(wormView)
        wormView.isUserInteractionEnabled = false
        wormView.alpha = 0
        addSubview(topContainer)
        topContainer.isUserInteractionEnabled = false
        topContainer.backgroundColor = .white
        topContainer.addSubview(titleLabel)
        topContainer.addSubview(subtitleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var lockState: LockState = .disconnected {
        didSet {
            guard lockState != oldValue else { return }
            var background: UIColor = .elBluegrey
            switch lockState {
            case .disconnected:
                titleLabel.text = "action_notconnected".localized()
                subtitleLabel.text = nil
                background = .elBlueyGreyTwo
                stopAnimation()
            case .connecting:
                titleLabel.text = "connecting".localized()
                subtitleLabel.text = nil
                startAnimation()
            case .unlocked:
                titleLabel.text = "action_unlocked".localized()
                subtitleLabel.text = "tap_to_lock".localized()
                stopAnimation()
            case .locked:
                titleLabel.text = "action_locked".localized()
                subtitleLabel.text = "tap_to_unlock".localized()
                background = .elNiceBlue
                stopAnimation()
            case .processing:
                startAnimation()
            }
            backgroundColor = background
            sendActions(for: .valueChanged)
            setNeedsLayout()
        }
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        topContainer.frame = bounds.insetBy(dx: 25, dy: 25)
        layer.cornerRadius = bounds.height*0.5
        topContainer.layer.cornerRadius = topContainer.bounds.height*0.5
        let innerSquare: CGRect = {
            var frame = topContainer.bounds
            frame.size.height = frame.height/sqrt(2)
            frame.size.width = frame.height + 30
            frame.origin.x = topContainer.bounds.midX - frame.width*0.5
            frame.origin.y = topContainer.bounds.midY - frame.height*0.5
            return frame
        }()
        if subtitleLabel.text == nil || subtitleLabel.text!.isEmpty {
            titleLabel.frame = innerSquare
        } else {
            let inset: CGFloat = 10
            titleLabel.frame = {
                var frame = innerSquare
                frame.size.height = titleLabel.font.lineHeight
                frame.origin.y = innerSquare.midY - (frame.height + subtitleLabel.font.lineHeight + inset)/2
                return frame
            }()
            
            subtitleLabel.frame = {
                var frame = titleLabel.frame
                frame.size.height = subtitleLabel.font.lineHeight
                frame.origin.y = titleLabel.frame.maxY + inset
                return frame
            }()
        }
        updateWormLayer()
    }
    
    private func updateWormLayer() {
        wormView.layer.cornerRadius = layer.cornerRadius
        wormView.frame = bounds
        wormView.layer.sublayers?.forEach{$0.removeFromSuperlayer()}
        
        let innerRadius = topContainer.frame.height*0.5
        let halfArcHeight = 0.5*(bounds.height*0.5 - innerRadius)
        let startPoint = CGPoint(x: self.wormView.center.x, y: self.wormView.center.y - innerRadius)
        let center1 = CGPoint(x: self.wormView.center.x, y: halfArcHeight)
        let center2 = CGPoint(x: self.wormView.center.x, y: self.wormView.bounds.size.height - halfArcHeight)
        
        let bezierPath:UIBezierPath = UIBezierPath()
        bezierPath.move(to: startPoint)
        bezierPath.addArc(
            withCenter: center1,
            radius: halfArcHeight,
            startAngle: .pi/2,
            endAngle: 3.0*(.pi/2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: self.wormView.center,
            radius: bounds.height*0.5,
            startAngle: -CGFloat(Double.pi/2),
            endAngle: CGFloat(Double.pi/2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: center2,
            radius: halfArcHeight,
            startAngle: CGFloat(Double.pi)/2.0,
            endAngle: 3.0*CGFloat(Double.pi/2),
            clockwise: true
        )
        bezierPath.addArc(
            withCenter: self.wormView.center,
            radius: innerRadius,
            startAngle: 0,
            endAngle: -CGFloat(Double.pi)/2.0,
            clockwise: false
        )
        
        let shapeLayer:CAShapeLayer = CAShapeLayer()
        shapeLayer.path = bezierPath.cgPath
        shapeLayer.fillColor = UIColor.white.cgColor
        
        let colors:[CGColor] = [
                UIColor.elBluegrey.cgColor,
                UIColor.elNiceBlue.cgColor
            ]
        
        let locations = [0.0, 1.0]
        
        let gradientLayer:CAGradientLayer = CAGradientLayer()
        gradientLayer.frame = bounds
        gradientLayer.colors = colors
        gradientLayer.locations = locations as [NSNumber]?
        gradientLayer.mask = shapeLayer
        
        self.wormView.layer.addSublayer(gradientLayer)
    }
    
    private func startAnimation() {
        isAnimating = true
        animate()
        UIView.animate(withDuration: 0.3) {
            self.wormView.alpha = 1
        }
    }
    
    private func animate() {
        UIView.animate(withDuration: 0.5, delay: 0.0, options: .curveLinear, animations: {
            self.wormView.transform = self.wormView.transform.rotated(by: CGFloat.pi)
        }, completion: { _ in
            if self.isAnimating {
                self.animate()
            }
        })
    }
    
    private func stopAnimation() {
        UIView.animate(withDuration: 0.3, animations: {
            self.wormView.alpha = 0
        }, completion: { _ in
            self.isAnimating = false
            self.wormView.layer.removeAllAnimations()
        })
    }
}

extension LockControl {
    enum LockState {
        case disconnected
        case connecting
        case unlocked
        case locked
        case processing
    }
}
