//
//  LockSlider.swift
//  Lattis
//
//  Created by Ravil Khusainov on 20/02/2017.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import UIKit
import JTMaterialSpinner
import Localize_Swift

class LockSlider: UIControl {
    var lockState: LockState = .locked {
        didSet {
            switch lockState {
            case .processing (_):
                activeView.gestureRecognizers?.forEach{ $0.cancel() }
                spinner.beginRefreshing()
                sendActions(for: .valueChanged)
                alphaContainer.alpha = 0
                activeImageView.alpha = 0
                isUserInteractionEnabled = true
            case .connecting:
                activeImageView.alpha = 0.5
                activeImageView.image = lockedImage
                spinner.beginRefreshing()
                alphaContainer.alpha = 0
                isUserInteractionEnabled = false
            default:
                isUserInteractionEnabled = true
                updateState()
            }
        }
    }
    
    var isLocked: Bool {
        switch lockState {
        case .locked:
            return true
        default:
            return false
        }
    }
    
    let backgroundView = UIView()
    private let alphaContainer = UIView()
//    private var targetView: UIView!
    private var activeView: UIView!
    private let lockedImage = #imageLiteral(resourceName: "icon_lock_locked")
    private let unLockedImage = #imageLiteral(resourceName: "icon_lock_unlocked")
    private let targetImageView = UIImageView(frame: .zero)
    private let activeImageView = UIImageView(frame: .zero)
    private var gerstureStart: Position = .zero
    private let spinner = JTMaterialSpinner()
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = UIFont.systemFont(ofSize: 12)
        label.textAlignment = .center
        label.textColor = .lsWarmGreyFour
        return label
    }()
    override func awakeFromNib() {
        super.awakeFromNib()
        setupView()
    }
    
    private func setupView() {
        clipsToBounds = true
        layer.cornerRadius = frame.height*0.5
        
        addSubview(backgroundView)
        backgroundView.backgroundColor = backgroundColor
        backgroundColor = .clear
        backgroundView.addSubview(alphaContainer)
        
//        targetView = UIView(cornerRadius: frame.height*0.5 - 2)
//        alphaContainer.addSubview(targetView)
//        targetView.alpha = 0.46
//        targetView.backgroundColor = .white
//        targetView.addSubview(targetImageView)
//        targetImageView.image = unLockedImage
//        targetImageView.contentMode = .center
        
        alphaContainer.addSubview(titleLabel)
        
        activeView = UIView(cornerRadius: frame.height*0.5 - 2)
        addSubview(activeView)
        activeView.backgroundColor = .lsTurquoiseBlue
        
        let gesture = UIPanGestureRecognizer(target: self, action: #selector(handle(gesture:)))
        activeView.addGestureRecognizer(gesture)
        
        activeView.addSubview(activeImageView)
        activeImageView.image = lockedImage
        activeImageView.contentMode = .center
        
        spinner.circleLayer.lineWidth = 3
        spinner.circleLayer.strokeColor = UIColor.white.cgColor
        spinner.animationDuration = 1.5
        activeView.addSubview(spinner)
        
//        let tap = UITapGestureRecognizer(target: self, action:#selector(tapAction(_:)))
//        activeView.addGestureRecognizer(tap)
    }
    
//    @objc private func tapAction(_ gesture: UIGestureRecognizer) {
//        guard gesture.state == .ended else { return }
//        guard let layout = constraints(for: .width).first else { return }
//        let isExpanded = layout.constant != frame.height
//        let newValue = isExpanded == false ? superview!.frame.width - frame.minX*2 : frame.height
//        UIView.animate(withDuration: .defaultAnimation) { 
//            layout.constant = newValue
//            self.backgroundView.alpha = isExpanded ? 0 : 1
//            self.superview?.layoutIfNeeded()
//        }
//        guard isExpanded == false else { return }
//        Timer.after(6.seconds) { [weak self] in
//            guard let `self` = self else { return }
//            UIView.animate(withDuration: .defaultAnimation) {
//                layout.constant = self.frame.height
//                self.backgroundView.alpha = 0
//                self.superview?.layoutIfNeeded()
//            }
//        }
//    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        backgroundView.frame = bounds
        alphaContainer.frame = bounds
        titleLabel.frame = {
            var frame = alphaContainer.bounds
            frame.size.width -= bounds.height*2 + 16
            frame.origin.x = bounds.height + 8
            return frame
        }()
        updateFrames()
    }
    
    private func updateFrames() {
        let distance = bounds.width - bounds.height
        var unlocked = false
        if case .unlocked = lockState { unlocked = true }
//        targetView.frame = {
//            var frame = alphaContainer.bounds
//            frame.origin.y = 2
//            frame.size.height -= 4
//            frame.size.width = frame.size.height
//            frame.origin.x = 2 + ( unlocked ? 0 : distance )
//            return frame
//        }()
//        targetImageView.frame = targetView.bounds
        
        activeView.frame = {
            var frame = alphaContainer.bounds
            frame.origin.y = 2
            frame.size.height -= 4
            frame.size.width = frame.size.height
            frame.origin.x = 2 + ( unlocked ? distance : 0 )
            return frame
        }()
        activeImageView.frame = activeView.bounds
        spinner.frame = activeView.bounds.insetBy(dx: 6, dy: 6)
//        titleLabel.textAlignment = unlocked ? .right : .left
        titleLabel.text = unlocked ? "lock_slide_to_lock".localized() : "lock_slide_to_unlock".localized()
    }
    
    @objc private func handle(gesture: UIPanGestureRecognizer) {
        if case .processing(_) = lockState { return }
        switch gesture.state {
        case .began:
            gerstureStart = Position(gest: gesture.location(in: self).x, view: activeView.frame.minX)
        case .changed:
            updateActiveViewFrame(with: gerstureStart.viewPosition(for: gesture.location(in: self).x))
        case .ended:
            fallBack()
        default:
            break
        }
    }
    
    private func fallBack() {
        UIView.animate(withDuration: 0.5, delay: 0, options: .curveEaseIn, animations: {
            self.updateActiveViewFrame(with: self.lockState.position)
        }, completion: nil)
    }
    
    private func updateActiveViewFrame(with xPosition: CGFloat) {
        let maximum = bounds.width - activeView.frame.width - 2
        let minimum: CGFloat = 2
        let position = max(min(maximum, xPosition), minimum)
        activeView.frame = {
            var frame = activeView.frame
            frame.origin.x = position
            return frame
        }()
        updateProgress()
    }
    
    private func updateProgress() {
        let distance = bounds.width - activeView.frame.width - 4
        let curent = activeView.frame.minX - 2
        progress = curent/distance
    }
    
    private var progress: CGFloat = 0 {
        didSet {
            switch lockState {
            case .locked where progress == 1:
                lockState = .processing(.unlocked)
            case .unlocked where progress == 0:
                lockState = .processing(.locked)
            case .locked where progress <= 1:
                alphaContainer.alpha = 1 - progress
            case .unlocked where progress >= 0:
                alphaContainer.alpha = progress
            default:
                break
            }
            activeImageView.alpha = alphaContainer.alpha
        }
    }
    
    private func updateState() {
        var activeImage = lockedImage
        var targetImage = unLockedImage
        switch lockState {
        case .unlocked:
            activeImage = unLockedImage
            targetImage = lockedImage
        default:
            break
        }
        activeImageView.image = activeImage
        targetImageView.image = targetImage
        updateFrames()
        
        UIView.animate(withDuration: 0.3, delay: 0, options: .curveEaseOut, animations: { 
            self.alphaContainer.alpha = 1
            self.activeImageView.alpha = 1
        }, completion: nil)
        spinner.endRefreshing()
    }
}

extension LockSlider {
    indirect enum LockState {
        case unlocked, locked, processing(LockState), connecting
        var position: CGFloat {
            switch self {
            case .unlocked:
                return .greatestFiniteMagnitude
            default:
                return 0
            }
        }
        
        var isLocked: Bool {
            switch self {
            case .locked:
                return true
            default:
                return false
            }
        }
    }
    
    fileprivate struct Position {
        let gest: CGFloat
        let view: CGFloat
        func viewPosition(for gesture: CGFloat) -> CGFloat {
            return view + (gesture - gest)
        }
        static let zero = Position(gest: 0, view: 0)
    }
}

private extension UIView {
    convenience init(cornerRadius: CGFloat) {
        self.init()
        layer.cornerRadius = cornerRadius
        layer.shadowColor = UIColor.black.cgColor
        layer.shadowOffset = CGSize(width: 0, height: 1)
        layer.shadowRadius = 2
        layer.shadowOpacity = 0.21
    }
}

extension UIGestureRecognizer {
    func cancel() {
        isEnabled = false
        isEnabled = true
    }
}
